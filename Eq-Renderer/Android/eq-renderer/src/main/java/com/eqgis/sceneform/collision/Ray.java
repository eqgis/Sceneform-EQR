package com.eqgis.sceneform.collision;

import com.eqgis.sceneform.math.Vector3;
import com.eqgis.sceneform.utilities.Preconditions;

/**
 * 射线
 * <p>射线的数学表示，用于射线检测</p>
 * */
public class Ray {
  private Vector3 origin = new Vector3();
  private Vector3 direction = Vector3.forward();

  /** 构造函数 */
  public Ray() {}

  /**
   * 构造函数
   * @param origin 射线起点
   * @param direction 射线的方向
   */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Ray(Vector3 origin, Vector3 direction) {
    Preconditions.checkNotNull(origin, "Parameter \"origin\" was null.");
    Preconditions.checkNotNull(direction, "Parameter \"direction\" was null.");

    setOrigin(origin);
    setDirection(direction);
  }

  /**
   * 构造函数
   * @param origin 射线起点
   */
  public void setOrigin(Vector3 origin) {
    Preconditions.checkNotNull(origin, "Parameter \"origin\" was null.");
    this.origin.set(origin);
  }

  /**
   * 获取射线起点
   * @return 起点位置
   */
  public Vector3 getOrigin() {
    return new Vector3(origin);
  }

  /**
   * 设置射线的方向
   * <p>参数会自动进行归一化处理</p>
   * @param direction 射线的方向向量
   */
  public void setDirection(Vector3 direction) {
    Preconditions.checkNotNull(direction, "Parameter \"direction\" was null.");

    this.direction.set(direction.normalized());
  }

  /**
   * 获取射线的方向向量
   * @return 射线的方向向量
   */
  public Vector3 getDirection() {
    return new Vector3(direction);
  }

  /**
   * 获取指定距离的空间位置
   * @param distance 距离
   * @return 空间位置
   */
  public Vector3 getPoint(float distance) {
    return Vector3.add(origin, direction.scaled(distance));
  }

  @Override
  public String toString() {
    return "[Origin:" + origin + ", Direction:" + direction + "]";
  }
}
