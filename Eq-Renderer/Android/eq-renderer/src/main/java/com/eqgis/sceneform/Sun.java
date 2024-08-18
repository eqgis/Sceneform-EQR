package com.eqgis.sceneform;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.eqgis.sceneform.math.Quaternion;
import com.eqgis.sceneform.math.Vector3;
import com.eqgis.sceneform.rendering.Color;
import com.eqgis.sceneform.rendering.Light;
import com.eqgis.sceneform.utilities.EnvironmentalHdrParameters;
import com.eqgis.sceneform.utilities.Preconditions;

/**
 * 太阳光对象
 * <p>场景中默认的Node，并已设置平行光{@link Node#setLight(Light)}</p>
 * <pre>
 *     场景中默认的平行光。
 *     父节点是场景，不可修改
 *     支持Node中的所有其他功能。您可以访问太阳的位置和旋转，为太阳分配一个碰撞形状，
 *     或者为太阳添加子节点。禁用太阳会关闭默认的定向光。
 * </pre>
 */
public class Sun extends Node {
  //默认的光照颜色
  public static int DEFAULT_SUNLIGHT_COLOR = 0xffffffff;
//  @ColorInt static final int DEFAULT_SUNLIGHT_COLOR = 0xfff2d3c4;
  //默认的平行光方向
  public static Vector3 DEFAULT_SUNLIGHT_DIRECTION = new Vector3(0.0f,0.0f,-1f);
//  static final Vector3 DEFAULT_SUNLIGHT_DIRECTION = new Vector3(0.7f, -1.0f, -0.8f);

  // 光估计比例和偏移量允许控制强度的最终变化，以避免过度变暗或过于剧烈的变化:appliieestimate =估计*比例+偏移量
  private static final float LIGHT_ESTIMATE_SCALE = 1.8f;
  private static final float LIGHT_ESTIMATE_OFFSET = 0.0f;
  private float baseIntensity = 0.0f;

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  Sun() {}

  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  Sun(Scene scene) {
    super();
    Preconditions.checkNotNull(scene, "Parameter \"scene\" was null.");
    super.setParent(scene);

    setupDefaultLighting(scene.getView());
  }

  @Override
  public void setParent(@Nullable NodeParent parent) {
    throw new UnsupportedOperationException(
        "Sun's parent cannot be changed, it is always the scene.");
  }

  /**
   * 将环境HDR光估计应用于平行光
   *
   * <p>曝光计算： 1.0f / (1.2f * aperture^2 / shutter speed * 100.0f / iso);</p>
   *
   * @param direction 从光估计返回的定向光方向
   * @param color 从光估计返回的相对颜色
   * @param environmentalHdrIntensity 光估计的最大强度
   * @param exposure Filament的曝光值
   * @hide 用于每帧更新Hdr照明
   */

  void setEnvironmentalHdrLightEstimate(
      float[] direction,
      Color color,
      float environmentalHdrIntensity,
      float exposure,
      EnvironmentalHdrParameters environmentalHdrParameters) {
    Light light = getLight();
    if (light == null) {
      return;
    }

    // 使用硬编码值将环境hdr的相对值转换为filament的lux值。
    float filamentIntensity =
        environmentalHdrIntensity
            * environmentalHdrParameters.getDirectIntensityForFilament()
            / exposure;

    light.setColor(color);
    light.setIntensity(filamentIntensity);

    //如果检测到光线从下面照射过来，我们翻转Y组件，这样我们总是在地面上留下阴影，以满足用户体验要求。
    Vector3 lookDirection =
        new Vector3(-direction[0], -Math.abs(direction[1]), -direction[2]).normalized();
    Quaternion lookRotation = Quaternion.rotationBetweenVectors(Vector3.forward(), lookDirection);
    setWorldRotation(lookRotation);
  }

  void setLightEstimate(Color colorCorrection, float pixelIntensity) {
    Light light = getLight();
    if (light == null) {
      return;
    }

    //若不知光强，这里获取默认值
    if (baseIntensity == 0.0f) {
      baseIntensity = light.getIntensity();
    }

    //为了避免比过暗，这里有个scale和偏移设置
    float lightIntensity =
        baseIntensity
            * Math.min(pixelIntensity * LIGHT_ESTIMATE_SCALE + LIGHT_ESTIMATE_OFFSET, 1.0f);

    //通过色彩校正调节太阳的颜色。
    Color lightColor = new Color(DEFAULT_SUNLIGHT_COLOR);
    lightColor.r *= colorCorrection.r;
    lightColor.g *= colorCorrection.g;
    lightColor.b *= colorCorrection.b;

    //通过光估计修改光的颜色和强度。
    light.setColor(lightColor);
    light.setIntensity(lightIntensity);
  }

  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  private void setupDefaultLighting(SceneView view) {
    Preconditions.checkNotNull(view, "Parameter \"view\" was null.");

    final Color sunlightColor = new Color(DEFAULT_SUNLIGHT_COLOR);
    if (sunlightColor == null) {
      throw new AssertionError("Sunlight color is null.");
    }

    // 设置节点方向，使阳光指向所需的方向。
    setLookDirection(DEFAULT_SUNLIGHT_DIRECTION.normalized());

    // 创建平行光
    Light sunlight =
        Light.builder(Light.Type.DIRECTIONAL)
            .setColor(sunlightColor)
//            .setShadowCastingEnabled(true)
            .build();

    if (sunlight == null) {
      throw new AssertionError("Failed to create the default sunlight.");
    }
    this.setLight(sunlight);
  }
}
