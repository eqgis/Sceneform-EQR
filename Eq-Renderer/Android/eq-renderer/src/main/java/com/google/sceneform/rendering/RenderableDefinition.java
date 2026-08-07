package com.google.sceneform.rendering;

import androidx.annotation.Nullable;

import com.google.sceneform.math.MathHelper;
import com.google.sceneform.math.Matrix;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.utilities.AndroidPreconditions;
import com.google.sceneform.utilities.Preconditions;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.IndexBuffer.Builder.IndexType;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.VertexBuffer;
import com.google.android.filament.VertexBuffer.VertexAttribute;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * 自定义渲染对象
 * <p>
 *     可以用来动态地构造和修改可渲染对象。
 * </p>
 *
 * @see ModelRenderable.Builder
 * @see ViewRenderable.Builder
 */
public class RenderableDefinition implements IRenderableDefinition{
  private static final Matrix scratchMatrix = new Matrix();

  /**
   * 旧版子网格数据类型。
   *
   * @deprecated 请使用 {@link SubGeometry}。旧 Builder 会实际创建 {@link SubGeometry}，
   *     仅用于兼容既有源码和二进制调用。
   */
  @Deprecated
  public static class Submesh {
    private List<Integer> triangleIndices;
    private Material material;
    @Nullable private String name;

    public void setTriangleIndices(List<Integer> triangleIndices) {
      this.triangleIndices = triangleIndices;
    }

    public List<Integer> getTriangleIndices() {
      return triangleIndices;
    }

    public void setMaterial(Material material) {
      this.material = material;
    }

    public Material getMaterial() {
      return material;
    }

    public void setName(String name) {
      this.name = name;
    }

    @Nullable
    public String getName() {
      return name;
    }

    private Submesh(Builder builder) {
      triangleIndices = Preconditions.checkNotNull(builder.triangleIndices);
      material = Preconditions.checkNotNull(builder.material);
      name = builder.name;
    }

    public static Builder builder() {
      return new Builder();
    }

    /**
     * 旧版子网格 Builder。
     *
     * @deprecated 请使用 {@link SubGeometry.Builder}。
     */
    @Deprecated
    public static class Builder {
      @Nullable private List<Integer> triangleIndices;
      @Nullable private Material material;
      @Nullable private String name;

      public Builder setTriangleIndices(List<Integer> triangleIndices) {
        this.triangleIndices = triangleIndices;
        return this;
      }

      public Builder setName(String name) {
        this.name = name;
        return this;
      }

      public Builder setMaterial(Material material) {
        this.material = material;
        return this;
      }

      public Submesh build() {
        //desc- 旧 Builder 返回类型保持 Submesh，但实例统一使用 SubGeometry，保证新旧列表转换安全。
        return new SubGeometry(this);
      }
    }
  }

  /**
   * 表示 {@link RenderableDefinition} 中一组独立的子几何数据。
   * <p>每个子几何通过索引范围绑定一个材质，一个定义可以包含多个子几何。</p>
   */
  public static class SubGeometry extends Submesh {
    private SubGeometry(Submesh.Builder builder) {
      super(builder);
    }

    public static Builder builder() {
      return new Builder();
    }

    /** Factory class for {@link SubGeometry}. */
    public static final class Builder extends Submesh.Builder {
      @Override
      public Builder setTriangleIndices(List<Integer> triangleIndices) {
        super.setTriangleIndices(triangleIndices);
        return this;
      }

      @Override
      public Builder setName(String name) {
        super.setName(name);
        return this;
      }

      @Override
      public Builder setMaterial(Material material) {
        super.setMaterial(material);
        return this;
      }

      @Override
      public SubGeometry build() {
        return new SubGeometry(this);
      }
    }
  }

  private List<Vertex> vertices;
  private List<SubGeometry> subGeometries;
  private RenderableManager.PrimitiveType primitiveType;

  private static final int BYTES_PER_FLOAT = Float.SIZE / 8;
  private static final int POSITION_SIZE = 3; // x, y, z
  private static final int UV_SIZE = 2;
  private static final int TANGENTS_SIZE = 4; // quaternion
  private static final int COLOR_SIZE = 4; // RGBA
  private static final int CUSTOM0_SIZE = 4; // Float4

