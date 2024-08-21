package com.google.sceneform.math;

/**
 * 数学计算助手
 * <p>
 *     包含常见数学运算的静态函数。
 * </p>
 * */
public class MathHelper {

  static final float FLT_EPSILON = 1.19209290E-07f;
  static final float MAX_DELTA = 1.0E-10f;

  /**
   * 如果两个浮点数在一定范围内相等，则返回true。用于比较浮点数数字，同时考虑到浮点精度的限制。
   */
  // https://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/
  public static boolean almostEqualRelativeAndAbs(float a, float b) {
    // Check if the numbers are really close -- needed
    // when comparing numbers near zero.
    float diff = Math.abs(a - b);
    if (diff <= MAX_DELTA) {
      return true;
    }

    a = Math.abs(a);
    b = Math.abs(b);
    float largest = Math.max(a, b);

    if (diff <= largest * FLT_EPSILON) {
      return true;
    }
    return false;
  }

  /** 将值夹在最小和最大范围之间。 */
  public static float clamp(float value, float min, float max) {
    return Math.min(max, Math.max(min, value));
  }

  /** 将值夹在[0,1]之间。 */
  static float clamp01(float value) {
    return clamp(value, 0.0f, 1.0f);
  }

  /**
   * 在a和b之间用一个比例进行线性插值。
   * @param a 起始值
   * @param b 结束值
   * @param t 比例
   * @return 线性插值结果
   */
  public static float lerp(float a, float b, float t) {
    return a + t * (b - a);
  }
}
