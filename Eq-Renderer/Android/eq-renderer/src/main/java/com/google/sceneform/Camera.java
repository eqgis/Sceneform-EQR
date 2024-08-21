package com.google.sceneform;

import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.eqgis.ar.ARCamera;
import com.google.sceneform.collision.Ray;
import com.google.sceneform.math.MathHelper;
import com.google.sceneform.math.Matrix;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.CameraProvider;
import com.google.sceneform.rendering.EngineInstance;
import com.google.sceneform.utilities.Preconditions;
import com.eqgis.ar.ARPose;

/**
 * 场景相机对象
 */
public class Camera extends Node implements CameraProvider {
  private final Matrix viewMatrix = new Matrix();
  private final Matrix projectionMatrix = new Matrix();

  private static final float DEFAULT_NEAR_PLANE = 0.01f;
  private static final float DEFAULT_FAR_PLANE = 30.0f;
  private static final int FALLBACK_VIEW_WIDTH = 1920;
  private static final int FALLBACK_VIEW_HEIGHT = 1080;

  // 默认的FOV，非AR场景时使用
  private static final float DEFAULT_VERTICAL_FOV_DEGREES = 90.0f;

  private float nearPlane = DEFAULT_NEAR_PLANE;
  private float farPlane = DEFAULT_FAR_PLANE;

  private float verticalFov = DEFAULT_VERTICAL_FOV_DEGREES;

  // ar场景相机标记，若使用AR，则为true
  private final boolean isArCamera;
  private boolean areMatricesInitialized;

  /**
   * 构造函数
   * @hide
   */
  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  Camera(boolean isArCamera) {
    this.isArCamera = isArCamera;
  }

