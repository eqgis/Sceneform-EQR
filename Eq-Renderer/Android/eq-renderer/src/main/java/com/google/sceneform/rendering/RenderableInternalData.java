package com.google.sceneform.rendering;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.sceneform.math.Vector3;
import com.google.sceneform.utilities.AndroidPreconditions;
import com.google.android.filament.Box;
import com.google.android.filament.Entity;
import com.google.android.filament.EntityInstance;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.VertexBuffer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 表示{@link Renderable}用于渲染的数据。
 * 当调用{@link RenderableInternalData#dispose()}函数时，这里包含的所有灯丝资源和材料将被销毁。
 */
class RenderableInternalData implements IRenderableInternalData {
  private static final String TAG = RenderableInternalData.class.getSimpleName();

  /** 表示用于渲染可渲染对象的每个网格的数据。 */
  static class MeshData {
    // 当前Mesh的indices的起始索引
    int indexStart;
    // 当前Mesh的indices的结束索引
    int indexEnd;
  }

  // Geometry
  private final Vector3 centerAabb = Vector3.zero();
  private final Vector3 extentsAabb = Vector3.zero();

  // Transform
  private float transformScale = 1f;
  private final Vector3 transformOffset = Vector3.zero();

  // Raw
  @Nullable private IntBuffer rawIndexBuffer;
  @Nullable private FloatBuffer rawPositionBuffer;
  @Nullable private FloatBuffer rawTangentsBuffer;
  @Nullable private FloatBuffer rawUvBuffer;
  @Nullable private FloatBuffer rawColorBuffer;

  // Filament Geometry buffers.
  @Nullable private IndexBuffer indexBuffer;
  @Nullable private VertexBuffer vertexBuffer;

  // Mesh数据集
  private final ArrayList<MeshData> meshes = new ArrayList<>();

  @Override
  public void setCenterAabb(Vector3 minAabb) {
    this.centerAabb.set(minAabb);
  }

  @Override
  public Vector3 getCenterAabb() {
    return new Vector3(centerAabb);
  }

  @Override
  public void setExtentsAabb(Vector3 maxAabb) {
    this.extentsAabb.set(maxAabb);
  }

  @Override
  public Vector3 getExtentsAabb() {
    return new Vector3(extentsAabb);
  }

  @Override
  public Vector3 getSizeAabb() {
    return extentsAabb.scaled(2.0f);
  }

  @Override
  public void setTransformScale(float scale) {
    this.transformScale = scale;
  }

  @Override
  public float getTransformScale() {
    return transformScale;
  }

  @Override
  public void setTransformOffset(Vector3 offset) {
    this.transformOffset.set(offset);
  }

  @Override
  public Vector3 getTransformOffset() {
    return new Vector3(transformOffset);
  }

  @Override
  public ArrayList<MeshData> getMeshes() {
    return meshes;
  }

  @Override
  public void setIndexBuffer(@Nullable IndexBuffer indexBuffer) {
    this.indexBuffer = indexBuffer;
  }

  @Override
  @Nullable
  public IndexBuffer getIndexBuffer() {
    return indexBuffer;
  }

  @Override
  public void setVertexBuffer(@Nullable VertexBuffer vertexBuffer) {
    this.vertexBuffer = vertexBuffer;
  }

  @Override
  @Nullable
  public VertexBuffer getVertexBuffer() {
    return vertexBuffer;
  }

  @Override
  public void setRawIndexBuffer(@Nullable IntBuffer rawIndexBuffer) {
    this.rawIndexBuffer = rawIndexBuffer;
  }

  @Override
  @Nullable
  public IntBuffer getRawIndexBuffer() {
    return rawIndexBuffer;
  }

  @Override
  public void setRawPositionBuffer(@Nullable FloatBuffer rawPositionBuffer) {
    this.rawPositionBuffer = rawPositionBuffer;
  }

  @Override
  @Nullable
  public FloatBuffer getRawPositionBuffer() {
    return rawPositionBuffer;
  }

  @Override
  public void setRawTangentsBuffer(@Nullable FloatBuffer rawTangentsBuffer) {
    this.rawTangentsBuffer = rawTangentsBuffer;
  }

  @Override
  @Nullable
  public FloatBuffer getRawTangentsBuffer() {
    return rawTangentsBuffer;
  }

  @Override
  public void setRawUvBuffer(@Nullable FloatBuffer rawUvBuffer) {
    this.rawUvBuffer = rawUvBuffer;
  }

  @Override
  @Nullable
  public FloatBuffer getRawUvBuffer() {
    return rawUvBuffer;
  }

  @Override
  public void setRawColorBuffer(@Nullable FloatBuffer rawColorBuffer) {
    this.rawColorBuffer = rawColorBuffer;
  }

  @Override
  @Nullable
  public FloatBuffer getRawColorBuffer() {
    return rawColorBuffer;
  }


  private void setupSkeleton(RenderableManager.Builder builder) {return ;}





  @Override
  public void buildInstanceData(RenderableInstance instance, @Entity int renderedEntity) {
    Renderable renderable = instance.getRenderable();
    IRenderableInternalData renderableData = renderable.getRenderableData();
    ArrayList<Material> materialBindings = renderable.getMaterialBindings();
    RenderableManager renderableManager = EngineInstance.getEngine().getRenderableManager();
    @EntityInstance int renderableInstance = renderableManager.getInstance(renderedEntity);

    // 判断是否需要创建新的实例
    int meshCount = renderableData.getMeshes().size();
    if (renderableInstance == 0
            || renderableManager.getPrimitiveCount(renderableInstance) != meshCount) {
      // 销毁旧实例
      if (renderableInstance != 0) {
        renderableManager.destroy(renderedEntity);
      }

      // 创建新实例
      RenderableManager.Builder builder =
              new RenderableManager.Builder(meshCount)
                      .priority(renderable.getRenderPriority())
                      .castShadows(renderable.isShadowCaster())
                      .receiveShadows(renderable.isShadowReceiver());

      setupSkeleton(builder);

      builder.build(EngineInstance.getEngine().getFilamentEngine(), renderedEntity);

      renderableInstance = renderableManager.getInstance(renderedEntity);
      if (renderableInstance == 0) {
        throw new AssertionError("Unable to create RenderableInstance.");
      }
    } else {
      renderableManager.setPriority(renderableInstance, renderable.getRenderPriority());
      renderableManager.setCastShadows(renderableInstance, renderable.isShadowCaster());
      renderableManager.setReceiveShadows(renderableInstance, renderable.isShadowReceiver());
    }

    //更新bounds
    Vector3 extents = renderableData.getExtentsAabb();
    Vector3 center = renderableData.getCenterAabb();
    Box filamentBox = new Box(center.x, center.y, center.z, extents.x, extents.y, extents.z);
    renderableManager.setAxisAlignedBoundingBox(renderableInstance, filamentBox);

    if (materialBindings.size() != meshCount) {
      throw new AssertionError("Material Bindings are out of sync with meshes.");
    }

    // 更新几何信息和材质
    final RenderableManager.PrimitiveType primitiveType = RenderableManager.PrimitiveType.TRIANGLES;
    for (int mesh = 0; mesh < meshCount; ++mesh) {
      // 更新mesh
      MeshData meshData = renderableData.getMeshes().get(mesh);
      @Nullable VertexBuffer vertexBuffer = renderableData.getVertexBuffer();
      @Nullable IndexBuffer indexBuffer = renderableData.getIndexBuffer();
      if (vertexBuffer == null || indexBuffer == null) {
        throw new AssertionError("Internal Error: Failed to get vertex or index buffer");
      }
      renderableManager.setGeometryAt(
              renderableInstance,
              mesh,
              primitiveType,
              vertexBuffer,
              indexBuffer,
              meshData.indexStart,
              meshData.indexEnd - meshData.indexStart);

      //更新材质
      Material material = materialBindings.get(mesh);
      renderableManager.setMaterialInstanceAt(
              renderableInstance, mesh, material.getFilamentMaterialInstance());
    }
  }

  @Override
  public void setAnimationNames(@NonNull List<String> animationNames) {}

  @NonNull
  @Override
  public List<String> getAnimationNames() {
    return Collections.emptyList();
  }


















  /** @hide */
  @Override
  protected void finalize() throws Throwable {
    //2024年8月19日21:29:17 Ikkyu备注：渲染的自定义Mesh不再在此处执行销毁操作
//    try {
//      ThreadPools.getMainExecutor().execute(() -> dispose());
//    } catch (Exception e) {
//      Log.e(TAG, "Error while Finalizing Renderable Internal Data.", e);
//    } finally {
//      super.finalize();
//    }
  }

  /**
   * Removes any memory used by the object.
   *
   * @hide
   */
  @Override
  public void dispose() {
    AndroidPreconditions.checkUiThread();

    IEngine engine = EngineInstance.getEngine();
    if (engine == null || !engine.isValid()) {
      return;
    }

    if (vertexBuffer != null) {
      engine.destroyVertexBuffer(vertexBuffer);
      vertexBuffer = null;
    }

    if (indexBuffer != null) {
      engine.destroyIndexBuffer(indexBuffer);
      indexBuffer = null;
    }
  }
}
