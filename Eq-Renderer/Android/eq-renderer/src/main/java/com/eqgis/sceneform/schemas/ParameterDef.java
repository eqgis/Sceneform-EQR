package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ParameterDef extends Table {
  public static ParameterDef getRootAsParameterDef(ByteBuffer _bb) {
    return getRootAsParameterDef(_bb, new ParameterDef());
  }
  
  public static ParameterDef getRootAsParameterDef(ByteBuffer _bb, ParameterDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public ParameterDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public String id() {
    int o = __offset(4);
    return (o != 0) ? __string(o + this.bb_pos) : null;
  }
  
  public ByteBuffer idAsByteBuffer() {
    return __vector_as_bytebuffer(4, 1);
  }
  
  public ByteBuffer idInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 4, 1);
  }
  
  public ParameterInitDef initialValue() {
    return initialValue(new ParameterInitDef());
  }
  
  public ParameterInitDef initialValue(ParameterInitDef obj) {
    int o = __offset(6);
    return (o != 0) ? obj.__assign(__indirect(o + this.bb_pos), this.bb) : null;
  }
  
  public static int createParameterDef(FlatBufferBuilder builder, int idOffset, int initial_valueOffset) {
    builder.startObject(2);
    addInitialValue(builder, initial_valueOffset);
    addId(builder, idOffset);
    return endParameterDef(builder);
  }
  
  public static void startParameterDef(FlatBufferBuilder builder) {
    builder.startObject(2);
  }
  
  public static void addId(FlatBufferBuilder builder, int idOffset) {
    builder.addOffset(0, idOffset, 0);
  }
  
  public static void addInitialValue(FlatBufferBuilder builder, int initialValueOffset) {
    builder.addOffset(1, initialValueOffset, 0);
  }
  
  public static int endParameterDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
