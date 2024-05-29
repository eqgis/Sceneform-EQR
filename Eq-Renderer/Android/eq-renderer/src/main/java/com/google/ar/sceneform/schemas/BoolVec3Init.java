package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class BoolVec3Init extends Table {
  public static BoolVec3Init getRootAsBoolVec3Init(ByteBuffer _bb) {
    return getRootAsBoolVec3Init(_bb, new BoolVec3Init());
  }
  
  public static BoolVec3Init getRootAsBoolVec3Init(ByteBuffer _bb, BoolVec3Init obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public BoolVec3Init __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public boolean x() {
    int o = __offset(4);
    return (o != 0) ? ((0 != this.bb.get(o + this.bb_pos))) : false;
  }
  
  public boolean y() {
    int o = __offset(6);
    return (o != 0) ? ((0 != this.bb.get(o + this.bb_pos))) : false;
  }
  
  public boolean z() {
    int o = __offset(8);
    return (o != 0) ? ((0 != this.bb.get(o + this.bb_pos))) : false;
  }
  
  public static int createBoolVec3Init(FlatBufferBuilder builder, boolean x, boolean y, boolean z) {
    builder.startObject(3);
    addZ(builder, z);
    addY(builder, y);
    addX(builder, x);
    return endBoolVec3Init(builder);
  }
  
  public static void startBoolVec3Init(FlatBufferBuilder builder) {
    builder.startObject(3);
  }
  
  public static void addX(FlatBufferBuilder builder, boolean x) {
    builder.addBoolean(0, x, false);
  }
  
  public static void addY(FlatBufferBuilder builder, boolean y) {
    builder.addBoolean(1, y, false);
  }
  
  public static void addZ(FlatBufferBuilder builder, boolean z) {
    builder.addBoolean(2, z, false);
  }
  
  public static int endBoolVec3Init(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
