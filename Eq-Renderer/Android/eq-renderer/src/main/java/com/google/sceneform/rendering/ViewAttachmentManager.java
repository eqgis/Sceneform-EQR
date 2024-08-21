package com.google.sceneform.rendering;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * 管理直接附加到{@link WindowManager}的{@link FrameLayout}视图可以添加和删除。
 * <p>
 *     要呈现{@link View}， {@link View}必须附加到{@link WindowManager}，以便
 *     它可以被恰当地画出来。这个类封装了一个{@link FrameLayout}，它附加到
 *     {@link WindowManager}，其他视图可以作为子视图添加。这使我们能够
 *     绘制{@link View}与{@link ViewRenderable}相关联的{@link View}，同时保留它们
 *     与活动视图层次结构的其余部分隔离。
 *     此外，这管理窗口的生命周期，以帮助确保窗口是有效的
 * </p>
 * 在适当的时候从WindowManager中添加/删除。
 * @hide
 */
class ViewAttachmentManager {
  // 拥有ViewAttachmentManager的视图，用于向UI线程触发回调。
  private final View ownerView;

  private final WindowManager windowManager;
  private final WindowManager.LayoutParams windowLayoutParams;

  private FrameLayout frameLayout;
  private final ViewGroup.LayoutParams viewLayoutParams;

  private static final String VIEW_RENDERABLE_WINDOW = "ViewRenderableWindow";
  private boolean isAdded = false;

  /**
   * 构造函数
   * @param ownerView ViewAttachmentManager用来在UI线程上触发回调
   */
  ViewAttachmentManager(Context context, View ownerView) {
    this.ownerView = ownerView;

    windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    windowLayoutParams = createWindowLayoutParams();

    frameLayout = new FrameLayout(context);
    viewLayoutParams = createViewLayoutParams();
  }

  FrameLayout getFrameLayout() {
    return frameLayout;
  }

  void onResume() {
    // ownerView只能在activity完成恢复后添加到WindowManager中。
    // 因此，我们必须使用post来确保只有在resume完成后才添加窗口。
    if (!isAdded){
      ownerView.postDelayed(
              () -> {
                if (frameLayout.getParent() == null && ownerView.isAttachedToWindow()) {
                  windowManager.addView(frameLayout, windowLayoutParams);
                }
              },100);
      isAdded = true;
    }
  }

  void onPause() {
    // ownerView必须在activity被销毁之前从WindowManager中移除，
    // 否则窗口将被泄露。因此，我们在resume/pause中添加/删除ownerView。
//    if (frameLayout.getParent() != null) {
//      windowManager.removeView(frameLayout);
//    }
  }

  void onDestroy(){
    if (frameLayout.getParent() != null) {
      windowManager.removeView(frameLayout);
    }
    frameLayout = null;
  }

  /**
   * 添加一个ownerView作为附加到{@link WindowManager}的{@link FrameLayout}的子视图。
   * <p>
   *     由{@link RenderViewToExternalTexture}使用，以确保ownerView被绘制并正确调用所有适当的生命周期事件。
   * </p>
   */
  void addView(View view) {
    if (view.getParent() == frameLayout) {
      return;
    }

    frameLayout.addView(view, viewLayoutParams);
  }

  /**
   * 从{@link FrameLayout}中移除附加到{@link WindowManager}的ownerView。
   * <p>由{@link RenderViewToExternalTexture}用来移除不再需要绘制的ownerView。
   */
  void removeView(View view) {
    if (view.getParent() != frameLayout) {
      return;
    }

    frameLayout.removeView(view);
  }

  private static WindowManager.LayoutParams createWindowLayoutParams() {
    WindowManager.LayoutParams params =
        new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT);
    params.setTitle(VIEW_RENDERABLE_WINDOW);

    return params;
  }

  private static ViewGroup.LayoutParams createViewLayoutParams() {
    ViewGroup.LayoutParams params =
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    return params;
  }
}