  public void setVertices(List<Vertex> vertices) {
    this.vertices = vertices;
  }

  public List<Vertex> getVertices() {
    return vertices;
  }

  /**
   * 设置当前定义包含的全部子几何。
   *
   * @param subGeometries 子几何列表，不能为空
   */
  public void setSubGeometries(List<SubGeometry> subGeometries) {
    this.subGeometries = subGeometries;
  }

  /**
   * 获取当前定义包含的全部子几何。
   *
   * @return 子几何列表
   */
  public List<SubGeometry> getSubGeometries() {
    return subGeometries;
  }

  /**
   * 设置当前定义包含的旧版子网格列表。
   *
   * @param submeshes 旧版子网格列表
   * @deprecated 请使用 {@link #setSubGeometries(List)}。
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  public void setSubmeshes(List<Submesh> submeshes) {
    //desc- Submesh.Builder 实际构建 SubGeometry，因此此处可保留原列表的可变语义并安全转换。
    subGeometries = (List<SubGeometry>) (List<?>) submeshes;
  }

  /**
   * 获取当前定义包含的旧版子网格列表。
   *
   * @return 旧版子网格列表
   * @deprecated 请使用 {@link #getSubGeometries()}。
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  public List<Submesh> getSubmeshes() {
    return (List<Submesh>) (List<?>) subGeometries;
  }

  /**
   * 设置此定义构建实例时使用的图元类型。
   *
   * @param primitiveType Filament 图元类型
   */
  public void setPrimitiveType(RenderableManager.PrimitiveType primitiveType) {
    this.primitiveType = Preconditions.checkNotNull(primitiveType);
  }

  /** @return 此定义构建实例时使用的图元类型 */
  public RenderableManager.PrimitiveType getPrimitiveType() {
    return primitiveType;
  }

  public void applyDefinitionToData(
      IRenderableInternalData data,
      ArrayList<Material> materialBindings,
      ArrayList<String> materialNames) {
    AndroidPreconditions.checkUiThread();

    if (data instanceof RenderableInternalData) {
      ((RenderableInternalData) data).setPrimitiveType(primitiveType);
    }

    applyDefinitionToDataIndexBuffer(data);
    applyDefinitionToDataVertexBuffer(data);

    // 添加网格数据
    int indexStart = 0;
    materialBindings.clear();
    materialNames.clear();
    for (int i = 0; i < subGeometries.size(); i++) {
      SubGeometry subGeometry = subGeometries.get(i);

      RenderableInternalData.MeshData meshData;
      if (i < data.getMeshes().size()) {
        meshData = data.getMeshes().get(i);
      } else {
        meshData = new RenderableInternalData.MeshData();
        data.getMeshes().add(meshData);
      }

      meshData.indexStart = indexStart;
      meshData.indexEnd = indexStart + subGeometry.getTriangleIndices().size();
      indexStart = meshData.indexEnd;
      materialBindings.add(subGeometry.getMaterial());
      final String name = subGeometry.getName();
      materialNames.add(name != null ? name : "");
    }

    // 移除旧数据
    while (data.getMeshes().size() > subGeometries.size()) {
      data.getMeshes().remove(data.getMeshes().size() - 1);
    }
  }

