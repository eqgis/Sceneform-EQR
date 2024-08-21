package com.google.sceneform.rendering;


import com.google.sceneform.resources.SharedReference;
import com.google.sceneform.utilities.AndroidPreconditions;

/**
 * 表示{@link ViewRenderable}用于渲染的共享数据。
 * 当使用这些数据的所有{@link ViewRenderable}完成时，数据将被释放。
 */

class ViewRenderableInternalData extends SharedReference {
  private final RenderViewToExternalTexture renderView;

  ViewRenderableInternalData(RenderViewToExternalTexture renderView) {
    this.renderView = renderView;
  }

  RenderViewToExternalTexture getRenderView() {
    return renderView;
  }

  @Override
  protected void onDispose() {
    AndroidPreconditions.checkUiThread();

    renderView.releaseResources();
  }
}
