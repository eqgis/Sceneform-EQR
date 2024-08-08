package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class MaterialDef extends Table {
  public static MaterialDef getRootAsMaterialDef(ByteBuffer _bb) {
    return getRootAsMaterialDef(_bb, new MaterialDef());
  }
  
  public static MaterialDef getRootAsMaterialDef(ByteBuffer _bb, MaterialDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public MaterialDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public ParameterDef parameters(int j) {
    return parameters(new ParameterDef(), j);
  }
  
  public ParameterDef parameters(ParameterDef obj, int j) {
    int o = __offset(4);
    return (o != 0) ? obj.__assign(__indirect(__vector(o) + j * 4), this.bb) : null;
  }
  
  public int parametersLength() {
    int o = __offset(4);
    return (o != 0) ? __vector_len(o) : 0;
  }
  
  public int compiledIndex() {
    int o = __offset(6);
    return (o != 0) ? (this.bb.get(o + this.bb_pos) & 0xFF) : 0;
  }
  
  public String name() {
    int o = __offset(8);
    return (o != 0) ? __string(o + this.bb_pos) : null;
  }
  
  public ByteBuffer nameAsByteBuffer() {
    return __vector_as_bytebuffer(8, 1);
  }
  
  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 8, 1);
  }
  
  public static int createMaterialDef(FlatBufferBuilder builder, int parametersOffset, int compiled_index, int nameOffset) {
    builder.startObject(3);
    addName(builder, nameOffset);
    addParameters(builder, parametersOffset);
    addCompiledIndex(builder, compiled_index);
    return endMaterialDef(builder);
  }
  
  public static void startMaterialDef(FlatBufferBuilder builder) {
    builder.startObject(3);
  }
  
  public static void addParameters(FlatBufferBuilder builder, int parametersOffset) {
    builder.addOffset(0, parametersOffset, 0);
  }
  
  public static int createParametersVector(FlatBufferBuilder builder, int[] data) {
    builder.startVector(4, data.length, 4);
    for (int i = data.length - 1; i >= 0; ) {
      builder.addOffset(data[i]);
      i--;
    } 
    return builder.endVector();
  }
  
  public static void startParametersVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(4, numElems, 4);
  }
  
  public static void addCompiledIndex(FlatBufferBuilder builder, int compiledIndex) {
    builder.addByte(1, (byte)compiledIndex, 0);
  }
  
  public static void addName(FlatBufferBuilder builder, int nameOffset) {
    builder.addOffset(2, nameOffset, 0);
  }
  
  public static int endMaterialDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
