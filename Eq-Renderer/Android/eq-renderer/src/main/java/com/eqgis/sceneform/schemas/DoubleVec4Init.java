package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DoubleVec4Init extends Table {
  public static DoubleVec4Init getRootAsDoubleVec4Init(ByteBuffer _bb) {
    return getRootAsDoubleVec4Init(_bb, new DoubleVec4Init());
  }
  
  public static DoubleVec4Init getRootAsDoubleVec4Init(ByteBuffer _bb, DoubleVec4Init obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public DoubleVec4Init __assign(int _i, ByteBuffer _bb) {
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
  
  public double w() {
    int o = __offset(10);
    return (o != 0) ? this.bb.getDouble(o + this.bb_pos) : 0.0D;
  }
  
  public static int createDoubleVec4Init(FlatBufferBuilder builder, double x, double y, double z, double w) {
    builder.startObject(4);
    addW(builder, w);
    addZ(builder, z);
    addY(builder, y);
    addX(builder, x);
    return endDoubleVec4Init(builder);
  }
  
  public static void startDoubleVec4Init(FlatBufferBuilder builder) {
    builder.startObject(4);
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
  
  public static void addW(FlatBufferBuilder builder, double w) {
    builder.addDouble(3, w, 0.0D);
  }
  
  public static int endDoubleVec4Init(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
