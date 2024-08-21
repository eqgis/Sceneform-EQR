package com.google.sceneform.rendering;

import androidx.annotation.Nullable;

import com.google.sceneform.utilities.AndroidPreconditions;

/**
 * 表示{@link Material}用于渲染的共享数据。
 */
class MaterialInternalDataImpl extends MaterialInternalData {
  @Nullable private com.google.android.filament.Material filamentMaterial;

  MaterialInternalDataImpl(com.google.android.filament.Material filamentMaterial) {
    this.filamentMaterial = filamentMaterial;
  }

  @Override
  com.google.android.filament.Material getFilamentMaterial() {
    if (filamentMaterial == null) {
      throw new IllegalStateException("Filament Material is null.");
    }
    return filamentMaterial;
  }

  @Override
  protected void onDispose() {
    AndroidPreconditions.checkUiThread();

    IEngine engine = EngineInstance.getEngine();
    com.google.android.filament.Material material = this.filamentMaterial;
    this.filamentMaterial = null;
    if (material != null && engine != null && engine.isValid()) {
      engine.destroyMaterial(material);
    }
  }
}
