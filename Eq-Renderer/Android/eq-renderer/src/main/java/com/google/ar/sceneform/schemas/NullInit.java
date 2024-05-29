package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class NullInit extends Table {
  public static NullInit getRootAsNullInit(ByteBuffer _bb) {
    return getRootAsNullInit(_bb, new NullInit());
  }
  
  public static NullInit getRootAsNullInit(ByteBuffer _bb, NullInit obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public NullInit __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public static void startNullInit(FlatBufferBuilder builder) {
    builder.startObject(0);
  }
  
  public static int endNullInit(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
