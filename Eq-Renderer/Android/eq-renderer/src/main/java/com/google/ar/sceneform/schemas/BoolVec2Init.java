package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class BoolVec2Init extends Table {
  public static BoolVec2Init getRootAsBoolVec2Init(ByteBuffer _bb) {
    return getRootAsBoolVec2Init(_bb, new BoolVec2Init());
  }
  
  public static BoolVec2Init getRootAsBoolVec2Init(ByteBuffer _bb, BoolVec2Init obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public BoolVec2Init __assign(int _i, ByteBuffer _bb) {
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
  
  public static int createBoolVec2Init(FlatBufferBuilder builder, boolean x, boolean y) {
    builder.startObject(2);
    addY(builder, y);
    addX(builder, x);
    return endBoolVec2Init(builder);
  }
  
  public static void startBoolVec2Init(FlatBufferBuilder builder) {
    builder.startObject(2);
  }
  
  public static void addX(FlatBufferBuilder builder, boolean x) {
    builder.addBoolean(0, x, false);
  }
  
  public static void addY(FlatBufferBuilder builder, boolean y) {
    builder.addBoolean(1, y, false);
  }
  
  public static int endBoolVec2Init(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
