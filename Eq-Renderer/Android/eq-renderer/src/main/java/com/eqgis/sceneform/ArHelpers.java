package com.eqgis.sceneform;

import com.eqgis.sceneform.math.Quaternion;
import com.eqgis.sceneform.math.Vector3;
import com.google.ar.core.Pose;
import com.eqgis.ar.ARPose;

/**
 * AR助手
 * <p>用于从ARCore或AREngine的Pose中提取姿态数据</p>
 * */
public class ArHelpers {
  /** 从{@link Pose}中提取位置 */
  static Vector3 extractPositionFromPose(Pose pose) {
    return new Vector3(pose.tx(), pose.ty(), pose.tz());
  }

  /**
   * 从{@link Pose}中提取旋转四元数
   */
  static Quaternion extractRotationFromPose(Pose pose) {
    return new Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw());
  }

  /**
   * 从{@link Pose}中提取位置
   */
  public static Vector3 extractPositionFromPose(ARPose pose) {
    return new Vector3(pose.tx(), pose.ty(), pose.tz());
//    return updatePoseOnAREngine(new Vector3(pose.tx(), pose.ty(), pose.tz()));
  }

  /**
   * 从{@link Pose}中提取旋转四元数
   */
  public static Quaternion extractRotationFromPose(ARPose pose) {
    return new Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw());
//    return updatePoseOnAREngine(new Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw()));
  }


  //<editor-fold> - 使AREngine的坐标系与ARCore保持一致
//  static Vector3 updatePoseOnAREngine(Vector3 position){
//    if (!AREngine.isUsingAREngine())return position;
//    //计算绕点旋转后的坐标，仅修改X,Z，y轴值不变
//    float offsetAngle = ARCamera.getOffsetAngle();
//    Quaternion offsetRotation = new Quaternion(Vector3.up(), offsetAngle);
//    //坐标旋转，相机坐标绕场景原点旋转，得到的新坐标作为相机位置
//    Vector3 newPosition = Quaternion.rotateVector(offsetRotation, position);
//    return newPosition;
//  }
//
//  static Quaternion updatePoseOnAREngine(Quaternion rotation){
//    if (!AREngine.isUsingAREngine())return rotation;
//    //desc-坐标系转换，右手系（X-right，Y-up）转换为（x-right，y-forward）
//    Quaternion newQuaternion = new Quaternion(rotation.x,-rotation.z, rotation.y, rotation.w);
//    //四元数转欧拉角
//    Vector3 vec = toEulerAngle(newQuaternion);
//    //水平面上，forward的偏移角度
//    vec.z += ARCamera.getOffsetAngle();;
//    //欧拉角转四元数
//    Quaternion quaternion = toQuaternion(vec.x, vec.y, vec.z);
//    //desc-坐标系转换右手系（x-right，y-forward）转换为右手系（X-right，Y-up）
//    Quaternion quaternion1 = new Quaternion(quaternion.x, quaternion.z, -quaternion.y, quaternion.w);
////    Log.e("IKKYU-C", "updateTrackedPose: "+vector3  + "--------"+vec + "Position " + newPosition);
//    return quaternion1;
//  }
//
//  /**
//   * 欧拉角to四元数
//   * 欧拉角单位为度
//   * @return
//   */
//  static Quaternion toQuaternion(float v1, float v2, float v3){
//    float c1 = (float) Math.cos(Math.toRadians(v1 / 2));
//    float c2 = (float) Math.cos(Math.toRadians(v2 / 2));
//    float c3 = (float) Math.cos(Math.toRadians(v3 / 2));
//
//    float s1 = (float) Math.sin(Math.toRadians(v1 / 2));
//    float s2 = (float) Math.sin(Math.toRadians(v2 / 2));
//    float s3 = (float) Math.sin(Math.toRadians(v3 / 2));
//
//    return new Quaternion(
//            s1 * c2 * c3 - c1 * s2 * s3,
//            c1 * s2 * c3 + s1 * c2 * s3,
//            c1 * c2 * s3 - s1 * s2 * c3,
//            c2 * c3 * c1 + s2 * s3 * s1
//    );
//  }
//
//  /**
//   * 四元数转欧拉角
//   * @param q
//   * @return
//   */
//  static Vector3 toEulerAngle(Quaternion q){
//    float x = (float) Math.toDegrees(Math.atan2(2*(q.w*q.x + q.y*q.z), 1-2*(q.x * q.x + q.y*q.y)));
//    float y = (float) Math.toDegrees(Math.asin( 2 * (q.w * q.y - q.x * q.z)));
//    float z = (float) Math.toDegrees(Math.atan2(2*(q.w*q.z + q.x*q.y), 1-2*(q.y * q.y + q.z*q.z)));
//    return new Vector3(x,y,z);
//  }
  //</editor-fold>
}
