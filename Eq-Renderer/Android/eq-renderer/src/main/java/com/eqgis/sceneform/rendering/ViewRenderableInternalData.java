package com.eqgis.sceneform.rendering;


import com.eqgis.sceneform.resources.SharedReference;
import com.eqgis.sceneform.utilities.AndroidPreconditions;

/**
 * Represents shared data used by {@link ViewRenderable}s for rendering. The data will be released
 * when all {@link ViewRenderable}s using this data are finalized.
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
    AndroidPreconditions.checkEngineThread();

    renderView.releaseResources();
  }
}
