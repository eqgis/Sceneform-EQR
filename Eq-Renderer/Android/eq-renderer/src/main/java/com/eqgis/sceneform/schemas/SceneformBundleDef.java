package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.lull.ModelDef;
import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SceneformBundleDef extends Table {
  public static SceneformBundleDef getRootAsSceneformBundleDef(ByteBuffer _bb) {
    return getRootAsSceneformBundleDef(_bb, new SceneformBundleDef());
  }
  
  public static SceneformBundleDef getRootAsSceneformBundleDef(ByteBuffer _bb, SceneformBundleDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public static boolean SceneformBundleDefBufferHasIdentifier(ByteBuffer _bb) {
    return __has_identifier(_bb, "RBUN");
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public SceneformBundleDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public VersionDef version() {
    return version(new VersionDef());
  }
  
  public VersionDef version(VersionDef obj) {
    int o = __offset(4);
    return (o != 0) ? obj.__assign(__indirect(o + this.bb_pos), this.bb) : null;
  }
  
  public ModelDef model() {
    return model(new ModelDef());
  }
  
  public ModelDef model(ModelDef obj) {
    int o = __offset(6);
    return (o != 0) ? obj.__assign(__indirect(o + this.bb_pos), this.bb) : null;
  }
  
  public MaterialDef materials(int j) {
    return materials(new MaterialDef(), j);
  }
  
  public MaterialDef materials(MaterialDef obj, int j) {
    int o = __offset(8);
    return (o != 0) ? obj.__assign(__indirect(__vector(o) + j * 4), this.bb) : null;
  }
  
  public int materialsLength() {
    int o = __offset(8);
    return (o != 0) ? __vector_len(o) : 0;
  }
  
  public CompiledMaterialDef compiledMaterials(int j) {
    return compiledMaterials(new CompiledMaterialDef(), j);
  }
  
  public CompiledMaterialDef compiledMaterials(CompiledMaterialDef obj, int j) {
    int o = __offset(10);
    return (o != 0) ? obj.__assign(__indirect(__vector(o) + j * 4), this.bb) : null;
  }
  
  public int compiledMaterialsLength() {
    int o = __offset(10);
    return (o != 0) ? __vector_len(o) : 0;
  }
  
  public SuggestedCollisionShapeDef suggestedCollisionShape() {
    return suggestedCollisionShape(new SuggestedCollisionShapeDef());
  }
  
  public SuggestedCollisionShapeDef suggestedCollisionShape(SuggestedCollisionShapeDef obj) {
    int o = __offset(12);
    return (o != 0) ? obj.__assign(__indirect(o + this.bb_pos), this.bb) : null;
  }
  
  public SamplerDef samplers(int j) {
    return samplers(new SamplerDef(), j);
  }
  
  public SamplerDef samplers(SamplerDef obj, int j) {
    int o = __offset(14);
    return (o != 0) ? obj.__assign(__indirect(__vector(o) + j * 4), this.bb) : null;
  }
  
  public int samplersLength() {
    int o = __offset(14);
    return (o != 0) ? __vector_len(o) : 0;
  }
  
  public InputDef inputs(int j) {
    return inputs(new InputDef(), j);
  }
  
  public InputDef inputs(InputDef obj, int j) {
    int o = __offset(16);
    return (o != 0) ? obj.__assign(__indirect(__vector(o) + j * 4), this.bb) : null;
  }
  
  public int inputsLength() {
    int o = __offset(16);
    return (o != 0) ? __vector_len(o) : 0;
  }
  
  public LightingDef lightingDefs(int j) {
    return lightingDefs(new LightingDef(), j);
  }
  
  public LightingDef lightingDefs(LightingDef obj, int j) {
    int o = __offset(18);
    return (o != 0) ? obj.__assign(__indirect(__vector(o) + j * 4), this.bb) : null;
  }
  
  public int lightingDefsLength() {
    int o = __offset(18);
    return (o != 0) ? __vector_len(o) : 0;
  }
  
  public AnimationDef animations(int j) {
    return animations(new AnimationDef(), j);
  }
  
  public AnimationDef animations(AnimationDef obj, int j) {
    int o = __offset(20);
    return (o != 0) ? obj.__assign(__indirect(__vector(o) + j * 4), this.bb) : null;
  }
  
  public int animationsLength() {
    int o = __offset(20);
    return (o != 0) ? __vector_len(o) : 0;
  }
  
  public TransformDef transform() {
    return transform(new TransformDef());
  }
  
  public TransformDef transform(TransformDef obj) {
    int o = __offset(22);
    return (o != 0) ? obj.__assign(__indirect(o + this.bb_pos), this.bb) : null;
  }
  
  public RuntimeAssetDef runtime() {
    return runtime(new RuntimeAssetDef());
  }
  
  public RuntimeAssetDef runtime(RuntimeAssetDef obj) {
    int o = __offset(24);
    return (o != 0) ? obj.__assign(__indirect(o + this.bb_pos), this.bb) : null;
  }
  
  public static int createSceneformBundleDef(FlatBufferBuilder builder, int versionOffset, int modelOffset, int materialsOffset, int compiled_materialsOffset, int suggested_collision_shapeOffset, int samplersOffset, int inputsOffset, int lighting_defsOffset, int animationsOffset, int transformOffset, int runtimeOffset) {
    builder.startObject(11);
    addRuntime(builder, runtimeOffset);
    addTransform(builder, transformOffset);
    addAnimations(builder, animationsOffset);
    addLightingDefs(builder, lighting_defsOffset);
    addInputs(builder, inputsOffset);
    addSamplers(builder, samplersOffset);
    addSuggestedCollisionShape(builder, suggested_collision_shapeOffset);
    addCompiledMaterials(builder, compiled_materialsOffset);
    addMaterials(builder, materialsOffset);
    addModel(builder, modelOffset);
    addVersion(builder, versionOffset);
    return endSceneformBundleDef(builder);
  }
  
  public static void startSceneformBundleDef(FlatBufferBuilder builder) {
    builder.startObject(11);
  }
  
  public static void addVersion(FlatBufferBuilder builder, int versionOffset) {
    builder.addOffset(0, versionOffset, 0);
  }
  
  public static void addModel(FlatBufferBuilder builder, int modelOffset) {
    builder.addOffset(1, modelOffset, 0);
  }
  
  public static void addMaterials(FlatBufferBuilder builder, int materialsOffset) {
    builder.addOffset(2, materialsOffset, 0);
  }
  
  public static int createMaterialsVector(FlatBufferBuilder builder, int[] data) {
    builder.startVector(4, data.length, 4);
    for (int i = data.length - 1; i >= 0; ) {
      builder.addOffset(data[i]);
      i--;
    } 
    return builder.endVector();
  }
  
  public static void startMaterialsVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(4, numElems, 4);
  }
  
  public static void addCompiledMaterials(FlatBufferBuilder builder, int compiledMaterialsOffset) {
    builder.addOffset(3, compiledMaterialsOffset, 0);
  }
  
  public static int createCompiledMaterialsVector(FlatBufferBuilder builder, int[] data) {
    builder.startVector(4, data.length, 4);
    for (int i = data.length - 1; i >= 0; ) {
      builder.addOffset(data[i]);
      i--;
    } 
    return builder.endVector();
  }
  
  public static void startCompiledMaterialsVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(4, numElems, 4);
  }
  
  public static void addSuggestedCollisionShape(FlatBufferBuilder builder, int suggestedCollisionShapeOffset) {
    builder.addOffset(4, suggestedCollisionShapeOffset, 0);
  }
  
  public static void addSamplers(FlatBufferBuilder builder, int samplersOffset) {
    builder.addOffset(5, samplersOffset, 0);
  }
  
  public static int createSamplersVector(FlatBufferBuilder builder, int[] data) {
    builder.startVector(4, data.length, 4);
    for (int i = data.length - 1; i >= 0; ) {
      builder.addOffset(data[i]);
      i--;
    } 
    return builder.endVector();
  }
  
  public static void startSamplersVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(4, numElems, 4);
  }
  
  public static void addInputs(FlatBufferBuilder builder, int inputsOffset) {
    builder.addOffset(6, inputsOffset, 0);
  }
  
  public static int createInputsVector(FlatBufferBuilder builder, int[] data) {
    builder.startVector(4, data.length, 4);
    for (int i = data.length - 1; i >= 0; ) {
      builder.addOffset(data[i]);
      i--;
    } 
    return builder.endVector();
  }
  
  public static void startInputsVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(4, numElems, 4);
  }
  
  public static void addLightingDefs(FlatBufferBuilder builder, int lightingDefsOffset) {
    builder.addOffset(7, lightingDefsOffset, 0);
  }
  
  public static int createLightingDefsVector(FlatBufferBuilder builder, int[] data) {
    builder.startVector(4, data.length, 4);
    for (int i = data.length - 1; i >= 0; ) {
      builder.addOffset(data[i]);
      i--;
    } 
    return builder.endVector();
  }
  
  public static void startLightingDefsVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(4, numElems, 4);
  }
  
  public static void addAnimations(FlatBufferBuilder builder, int animationsOffset) {
    builder.addOffset(8, animationsOffset, 0);
  }
  
  public static int createAnimationsVector(FlatBufferBuilder builder, int[] data) {
    builder.startVector(4, data.length, 4);
    for (int i = data.length - 1; i >= 0; ) {
      builder.addOffset(data[i]);
      i--;
    } 
    return builder.endVector();
  }
  
  public static void startAnimationsVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(4, numElems, 4);
  }
  
  public static void addTransform(FlatBufferBuilder builder, int transformOffset) {
    builder.addOffset(9, transformOffset, 0);
  }
  
  public static void addRuntime(FlatBufferBuilder builder, int runtimeOffset) {
    builder.addOffset(10, runtimeOffset, 0);
  }
  
  public static int endSceneformBundleDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  
  public static void finishSceneformBundleDefBuffer(FlatBufferBuilder builder, int offset) {
    builder.finish(offset, "RBUN");
  }
  
  public static void finishSizePrefixedSceneformBundleDefBuffer(FlatBufferBuilder builder, int offset) {
    builder.finishSizePrefixed(offset, "RBUN");
  }
}
