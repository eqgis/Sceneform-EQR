package com.google.sceneform.rendering;

import androidx.annotation.Nullable;

import com.google.sceneform.math.Vector3;

/**
 * 表示{@link RenderableDefinition}的顶点。用于动态构造可渲染对象。
 *
 * @see ModelRenderable.Builder
 * @see ViewRenderable.Builder
 */
public class Vertex {
  /** 表示顶点的纹理坐标。值应该在0到1之间。 */
  public static final class UvCoordinate {
    public float x;
    public float y;

    public UvCoordinate(float x, float y) {
      this.x = x;
      this.y = y;
    }
  }

  /** 表示一个可上传到 Filament CUSTOM 顶点属性的四维浮点值。 */
  public static final class Float4 {
    public float x;
    public float y;
    public float z;
    public float w;

    public Float4(float x, float y, float z, float w) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;
    }

    private Float4(Float4 value) {
      this(value.x, value.y, value.z, value.w);
    }
  }

  // Required.
  private final Vector3 position = Vector3.zero();

  // Optional.
  @Nullable private Vector3 normal;
  @Nullable private UvCoordinate uvCoordinate;
  @Nullable private Color color;
  @Nullable private Float4 custom0;

  public void setPosition(Vector3 position) {
    this.position.set(position);
  }

  public Vector3 getPosition() {
    return position;
  }

  public void setNormal(@Nullable Vector3 normal) {
    this.normal = normal;
  }

  @Nullable
  public Vector3 getNormal() {
    return normal;
  }

  public void setUvCoordinate(@Nullable UvCoordinate uvCoordinate) {
    this.uvCoordinate = uvCoordinate;
  }

  @Nullable
  public UvCoordinate getUvCoordinate() {
    return uvCoordinate;
  }

  public void setColor(@Nullable Color color) {
    this.color = color;
  }

  @Nullable
  public Color getColor() {
    return color;
  }

  /**
   * 设置 Filament CUSTOM0 顶点属性。
   *
   * @param custom0 四维自定义顶点数据，传入 {@code null} 表示不提供该属性
   */
  public void setCustom0(@Nullable Float4 custom0) {
    this.custom0 = custom0 != null ? new Float4(custom0) : null;
  }

  /** @return Filament CUSTOM0 顶点属性，未设置时返回 {@code null} */
  @Nullable
  public Float4 getCustom0() {
    return custom0;
  }

  private Vertex(Builder builder) {
    position.set(builder.position);
    normal = builder.normal;
    uvCoordinate = builder.uvCoordinate;
    color = builder.color;
    custom0 = builder.custom0 != null ? new Float4(builder.custom0) : null;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** 建造者模式 */
  public static final class Builder {
    // Required.
    private final Vector3 position = Vector3.zero();

    // Optional.
    @Nullable private Vector3 normal;
    @Nullable private UvCoordinate uvCoordinate;
    @Nullable private Color color;
    @Nullable private Float4 custom0;

    public Builder setPosition(Vector3 position) {
      this.position.set(position);
      return this;
    }

    public Builder setNormal(@Nullable Vector3 normal) {
      this.normal = normal;
      return this;
    }

    public Builder setUvCoordinate(@Nullable UvCoordinate uvCoordinate) {
      this.uvCoordinate = uvCoordinate;
      return this;
    }

    public Builder setColor(@Nullable Color color) {
      this.color = color;
      return this;
    }

    /**
     * 设置 Filament CUSTOM0 顶点属性。
     *
     * @param custom0 四维自定义顶点数据
     * @return 当前 Builder
     */
    public Builder setCustom0(@Nullable Float4 custom0) {
      this.custom0 = custom0;
      return this;
    }

    public Vertex build() {
      return new Vertex(this);
    }
  }
}
