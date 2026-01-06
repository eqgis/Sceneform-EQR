package com.google.sceneform.rendering;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.concurrent.CompletableFuture;

/**
 * 材质工厂
 * 用于构造默认{@link Material}的实用程序类
 * */
@RequiresApi(api = Build.VERSION_CODES.N)

public final class MaterialFactory {
  /**
   * 材质文件中的颜色参数名称
   *
   * @see Material#setFloat3(String, Color)
   * @see Material#setFloat4(String, Color)
   */
  public static final String MATERIAL_COLOR = "color";

  /**
   * 材质文件中的纹理参数名称
   *
   * @see Material#setTexture(String, Texture)
   */
  public static final String MATERIAL_TEXTURE = "texture";

  /**
   * 材质文件中的金属度参数名称
   *
   * @see Material#setFloat(String, float)
   */
  public static final String MATERIAL_METALLIC = "metallic";

  /**
   * 材质文件中的粗糙度参数名称
   *
   * @see Material#setFloat(String, float)
   */
  public static final String MATERIAL_ROUGHNESS = "roughness";

  /**
   * 材质文件中的反射率参数名称
   *
   * @see Material#setFloat(String, float)
   */
  public static final String MATERIAL_REFLECTANCE = "reflectance";

  private static final float DEFAULT_METALLIC_PROPERTY = 0.0f;
  private static final float DEFAULT_ROUGHNESS_PROPERTY = 0.4f;
  private static final float DEFAULT_REFLECTANCE_PROPERTY = 0.5f;

  /**
   * 根据颜色值使用不透明材质文件创建材质对象
   *
   * @see #MATERIAL_METALLIC
   * @see #MATERIAL_ROUGHNESS
   * @see #MATERIAL_REFLECTANCE
   * @param context 上下文
   * @param color 颜色值
   * @return 材质对象
   */
  @SuppressWarnings("AndroidApiChecker")
  // CompletableFuture requires api level 24
  public static CompletableFuture<Material> makeOpaqueWithColor(Context context, Color color) {
    CompletableFuture<Material> materialFuture =
        Material.builder()
            .setSource(
                context,
                RenderingResources.GetSceneformResource(
                    context, RenderingResources.Resource.OPAQUE_COLORED_MATERIAL))
            .build();

    return materialFuture.thenApply(
        material -> {
          material.setFloat3(MATERIAL_COLOR, color);
          applyDefaultPbrParams(material);
          return material;
        });
  }

  /**
   * 根据颜色值使用透明材质文件创建材质对象
   *
   * @see #MATERIAL_METALLIC
   * @see #MATERIAL_ROUGHNESS
   * @see #MATERIAL_REFLECTANCE
   * @param context 上下文
   * @param color 颜色值
   * @return 材质对象
   */
  @SuppressWarnings("AndroidApiChecker")
  // CompletableFuture requires api level 24
  public static CompletableFuture<Material> makeTransparentWithColor(Context context, Color color) {
    CompletableFuture<Material> materialFuture =
        Material.builder()
            .setSource(
                context,
                RenderingResources.GetSceneformResource(
                    context, RenderingResources.Resource.TRANSPARENT_COLORED_MATERIAL))
            .build();

    return materialFuture.thenApply(
        material -> {
          material.setFloat4(MATERIAL_COLOR, color);
          applyDefaultPbrParams(material);
          return material;
        });
  }

  /**
   * 根据纹理使用不透明材质文件创建材质对象
   *
   * @see #MATERIAL_METALLIC
   * @see #MATERIAL_ROUGHNESS
   * @see #MATERIAL_REFLECTANCE
   * @param context 上下文
   * @param texture 纹理对象
   * @return 材质对象
   */
  @SuppressWarnings("AndroidApiChecker")
  // CompletableFuture requires api level 24
  public static CompletableFuture<Material> makeOpaqueWithTexture(
      Context context, Texture texture) {
    CompletableFuture<Material> materialFuture =
        Material.builder()
            .setSource(
                context,
                RenderingResources.GetSceneformResource(
                    context, RenderingResources.Resource.OPAQUE_TEXTURED_MATERIAL))
            .build();

    return materialFuture.thenApply(
        material -> {
          material.setTexture(MATERIAL_TEXTURE, texture);
          applyDefaultPbrParams(material);
          return material;
        });
  }

  /**
   * 根据纹理使用透明材质文件创建材质对象
   *
   * @see #MATERIAL_METALLIC
   * @see #MATERIAL_ROUGHNESS
   * @see #MATERIAL_REFLECTANCE
   * @param context 上下文
   * @param texture 纹理对象
   * @return 材质对象
   */
  @SuppressWarnings("AndroidApiChecker")
  // CompletableFuture requires api level 24
  public static CompletableFuture<Material> makeTransparentWithTexture(
      Context context, Texture texture) {
    CompletableFuture<Material> materialFuture =
        Material.builder()
            .setSource(
                context,
                RenderingResources.GetSceneformResource(
                    context, RenderingResources.Resource.TRANSPARENT_TEXTURED_MATERIAL))
            .build();

    return materialFuture.thenApply(
        material -> {
          material.setTexture(MATERIAL_TEXTURE, texture);
          applyDefaultPbrParams(material);
          return material;
        });
  }

  public static void applyDefaultPbrParams(Material material) {
    material.setFloat(MATERIAL_METALLIC, DEFAULT_METALLIC_PROPERTY);
    material.setFloat(MATERIAL_ROUGHNESS, DEFAULT_ROUGHNESS_PROPERTY);
    material.setFloat(MATERIAL_REFLECTANCE, DEFAULT_REFLECTANCE_PROPERTY);
  }
}
