package com.eqgis.sceneform.rendering;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.filament.LightManager;
import com.eqgis.sceneform.common.TransformProvider;
import com.eqgis.sceneform.math.Vector3;
import com.eqgis.sceneform.utilities.AndroidPreconditions;
import com.google.android.filament.Colors;

import java.util.ArrayList;

/**
 * 光源
 * */
@RequiresApi(api = Build.VERSION_CODES.N)
public class Light {
  /** 光源类型 */
  public enum Type {
    /**
     * 点光源
     * <pre>
     *     从空间中一个点向各个方向辐射的光的近似值，其强度随距离的平方成反比而下降。
     *     光点有位置，但没有方向。使用{@link # setfallloffradius}来控制衰减。
     * </pre>
     */
    POINT,
    /**
     * 平行光
     * */
    DIRECTIONAL,
    /**
     * 射灯
     * 类似于点光，但在一个锥形而不是所有方向上辐射光。请注意，当你使圆锥体更宽时，能量被扩散，导致照明看起来更暗。
     * 聚光灯有位置和方向。使用{@link #setInnerConeAngle}和{@link #setOuterConeAngle}来控制锥的大小。
     */
    SPOTLIGHT,
    /**
     * 聚光灯
     * 与射灯相同，除了视光随着锥角的变化而保持不变。聚光灯有位置和方向。
     * 使用{@link #setInnerConeAngle}和{@link #setOuterConeAngle}来控制锥的大小。
     */
    FOCUSED_SPOTLIGHT
  };

  interface LightChangedListener {
    void onChange();
  };

  /** 可接收的最小光强 */
  private static final float MIN_LIGHT_INTENSITY = 0.0001f;

  private final Type type;
  private final boolean enableShadows;

  private Vector3 position;
  private Vector3 direction;
  private final Color color;
  private float intensity;
  private float falloffRadius;
  private float spotlightConeInner;
  private float spotlightConeOuter;

  private final ArrayList<LightChangedListener> changedListeners = new ArrayList<>();

  /** 光源构造器 */
  public static Builder builder(Type type) {
    AndroidPreconditions.checkMinAndroidApiLevel();
    return new Builder(type);
  }

  /**
   * 设置发光颜色
   *
   * @param color 颜色, 默认值: 0xffffffff
   */
  public void setColor(Color color) {
    this.color.set(color);
    fireChangedListeners();
  }

  /**
   * 设置色温值
   * <p>内部会将色温值转换为颜色值</p>
   * @param temperature 色温 单位：开尔文，范围从1000到10000 k。典型的商业和住宅照明在2000K到6500K的范围内。
   */
  public void setColorTemperature(float temperature) {
    final float[] rgbColor = Colors.cct(temperature);
    setColor(new Color(rgbColor[0], rgbColor[1], rgbColor[2]));
  }

  /**
   * 设置光强
   * <pre>
   *     以勒克斯(lx)或流明(lm)为单位确定光的亮度(取决于光的类型)。
   *     较大的值产生更亮的光，接近零的值产生很少的光。
   *     家用灯泡的亮度一般在800 - 2500流明之间，而太阳光的亮度大约在12万流明左右。
   *     没有绝对上限，但通常不需要大于太阳光(120,000 lx)的值。
   * </pre>
   *
   * @param intensity 光照强度
   */
  public void setIntensity(float intensity) {
    this.intensity = Math.max(intensity, MIN_LIGHT_INTENSITY);
    fireChangedListeners();
  }

  /**
   * 设置光强度下降到零的范围。这对{@link Type#DIRECTIONAL}类型没有影响。
   *
   * @param falloffRadius 以世界单位表示的光半径，默认为10.0
   */
  public void setFalloffRadius(float falloffRadius) {
    this.falloffRadius = falloffRadius;
    fireChangedListeners();
  }

  /**
   * 设置内锥角度
   * <pre>
   *     射灯在一个圆锥体中发光，这个值决定了圆锥体内部部分的大小。
   *     强度在内外锥角之间进行插值-这意味着如果它们是相同的，
   *     那么锥体是完全锋利的。一般来说，你会希望内锥比外锥小，以避免混叠。
   * </pre>
   *
   * @param coneInner 内锥角度，默认 0.5
   */
  public void setInnerConeAngle(float coneInner) {
    this.spotlightConeInner = coneInner;
    fireChangedListeners();
  }

  /**
   * 设置外锥角度
   * <pre>
   *     射灯在一个圆锥体中发光，这个值决定了圆锥体内部部分的大小。
   *     强度在内外锥角之间进行插值-这意味着如果它们是相同的，
   *     那么锥体是完全锋利的。一般来说，你会希望内锥比外锥小，以避免混叠。
   * </pre>
   *
   * @param coneOuter 设置外锥角度 默认 0.6
   */
  public void setOuterConeAngle(float coneOuter) {
    this.spotlightConeOuter = coneOuter;
    fireChangedListeners();
  }