  private void applyDefinitionToDataIndexBuffer(IRenderableInternalData data) {
    // 计算顶点索引
    int numIndices = 0;
    for (int i = 0; i < subGeometries.size(); i++) {
      SubGeometry subGeometry = subGeometries.get(i);
      numIndices += subGeometry.getTriangleIndices().size();
    }

    // 创建原始IndexBuffer
    IntBuffer rawIndexBuffer = data.getRawIndexBuffer();
    if (rawIndexBuffer == null || rawIndexBuffer.capacity() < numIndices) {
      rawIndexBuffer = IntBuffer.allocate(numIndices);
      data.setRawIndexBuffer(rawIndexBuffer);
    } else {
      rawIndexBuffer.rewind();
    }

    //填充索引数据
    for (int i = 0; i < subGeometries.size(); i++) {
      SubGeometry subGeometry = subGeometries.get(i);
      List<Integer> triangleIndices = subGeometry.getTriangleIndices();
      for (int j = 0; j < triangleIndices.size(); j++) {
        rawIndexBuffer.put(triangleIndices.get(j));
      }
    }
    rawIndexBuffer.rewind();

    //创建filament的索引缓冲区
    IndexBuffer indexBuffer = data.getIndexBuffer();
    IEngine engine = EngineInstance.getEngine();
    if (indexBuffer == null || indexBuffer.getIndexCount() < numIndices) {
      if (indexBuffer != null) {
        engine.destroyIndexBuffer(indexBuffer);
      }

      indexBuffer =
          new IndexBuffer.Builder()
              .indexCount(numIndices)
              .bufferType(IndexType.UINT)
              .build(engine.getFilamentEngine());
      data.setIndexBuffer(indexBuffer);
    }

    indexBuffer.setBuffer(engine.getFilamentEngine(), rawIndexBuffer, 0, numIndices);
  }

