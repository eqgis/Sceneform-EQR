package com.eqgis.sceneform.rendering;

import com.eqgis.sceneform.resources.SharedReference;

abstract class MaterialInternalData extends SharedReference {
  abstract com.google.android.filament.Material getFilamentMaterial();
}
