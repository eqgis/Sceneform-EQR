package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;
import com.eqgis.sceneform.lull.Vec3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class LightingDef extends Table {
  public static LightingDef getRootAsLightingDef(ByteBuffer _bb) {
    return getRootAsLightingDef(_bb, new LightingDef());
  }
  
  public static LightingDef getRootAsLightingDef(ByteBuffer _bb, LightingDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public LightingDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public String name() {
    int o = __offset(4);
    return (o != 0) ? __string(o + this.bb_pos) : null;
  }
  
  public ByteBuffer nameAsByteBuffer() {
    return __vector_as_bytebuffer(4, 1);
  }
  
  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 4, 1);
  }
  
  public float scale() {
    int o = __offset(6);
    return (o != 0) ? this.bb.getFloat(o + this.bb_pos) : 0.0F;
  }
  
  public LightingCubeDef cubeLevels(int j) {
    return cubeLevels(new LightingCubeDef(), j);
  }
  
  public LightingCubeDef cubeLevels(LightingCubeDef obj, int j) {
    int o = __offset(8);
    return (o != 0) ? obj.__assign(__indirect(__vector(o) + j * 4), this.bb) : null;
  }
  
  public int cubeLevelsLength() {
    int o = __offset(8);
    return (o != 0) ? __vector_len(o) : 0;
  }
  
  public Vec3 shCoefficients(int j) {
    return shCoefficients(new Vec3(), j);
  }
  
  public Vec3 shCoefficients(Vec3 obj, int j) {
    int o = __offset(10);
    return (o != 0) ? obj.__assign(__vector(o) + j * 12, this.bb) : null;
  }
  
  public int shCoefficientsLength() {
    int o = __offset(10);
    return (o != 0) ? __vector_len(o) : 0;
  }
  
  public static int createLightingDef(FlatBufferBuilder builder, int nameOffset, float scale, int cube_levelsOffset, int sh_coefficientsOffset) {
    builder.startObject(4);
    addShCoefficients(builder, sh_coefficientsOffset);
    addCubeLevels(builder, cube_levelsOffset);
    addScale(builder, scale);
    addName(builder, nameOffset);
    return endLightingDef(builder);
  }
  
  public static void startLightingDef(FlatBufferBuilder builder) {
    builder.startObject(4);
  }
  
  public static void addName(FlatBufferBuilder builder, int nameOffset) {
    builder.addOffset(0, nameOffset, 0);
  }
  
  public static void addScale(FlatBufferBuilder builder, float scale) {
    builder.addFloat(1, scale, 0.0D);
  }
  
  public static void addCubeLevels(FlatBufferBuilder builder, int cubeLevelsOffset) {
    builder.addOffset(2, cubeLevelsOffset, 0);
  }
  
  public static int createCubeLevelsVector(FlatBufferBuilder builder, int[] data) {
    builder.startVector(4, data.length, 4);
    for (int i = data.length - 1; i >= 0; ) {
      builder.addOffset(data[i]);
      i--;
    } 
    return builder.endVector();
  }
  
  public static void startCubeLevelsVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(4, numElems, 4);
  }
  
  public static void addShCoefficients(FlatBufferBuilder builder, int shCoefficientsOffset) {
    builder.addOffset(3, shCoefficientsOffset, 0);
  }
  
  public static void startShCoefficientsVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(12, numElems, 4);
  }
  
  public static int endLightingDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
