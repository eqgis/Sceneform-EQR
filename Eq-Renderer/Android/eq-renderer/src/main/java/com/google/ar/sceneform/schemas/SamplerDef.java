package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SamplerDef extends Table {
  public static SamplerDef getRootAsSamplerDef(ByteBuffer _bb) {
    return getRootAsSamplerDef(_bb, new SamplerDef());
  }
  
  public static SamplerDef getRootAsSamplerDef(ByteBuffer _bb, SamplerDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public SamplerDef __assign(int _i, ByteBuffer _bb) {
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
  
  public String file() {
    int o = __offset(6);
    return (o != 0) ? __string(o + this.bb_pos) : null;
  }
  
  public ByteBuffer fileAsByteBuffer() {
    return __vector_as_bytebuffer(6, 1);
  }
  
  public ByteBuffer fileInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 6, 1);
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
  
  public SamplerParamsDef params() {
    return params(new SamplerParamsDef());
  }
  
  public SamplerParamsDef params(SamplerParamsDef obj) {
    int o = __offset(10);
    return (o != 0) ? obj.__assign(__indirect(o + this.bb_pos), this.bb) : null;
  }
  
  public static int createSamplerDef(FlatBufferBuilder builder, int nameOffset, int fileOffset, int dataOffset, int paramsOffset) {
    builder.startObject(4);
    addParams(builder, paramsOffset);
    addData(builder, dataOffset);
    addFile(builder, fileOffset);
    addName(builder, nameOffset);
    return endSamplerDef(builder);
  }
  
  public static void startSamplerDef(FlatBufferBuilder builder) {
    builder.startObject(4);
  }
  
  public static void addName(FlatBufferBuilder builder, int nameOffset) {
    builder.addOffset(0, nameOffset, 0);
  }
  
  public static void addFile(FlatBufferBuilder builder, int fileOffset) {
    builder.addOffset(1, fileOffset, 0);
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
  
  public static void addParams(FlatBufferBuilder builder, int paramsOffset) {
    builder.addOffset(3, paramsOffset, 0);
  }
  
  public static int endSamplerDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
