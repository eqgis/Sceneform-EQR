package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class BoolInit extends Table {
  public static BoolInit getRootAsBoolInit(ByteBuffer _bb) {
    return getRootAsBoolInit(_bb, new BoolInit());
  }
  
  public static BoolInit getRootAsBoolInit(ByteBuffer _bb, BoolInit obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public BoolInit __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public boolean value() {
    int o = __offset(4);
    return (o != 0) ? ((0 != this.bb.get(o + this.bb_pos))) : false;
  }
  
  public static int createBoolInit(FlatBufferBuilder builder, boolean value) {
    builder.startObject(1);
    addValue(builder, value);
    return endBoolInit(builder);
  }
  
  public static void startBoolInit(FlatBufferBuilder builder) {
    builder.startObject(1);
  }
  
  public static void addValue(FlatBufferBuilder builder, boolean value) {
    builder.addBoolean(0, value, false);
  }
  
  public static int endBoolInit(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
