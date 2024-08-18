package com.eqgis.sceneform.math;

import com.eqgis.sceneform.utilities.Preconditions;

/**
 * 四元数
 * <pre>
 *     用于表示旋转姿态，四元数运算是使用右手规则约定的哈密顿运算。
 * </pre>
 */
// TODO: Evaluate combining with java/com/google/ar/core/Quaternion.java
public class Quaternion {
  private static final float SLERP_THRESHOLD = 0.9995f;
  public float x;
  public float y;
  public float z;
  public float w;

  /** 构造函数 */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Quaternion() {
    x = 0;
    y = 0;
    z = 0;
    w = 1;
  }

  /**
   * 构造函数
   * @param x x分量
   * @param y y分量
   * @param z z分量
   * @param w w分量
   */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Quaternion(float x, float y, float z, float w) {
    set(x, y, z, w);
  }

  /**
   * 构造函数
   * @param q 四元数
   * */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Quaternion(Quaternion q) {
    Preconditions.checkNotNull(q, "Parameter \"q\" was null.");
    set(q);
  }

  /**
   * 构造函数
   * @param axis 旋转轴
   * @param angle 角度，单位：度
   */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Quaternion(Vector3 axis, float angle) {
    Preconditions.checkNotNull(axis, "Parameter \"axis\" was null.");
    set(Quaternion.axisAngle(axis, angle));
  }

  /**
   * 构造函数
   * @see #eulerAngles(Vector3 eulerAngles)
   * @param eulerAngles 欧拉角，包含每个XYZ三个旋转轴的旋转角度
   */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Quaternion(Vector3 eulerAngles) {
    Preconditions.checkNotNull(eulerAngles, "Parameter \"eulerAngles\" was null.");
    set(Quaternion.eulerAngles(eulerAngles));
  }

  /**
   * 更新四元数的值
   * */
  public void set(Quaternion q) {
    Preconditions.checkNotNull(q, "Parameter \"q\" was null.");
    x = q.x;
    y = q.y;
    z = q.z;
    w = q.w;
    normalize();
  }

  /**
   * 通过旋转轴/角度更新四元数
   * */
  public void set(Vector3 axis, float angle) {
    Preconditions.checkNotNull(axis, "Parameter \"axis\" was null.");
    set(Quaternion.axisAngle(axis, angle));
  }

  /**
   * 更新四元数
   * */
  public void set(float qx, float qy, float qz, float qw) {
    x = qx;
    y = qy;
    z = qz;
    w = qw;
    normalize();
  }

  /**
   * 设置单位初始值
   * */
  public void setIdentity() {
    x = 0;
    y = 0;
    z = 0;
    w = 1;
  }

  /**
   * 归一化
   * <p>将四元数各分量缩放成为单位长度，如果四元数不能缩放，则将其设置为identity并返回false。</p>
   * @return 如果四元数非零，则为true
   */
  public boolean normalize() {
    float normSquared = Quaternion.dot(this, this);
    if (MathHelper.almostEqualRelativeAndAbs(normSquared, 0.0f)) {
      setIdentity();
      return false;
    } else if (normSquared != 1) {
      float norm = (float) (1.0 / Math.sqrt(normSquared));
      x *= norm;
      y *= norm;
      z *= norm;
      w *= norm;
    } else {
      // do nothing if normSquared is already the unit length
    }
    return true;
  }

  /**
   * 获取归一化后的四元数
   * @return 四元数缩放到单位长度，如果不能，则为零。
   */
  public Quaternion normalized() {
    Quaternion result = new Quaternion(this);
    result.normalize();
    return result;
  }

  /**
   * 获取一个反向旋转的四元数
   * @return 相反的四元数
   */
  public Quaternion inverted() {
    return new Quaternion(-this.x, -this.y, -this.z, this.w);
  }

  /**
   * 翻转四元数的符号
   * <p>注意：符号翻转，但表示相同的旋转。</p>
   * @return 符号翻转后的四元数
   */
  Quaternion negated() {
    return new Quaternion(-this.x, -this.y, -this.z, -this.w);
  }

  @Override
  public String toString() {
    return "[x=" + x + ", y=" + y + ", z=" + z + ", w=" + w + "]";
  }

