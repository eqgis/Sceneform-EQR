package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DoubleVec3Init extends Table {
  public static DoubleVec3Init getRootAsDoubleVec3Init(ByteBuffer _bb) {
    return getRootAsDoubleVec3Init(_bb, new DoubleVec3Init());
  }
  
  public static DoubleVec3Init getRootAsDoubleVec3Init(ByteBuffer _bb, DoubleVec3Init obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public DoubleVec3Init __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public double x() {
    int o = __offset(4);
    return (o != 0) ? this.bb.getDouble(o + this.bb_pos) : 0.0D;
  }
  
  public double y() {
    int o = __offset(6);
    return (o != 0) ? this.bb.getDouble(o + this.bb_pos) : 0.0D;
  }
  
  public double z() {
    int o = __offset(8);
    return (o != 0) ? this.bb.getDouble(o + this.bb_pos) : 0.0D;
  }
  
  public static int createDoubleVec3Init(FlatBufferBuilder builder, double x, double y, double z) {
    builder.startObject(3);
    addZ(builder, z);
    addY(builder, y);
    addX(builder, x);
    return endDoubleVec3Init(builder);
  }
  
  public static void startDoubleVec3Init(FlatBufferBuilder builder) {
    builder.startObject(3);
  }
  
  public static void addX(FlatBufferBuilder builder, double x) {
    builder.addDouble(0, x, 0.0D);
  }
  
  public static void addY(FlatBufferBuilder builder, double y) {
    builder.addDouble(1, y, 0.0D);
  }
  
  public static void addZ(FlatBufferBuilder builder, double z) {
    builder.addDouble(2, z, 0.0D);
  }
  
  public static int endDoubleVec3Init(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
