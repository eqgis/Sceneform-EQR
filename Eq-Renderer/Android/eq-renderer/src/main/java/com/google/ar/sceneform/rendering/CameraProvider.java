package com.google.ar.sceneform.rendering;

import com.google.ar.sceneform.common.TransformProvider;
import com.google.ar.sceneform.math.Matrix;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.eqgis.ar.ARCamera;

/**
 * Required interface for a virtual camera.
 *
 * @hide
 */
public interface CameraProvider extends TransformProvider {
  boolean isActive();

  float getNearClipPlane();

  float getFarClipPlane();

  Matrix getViewMatrix();

  Matrix getProjectionMatrix();

  void updateTrackedPose(ARCamera camera);

  /**============（added by Ikkyu(tanyx)）top====================*/

  void updateTrackedPose(ARCamera camera,
                         Vector3 position, Quaternion quaternion);
  /**============（added by Ikkyu(tanyx)）bottom====================*/
}
