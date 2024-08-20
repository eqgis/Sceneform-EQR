package com.eqgis.sceneform.rendering;

import androidx.annotation.ColorInt;

import com.google.android.filament.Colors;

/**
 * 颜色值对象
 * <pre>
 *     RGBA颜色值
 *     每一个分量值都位于[0,1]之间
 * </pre>
 */
public class Color {
  private static final float INT_COLOR_SCALE = 1.0f / 255.0f;

  public float r;
  public float g;
  public float b;
  public float a;

  /**
   * 构造函数
   * <p>默认白色</p>
   * */
  @SuppressWarnings("initialization")
  public Color() {
    setWhite();
  }

  /** 构造函数 */
  @SuppressWarnings("initialization")
  public Color(Color color) {
    set(color);
  }

  /** 构造函数 */
  @SuppressWarnings("initialization")
  public Color(float r, float g, float b) {
    set(r, g, b);
  }

  /** 构造函数 */
  @SuppressWarnings("initialization")
  public Color(float r, float g, float b, float a) {
    set(r, g, b, a);
  }

  /**
   * 构造函数
   * <p>
   *     在sRGB色彩空间中用一个整数构造一个颜色，作为ARGB值。用于从Android ColorInt进行构造。
   * </p>
   */
  @SuppressWarnings("initialization")
  public Color(@ColorInt int argb) {
    set(argb);
  }

  /** 更新颜色值 */
  public void set(Color color) {
    set(color.r, color.g, color.b, color.a);
  }

  /** 更新颜色值 */
  public void set(float r, float g, float b) {
    set(r, g, b, 1.0f);
  }

  /** 更新颜色值 */
  public void set(float r, float g, float b, float a) {
    this.r = Math.max(0.0f, Math.min(1.0f, r));
    this.g = Math.max(0.0f, Math.min(1.0f, g));
    this.b = Math.max(0.0f, Math.min(1.0f, b));
    this.a = Math.max(0.0f, Math.min(1.0f, a));
  }

  /**
   * 更新颜色值
   * @param argb Android ColorInt
   */
  public void set(@ColorInt int argb) {
    // sRGB color
    final int red = android.graphics.Color.red(argb);
    final int green = android.graphics.Color.green(argb);
    final int blue = android.graphics.Color.blue(argb);
    final int alpha = android.graphics.Color.alpha(argb);

    // Convert from sRGB to linear and from int to float.
    float[] linearColor =
        Colors.toLinear(
            Colors.RgbType.SRGB,
            (float) red * INT_COLOR_SCALE,
            (float) green * INT_COLOR_SCALE,
            (float) blue * INT_COLOR_SCALE);

    r = linearColor[0];
    g = linearColor[1];
    b = linearColor[2];
    a = (float) alpha * INT_COLOR_SCALE;
  }

  /**
   * 设置颜色值为白色
   * RGBA is (1, 1, 1, 1). */
  private void setWhite() {
    set(1.0f, 1.0f, 1.0f);
  }

  /** 计算一个通过Tonemap映射方式相反的颜色值 */
  public Color inverseTonemap() {
    Color color = new Color(r, g, b, a);
    color.r = inverseTonemap(r);
    color.g = inverseTonemap(g);
    color.b = inverseTonemap(b);
    return color;
  }

  private static float inverseTonemap(float val) {
    return (val * -0.155f) / (val - 1.019f);
  }
}