  /**
   * 通过四元数旋转指定向量
   * @return 旋转后的向量
   */
  public static Vector3 rotateVector(Quaternion q, Vector3 src) {
    Preconditions.checkNotNull(q, "Parameter \"q\" was null.");
    Preconditions.checkNotNull(src, "Parameter \"src\" was null.");
    Vector3 result = new Vector3();
    float w2 = q.w * q.w;
    float x2 = q.x * q.x;
    float y2 = q.y * q.y;
    float z2 = q.z * q.z;
    float zw = q.z * q.w;
    float xy = q.x * q.y;
    float xz = q.x * q.z;
    float yw = q.y * q.w;
    float yz = q.y * q.z;
    float xw = q.x * q.w;
    float m00 = w2 + x2 - z2 - y2;
    float m01 = xy + zw + zw + xy;
    float m02 = xz - yw + xz - yw;
    float m10 = -zw + xy - zw + xy;
    float m11 = y2 - z2 + w2 - x2;
    float m12 = yz + yz + xw + xw;
    float m20 = yw + xz + xz + yw;
    float m21 = yz + yz - xw - xw;
    float m22 = z2 - y2 - x2 + w2;
    float sx = src.x;
    float sy = src.y;
    float sz = src.z;
    result.x = m00 * sx + m10 * sy + m20 * sz;
    result.y = m01 * sx + m11 * sy + m21 * sz;
    result.z = m02 * sx + m12 * sy + m22 * sz;
    return result;
  }

  /**
   * 通过四元数反向旋转指定向量
   * @return 旋转后的向量
   */
  public static Vector3 inverseRotateVector(Quaternion q, Vector3 src) {
    Preconditions.checkNotNull(q, "Parameter \"q\" was null.");
    Preconditions.checkNotNull(src, "Parameter \"src\" was null.");
    Vector3 result = new Vector3();
    float w2 = q.w * q.w;
    float x2 = -q.x * -q.x;
    float y2 = -q.y * -q.y;
    float z2 = -q.z * -q.z;
    float zw = -q.z * q.w;
    float xy = -q.x * -q.y;
    float xz = -q.x * -q.z;
    float yw = -q.y * q.w;
    float yz = -q.y * -q.z;
    float xw = -q.x * q.w;
    float m00 = w2 + x2 - z2 - y2;
    float m01 = xy + zw + zw + xy;
    float m02 = xz - yw + xz - yw;
    float m10 = -zw + xy - zw + xy;
    float m11 = y2 - z2 + w2 - x2;
    float m12 = yz + yz + xw + xw;
    float m20 = yw + xz + xz + yw;
    float m21 = yz + yz - xw - xw;
    float m22 = z2 - y2 - x2 + w2;

    float sx = src.x;
    float sy = src.y;
    float sz = src.z;
    result.x = m00 * sx + m10 * sy + m20 * sz;
    result.y = m01 * sx + m11 * sy + m21 * sz;
    result.z = m02 * sx + m12 * sy + m22 * sz;
    return result;
  }

  /**
   * 四元数相乘
   * <p>
   * 创建一个四元数通过组合两个四元数相乘(lhs, rhs)相当于执行
   * rhs旋转然后lhs旋转排序对于这个操作很重要。
   * </p>
   * @return 四元数
   */
  public static Quaternion multiply(Quaternion lhs, Quaternion rhs) {
    Preconditions.checkNotNull(lhs, "Parameter \"lhs\" was null.");
    Preconditions.checkNotNull(rhs, "Parameter \"rhs\" was null.");
    float lx = lhs.x;
    float ly = lhs.y;
    float lz = lhs.z;
    float lw = lhs.w;
    float rx = rhs.x;
    float ry = rhs.y;
    float rz = rhs.z;
    float rw = rhs.w;

    Quaternion result =
        new Quaternion(
            lw * rx + lx * rw + ly * rz - lz * ry,
            lw * ry - lx * rz + ly * rw + lz * rx,
            lw * rz + lx * ry - ly * rx + lz * rw,
            lw * rw - lx * rx - ly * ry - lz * rz);
    return result;
  }

  /**
   * 对四元数进行缩放
   * <p>注意：这不是归一化操作，内部也未进行归一化</p>
   * @return 一个四元数乘以一个标量得到的结果
   */
  Quaternion scaled(float a) {
    Quaternion result = new Quaternion();
    result.x = this.x * a;
    result.y = this.y * a;
    result.z = this.z * a;
    result.w = this.w * a;

    return result;
  }

