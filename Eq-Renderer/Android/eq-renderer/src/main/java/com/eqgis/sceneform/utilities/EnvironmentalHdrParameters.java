package com.eqgis.sceneform.utilities;

/**
 * 环境HDR参数类
 * <p>用于filament中设置环境HDR</p>
 *
 * 这是早期scenefrom中用于转换HDR参数用于filament的类。后续可能移除
 * @hide
 */
// TODO: Replace each of these values with principled numbers.
public class EnvironmentalHdrParameters {
  public static final float DEFAULT_AMBIENT_SH_SCALE_FOR_FILAMENT = 1.0f;
  public static final float DEFAULT_DIRECT_INTENSITY_FOR_FILAMENT = 1.0f;
  public static final float DEFAULT_REFLECTION_SCALE_FOR_FILAMENT = 1.0f;

  /** 构建ViewerConfig，这是查看器的运行时配置选项集合。 */
  public static class Builder {
    public Builder() {}

    public EnvironmentalHdrParameters build() {
      return new EnvironmentalHdrParameters(this);
    }

    /** 设置定向照明的转换系数 */
    public Builder setDirectIntensityForFilament(float directIntensityForFilament) {
      this.directIntensityForFilament = directIntensityForFilament;
      return this;
    }

    /** 设置环境球谐波的转换系数 */
    public Builder setAmbientShScaleForFilament(float ambientShScaleForFilament) {
      this.ambientShScaleForFilament = ambientShScaleForFilament;
      return this;
    }

    /** 设置反射的转换系数*/
    public Builder setReflectionScaleForFilament(float reflectionScaleForFilament) {
      this.reflectionScaleForFilament = reflectionScaleForFilament;
      return this;
    }

    private float ambientShScaleForFilament;
    private float directIntensityForFilament;
    private float reflectionScaleForFilament;
  }

  /** 构造构造器时，必须指定所有必需的字段。 */
  public static Builder builder() {
    return new Builder();
  }

  public static EnvironmentalHdrParameters makeDefault() {
    return builder()
        .setAmbientShScaleForFilament(DEFAULT_AMBIENT_SH_SCALE_FOR_FILAMENT)
        .setDirectIntensityForFilament(DEFAULT_DIRECT_INTENSITY_FOR_FILAMENT)
        .setReflectionScaleForFilament(DEFAULT_REFLECTION_SCALE_FOR_FILAMENT)
        .build();
  }

  private EnvironmentalHdrParameters(Builder builder) {
    ambientShScaleForFilament = builder.ambientShScaleForFilament;
    directIntensityForFilament = builder.directIntensityForFilament;
    reflectionScaleForFilament = builder.reflectionScaleForFilament;
  }

  /**
   * 获取Filament的 ambient系数
   */
  public float getAmbientShScaleForFilament() {
    return ambientShScaleForFilament;
  }

  /**
   * 获取强度值
   * 环境Hdr提供了一个相对强度，高于零，通常低于8。
   * */
  public float getDirectIntensityForFilament() {
    return directIntensityForFilament;
  }

  /**
   * 获取反射系数
   */
  public float getReflectionScaleForFilament() {
    return reflectionScaleForFilament;
  }

  private final float ambientShScaleForFilament;
  private final float directIntensityForFilament;
  private final float reflectionScaleForFilament;
}