  private void applyDefinitionToDataVertexBuffer(IRenderableInternalData data) {
    if (vertices.isEmpty()) {
      throw new IllegalArgumentException("RenderableDescription must have at least one vertex.");
    }

    int numVertices = vertices.size();
    Vertex firstVertex = vertices.get(0);

    //计算顶点数据
    EnumSet<VertexAttribute> descriptionAttributes = EnumSet.of(VertexAttribute.POSITION);
    if (firstVertex.getNormal() != null) {
      descriptionAttributes.add(VertexAttribute.TANGENTS);
    }
    if (firstVertex.getUvCoordinate() != null) {
      descriptionAttributes.add(VertexAttribute.UV0);
    }
    if (firstVertex.getColor() != null) {
      descriptionAttributes.add(VertexAttribute.COLOR);
    }
    if (firstVertex.getCustom0() != null) {
      descriptionAttributes.add(VertexAttribute.CUSTOM0);
    }

    //计算vertexBuffer
    VertexBuffer vertexBuffer = data.getVertexBuffer();
    boolean createVertexBuffer = true;
    if (vertexBuffer != null) {
      EnumSet<VertexAttribute> oldAttributes = EnumSet.of(VertexAttribute.POSITION);
      if (data.getRawTangentsBuffer() != null) {
        oldAttributes.add(VertexAttribute.TANGENTS);
      }
      if (data.getRawUvBuffer() != null) {
        oldAttributes.add(VertexAttribute.UV0);
      }
      if (data.getRawColorBuffer() != null) {
        oldAttributes.add(VertexAttribute.COLOR);
      }
      if (data.getRawCustom0Buffer() != null) {
        oldAttributes.add(VertexAttribute.CUSTOM0);
      }

      createVertexBuffer =
          !oldAttributes.equals(descriptionAttributes)
              || vertexBuffer.getVertexCount() < numVertices;

      if (createVertexBuffer) {
        EngineInstance.getEngine().destroyVertexBuffer(vertexBuffer);
      }
    }

    if (createVertexBuffer) {
      vertexBuffer = createVertexBuffer(numVertices, descriptionAttributes);
      data.setVertexBuffer(vertexBuffer);
    }

    //创建顶点缓冲区
    FloatBuffer positionBuffer = data.getRawPositionBuffer();
    if (positionBuffer == null || positionBuffer.capacity() < numVertices * POSITION_SIZE) {
      positionBuffer = FloatBuffer.allocate(numVertices * POSITION_SIZE);
      data.setRawPositionBuffer(positionBuffer);
    } else {
      positionBuffer.rewind();
    }

    //创建tangentsBuffer
    FloatBuffer tangentsBuffer = data.getRawTangentsBuffer();
    if (descriptionAttributes.contains(VertexAttribute.TANGENTS)
        && (tangentsBuffer == null || tangentsBuffer.capacity() < numVertices * TANGENTS_SIZE)) {
      tangentsBuffer = FloatBuffer.allocate(numVertices * TANGENTS_SIZE);
      data.setRawTangentsBuffer(tangentsBuffer);
    } else if (tangentsBuffer != null) {
      tangentsBuffer.rewind();
    }

    //创建uvBuffer
    FloatBuffer uvBuffer = data.getRawUvBuffer();
    if (descriptionAttributes.contains(VertexAttribute.UV0)
        && (uvBuffer == null || uvBuffer.capacity() < numVertices * UV_SIZE)) {
      uvBuffer = FloatBuffer.allocate(numVertices * UV_SIZE);
      data.setRawUvBuffer(uvBuffer);
    } else if (uvBuffer != null) {
      uvBuffer.rewind();
    }

    //创建colorBuffer
    FloatBuffer colorBuffer = data.getRawColorBuffer();
    if (descriptionAttributes.contains(VertexAttribute.COLOR)
        && (colorBuffer == null || colorBuffer.capacity() < numVertices * COLOR_SIZE)) {
      colorBuffer = FloatBuffer.allocate(numVertices * COLOR_SIZE);
      data.setRawColorBuffer(colorBuffer);
    } else if (colorBuffer != null) {
      colorBuffer.rewind();
    }

    //创建 CUSTOM0 Buffer
    FloatBuffer custom0Buffer = data.getRawCustom0Buffer();
    if (descriptionAttributes.contains(VertexAttribute.CUSTOM0)
        && (custom0Buffer == null || custom0Buffer.capacity() < numVertices * CUSTOM0_SIZE)) {
      custom0Buffer = FloatBuffer.allocate(numVertices * CUSTOM0_SIZE);
      data.setRawCustom0Buffer(custom0Buffer);
    } else if (descriptionAttributes.contains(VertexAttribute.CUSTOM0)) {
      custom0Buffer.rewind();
    } else {
      data.setRawCustom0Buffer(null);
      custom0Buffer = null;
    }

    //计算AABB包围盒
    Vector3 minAabb = new Vector3();
    Vector3 maxAabb = new Vector3();
    Vector3 firstPosition = firstVertex.getPosition();
    minAabb.set(firstPosition);
    maxAabb.set(firstPosition);

    // 更新原始缓冲区并在一次遍历顶点时计算Aabb。
    for (int i = 0; i < vertices.size(); i++) {
      Vertex vertex = vertices.get(i);

      // Aabb.
      Vector3 position = vertex.getPosition();
      minAabb.set(Vector3.min(minAabb, position));
      maxAabb.set(Vector3.max(maxAabb, position));

      // Position
      addVector3ToBuffer(position, positionBuffer);

      // Tangents
      if (tangentsBuffer != null) {
        Vector3 normal = vertex.getNormal();
        if (normal == null) {
          throw new IllegalArgumentException(
              "Missing normal: If any Vertex in a "
                  + "RenderableDescription has a normal, all vertices must have one.");
        }

        Quaternion tangent = normalToTangent(normal);
        addQuaternionToBuffer(tangent, tangentsBuffer);
      }

      // Uv
      if (uvBuffer != null) {
        Vertex.UvCoordinate uvCoordinate = vertex.getUvCoordinate();
        if (uvCoordinate == null) {
          throw new IllegalArgumentException(
              "Missing UV Coordinate: If any Vertex in a "
                  + "RenderableDescription has a UV Coordinate, all vertices must have one.");
        }

        addUvToBuffer(uvCoordinate, uvBuffer);
      }

      // Color
      if (colorBuffer != null) {
        Color color = vertex.getColor();
        if (color == null) {
          throw new IllegalArgumentException(
              "Missing Color: If any Vertex in a "
                  + "RenderableDescription has a Color, all vertices must have one.");
        }

        addColorToBuffer(color, colorBuffer);
      }

      // CUSTOM0
      if (custom0Buffer != null) {
        Vertex.Float4 custom0 = vertex.getCustom0();
        if (custom0 == null) {
          throw new IllegalArgumentException(
              "Missing CUSTOM0: If any Vertex in a "
                  + "RenderableDescription has CUSTOM0, all vertices must have one.");
        }

        addFloat4ToBuffer(custom0, custom0Buffer);
      }
    }

    // 在可渲染数据中设置Aabb
    Vector3 extentsAabb = Vector3.subtract(maxAabb, minAabb).scaled(0.5f);
    Vector3 centerAabb = Vector3.add(minAabb, extentsAabb);
    data.setExtentsAabb(extentsAabb);
    data.setCenterAabb(centerAabb);

    if (vertexBuffer == null) {
      throw new AssertionError("VertexBuffer is null.");
    }

    IEngine engine = EngineInstance.getEngine();
    positionBuffer.rewind();
    int bufferIndex = 0;
    vertexBuffer.setBufferAt(
        engine.getFilamentEngine(), bufferIndex, positionBuffer, 0, numVertices * POSITION_SIZE);

    if (tangentsBuffer != null) {
      tangentsBuffer.rewind();
      bufferIndex++;
      vertexBuffer.setBufferAt(
          engine.getFilamentEngine(), bufferIndex, tangentsBuffer, 0, numVertices * TANGENTS_SIZE);
    }

    if (uvBuffer != null) {
      uvBuffer.rewind();
      bufferIndex++;
      vertexBuffer.setBufferAt(
          engine.getFilamentEngine(), bufferIndex, uvBuffer, 0, numVertices * UV_SIZE);
    }

    if (colorBuffer != null) {
      colorBuffer.rewind();
      bufferIndex++;
      vertexBuffer.setBufferAt(
          engine.getFilamentEngine(), bufferIndex, colorBuffer, 0, numVertices * COLOR_SIZE);
    }

    if (custom0Buffer != null) {
      custom0Buffer.rewind();
      bufferIndex++;
      vertexBuffer.setBufferAt(
          engine.getFilamentEngine(), bufferIndex, custom0Buffer, 0, numVertices * CUSTOM0_SIZE);
    }
  }