  /** 获取光源类型 */
  public Type getType() {
    return this.type;
  }

  /** 如果灯光启用了阴影投射，则返回true。 */
  public boolean isShadowCastingEnabled() {
    return this.enableShadows;
  }

  /** @hide 内部方法 */
  public Vector3 getLocalPosition() {
    return new Vector3(this.position);
  }

  /** @hide 内部方法 */
  public Vector3 getLocalDirection() {
    return new Vector3(this.direction);
  }

  /** 获取光源颜色 */
  public Color getColor() {
    return new Color(this.color);
  }

  /** 获取光照强度 */
  public float getIntensity() {
    return this.intensity;
  }

  /** 获取衰减半径*/
  public float getFalloffRadius() {
    return this.falloffRadius;
  }

  /** 获取内锥角 */
  public float getInnerConeAngle() {
    return this.spotlightConeInner;
  }

  /** 获取外锥角 */
  public float getOuterConeAngle() {
    return this.spotlightConeOuter;
  }

  /** @hide 内部使用 */
  public LightInstance createInstance(TransformProvider transformProvider) {
    LightInstance instance = new LightInstance(this, transformProvider);
    if (instance == null) {
      throw new AssertionError("Failed to create light instance, result is null.");
    }
    return instance;
  }

  /** Builder */
  public static final class Builder {
    // LINT.IfChange
    public static float DEFAULT_DIRECTIONAL_INTENSITY = 420.0f;
    // LINT.ThenChange(//depot/google3/third_party/arcore/ar/sceneform/viewer/viewer.cc)

    private final Type type;

    private boolean enableShadows = false;
    private Vector3 position = new Vector3(0.0f, 0.0f, 0.0f);
    private Vector3 direction = new Vector3(0.0f, 0.0f, -1.0f);
    private Color color = new Color(1.0f, 1.0f, 1.0f);
    private float intensity = 2500.0f;
    private float falloffRadius = 10.0f;
    private float spotlightConeInner = 0.5f;
    private float spotlightConeOuter = 0.6f;

    /** Constructor for building. */
    private Builder(Type type) {
      this.type = type;
      // Directional lights should have a different default intensity
      if (type == Type.DIRECTIONAL) {
        intensity = DEFAULT_DIRECTIONAL_INTENSITY;
      }
    }

    /**
     * 确定光线是否投射阴影，或者合成物体是否会遮挡光线。
     * @param enableShadows  默认false.
     */
    public Builder setShadowCastingEnabled(boolean enableShadows) {
      this.enableShadows = enableShadows;
      return this;
    }

    /**
     * 设置颜色值
     * @param color 颜色
     */
    public Builder setColor(Color color) {
      this.color = color;
      return this;
    }

    /**
     * 设置色温
     * <p>内部自动根据色温转换成颜色值，再设置颜色</p>
     * @param temperature 色温
     */
    public Builder setColorTemperature(float temperature) {
      final float[] rgbColor = Colors.cct(temperature);
      setColor(new Color(rgbColor[0], rgbColor[1], rgbColor[2]));
      return this;
    }

    /**
     * 设置光强
     * @param intensity 光强
     */
    public Builder setIntensity(float intensity) {
      this.intensity = intensity;
      return this;
    }

    /**
     * 设置衰减半径
     * <p>注意：平行光不会生效</p>
     * @param falloffRadius 设置衰减半径，默认1米
     */
    public Builder setFalloffRadius(float falloffRadius) {
      this.falloffRadius = falloffRadius;
      return this;
    }

    /**
     * 设置内锥角
     * @param coneInner 内锥角度
     */
    public Builder setInnerConeAngle(float coneInner) {
      this.spotlightConeInner = coneInner;
      return this;
    }

    /**
     * 设置外锥角
     * @param coneOuter 外锥角度
     */
    public Builder setOuterConeAngle(float coneOuter) {
      this.spotlightConeOuter = coneOuter;
      return this;
    }

    /** 构建光源 */
    public Light build() {
      Light light = new Light(this);
      if (light == null) {
        throw new AssertionError("Allocating a new light failed.");
      }
      return light;
    }
  }

  /**
   * 添加监听事件，以便在光照参数改变时更新光照实例。
   */
  void addChangedListener(LightChangedListener listener) {
    changedListeners.add(listener);
  }

  /** 移除监听事件 */
  void removeChangedListener(LightChangedListener listener) {
    changedListeners.remove(listener);
  }

  private Light(Builder builder) {
    this.type = builder.type;
    this.enableShadows = builder.enableShadows;
    this.position = builder.position;
    this.direction = builder.direction;
    this.color = builder.color;
    this.intensity = builder.intensity;
    this.falloffRadius = builder.falloffRadius;
    this.spotlightConeInner = builder.spotlightConeInner;
    this.spotlightConeOuter = builder.spotlightConeOuter;
  }

  private void fireChangedListeners() {
    for (LightChangedListener listener : changedListeners) {
      listener.onChange();
    }
  }
}
