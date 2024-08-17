package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class IntVec2Init extends Table {
  public static IntVec2Init getRootAsIntVec2Init(ByteBuffer _bb) {
    return getRootAsIntVec2Init(_bb, new IntVec2Init());
  }
  
  public static IntVec2Init getRootAsIntVec2Init(ByteBuffer _bb, IntVec2Init obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public IntVec2Init __assign(int _i, ByteBuffer _bb) {
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
  
  public static int createIntVec2Init(FlatBufferBuilder builder, int x, int y) {
    builder.startObject(2);
    addY(builder, y);
    addX(builder, x);
    return endIntVec2Init(builder);
  }
  
  public static void startIntVec2Init(FlatBufferBuilder builder) {
    builder.startObject(2);
  }
  
  public static void addX(FlatBufferBuilder builder, int x) {
    builder.addInt(0, x, 0);
  }
  
  public static void addY(FlatBufferBuilder builder, int y) {
    builder.addInt(1, y, 0);
  }
  
  public static int endIntVec2Init(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
