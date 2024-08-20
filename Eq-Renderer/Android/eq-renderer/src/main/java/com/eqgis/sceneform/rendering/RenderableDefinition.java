package com.eqgis.sceneform.rendering;

import androidx.annotation.Nullable;

import com.eqgis.sceneform.math.MathHelper;
import com.eqgis.sceneform.math.Matrix;
import com.eqgis.sceneform.math.Quaternion;
import com.eqgis.sceneform.math.Vector3;
import com.eqgis.sceneform.utilities.AndroidPreconditions;
import com.eqgis.sceneform.utilities.Preconditions;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.IndexBuffer.Builder.IndexType;
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
public class RenderableDefinition {
  private static final Matrix scratchMatrix = new Matrix();

  /**
   * 表示RenderableDefinition的子网格。每个RenderableDefinition可以有多个Submeshes。
   */
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

    /** Factory class for {@link Submesh}. */
    public static final class Builder {
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
        return new Submesh(this);
      }
    }
  }

  private List<Vertex> vertices;
  private List<Submesh> submeshes;

  private static final int BYTES_PER_FLOAT = Float.SIZE / 8;
  private static final int POSITION_SIZE = 3; // x, y, z
  private static final int UV_SIZE = 2;
  private static final int TANGENTS_SIZE = 4; // quaternion
  private static final int COLOR_SIZE = 4; // RGBA

  public void setVertices(List<Vertex> vertices) {
    this.vertices = vertices;
  }

  List<Vertex> getVertices() {
    return vertices;
  }

  public void setSubmeshes(List<Submesh> submeshes) {
    this.submeshes = submeshes;
  }

  public List<Submesh> getSubmeshes() {
    return submeshes;
  }

  void applyDefinitionToData(
      IRenderableInternalData data,
      ArrayList<Material> materialBindings,
      ArrayList<String> materialNames) {
    AndroidPreconditions.checkUiThread();

    applyDefinitionToDataIndexBuffer(data);
    applyDefinitionToDataVertexBuffer(data);

    // 添加网格数据
    int indexStart = 0;
    materialBindings.clear();
    materialNames.clear();
    for (int i = 0; i < submeshes.size(); i++) {
      Submesh submesh = submeshes.get(i);

      RenderableInternalData.MeshData meshData;
      if (i < data.getMeshes().size()) {
        meshData = data.getMeshes().get(i);
      } else {
        meshData = new RenderableInternalData.MeshData();
        data.getMeshes().add(meshData);
      }

      meshData.indexStart = indexStart;
      meshData.indexEnd = indexStart + submesh.getTriangleIndices().size();
      indexStart = meshData.indexEnd;
      materialBindings.add(submesh.getMaterial());
      final String name = submesh.getName();
      materialNames.add(name != null ? name : "");
    }

    // 移除旧数据
    while (data.getMeshes().size() > submeshes.size()) {
      data.getMeshes().remove(data.getMeshes().size() - 1);
    }
  }

  private void applyDefinitionToDataIndexBuffer(IRenderableInternalData data) {
    // 计算顶点索引
    int numIndices = 0;
    for (int i = 0; i < submeshes.size(); i++) {
      Submesh submesh = submeshes.get(i);
      numIndices += submesh.getTriangleIndices().size();
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
    for (int i = 0; i < submeshes.size(); i++) {
      Submesh submesh = submeshes.get(i);
      List<Integer> triangleIndices = submesh.getTriangleIndices();
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
  }

  private RenderableDefinition(Builder builder) {
    vertices = Preconditions.checkNotNull(builder.vertices);
    submeshes = Preconditions.checkNotNull(builder.submeshes);
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
    @Nullable private List<Submesh> submeshes = new ArrayList<>();

    public Builder setVertices(List<Vertex> vertices) {
      this.vertices = vertices;
      return this;
    }

    public Builder setSubmeshes(List<Submesh> submeshes) {
      this.submeshes = submeshes;
      return this;
    }

    public RenderableDefinition build() {
      return new RenderableDefinition(this);
    }
  }
}