  /**
   * 实现两个四元数相加
   * <p>注意：这不是归一化操作，内部也未进行归一化</p>
   * @return 四元数
   */
  static Quaternion add(Quaternion lhs, Quaternion rhs) {
    Preconditions.checkNotNull(lhs, "Parameter \"lhs\" was null.");
    Preconditions.checkNotNull(rhs, "Parameter \"rhs\" was null.");
    Quaternion result = new Quaternion();
    result.x = lhs.x + rhs.x;
    result.y = lhs.y + rhs.y;
    result.z = lhs.z + rhs.z;
    result.w = lhs.w + rhs.w;
    return result;
  }

  /** 求两个四元数的点积 */
  static float dot(Quaternion lhs, Quaternion rhs) {
    Preconditions.checkNotNull(lhs, "Parameter \"lhs\" was null.");
    Preconditions.checkNotNull(rhs, "Parameter \"rhs\" was null.");
    return lhs.x * rhs.x + lhs.y * rhs.y + lhs.z * rhs.z + lhs.w * rhs.w;
  }

  /**
   * 根据比例在两个四元数之间进行线性插值
   */
  static Quaternion lerp(Quaternion a, Quaternion b, float ratio) {
    Preconditions.checkNotNull(a, "Parameter \"a\" was null.");
    Preconditions.checkNotNull(b, "Parameter \"b\" was null.");
    return new Quaternion(
        MathHelper.lerp(a.x, b.x, ratio),
        MathHelper.lerp(a.y, b.y, ratio),
        MathHelper.lerp(a.z, b.z, ratio),
        MathHelper.lerp(a.w, b.w, ratio));
  }

  /**
   * 返回两个给定方向之间的球面线性插值。
   * 如果t为0，则返回a。
   * 当t接近1时可能接近b或-b(以最接近a的为准)
   * 如果t大于1或小于0，结果将被外推。
   * @param start 起始值
   * @param end 结束值
   * @param t 两个浮点数之间的比率
   * @return 两个浮点数之间的插值值
   */
  public static Quaternion slerp(final Quaternion start, final Quaternion end, float t) {
    Preconditions.checkNotNull(start, "Parameter \"start\" was null.");
    Preconditions.checkNotNull(end, "Parameter \"end\" was null.");
    Quaternion orientation0 = start.normalized();
    Quaternion orientation1 = end.normalized();

    // cosTheta0 provides the angle between the rotations at t=0
    double cosTheta0 = Quaternion.dot(orientation0, orientation1);

    // Flip end rotation to get shortest path if needed
    if (cosTheta0 < 0.0f) {
      orientation1 = orientation1.negated();
      cosTheta0 = -cosTheta0;
    }

    // Small rotations should just use lerp
    if (cosTheta0 > SLERP_THRESHOLD) {
      return lerp(orientation0, orientation1, t);
    }

    // Cosine function range is -1,1. Clamp larger rotations.
    cosTheta0 = Math.max(-1, Math.min(1, cosTheta0));

    double theta0 = Math.acos(cosTheta0); // Angle between orientations at t=0
    double thetaT = theta0 * t; // theta0 scaled to current t

    // s0 = sin(theta0 - thetaT) / sin(theta0)
    double s0 = (Math.cos(thetaT) - cosTheta0 * Math.sin(thetaT) / Math.sin(theta0));
    double s1 = (Math.sin(thetaT) / Math.sin(theta0));
    // result = s0*start + s1*end
    Quaternion result =
        Quaternion.add(orientation0.scaled((float) s0), orientation1.scaled((float) s1));
    return result.normalized();
  }

  /**
   * 使用轴/角度来定义旋转，获得一个新的四元数
   * @param axis 旋转轴
   * @param degrees 旋转角度，单位：度
   */
  public static Quaternion axisAngle(Vector3 axis, float degrees) {
    Preconditions.checkNotNull(axis, "Parameter \"axis\" was null.");
    Quaternion dest = new Quaternion();
    double angle = Math.toRadians(degrees);
    double factor = Math.sin(angle / 2.0);

    dest.x = (float) (axis.x * factor);
    dest.y = (float) (axis.y * factor);
    dest.z = (float) (axis.z * factor);
    dest.w = (float) Math.cos(angle / 2.0);
    dest.normalize();
    return dest;
  }

  /**
   * 使用eulerAngles获得一个新的四元数来定义旋转。
   * <pre>
   *     旋转应用于Z, Y, X顺序。这与其他图形引擎是一致的。
   *     注意：这里（OpengGL右手坐标系）和Unity（左手坐标系）的坐标系统是不同的
   * </pre>
   * @param eulerAngles - 欧拉角
   */
  public static Quaternion eulerAngles(Vector3 eulerAngles) {
    Preconditions.checkNotNull(eulerAngles, "Parameter \"eulerAngles\" was null.");
    Quaternion qX = new Quaternion(Vector3.right(), eulerAngles.x);
    Quaternion qY = new Quaternion(Vector3.up(), eulerAngles.y);
    Quaternion qZ = new Quaternion(Vector3.back(), eulerAngles.z);
    return Quaternion.multiply(Quaternion.multiply(qY, qX), qZ);
  }