  @SuppressWarnings("initialization")
  Camera(Scene scene) {
    super();
    Preconditions.checkNotNull(scene, "Parameter \"scene\" was null.");
    super.setParent(scene);

    isArCamera = scene.getView() instanceof ArSceneView;
    if (!isArCamera) {
      scene
          .getView()
          .addOnLayoutChangeListener(
              (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                  refreshProjectionMatrix());
    }
  }

  /** @hide */
  public void setNearClipPlane(float nearPlane) {
    this.nearPlane = nearPlane;

    // 如果这是一个ArCamera，当每一帧调用updateTrackedPose时，投影矩阵被重新创建。否则，现在更新它。
    if (!isArCamera) {
      refreshProjectionMatrix();
    }
  }

  @Override
  public float getNearClipPlane() {
    return nearPlane;
  }

  /** @hide */
  public void setFarClipPlane(float farPlane) {
    this.farPlane = farPlane;

    // 如果这是一个ArCamera，当每一帧调用updateTrackedPose时，投影矩阵被重新创建。否则，现在更新它。
    if (!isArCamera) {
      refreshProjectionMatrix();
    }
  }

  /**
   * 设置垂直视场角
   * @throws UnsupportedOperationException 若为AR模式，则抛出异常
   */
  
  public void setVerticalFovDegrees(float verticalFov) {
    this.verticalFov = verticalFov;

    if (!isArCamera) {
      refreshProjectionMatrix();
    } else {
      throw new UnsupportedOperationException("Cannot set the field of view for AR cameras.");
    }
  }

  /**
   * 获取垂直视场角
   * @throws IllegalStateException 若为AR模式，则抛出异常
   */
  
  public float getVerticalFovDegrees() {
    if (isArCamera) {
      if (areMatricesInitialized) {
        double fovRadians = 2.0 * Math.atan(1.0 / projectionMatrix.data[5]);
        return (float) Math.toDegrees(fovRadians);
      } else {
        throw new IllegalStateException(
            "Cannot get the field of view for AR cameras until the first frame after ARCore has "
                + "been resumed.");
      }
    } else {
      return verticalFov;
    }
  }

  @Override
  public float getFarClipPlane() {
    return farPlane;
  }

  /**
   * 获取视图矩阵
   *  @hide Used internally (b/113516741) */
  @Override
  public Matrix getViewMatrix() {
    return viewMatrix;
  }

  /**
   * 获取投影矩阵
   * @hide Used internally (b/113516741) and within rendering package */
  @Override
  public Matrix getProjectionMatrix() {
    return projectionMatrix;
  }

  /**
   * 更新跟踪姿态数据
   * @hide Called internally as part of the integration with ARCore, should not be called directly.
   */
  @Override
  public void updateTrackedPose(ARCamera camera) {
    Preconditions.checkNotNull(camera, "Parameter \"camera\" was null.");

    //更新投影矩阵
    camera.getProjectionMatrix(projectionMatrix.data, 0, nearPlane, farPlane);

    //更新视图矩阵
    camera.getViewMatrix(viewMatrix.data, 0);

    //更新节点的几何变换属性（模型矩阵）以匹配跟踪的姿态。
    ARPose pose = camera.getDisplayOrientedPose();
    Vector3 position = ArHelpers.extractPositionFromPose(pose);
    Quaternion rotation = ArHelpers.extractRotationFromPose(pose);

    super.setWorldPosition(position);
    super.setWorldRotation(rotation);

    areMatricesInitialized = true;
  }

  /**============新增代码段（added by Ikkyu(tanyx)）top====================*/


  /**
   * 使用融合后的姿态更新追踪姿态
   * @param camera 相机
   * @param position 位置
   * @param rotation 姿态
   */
  @Override
  public void updateTrackedPose(ARCamera camera, Vector3 position, Quaternion rotation) {
    Preconditions.checkNotNull(camera, "Parameter \"camera\" was null.");

    //更新投影矩阵
    camera.getProjectionMatrix(projectionMatrix.data, 0, nearPlane, farPlane);

    //更新视图矩阵
    camera.getViewMatrix(viewMatrix.data, 0);

    super.setWorldPosition(position);
    super.setWorldRotation(rotation);

    areMatricesInitialized = true;
  }

  /**
   * 更新跟踪姿态
   * @param mat 视图矩阵ViewMatrix
   * @param cameraPose 相机位姿{tx,ty,tz,qx,qy,qz,qw}
   */
  public void updateTrackedPose(float[] mat,float[] cameraPose){
    viewMatrix.set(mat);
    super.setWorldPosition(new Vector3(cameraPose[0],cameraPose[1],cameraPose[2]));
    super.setWorldRotation(new Quaternion(cameraPose[3],cameraPose[4],cameraPose[5],cameraPose[6]));
    areMatricesInitialized = true;
  }

  private float horizontalFOV = 1.5f;
  private boolean isUseCustomRatio = false;
  /**
   * @param horizontalFOV 水平fov
   * @param verticalFov 垂直fov
   * @param isUse 是否生效
   */
  public void setFOV(float horizontalFOV,float verticalFov,boolean isUse) {
    this.isUseCustomRatio = isUse;
    if (this.verticalFov == verticalFov && this.horizontalFOV == horizontalFOV){
      return;
    }
    this.verticalFov = verticalFov;
    this.horizontalFOV = horizontalFOV;
    if (!isArCamera) {
      refreshProjectionMatrix();//horizontalFOV
    }
  }

  /**============新增代码段（added by Ikkyu(tanyx)）bottom====================*/

  Ray motionEventToRay(MotionEvent motionEvent) {
    Preconditions.checkNotNull(motionEvent, "Parameter \"motionEvent\" was null.");
    int index = motionEvent.getActionIndex();
    return screenPointToRay(motionEvent.getX(index), motionEvent.getY(index));
  }

  /**
   * 屏幕坐标转射线
   * @param x 屏幕坐标的X值
   * @param y 屏幕坐标的Y值
   */
  public Ray screenPointToRay(float x, float y) {
    Vector3 startPoint = new Vector3();
    Vector3 endPoint = new Vector3();

    unproject(x, y, 0.0f, startPoint);
    unproject(x, y, 1.0f, endPoint);

    Vector3 direction = Vector3.subtract(endPoint, startPoint);

    return new Ray(startPoint, direction);
  }

  /**
   * 世界坐标转屏幕坐标
   * @param point 世界坐标系下的空间坐标位置
   * @return 屏幕坐标
   */
  public Vector3 worldToScreenPoint(Vector3 point) {
    Matrix m = new Matrix();
    Matrix.multiply(projectionMatrix, viewMatrix, m);

    int viewWidth = getViewWidth();
    int viewHeight = getViewHeight();
    float x = point.x;
    float y = point.y;
    float z = point.z;
    float w = 1.0f;

    //乘以世界坐标
    Vector3 screenPoint = new Vector3();
    screenPoint.x = x * m.data[0] + y * m.data[4] + z * m.data[8] + w * m.data[12];
    screenPoint.y = x * m.data[1] + y * m.data[5] + z * m.data[9] + w * m.data[13];
    w = x * m.data[3] + y * m.data[7] + z * m.data[11] + w * m.data[15];

    //转至裁剪平面
    screenPoint.x = ((screenPoint.x / w) + 1.0f) * 0.5f;
    screenPoint.y = ((screenPoint.y / w) + 1.0f) * 0.5f;

    //转至屏幕空间
    screenPoint.x = screenPoint.x * viewWidth;
    screenPoint.y = screenPoint.y * viewHeight;

    //反转Y，因为屏幕Y向下，3D场景Y向上。
    screenPoint.y = viewHeight - screenPoint.y;

    return screenPoint;
  }

  /** 不支持的操作。摄像机的父级是无法改变的，它始终是场景。*/
  @Override
  public void setParent(@Nullable NodeParent parent) {
    throw new UnsupportedOperationException(
        "Camera's parent cannot be changed, it is always the scene.");
  }

  /**
   * 设置本地坐标系下的位置
   */
  @Override
  public void setLocalPosition(Vector3 position) {
    if (isArCamera) {
      throw new UnsupportedOperationException(
          "Camera's position cannot be changed, it is controller by the ARCore camera pose.");
    } else {
      super.setLocalPosition(position);
      Matrix.invert(getWorldModelMatrix(), viewMatrix);
    }
  }

  /**
   * 设置本地坐标系下的旋转四元数
   */
  @Override
  public void setLocalRotation(Quaternion rotation) {
    if (isArCamera) {
      throw new UnsupportedOperationException(
          "Camera's rotation cannot be changed, it is controller by the ARCore camera pose.");
    } else {
      super.setLocalRotation(rotation);
      Matrix.invert(getWorldModelMatrix(), viewMatrix);
    }
  }

  /**
   * 设置世界坐标系下的位置
   */
  @Override
  public void setWorldPosition(Vector3 position) {
    if (isArCamera) {
      throw new UnsupportedOperationException(
          "Camera's position cannot be changed, it is controller by the ARCore camera pose.");
    } else {
      super.setWorldPosition(position);
      Matrix.invert(getWorldModelMatrix(), viewMatrix);
    }
  }

  /**
   * 设置世界坐标系下的旋转四元数
   */
  @Override
  public void setWorldRotation(Quaternion rotation) {
    if (isArCamera) {
      throw new UnsupportedOperationException(
          "Camera's rotation cannot be changed, it is controller by the ARCore camera pose.");
    } else {
      super.setWorldRotation(rotation);
      Matrix.invert(getWorldModelMatrix(), viewMatrix);
    }
  }

  /** @hide 调测时使用，设置投影矩阵 */
  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  public void setProjectionMatrix(Matrix matrix) {
    projectionMatrix.set(matrix.data);
  }

  private boolean unproject(float x, float y, float z, final Vector3 dest) {
    Preconditions.checkNotNull(dest, "Parameter \"dest\" was null.");

    Matrix m = new Matrix();
    Matrix.multiply(projectionMatrix, viewMatrix, m);
    Matrix.invert(m, m);

    int viewWidth = getViewWidth();
    int viewHeight = getViewHeight();

    // Invert Y because screen Y points down and Sceneform Y points up.
    y = viewHeight - y;

    // Normalize between -1 and 1.
    x = x / viewWidth * 2.0f - 1.0f;
    y = y / viewHeight * 2.0f - 1.0f;
    z = 2.0f * z - 1.0f;
    float w = 1.0f;

    dest.x = x * m.data[0] + y * m.data[4] + z * m.data[8] + w * m.data[12];
    dest.y = x * m.data[1] + y * m.data[5] + z * m.data[9] + w * m.data[13];
    dest.z = x * m.data[2] + y * m.data[6] + z * m.data[10] + w * m.data[14];
    w = x * m.data[3] + y * m.data[7] + z * m.data[11] + w * m.data[15];

    if (MathHelper.almostEqualRelativeAndAbs(w, 0.0f)) {
      dest.set(0, 0, 0);
      return false;
    }

    w = 1.0f / w;
    dest.set(dest.scaled(w));
    return true;
  }

  private int getViewWidth() {
    Scene scene = getScene();
    if (scene == null || EngineInstance.isHeadlessMode()) {
      return FALLBACK_VIEW_WIDTH;
    }

    return scene.getView().getWidth();
  }

  private int getViewHeight() {
    Scene scene = getScene();
    if (scene == null || EngineInstance.isHeadlessMode()) {
      return FALLBACK_VIEW_HEIGHT;
    }

    return scene.getView().getHeight();
  }

  //仅在非AR下，生效，刷新投影矩阵
  private void refreshProjectionMatrix() {
    if (isArCamera) {
      return;
    }

    if (isUseCustomRatio){
      //desc-added by Ikkyu 2022年1月18日14:11:04
      setCustomPerspective(verticalFov, /*自定义水平Fov*/horizontalFOV, nearPlane, farPlane);
    }else {
      int width = getViewWidth();
      int height = getViewHeight();

      if (width == 0 || height == 0) {
        return;
      }

      float aspect = (float) width / (float) height;
      setPerspective(verticalFov, aspect, nearPlane, farPlane);
    }
  }

  /**
   * 根据视野、长宽比、近平面和远平面设置相机视场角。
   * verticalFovInDegrees必须大于0且小于180°。远-近必须大于零。Aspect必须大于零。近和远必须大于零。
   *
   * @param verticalFovInDegrees 垂直FOV
   * @param aspect viewport的宽高比，即widthInPixels / hightinpixels。
   * @param near 近裁剪平面
   * @param far 远裁剪屏幕
   * @throws IllegalArgumentException 如果不满足以下任何一个前提条件:
   *     <ul>
   *       <li>0 < verticalFovInDegrees < 180
   *       <li>aspect > 0
   *       <li>near > 0
   *       <li>far > near
   *     </ul>
   */
  private void setPerspective(float verticalFovInDegrees, float aspect, float near, float far) {
    if (verticalFovInDegrees <= 0.0f || verticalFovInDegrees >= 180.0f) {
//      throw new IllegalArgumentException(
//          "Parameter \"verticalFovInDegrees\" is out of the valid range of (0, 180) degrees.");
      return;
    }
    if (aspect <= 0.0f) {
      throw new IllegalArgumentException("Parameter \"aspect\" must be greater than zero.");
    }

    final double fovInRadians = Math.toRadians((double) verticalFovInDegrees);
    final float top = (float) Math.tan(fovInRadians * 0.5) * near;
    final float bottom = -top;
    final float right = top * aspect;
    final float left = -right;

    setPerspective(left, right, bottom, top, near, far);
  }
  private void setCustomPerspective(float verticalFovInDegrees, float horizontalFOVInDegrees, float near, float far) {
    if (verticalFovInDegrees <= 0.0f || verticalFovInDegrees >= 180.0f) {
      throw new IllegalArgumentException(
              "Parameter \"verticalFovInDegrees\" is out of the valid range of (0, 180) degrees.");
    }
    if (horizontalFOVInDegrees <= 0.0f || horizontalFOVInDegrees >= 180.0f) {
      throw new IllegalArgumentException(
              "Parameter \"horizontalFOVInDegrees\" is out of the valid range of (0, 180) degrees.");
    }

    final double fovInRadians = Math.toRadians((double) verticalFovInDegrees);
    final float top = (float) Math.tan(fovInRadians * 0.5) * near;
    final float bottom = -top;
//    final float right = top *1520f/1575f * 1.77777f;//test
    final float right = (float) Math.tan(Math.toRadians((double) horizontalFOVInDegrees) * 0.5) * near;
    final float left = -right;

    setPerspective(left, right, bottom, top, near, far);
  }

  /**
   * 根据六个剪辑平面设置摄像机透视投影。左右必须更大
   * 大于0。Top - bottom必须大于零。远-近必须大于零。附近
   * 和far必须大于零。
   *
   * @param left 在近平面从相机到左平面的世界单位偏移量。
   * @param right 在近平面上，从相机到右平面的世界单位偏移量。
   * @param bottom 在近平面上，从相机到底平面的世界单位偏移量。
   * @param top 在近平面上，从摄像机到顶平面的世界单位偏移量。
   * @param near 近裁剪平面
   * @param far 远裁剪屏幕
   * @throws IllegalArgumentException 如果不满足以下任何一个前提条件:
   *     <ul>
   *       <li>left != right
   *       <li>bottom != top
   *       <li>near > 0
   *       <li>far > near
   *     </ul>
   */
  private void setPerspective(
      float left, float right, float bottom, float top, float near, float far) {
    float[] data = projectionMatrix.data;

    if (left == right || bottom == top || near <= 0.0f || far <= near) {
      throw new IllegalArgumentException(
          "Invalid parameters to setPerspective, valid values: "
              + " width != height, bottom != top, near > 0.0f, far > near");
    }

    final float reciprocalWidth = 1.0f / (right - left);
    final float reciprocalHeight = 1.0f / (top - bottom);
    final float reciprocalDepthRange = 1.0f / (far - near);

    //右手坐标系，4X4矩阵
    data[0] = 2.0f * near * reciprocalWidth;
    data[1] = 0.0f;
    data[2] = 0.0f;
    data[3] = 0.0f;

    data[4] = 0.0f;
    data[5] = 2.0f * near * reciprocalHeight;
    data[6] = 0.0f;
    data[7] = 0.0f;

    data[8] = (right + left) * reciprocalWidth;
    data[9] = (top + bottom) * reciprocalHeight;
    data[10] = -(far + near) * reciprocalDepthRange;
    data[11] = -1.0f;

    data[12] = 0.0f;
    data[13] = 0.0f;
    data[14] = -2.0f * far * near * reciprocalDepthRange;
    data[15] = 0.0f;

    nearPlane = near;
    farPlane = far;
    areMatricesInitialized = true;
  }

  /**
   * 更新投影矩阵
   * @param mat 投影矩阵
   * @param nearClipPlane 近裁剪平面距离
   * @param farClipPlane 远裁剪平面距离
   */
  public void updateProjectionMatrix(float[] mat, float nearClipPlane, float farClipPlane) {
    projectionMatrix.set(mat);
    //由于vFov具有get方法，因此这里需要同步更新一下
    verticalFov = (float) Math.toDegrees((2.0 * Math.atan(1.0 / projectionMatrix.data[5])));
    horizontalFOV = (float) Math.toDegrees((2.0 * Math.atan(1.0 / projectionMatrix.data[0])));
    this.nearPlane = nearClipPlane;
    this.farPlane = farClipPlane;
    areMatricesInitialized = true;
  }
}
