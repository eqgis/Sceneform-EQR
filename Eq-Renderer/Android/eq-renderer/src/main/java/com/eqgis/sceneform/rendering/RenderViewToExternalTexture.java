package com.eqgis.sceneform.rendering;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.view.Surface;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.eqgis.sceneform.utilities.Preconditions;

import java.util.ArrayList;

/**
 * 用于渲染安卓View的扩展纹理对象
 * <p>
 *     用于渲染android视图到一个本地的open GL纹理，然后再由open GL渲染。
 * </p>
 * <p>要正确绘制硬件加速的动画视图到表面纹理，视图必须是
 * 附加到一个窗口，并绘制到一个真正的DisplayListCanvas，这是一个隐藏类。为了实现此，完成以下操作:
 * <ul>
 *     <li>将RenderViewToSurfaceTexture附加到WindowManager。
 *     <li>重写dispatchDraw。
 *     <li>调用superdispatchDraw使用真正的DisplayListCanvas
 *     <li>在DisplayListCanvas上绘制透明颜色，这样它在屏幕上就不可见了。
 *     <li>每帧绘制一个视图到SurfaceTexture。每一帧都必须这样做，因为当子视图在硬件加速时动画时，视图不会被标记为dirty。
 * </ul>
 *
 * @hide 内部调用，外部直接使用{@link ViewRenderable}即可
 */

class RenderViewToExternalTexture extends LinearLayout {
  /** 当视图的大小发生变化时的监听事件 */
  public interface OnViewSizeChangedListener {
    /**
     * View尺寸改变时触发回调
     * @param width View的宽度
     * @param height View的高度
     */
    void onViewSizeChanged(int width, int height);
  }

  private final View view;
  private final ExternalTexture externalTexture;
  private final Picture picture = new Picture();
  private boolean hasDrawnToSurfaceTexture = false;

  @Nullable private ViewAttachmentManager viewAttachmentManager;
  private final ArrayList<OnViewSizeChangedListener> onViewSizeChangedListeners = new ArrayList<>();

  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  RenderViewToExternalTexture(Context context, View view) {
    super(context);
    Preconditions.checkNotNull(view, "Parameter \"view\" was null.");

    externalTexture = new ExternalTexture();

    this.view = view;
    addView(view);
  }

  /**
   * 添加回调
   */
  void addOnViewSizeChangedListener(OnViewSizeChangedListener onViewSizeChangedListener) {
    if (!onViewSizeChangedListeners.contains(onViewSizeChangedListener)) {
      onViewSizeChangedListeners.add(onViewSizeChangedListener);
    }
  }

  /**
   * 移除回调
   */
  void removeOnViewSizeChangedListener(OnViewSizeChangedListener onViewSizeChangedListener) {
    onViewSizeChangedListeners.remove(onViewSizeChangedListener);
  }

  ExternalTexture getExternalTexture() {
    return externalTexture;
  }

  boolean hasDrawnToSurfaceTexture() {
    return hasDrawnToSurfaceTexture;
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override
  public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    externalTexture.getSurfaceTexture().setDefaultBufferSize(view.getWidth(), view.getHeight());
  }

  @Override
  public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
    for (OnViewSizeChangedListener onViewSizeChangedListener : onViewSizeChangedListeners) {
      onViewSizeChangedListener.onViewSizeChanged(width, height);
    }
  }

  @Override
  public void dispatchDraw(Canvas canvas) {
    // Sanity that the surface is valid.
    Surface targetSurface = externalTexture.getSurface();
    if (!targetSurface.isValid()) {
      return;
    }

    if (view.isDirty()) {
      Canvas pictureCanvas = picture.beginRecording(view.getWidth(), view.getHeight());
      pictureCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
      super.dispatchDraw(pictureCanvas);
      picture.endRecording();

      Canvas surfaceCanvas = targetSurface.lockCanvas(null);
      picture.draw(surfaceCanvas);
      targetSurface.unlockCanvasAndPost(surfaceCanvas);

      hasDrawnToSurfaceTexture = true;
    }

    invalidate();
  }

  void attachView(ViewAttachmentManager viewAttachmentManager) {
    if (this.viewAttachmentManager != null) {
      if (this.viewAttachmentManager != viewAttachmentManager) {
        throw new IllegalStateException(
            "Cannot use the same ViewRenderable with multiple SceneViews.");
      }

      return;
    }

    this.viewAttachmentManager = viewAttachmentManager;
    viewAttachmentManager.addView(this);
  }

  void detachView() {
    if (viewAttachmentManager != null) {
      viewAttachmentManager.removeView(this);
      viewAttachmentManager = null;
    }
  }

  void releaseResources() {
    detachView();
    //释放Surface和SurfaceTexture
    //由它们的finalizer自动执行。
  }
}
