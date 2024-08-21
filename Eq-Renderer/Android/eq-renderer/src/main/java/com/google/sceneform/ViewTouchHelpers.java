package com.google.sceneform;

import android.view.MotionEvent;
import android.view.View;

import com.google.sceneform.collision.Plane;
import com.google.sceneform.collision.Ray;
import com.google.sceneform.collision.RayHit;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.ViewRenderable;
import com.google.sceneform.utilities.Preconditions;

/** 用于触摸在世界空间中呈现的视图的实用函数的Helper类。 */

class ViewTouchHelpers {
  /**
   * 通过将触摸事件转换为视图的本地坐标空间，将一个触摸事件分派给一个节点的ViewRenderable(如果该节点有一个ViewRenderable)。
   */
  static boolean dispatchTouchEventToView(Node node, MotionEvent motionEvent) {
    Preconditions.checkNotNull(node, "Parameter \"node\" was null.");
    Preconditions.checkNotNull(motionEvent, "Parameter \"motionEvent\" was null.");

    if (!(node.getRenderable() instanceof ViewRenderable)) {
      return false;
    }

    if (!node.isActive()) {
      return false;
    }

    Scene scene = node.getScene();
    if (scene == null) {
      return false;
    }

    ViewRenderable viewRenderable = (ViewRenderable) node.getRenderable();
    if (viewRenderable == null) {
      return false;
    }

    int pointerCount = motionEvent.getPointerCount();

    MotionEvent.PointerProperties[] pointerProperties =
        new MotionEvent.PointerProperties[pointerCount];

    MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[pointerCount];

    /*
     * 投射光线到一个平面上，延伸到无限，位于视图在3D空间，而不是投射到节点的碰撞形状。
     * 这对于初始ACTION_DOWN事件之后的触摸事件的用户体验非常重要。
     * 例如，如果用户正在拖动滑动条，并且他们的手指移动到视图之外，
     * 他们的手指相对于滑动条的位置仍然有效。
     */
    Plane plane = new Plane(node.getWorldPosition(), node.getForward());
    RayHit rayHit = new RayHit();

    // 也投射光线到背面的平面上，因为我们渲染的视图是双面的。
    Plane backPlane = new Plane(node.getWorldPosition(), node.getBack());

    // 将每个指针的指针坐标转换为视图的本地坐标空间。
    for (int i = 0; i < pointerCount; i++) {
      MotionEvent.PointerProperties props = new MotionEvent.PointerProperties();
      MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();

      motionEvent.getPointerProperties(i, props);
      motionEvent.getPointerCoords(i, coords);

      Camera camera = scene.getCamera();
      Ray ray = camera.screenPointToRay(coords.x, coords.y);
      if (plane.rayIntersection(ray, rayHit)) {
        Vector3 viewPosition =
            convertWorldPositionToLocalView(node, rayHit.getPoint(), viewRenderable);

        coords.x = viewPosition.x;
        coords.y = viewPosition.y;
      } else if (backPlane.rayIntersection(ray, rayHit)) {
        Vector3 viewPosition =
            convertWorldPositionToLocalView(node, rayHit.getPoint(), viewRenderable);

        // 若为背面的平面，则X值翻转
        coords.x = viewRenderable.getView().getWidth() - viewPosition.x;
        coords.y = viewPosition.y;
      } else {
        coords.clear();
        props.clear();
      }

      pointerProperties[i] = props;
      pointerCoords[i] = coords;
    }

    // 必须复制带有新坐标的触摸事件，并将它分派给视图。
    MotionEvent me =
        MotionEvent.obtain(
            motionEvent.getDownTime(),
            motionEvent.getEventTime(),
            motionEvent.getAction(),
            pointerCount,
            pointerProperties,
            pointerCoords,
            motionEvent.getMetaState(),
            motionEvent.getButtonState(),
            motionEvent.getXPrecision(),
            motionEvent.getYPrecision(),
            motionEvent.getDeviceId(),
            motionEvent.getEdgeFlags(),
            motionEvent.getSource(),
            motionEvent.getFlags());

    return viewRenderable.getView().dispatchTouchEvent(me);
  }

  static Vector3 convertWorldPositionToLocalView(
      Node node, Vector3 worldPos, ViewRenderable viewRenderable) {
    Preconditions.checkNotNull(node, "Parameter \"node\" was null.");
    Preconditions.checkNotNull(worldPos, "Parameter \"worldPos\" was null.");
    Preconditions.checkNotNull(viewRenderable, "Parameter \"viewRenderable\" was null.");

    // 查找视图可渲染对象在局部空间中被触摸的位置。
    // 这将以米为单位相对于视图的中下。
    Vector3 localPos = node.worldToLocalPoint(worldPos);

    // 计算像素与米的比率。
    View view = viewRenderable.getView();
    int width = view.getWidth();
    int height = view.getHeight();
    float pixelsToMetersRatio = getPixelsToMetersRatio(viewRenderable);

    // 将位置转换为像素
    int xPixels = (int) (localPos.x * pixelsToMetersRatio);
    int yPixels = (int) (localPos.y * pixelsToMetersRatio);

    // 将坐标从可渲染对象的对齐原点转换为左上角原点。

    int halfWidth = width / 2;
    int halfHeight = height / 2;

    ViewRenderable.VerticalAlignment verticalAlignment = viewRenderable.getVerticalAlignment();
    switch (verticalAlignment) {
      case BOTTOM:
        yPixels = height - yPixels;
        break;
      case CENTER:
        yPixels = height - (yPixels + halfHeight);
        break;
      case TOP:
        yPixels = height - (yPixels + height);
        break;
    }

    ViewRenderable.HorizontalAlignment horizontalAlignment =
        viewRenderable.getHorizontalAlignment();
    switch (horizontalAlignment) {
      case LEFT:
        // Do nothing.
        break;
      case CENTER:
        xPixels = (xPixels + halfWidth);
        break;
      case RIGHT:
        xPixels = xPixels + width;
        break;
    }

    return new Vector3(xPixels, yPixels, 0.0f);
  }

  private static float getPixelsToMetersRatio(ViewRenderable viewRenderable) {
    View view = viewRenderable.getView();
    int width = view.getWidth();
    Vector3 size = viewRenderable.getSizer().getSize(viewRenderable.getView());

    if (size.x == 0.0f) {
      return 0.0f;
    }

    return (float) width / size.x;
  }
}
