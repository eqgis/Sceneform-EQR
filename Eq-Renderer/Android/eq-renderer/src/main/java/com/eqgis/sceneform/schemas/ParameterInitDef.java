package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ParameterInitDef extends Table {
  public static ParameterInitDef getRootAsParameterInitDef(ByteBuffer _bb) {
    return getRootAsParameterInitDef(_bb, new ParameterInitDef());
  }
  
  public static ParameterInitDef getRootAsParameterInitDef(ByteBuffer _bb, ParameterInitDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public ParameterInitDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public byte initType() {
    int o = __offset(4);
    return (o != 0) ? this.bb.get(o + this.bb_pos) : 0;
  }
  
  public Table init(Table obj) {
    int o = __offset(6);
    return (o != 0) ? __union(obj, o) : null;
  }
  
  public static int createParameterInitDef(FlatBufferBuilder builder, byte init_type, int initOffset) {
    builder.startObject(2);
    addInit(builder, initOffset);
    addInitType(builder, init_type);
    return endParameterInitDef(builder);
  }
  
  public static void startParameterInitDef(FlatBufferBuilder builder) {
    builder.startObject(2);
  }
  
  public static void addInitType(FlatBufferBuilder builder, byte initType) {
    builder.addByte(0, initType, 0);
  }
  
  public static void addInit(FlatBufferBuilder builder, int initOffset) {
    builder.addOffset(1, initOffset, 0);
  }
  
  public static int endParameterInitDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
