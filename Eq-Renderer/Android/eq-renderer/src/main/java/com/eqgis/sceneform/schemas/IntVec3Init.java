package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class IntVec3Init extends Table {
  public static IntVec3Init getRootAsIntVec3Init(ByteBuffer _bb) {
    return getRootAsIntVec3Init(_bb, new IntVec3Init());
  }
  
  public static IntVec3Init getRootAsIntVec3Init(ByteBuffer _bb, IntVec3Init obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public IntVec3Init __assign(int _i, ByteBuffer _bb) {
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
  
  public static int createIntVec3Init(FlatBufferBuilder builder, int x, int y, int z) {
    builder.startObject(3);
    addZ(builder, z);
    addY(builder, y);
    addX(builder, x);
    return endIntVec3Init(builder);
  }
  
  public static void startIntVec3Init(FlatBufferBuilder builder) {
    builder.startObject(3);
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
  
  public static int endIntVec3Init(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
