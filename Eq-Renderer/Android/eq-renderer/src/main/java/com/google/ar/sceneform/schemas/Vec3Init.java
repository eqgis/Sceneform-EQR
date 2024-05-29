package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Vec3Init extends Table {
  public static Vec3Init getRootAsVec3Init(ByteBuffer _bb) {
    return getRootAsVec3Init(_bb, new Vec3Init());
  }
  
  public static Vec3Init getRootAsVec3Init(ByteBuffer _bb, Vec3Init obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public Vec3Init __assign(int _i, ByteBuffer _bb) {
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
  
  public static int createVec3Init(FlatBufferBuilder builder, float x, float y, float z) {
    builder.startObject(3);
    addZ(builder, z);
    addY(builder, y);
    addX(builder, x);
    return endVec3Init(builder);
  }
  
  public static void startVec3Init(FlatBufferBuilder builder) {
    builder.startObject(3);
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
  
  public static int endVec3Init(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
