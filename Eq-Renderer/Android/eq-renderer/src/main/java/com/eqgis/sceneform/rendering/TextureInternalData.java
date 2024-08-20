package com.eqgis.sceneform.rendering;

import androidx.annotation.Nullable;

import com.eqgis.sceneform.resources.SharedReference;
import com.eqgis.sceneform.utilities.AndroidPreconditions;
import com.google.ar.core.annotations.UsedByNative;

/**
 * 表示{@link Texture}用于渲染的共享数据。当使用该数据的所有{@link Texture}完成时，数据将被释放。
 * @hide
 */
@UsedByNative("material_java_wrappers.h")
public class TextureInternalData extends SharedReference {
  @Nullable private com.google.android.filament.Texture filamentTexture;

  private final Texture.Sampler sampler;

  @UsedByNative("material_java_wrappers.h")
  public TextureInternalData(
      com.google.android.filament.Texture filamentTexture, Texture.Sampler sampler) {
    this.filamentTexture = filamentTexture;
    this.sampler = sampler;
  }

  com.google.android.filament.Texture getFilamentTexture() {
    if (filamentTexture == null) {
      throw new IllegalStateException("Filament Texture is null.");
    }

    return filamentTexture;
  }

  Texture.Sampler getSampler() {
    return sampler;
  }

  @Override
  protected void onDispose() {
    AndroidPreconditions.checkUiThread();

    IEngine engine = EngineInstance.getEngine();
    com.google.android.filament.Texture filamentTexture = this.filamentTexture;
    this.filamentTexture = null;
    if (filamentTexture != null && engine != null && engine.isValid()) {
      engine.destroyTexture(filamentTexture);
    }
  }
}
