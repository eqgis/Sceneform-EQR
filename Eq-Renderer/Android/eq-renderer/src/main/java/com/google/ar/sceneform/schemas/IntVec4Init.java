package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class IntVec4Init extends Table {
  public static IntVec4Init getRootAsIntVec4Init(ByteBuffer _bb) {
    return getRootAsIntVec4Init(_bb, new IntVec4Init());
  }
  
  public static IntVec4Init getRootAsIntVec4Init(ByteBuffer _bb, IntVec4Init obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public IntVec4Init __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public int x() {
    int o = __offset(4);
    return (o != 0) ? this.bb.getInt(o + this.bb_pos) : 0;
  }
  
  public int y() {
    int o = __offset(6);
    return (o != 0) ? this.bb.getInt(o + this.bb_pos) : 0;
  }
  
  public int z() {
    int o = __offset(8);
    return (o != 0) ? this.bb.getInt(o + this.bb_pos) : 0;
  }
  
  public int w() {
    int o = __offset(10);
    return (o != 0) ? this.bb.getInt(o + this.bb_pos) : 0;
  }
  
  public static int createIntVec4Init(FlatBufferBuilder builder, int x, int y, int z, int w) {
    builder.startObject(4);
    addW(builder, w);
    addZ(builder, z);
    addY(builder, y);
    addX(builder, x);
    return endIntVec4Init(builder);
  }
  
  public static void startIntVec4Init(FlatBufferBuilder builder) {
    builder.startObject(4);
  }
  
  public static void addX(FlatBufferBuilder builder, int x) {
    builder.addInt(0, x, 0);
  }
  
  public static void addY(FlatBufferBuilder builder, int y) {
    builder.addInt(1, y, 0);
  }
  
  public static void addZ(FlatBufferBuilder builder, int z) {
    builder.addInt(2, z, 0);
  }
  
  public static void addW(FlatBufferBuilder builder, int w) {
    builder.addInt(3, w, 0);
  }
  
  public static int endIntVec4Init(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
