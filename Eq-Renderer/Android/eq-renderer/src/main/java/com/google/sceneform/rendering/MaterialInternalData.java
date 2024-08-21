package com.google.sceneform.rendering;

import com.google.sceneform.resources.SharedReference;

abstract class MaterialInternalData extends SharedReference {
  abstract com.google.android.filament.Material getFilamentMaterial();
}
