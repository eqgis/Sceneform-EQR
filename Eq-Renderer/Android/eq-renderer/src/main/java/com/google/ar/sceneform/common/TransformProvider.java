package com.google.ar.sceneform.common;

import com.google.ar.sceneform.math.Matrix;
import com.google.ar.sceneform.Node;

/**
 * Interface for providing information about a 3D transformation. See {@link
 * Node}.
 *
 * @hide
 */
public interface TransformProvider {
  Matrix getWorldModelMatrix();
}
