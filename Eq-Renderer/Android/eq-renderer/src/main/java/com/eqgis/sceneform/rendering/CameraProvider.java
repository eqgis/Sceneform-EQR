package com.eqgis.sceneform.rendering;

import com.eqgis.sceneform.common.TransformProvider;
import com.eqgis.sceneform.math.Matrix;
import com.eqgis.sceneform.math.Quaternion;
import com.eqgis.sceneform.math.Vector3;
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
