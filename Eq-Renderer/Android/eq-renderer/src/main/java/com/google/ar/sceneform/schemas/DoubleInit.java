package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DoubleInit extends Table {
  public static DoubleInit getRootAsDoubleInit(ByteBuffer _bb) {
    return getRootAsDoubleInit(_bb, new DoubleInit());
  }
  
  public static DoubleInit getRootAsDoubleInit(ByteBuffer _bb, DoubleInit obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public DoubleInit __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public double value() {
    int o = __offset(4);
    return (o != 0) ? this.bb.getDouble(o + this.bb_pos) : 0.0D;
  }
  
  public static int createDoubleInit(FlatBufferBuilder builder, double value) {
    builder.startObject(1);
    addValue(builder, value);
    return endDoubleInit(builder);
  }
  
  public static void startDoubleInit(FlatBufferBuilder builder) {
    builder.startObject(1);
  }
  
  public static void addValue(FlatBufferBuilder builder, double value) {
    builder.addDouble(0, value, 0.0D);
  }
  
  public static int endDoubleInit(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
