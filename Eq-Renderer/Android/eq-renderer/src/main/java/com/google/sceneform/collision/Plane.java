package com.google.sceneform.collision;


import com.google.sceneform.math.Vector3;
import com.google.sceneform.utilities.Preconditions;

/**
 * 平面类
 * <p>无限大小平面的数学表示。用于交叉测试。</p>
 *
 * @hide
 */
public class Plane {
  private final Vector3 center = new Vector3();
  private final Vector3 normal = new Vector3();

  private static final double NEAR_ZERO_THRESHOLD = 1e-6;

  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Plane(Vector3 center, Vector3 normal) {
    setCenter(center);
    setNormal(normal);
  }

  /**
   * 设置中心位置
   * @param center 中心位置
   */
  public void setCenter(Vector3 center) {
    Preconditions.checkNotNull(center, "Parameter \"center\" was null.");

    this.center.set(center);
  }

  /**
   * 获取中心位置
   * @return 位置
   */
  public Vector3 getCenter() {
    return new Vector3(center);
  }

  /**
   * 设置法向量
   * @param normal 法向量
   */
  public void setNormal(Vector3 normal) {
    Preconditions.checkNotNull(normal, "Parameter \"normal\" was null.");
    this.normal.set(normal.normalized());
  }

  /**
   * 获取平面的法向量
   * @return 向量
   */
  public Vector3 getNormal() {
    return new Vector3(normal);
  }

  /**
   * 执行射线检测
   * @param ray 射线
   * @param result 检测结果
   * @return 有交则返回true
   */
  public boolean rayIntersection(Ray ray, RayHit result) {
    Preconditions.checkNotNull(ray, "Parameter \"ray\" was null.");
    Preconditions.checkNotNull(result, "Parameter \"result\" was null.");

    Vector3 rayDirection = ray.getDirection();
    Vector3 rayOrigin = ray.getOrigin();

    float denominator = Vector3.dot(normal, rayDirection);
    if (Math.abs(denominator) > NEAR_ZERO_THRESHOLD) {
      Vector3 delta = Vector3.subtract(center, rayOrigin);
      float distance = Vector3.dot(delta, normal) / denominator;
      if (distance >= 0) {
        result.setDistance(distance);
        result.setPoint(ray.getPoint(result.getDistance()));
        return true;
      }
    }

    return false;
  }
}
