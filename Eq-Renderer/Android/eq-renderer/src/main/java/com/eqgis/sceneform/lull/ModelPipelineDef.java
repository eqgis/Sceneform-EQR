// automatically generated by the FlatBuffers compiler, do not modify

package com.eqgis.sceneform.lull;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
/**
 * An optional configuration file that can be provided to the model pipeline
 * to control behaviour.
 */
public final class ModelPipelineDef extends Table {
  public static ModelPipelineDef getRootAsModelPipelineDef(ByteBuffer _bb) { return getRootAsModelPipelineDef(_bb, new ModelPipelineDef()); }
  public static ModelPipelineDef getRootAsModelPipelineDef(ByteBuffer _bb, ModelPipelineDef obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; vtable_start = bb_pos - bb.getInt(bb_pos); vtable_size = bb.getShort(vtable_start); }
  public ModelPipelineDef __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  /**
   * The list of assets to import.
   */
  public ModelPipelineImportDef sources(int j) { return sources(new ModelPipelineImportDef(), j); }
  public ModelPipelineImportDef sources(ModelPipelineImportDef obj, int j) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int sourcesLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  /**
   * The list of models used for rendering. Each index in the list specifies
   * an LOD level.
   */
  public ModelPipelineRenderableDef renderables(int j) { return renderables(new ModelPipelineRenderableDef(), j); }
  public ModelPipelineRenderableDef renderables(ModelPipelineRenderableDef obj, int j) { int o = __offset(6); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int renderablesLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  /**
   * The model used for collision.
   */
  public ModelPipelineCollidableDef collidable() { return collidable(new ModelPipelineCollidableDef()); }
  public ModelPipelineCollidableDef collidable(ModelPipelineCollidableDef obj) { int o = __offset(8); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }
  /**
   * The model used for skeletal animations.
   */
  public ModelPipelineSkeletonDef skeleton() { return skeleton(new ModelPipelineSkeletonDef()); }
  public ModelPipelineSkeletonDef skeleton(ModelPipelineSkeletonDef obj) { int o = __offset(10); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }
  /**
   * The textures to be used by the renderables.
   */
  public TextureDef textures(int j) { return textures(new TextureDef(), j); }
  public TextureDef textures(TextureDef obj, int j) { int o = __offset(12); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int texturesLength() { int o = __offset(12); return o != 0 ? __vector_len(o) : 0; }

  public static int createModelPipelineDef(FlatBufferBuilder builder,
                                           int sourcesOffset,
                                           int renderablesOffset,
                                           int collidableOffset,
                                           int skeletonOffset,
                                           int texturesOffset) {
    builder.startObject(5);
    ModelPipelineDef.addTextures(builder, texturesOffset);
    ModelPipelineDef.addSkeleton(builder, skeletonOffset);
    ModelPipelineDef.addCollidable(builder, collidableOffset);
    ModelPipelineDef.addRenderables(builder, renderablesOffset);
    ModelPipelineDef.addSources(builder, sourcesOffset);
    return ModelPipelineDef.endModelPipelineDef(builder);
  }

  public static void startModelPipelineDef(FlatBufferBuilder builder) { builder.startObject(5); }
  public static void addSources(FlatBufferBuilder builder, int sourcesOffset) { builder.addOffset(0, sourcesOffset, 0); }
  public static int createSourcesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startSourcesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addRenderables(FlatBufferBuilder builder, int renderablesOffset) { builder.addOffset(1, renderablesOffset, 0); }
  public static int createRenderablesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startRenderablesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addCollidable(FlatBufferBuilder builder, int collidableOffset) { builder.addOffset(2, collidableOffset, 0); }
  public static void addSkeleton(FlatBufferBuilder builder, int skeletonOffset) { builder.addOffset(3, skeletonOffset, 0); }
  public static void addTextures(FlatBufferBuilder builder, int texturesOffset) { builder.addOffset(4, texturesOffset, 0); }
  public static int createTexturesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startTexturesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endModelPipelineDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
