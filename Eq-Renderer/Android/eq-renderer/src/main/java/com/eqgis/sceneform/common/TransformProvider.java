package com.eqgis.sceneform.common;

import com.eqgis.sceneform.math.Matrix;
import com.eqgis.sceneform.Node;

/**
 * Interface for providing information about a 3D transformation. See {@link
 * Node}.
 *
 * @hide
 */
public interface TransformProvider {
  Matrix getWorldModelMatrix();
}
