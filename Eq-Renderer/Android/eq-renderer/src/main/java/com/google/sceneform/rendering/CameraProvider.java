package com.google.sceneform.rendering;

import com.google.sceneform.common.TransformProvider;
import com.google.sceneform.math.Matrix;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.eqgis.ar.ARCamera;

/**
 * 相机接口
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

  void updateTrackedPose(ARCamera camera,
                         Vector3 position, Quaternion quaternion);
}
