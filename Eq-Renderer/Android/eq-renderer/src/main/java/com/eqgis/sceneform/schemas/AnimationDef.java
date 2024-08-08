package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class AnimationDef extends Table {
  public static AnimationDef getRootAsAnimationDef(ByteBuffer _bb) {
    return getRootAsAnimationDef(_bb, new AnimationDef());
  }
  
  public static AnimationDef getRootAsAnimationDef(ByteBuffer _bb, AnimationDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public AnimationDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public String name() {
    int o = __offset(4);
    return (o != 0) ? __string(o + this.bb_pos) : null;
  }
  
  public ByteBuffer nameAsByteBuffer() {
    return __vector_as_bytebuffer(4, 1);
  }
  
  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 4, 1);
  }
  
  public int size() {
    int o = __offset(6);
    return (o != 0) ? this.bb.getInt(o + this.bb_pos) : 0;
  }
  
  public int data(int j) {
    int o = __offset(8);
    return (o != 0) ? (this.bb.get(__vector(o) + j * 1) & 0xFF) : 0;
  }
  
  public int dataLength() {
    int o = __offset(8);
    return (o != 0) ? __vector_len(o) : 0;
  }
  
  public ByteBuffer dataAsByteBuffer() {
    return __vector_as_bytebuffer(8, 1);
  }
  
  public ByteBuffer dataInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 8, 1);
  }
  
  public static int createAnimationDef(FlatBufferBuilder builder, int nameOffset, int size, int dataOffset) {
    builder.startObject(3);
    addData(builder, dataOffset);
    addSize(builder, size);
    addName(builder, nameOffset);
    return endAnimationDef(builder);
  }
  
  public static void startAnimationDef(FlatBufferBuilder builder) {
    builder.startObject(3);
  }
  
  public static void addName(FlatBufferBuilder builder, int nameOffset) {
    builder.addOffset(0, nameOffset, 0);
  }
  
  public static void addSize(FlatBufferBuilder builder, int size) {
    builder.addInt(1, size, 0);
  }
  
  public static void addData(FlatBufferBuilder builder, int dataOffset) {
    builder.addOffset(2, dataOffset, 0);
  }
  
  public static int createDataVector(FlatBufferBuilder builder, byte[] data) {
    return builder.createByteVector(data);
  }
  
  public static int createDataVector(FlatBufferBuilder builder, ByteBuffer data) {
    return builder.createByteVector(data);
  }
  
  public static void startDataVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(1, numElems, 1);
  }
  
  public static int endAnimationDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
