package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CompiledMaterialDeclDef extends Table {
  public static CompiledMaterialDeclDef getRootAsCompiledMaterialDeclDef(ByteBuffer _bb) {
    return getRootAsCompiledMaterialDeclDef(_bb, new CompiledMaterialDeclDef());
  }
  
  public static CompiledMaterialDeclDef getRootAsCompiledMaterialDeclDef(ByteBuffer _bb, CompiledMaterialDeclDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public CompiledMaterialDeclDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public String source() {
    int o = __offset(4);
    return (o != 0) ? __string(o + this.bb_pos) : null;
  }
  
  public ByteBuffer sourceAsByteBuffer() {
    return __vector_as_bytebuffer(4, 1);
  }
  
  public ByteBuffer sourceInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 4, 1);
  }
  
  public String matSha1sum() {
    int o = __offset(6);
    return (o != 0) ? __string(o + this.bb_pos) : null;
  }
  
  public ByteBuffer matSha1sumAsByteBuffer() {
    return __vector_as_bytebuffer(6, 1);
  }
  
  public ByteBuffer matSha1sumInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 6, 1);
  }
  
  public static int createCompiledMaterialDeclDef(FlatBufferBuilder builder, int sourceOffset, int mat_sha1sumOffset) {
    builder.startObject(2);
    addMatSha1sum(builder, mat_sha1sumOffset);
    addSource(builder, sourceOffset);
    return endCompiledMaterialDeclDef(builder);
  }
  
  public static void startCompiledMaterialDeclDef(FlatBufferBuilder builder) {
    builder.startObject(2);
  }
  
  public static void addSource(FlatBufferBuilder builder, int sourceOffset) {
    builder.addOffset(0, sourceOffset, 0);
  }
  
  public static void addMatSha1sum(FlatBufferBuilder builder, int matSha1sumOffset) {
    builder.addOffset(1, matSha1sumOffset, 0);
  }
  
  public static int endCompiledMaterialDeclDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
