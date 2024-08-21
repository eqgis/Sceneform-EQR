package com.google.sceneform.math;

import com.google.sceneform.utilities.Preconditions;

/**
 * 三维矢量
 * <pre>
 *     1. 用于表示空间位置
 *     2. 用于表示向量(方向向量、法向量等)
 * </pre>
 * */
//Additional bugs: b/69935335
public class Vector3 {
  public float x;
  public float y;
  public float z;

  /** 构造函数 */
  public Vector3() {
    x = 0;
    y = 0;
    z = 0;
  }

  /** 构造函数 */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Vector3(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /** 构造函数 */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Vector3(Vector3 v) {
    Preconditions.checkNotNull(v, "Parameter \"v\" was null.");
    set(v);
  }

  /** 更新值 */
  public void set(Vector3 v) {
    Preconditions.checkNotNull(v, "Parameter \"v\" was null.");
    x = v.x;
    y = v.y;
    z = v.z;
  }

  /** 更新值 */
  public void set(float vx, float vy, float vz) {
    x = vx;
    y = vy;
    z = vz;
  }

  /** 更新值为0 */
  void setZero() {
    set(0, 0, 0);
  }

  /** 更新各分量为1 */
  void setOne() {
    set(1, 1, 1);
  }

  /**
   * 设置向前的方向向量
   * <p>注意：这跟坐标系有关</p>
   * */
  void setForward() {
    set(0, 0, -1);
  }

  /**
   * 设置向后的方向向量
   * <p>注意：这跟坐标系有关</p>
   * */
  void setBack() {
    set(0, 0, 1);
  }

  /**
   * 设置向上的方向向量
   * <p>注意：这跟坐标系有关</p>
   * */
  void setUp() {
    set(0, 1, 0);
  }

  /**
   * 设置向下的方向向量
   * <p>注意：这跟坐标系有关</p>
   * */
  void setDown() {
    set(0, -1, 0);
  }

  /**
   * 设置向右的方向向量
   * <p>注意：这跟坐标系有关</p>
   *  */
  void setRight() {
    set(1, 0, 0);
  }

  /**
   * 设置向左的方向向量
   * <p>注意：这跟坐标系有关</p>
   * */
  void setLeft() {
    set(-1, 0, 0);
  }

  /**
   * 获取长度的平方
   * @return 值
   */
  public float lengthSquared() {
    return x * x + y * y + z * z;
  }

  /**
   * 获取长度
   * @return 值
   */
  public float length() {
    return (float) Math.sqrt(lengthSquared());
  }

  @Override
  public String toString() {
    return "[x=" + x + ", y=" + y + ", z=" + z + "]";
  }

  /**
   * 归一化
   * */
  public Vector3 normalized() {
    Vector3 result = new Vector3(this);
    float normSquared = Vector3.dot(this, this);

    if (MathHelper.almostEqualRelativeAndAbs(normSquared, 0.0f)) {
      result.setZero();
    } else if (normSquared != 1) {
      float norm = (float) (1.0 / Math.sqrt(normSquared));
      result.set(this.scaled(norm));
    }
    return result;
  }

  /**
   * 缩放
   * @return 缩放后的矢量
   */
  public Vector3 scaled(float a) {
    return new Vector3(x * a, y * a, z * a);
  }

  /**
   * 取反
   * @return 相反的矢量
   */
  public Vector3 negated() {
    return new Vector3(-x, -y, -z);
  }

  /**
   * 相加
   * @return 相加后的矢量
   */
  public static Vector3 add(Vector3 lhs, Vector3 rhs) {
    Preconditions.checkNotNull(lhs, "Parameter \"lhs\" was null.");
    Preconditions.checkNotNull(rhs, "Parameter \"rhs\" was null.");
    return new Vector3(lhs.x + rhs.x, lhs.y + rhs.y, lhs.z + rhs.z);
  }

  /**
   * 相减
   * <p>lhs - rhs</p>
   * @return 相减后的矢量
   */
  public static Vector3 subtract(Vector3 lhs, Vector3 rhs) {
    Preconditions.checkNotNull(lhs, "Parameter \"lhs\" was null.");
    Preconditions.checkNotNull(rhs, "Parameter \"rhs\" was null.");
    return new Vector3(lhs.x - rhs.x, lhs.y - rhs.y, lhs.z - rhs.z);
  }

  /**
   * 点乘
   * @return 点积
   */
  public static float dot(Vector3 lhs, Vector3 rhs) {
    Preconditions.checkNotNull(lhs, "Parameter \"lhs\" was null.");
    Preconditions.checkNotNull(rhs, "Parameter \"rhs\" was null.");
    return lhs.x * rhs.x + lhs.y * rhs.y + lhs.z * rhs.z;
  }

  /**
   * 叉乘
   * @return 垂直于两个矢量的矢量
   */
  public static Vector3 cross(Vector3 lhs, Vector3 rhs) {
    Preconditions.checkNotNull(lhs, "Parameter \"lhs\" was null.");
    Preconditions.checkNotNull(rhs, "Parameter \"rhs\" was null.");
    float lhsX = lhs.x;
    float lhsY = lhs.y;
    float lhsZ = lhs.z;
    float rhsX = rhs.x;
    float rhsY = rhs.y;
    float rhsZ = rhs.z;
    return new Vector3(
        lhsY * rhsZ - lhsZ * rhsY, lhsZ * rhsX - lhsX * rhsZ, lhsX * rhsY - lhsY * rhsX);
  }

  /** 获取一个Vector3，每个值设置为两个Vector3值的元素最小值 */
  public static Vector3 min(Vector3 lhs, Vector3 rhs) {
    Preconditions.checkNotNull(lhs, "Parameter \"lhs\" was null.");
    Preconditions.checkNotNull(rhs, "Parameter \"rhs\" was null.");
    return new Vector3(Math.min(lhs.x, rhs.x), Math.min(lhs.y, rhs.y), Math.min(lhs.z, rhs.z));
  }

  /** 获取一个Vector3，每个值设置为两个Vector3值的元素最大值 */
  public static Vector3 max(Vector3 lhs, Vector3 rhs) {
    Preconditions.checkNotNull(lhs, "Parameter \"lhs\" was null.");
    Preconditions.checkNotNull(rhs, "Parameter \"rhs\" was null.");
    return new Vector3(Math.max(lhs.x, rhs.x), Math.max(lhs.y, rhs.y), Math.max(lhs.z, rhs.z));
  }

  /** 获取XYZ分量中的最大值 */
  static float componentMax(Vector3 a) {
    Preconditions.checkNotNull(a, "Parameter \"a\" was null.");
    return Math.max(Math.max(a.x, a.y), a.z);
  }

  /** 获取XYZ分量重的最小值 */
  static float componentMin(Vector3 a) {
    Preconditions.checkNotNull(a, "Parameter \"a\" was null.");
    return Math.min(Math.min(a.x, a.y), a.z);
  }

  /**
   * 线性插值
   * @param a 开始值
   * @param b 结束值
   * @param t 比例
   * @return 插值结果
   */
  public static Vector3 lerp(Vector3 a, Vector3 b, float t) {
    Preconditions.checkNotNull(a, "Parameter \"a\" was null.");
    Preconditions.checkNotNull(b, "Parameter \"b\" was null.");
    return new Vector3(
        MathHelper.lerp(a.x, b.x, t), MathHelper.lerp(a.y, b.y, t), MathHelper.lerp(a.z, b.z, t));
  }

  /**
   * 返回两个向量之间的最短角度(以度为单位)
   * <p>注意：结果永远不会大于180度</p>
   */
  public static float angleBetweenVectors(Vector3 a, Vector3 b) {
    float lengthA = a.length();
    float lengthB = b.length();
    float combinedLength = lengthA * lengthB;

    if (MathHelper.almostEqualRelativeAndAbs(combinedLength, 0.0f)) {
      return 0.0f;
    }

    float dot = Vector3.dot(a, b);
    float cos = dot / combinedLength;

    // Clamp due to floating point precision that could cause dot to be > combinedLength.
    // Which would cause acos to return NaN.
    cos = MathHelper.clamp(cos, -1.0f, 1.0f);
    float angleRadians = (float) Math.acos(cos);
    return (float) Math.toDegrees(angleRadians);
  }

  /**
   * 比较两个向量是否相等
   * <p>注意：是在误差允许范围内的相等</p>
   * */
  public static boolean equals(Vector3 lhs, Vector3 rhs) {
    Preconditions.checkNotNull(lhs, "Parameter \"lhs\" was null.");
    Preconditions.checkNotNull(rhs, "Parameter \"rhs\" was null.");
    boolean result = true;
    result &= MathHelper.almostEqualRelativeAndAbs(lhs.x, rhs.x);
    result &= MathHelper.almostEqualRelativeAndAbs(lhs.y, rhs.y);
    result &= MathHelper.almostEqualRelativeAndAbs(lhs.z, rhs.z);
    return result;
  }

  /**
   * 比较两个向量是否相等
   * <p>注意：是在误差允许范围内的相等</p>
   */
  @Override
  @SuppressWarnings("override.param.invalid")
  public boolean equals(Object other) {
    if (!(other instanceof Vector3)) {
      return false;
    }
    if (this == other) {
      return true;
    }
    return Vector3.equals(this, (Vector3) other);
  }

  /** @hide */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(x);
    result = prime * result + Float.floatToIntBits(y);
    result = prime * result + Float.floatToIntBits(z);
    return result;
  }

  /** 获取一个所有分量值为0的矢量 */
  public static Vector3 zero() {
    return new Vector3();
  }

  /** 获取一个所有分量值为1的矢量 */
  public static Vector3 one() {
    Vector3 result = new Vector3();
    result.setOne();
    return result;
  }

  /** 获取向前的矢量 */
  public static Vector3 forward() {
    Vector3 result = new Vector3();
    result.setForward();
    return result;
  }

  /** 获取向后的矢量 */
  public static Vector3 back() {
    Vector3 result = new Vector3();
    result.setBack();
    return result;
  }

  /** 获取向上的矢量 */
  public static Vector3 up() {
    Vector3 result = new Vector3();
    result.setUp();
    return result;
  }

  /** 获取向下的矢量 */
  public static Vector3 down() {
    Vector3 result = new Vector3();
    result.setDown();
    return result;
  }

  /** 获取向右的矢量 */
  public static Vector3 right() {
    Vector3 result = new Vector3();
    result.setRight();
    return result;
  }

  /** 获取向左的矢量 */
  public static Vector3 left() {
    Vector3 result = new Vector3();
    result.setLeft();
    return result;
  }
}
