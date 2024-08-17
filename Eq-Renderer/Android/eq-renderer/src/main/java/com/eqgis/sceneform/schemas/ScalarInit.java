package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ScalarInit extends Table {
  public static ScalarInit getRootAsScalarInit(ByteBuffer _bb) {
    return getRootAsScalarInit(_bb, new ScalarInit());
  }
  
  public static ScalarInit getRootAsScalarInit(ByteBuffer _bb, ScalarInit obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public ScalarInit __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public float value() {
    int o = __offset(4);
    return (o != 0) ? this.bb.getFloat(o + this.bb_pos) : 0.0F;
  }
  
  public static int createScalarInit(FlatBufferBuilder builder, float value) {
    builder.startObject(1);
    addValue(builder, value);
    return endScalarInit(builder);
  }
  
  public static void startScalarInit(FlatBufferBuilder builder) {
    builder.startObject(1);
  }
  
  public static void addValue(FlatBufferBuilder builder, float value) {
    builder.addFloat(0, value, 0.0D);
  }
  
  public static int endScalarInit(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
