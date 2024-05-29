package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class IntInit extends Table {
  public static IntInit getRootAsIntInit(ByteBuffer _bb) {
    return getRootAsIntInit(_bb, new IntInit());
  }
  
  public static IntInit getRootAsIntInit(ByteBuffer _bb, IntInit obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public IntInit __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public int value() {
    int o = __offset(4);
    return (o != 0) ? this.bb.getInt(o + this.bb_pos) : 0;
  }
  
  public static int createIntInit(FlatBufferBuilder builder, int value) {
    builder.startObject(1);
    addValue(builder, value);
    return endIntInit(builder);
  }
  
  public static void startIntInit(FlatBufferBuilder builder) {
    builder.startObject(1);
  }
  
  public static void addValue(FlatBufferBuilder builder, int value) {
    builder.addInt(0, value, 0);
  }
  
  public static int endIntInit(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