  private RenderableDefinition(Builder builder) {
    vertices = Preconditions.checkNotNull(builder.vertices);
    subGeometries = Preconditions.checkNotNull(builder.subGeometries);
    primitiveType = Preconditions.checkNotNull(builder.primitiveType);
  }

  public static Builder builder() {
    return new Builder();
  }

  private static VertexBuffer createVertexBuffer(
      int vertexCount, EnumSet<VertexAttribute> attributes) {
    VertexBuffer.Builder builder = new VertexBuffer.Builder();

    builder.vertexCount(vertexCount).bufferCount(attributes.size());

    // Position
    int bufferIndex = 0;
    builder.attribute(
        VertexAttribute.POSITION,
        bufferIndex,
        VertexBuffer.AttributeType.FLOAT3,
        0,
        POSITION_SIZE * BYTES_PER_FLOAT);

    // Tangents
    if (attributes.contains(VertexAttribute.TANGENTS)) {
      bufferIndex++;
      builder.attribute(
          VertexAttribute.TANGENTS,
          bufferIndex,
          VertexBuffer.AttributeType.FLOAT4,
          0,
          TANGENTS_SIZE * BYTES_PER_FLOAT);
    }

    // Uv
    if (attributes.contains(VertexAttribute.UV0)) {
      bufferIndex++;
      builder.attribute(
          VertexAttribute.UV0,
          bufferIndex,
          VertexBuffer.AttributeType.FLOAT2,
          0,
          UV_SIZE * BYTES_PER_FLOAT);
    }

    // Color
    if (attributes.contains(VertexAttribute.COLOR)) {
      bufferIndex++;
      builder.attribute(
          VertexAttribute.COLOR,
          bufferIndex,
          VertexBuffer.AttributeType.FLOAT4,
          0,
          COLOR_SIZE * BYTES_PER_FLOAT);
    }

    // CUSTOM0
    if (attributes.contains(VertexAttribute.CUSTOM0)) {
      bufferIndex++;
      builder.attribute(
          VertexAttribute.CUSTOM0,
          bufferIndex,
          VertexBuffer.AttributeType.FLOAT4,
          0,
          CUSTOM0_SIZE * BYTES_PER_FLOAT);
    }

    return builder.build(EngineInstance.getEngine().getFilamentEngine());
  }

  private static void addVector3ToBuffer(Vector3 vector3, FloatBuffer buffer) {
    buffer.put(vector3.x);
    buffer.put(vector3.y);
    buffer.put(vector3.z);
  }

  private static void addUvToBuffer(Vertex.UvCoordinate uvCoordinate, FloatBuffer buffer) {
    buffer.put(uvCoordinate.x);
    buffer.put(uvCoordinate.y);
  }