  /** 获取一个新的四元数，表示从一个向量到另一个向量的旋转。 */
  public static Quaternion rotationBetweenVectors(Vector3 start, Vector3 end) {
    Preconditions.checkNotNull(start, "Parameter \"start\" was null.");
    Preconditions.checkNotNull(end, "Parameter \"end\" was null.");

    start = start.normalized();
    end = end.normalized();

    float cosTheta = Vector3.dot(start, end);
    Vector3 rotationAxis;

    if (cosTheta < -1.0f + 0.001f) {
      // special case when vectors in opposite directions:
      // there is no "ideal" rotation axis
      // So guess one; any will do as long as it's perpendicular to start
      rotationAxis = Vector3.cross(Vector3.back(), start);
      if (rotationAxis.lengthSquared() < 0.01f) { // bad luck, they were parallel, try again!
        rotationAxis = Vector3.cross(Vector3.right(), start);
      }

      rotationAxis = rotationAxis.normalized();
      return axisAngle(rotationAxis, 180.0f);
    }

    rotationAxis = Vector3.cross(start, end);

    float squareLength = (float) Math.sqrt((1.0 + cosTheta) * 2.0);
    float inverseSquareLength = 1.0f / squareLength;

    return new Quaternion(
        rotationAxis.x * inverseSquareLength,
        rotationAxis.y * inverseSquareLength,
        rotationAxis.z * inverseSquareLength,
        squareLength * 0.5f);
  }

  /**
   * 获得一个新的四元数，表示向指定的向前方向(目标方向)旋转。
   * 如果upInWorld与forwardInWorld正交，然后Y轴与desiredUpInWorld对齐。
   */
  public static Quaternion lookRotation(Vector3 forwardInWorld, Vector3 desiredUpInWorld) {
    Preconditions.checkNotNull(forwardInWorld, "Parameter \"forwardInWorld\" was null.");
    Preconditions.checkNotNull(desiredUpInWorld, "Parameter \"desiredUpInWorld\" was null.");

    // Find the rotation between the world forward and the forward to look at.
    Quaternion rotateForwardToDesiredForward =
        rotationBetweenVectors(Vector3.forward(), forwardInWorld);

    // Recompute upwards so that it's perpendicular to the direction
    Vector3 rightInWorld = Vector3.cross(forwardInWorld, desiredUpInWorld);
    desiredUpInWorld = Vector3.cross(rightInWorld, forwardInWorld);

    // Find the rotation between the "up" of the rotated object, and the desired up
    Vector3 newUp = Quaternion.rotateVector(rotateForwardToDesiredForward, Vector3.up());
    Quaternion rotateNewUpToUpwards = rotationBetweenVectors(newUp, desiredUpInWorld);

    return Quaternion.multiply(rotateNewUpToUpwards, rotateForwardToDesiredForward);
  }

  /**
   * 比较两个四元数
   * <p>通过计算lhs和rhs的点积来检验是否相等</p>
   */
  public static boolean equals(Quaternion lhs, Quaternion rhs) {
    Preconditions.checkNotNull(lhs, "Parameter \"lhs\" was null.");
    Preconditions.checkNotNull(rhs, "Parameter \"rhs\" was null.");
    float dot = Quaternion.dot(lhs, rhs);
    return MathHelper.almostEqualRelativeAndAbs(dot, 1.0f);
  }

  /**
   * 如果另一个对象A是四元数并且点积是1.0 +/- A的公差，则返回true。
   */
  @Override
  @SuppressWarnings("override.param.invalid")
  public boolean equals(Object other) {
    if (!(other instanceof Quaternion)) {
      return false;
    }
    if (this == other) {
      return true;
    }
    return Quaternion.equals(this, (Quaternion) other);
  }

  /** @hide */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(w);
    result = prime * result + Float.floatToIntBits(x);
    result = prime * result + Float.floatToIntBits(y);
    result = prime * result + Float.floatToIntBits(z);
    return result;
  }

  /** 获取一个单位四元数*/
  public static Quaternion identity() {
    return new Quaternion();
  }
}
