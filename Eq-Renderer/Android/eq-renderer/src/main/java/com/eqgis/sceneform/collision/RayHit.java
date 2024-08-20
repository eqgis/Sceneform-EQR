package com.eqgis.sceneform.collision;

import com.eqgis.sceneform.math.Vector3;
import com.eqgis.sceneform.utilities.Preconditions;

/**
 * 射线检测结果
 * <p>存储针对各种类型的CollisionShape的光线相交测试的结果。</p>
 * @hide
 */
public class RayHit {
  private float distance = Float.MAX_VALUE;
  private final Vector3 point = new Vector3();

  /** @hide */
  public void setDistance(float distance) {
    this.distance = distance;
  }

  /**
   * 得到沿射线到碰撞形状表面上的撞击点的距离。
   * @return 距离
   */
  public float getDistance() {
    return distance;
  }

  /** @hide */
  public void setPoint(Vector3 point) {
    Preconditions.checkNotNull(point, "Parameter \"point\" was null.");
    this.point.set(point);
  }

  /**
   * 获得射线在世界空间中撞击到碰撞形状的位置。
   * @return 位置
   */
  public Vector3 getPoint() {
    return new Vector3(point);
  }

  /** @hide */
  public void set(RayHit other) {
    Preconditions.checkNotNull(other, "Parameter \"other\" was null.");

    setDistance(other.distance);
    setPoint(other.point);
  }

  /** @hide */
  public void reset() {
    distance = Float.MAX_VALUE;
    point.set(0, 0, 0);
  }
}
