package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DoubleVec2Init extends Table {
  public static DoubleVec2Init getRootAsDoubleVec2Init(ByteBuffer _bb) {
    return getRootAsDoubleVec2Init(_bb, new DoubleVec2Init());
  }
  
  public static DoubleVec2Init getRootAsDoubleVec2Init(ByteBuffer _bb, DoubleVec2Init obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public DoubleVec2Init __assign(int _i, ByteBuffer _bb) {
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
  
  public static int createDoubleVec2Init(FlatBufferBuilder builder, double x, double y) {
    builder.startObject(2);
    addY(builder, y);
    addX(builder, x);
    return endDoubleVec2Init(builder);
  }
  
  public static void startDoubleVec2Init(FlatBufferBuilder builder) {
    builder.startObject(2);
  }
  
  public static void addX(FlatBufferBuilder builder, double x) {
    builder.addDouble(0, x, 0.0D);
  }
  
  public static void addY(FlatBufferBuilder builder, double y) {
    builder.addDouble(1, y, 0.0D);
  }
  
  public static int endDoubleVec2Init(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