  private static void addQuaternionToBuffer(Quaternion quaternion, FloatBuffer buffer) {
    buffer.put(quaternion.x);
    buffer.put(quaternion.y);
    buffer.put(quaternion.z);
    buffer.put(quaternion.w);
  }

  private static void addColorToBuffer(Color color, FloatBuffer buffer) {
    buffer.put(color.r);
    buffer.put(color.g);
    buffer.put(color.b);
    buffer.put(color.a);
  }

  private static void addFloat4ToBuffer(Vertex.Float4 value, FloatBuffer buffer) {
    buffer.put(value.x);
    buffer.put(value.y);
    buffer.put(value.z);
    buffer.put(value.w);
  }

  private static Quaternion normalToTangent(Vector3 normal) {
    Vector3 tangent;
    Vector3 bitangent;

    // 计算 basis vectors (+x = tangent, +y = bitangent, +z = normal).
    tangent = Vector3.cross(Vector3.up(), normal);

    // 使用almostEqualRelativeAndAbs进行相等性检查，以解释浮点数的不准确性。
    if (MathHelper.almostEqualRelativeAndAbs(Vector3.dot(tangent, tangent), 0.0f)) {
      bitangent = Vector3.cross(normal, Vector3.right()).normalized();
      tangent = Vector3.cross(bitangent, normal).normalized();
    } else {
      tangent.set(tangent.normalized());
      bitangent = Vector3.cross(normal, tangent).normalized();
    }

    // 一个4x4变换矩阵的旋转由左上角的3x3元素表示。
    final int rowOne = 0;
    scratchMatrix.data[rowOne] = tangent.x;
    scratchMatrix.data[rowOne + 1] = tangent.y;
    scratchMatrix.data[rowOne + 2] = tangent.z;

    final int rowTwo = 4;
    scratchMatrix.data[rowTwo] = bitangent.x;
    scratchMatrix.data[rowTwo + 1] = bitangent.y;
    scratchMatrix.data[rowTwo + 2] = bitangent.z;

    final int rowThree = 8;
    scratchMatrix.data[rowThree] = normal.x;
    scratchMatrix.data[rowThree + 1] = normal.y;
    scratchMatrix.data[rowThree + 2] = normal.z;

    Quaternion orientationQuaternion = new Quaternion();
    scratchMatrix.extractQuaternion(orientationQuaternion);
    return orientationQuaternion;
  }

  /** Factory class for {@link RenderableDefinition}. */
  public static final class Builder {
    @Nullable private List<Vertex> vertices;
    @Nullable private List<SubGeometry> subGeometries = new ArrayList<>();
    private RenderableManager.PrimitiveType primitiveType =
        RenderableManager.PrimitiveType.TRIANGLES;

    public Builder setVertices(List<Vertex> vertices) {
      this.vertices = vertices;
      return this;
    }

    /**
     * 设置待构建定义包含的全部子几何。
     *
     * @param subGeometries 子几何列表，不能为空
     * @return 当前 Builder
     */
    public Builder setSubGeometries(List<SubGeometry> subGeometries) {
      this.subGeometries = subGeometries;
      return this;
    }

    /**
     * 设置待构建定义包含的旧版子网格列表。
     *
     * @param submeshes 旧版子网格列表
     * @return 当前 Builder
     * @deprecated 请使用 {@link #setSubGeometries(List)}。
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public Builder setSubmeshes(List<Submesh> submeshes) {
      //desc- 旧 Builder 生成的元素实际为 SubGeometry，保留列表引用以兼容调用方后续修改。
      subGeometries = (List<SubGeometry>) (List<?>) submeshes;
      return this;
    }

    /**
     * 设置 Renderable 的初始图元类型，默认值为 {@link RenderableManager.PrimitiveType#TRIANGLES}。
     *
     * @param primitiveType Filament 图元类型
     * @return 当前 Builder
     */
    public Builder setPrimitiveType(RenderableManager.PrimitiveType primitiveType) {
      this.primitiveType = Preconditions.checkNotNull(primitiveType);
      return this;
    }

    public RenderableDefinition build() {
      return new RenderableDefinition(this);
    }
  }
}
