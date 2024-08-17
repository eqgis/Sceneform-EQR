package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class InputDef extends Table {
  public static InputDef getRootAsInputDef(ByteBuffer _bb) {
    return getRootAsInputDef(_bb, new InputDef());
  }
  
  public static InputDef getRootAsInputDef(ByteBuffer _bb, InputDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public InputDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public String path() {
    int o = __offset(4);
    return (o != 0) ? __string(o + this.bb_pos) : null;
  }
  
  public ByteBuffer pathAsByteBuffer() {
    return __vector_as_bytebuffer(4, 1);
  }
  
  public ByteBuffer pathInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 4, 1);
  }
  
  public String hash() {
    int o = __offset(6);
    return (o != 0) ? __string(o + this.bb_pos) : null;
  }
  
  public ByteBuffer hashAsByteBuffer() {
    return __vector_as_bytebuffer(6, 1);
  }
  
  public ByteBuffer hashInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 6, 1);
  }
  
  public static int createInputDef(FlatBufferBuilder builder, int pathOffset, int hashOffset) {
    builder.startObject(2);
    addHash(builder, hashOffset);
    addPath(builder, pathOffset);
    return endInputDef(builder);
  }
  
  public static void startInputDef(FlatBufferBuilder builder) {
    builder.startObject(2);
  }
  
  public static void addPath(FlatBufferBuilder builder, int pathOffset) {
    builder.addOffset(0, pathOffset, 0);
  }
  
  public static void addHash(FlatBufferBuilder builder, int hashOffset) {
    builder.addOffset(1, hashOffset, 0);
  }
  
  public static int endInputDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
