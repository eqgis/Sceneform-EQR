package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Vec4Init extends Table {
  public static Vec4Init getRootAsVec4Init(ByteBuffer _bb) {
    return getRootAsVec4Init(_bb, new Vec4Init());
  }
  
  public static Vec4Init getRootAsVec4Init(ByteBuffer _bb, Vec4Init obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public Vec4Init __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public float x() {
    int o = __offset(4);
    return (o != 0) ? this.bb.getFloat(o + this.bb_pos) : 0.0F;
  }
  
  public float y() {
    int o = __offset(6);
    return (o != 0) ? this.bb.getFloat(o + this.bb_pos) : 0.0F;
  }
  
  public float z() {
    int o = __offset(8);
    return (o != 0) ? this.bb.getFloat(o + this.bb_pos) : 0.0F;
  }
  
  public float w() {
    int o = __offset(10);
    return (o != 0) ? this.bb.getFloat(o + this.bb_pos) : 0.0F;
  }
  
  public static int createVec4Init(FlatBufferBuilder builder, float x, float y, float z, float w) {
    builder.startObject(4);
    addW(builder, w);
    addZ(builder, z);
    addY(builder, y);
    addX(builder, x);
    return endVec4Init(builder);
  }
  
  public static void startVec4Init(FlatBufferBuilder builder) {
    builder.startObject(4);
  }
  
  public static void addX(FlatBufferBuilder builder, float x) {
    builder.addFloat(0, x, 0.0D);
  }
  
  public static void addY(FlatBufferBuilder builder, float y) {
    builder.addFloat(1, y, 0.0D);
  }
  
  public static void addZ(FlatBufferBuilder builder, float z) {
    builder.addFloat(2, z, 0.0D);
  }
  
  public static void addW(FlatBufferBuilder builder, float w) {
    builder.addFloat(3, w, 0.0D);
  }
  
  public static int endVec4Init(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
