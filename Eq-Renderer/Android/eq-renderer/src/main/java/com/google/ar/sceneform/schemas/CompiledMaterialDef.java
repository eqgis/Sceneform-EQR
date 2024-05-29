package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CompiledMaterialDef extends Table {
  public static CompiledMaterialDef getRootAsCompiledMaterialDef(ByteBuffer _bb) {
    return getRootAsCompiledMaterialDef(_bb, new CompiledMaterialDef());
  }
  
  public static CompiledMaterialDef getRootAsCompiledMaterialDef(ByteBuffer _bb, CompiledMaterialDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public CompiledMaterialDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public int compiledMaterial(int j) {
    int o = __offset(4);
    return (o != 0) ? (this.bb.get(__vector(o) + j * 1) & 0xFF) : 0;
  }
  
  public int compiledMaterialLength() {
    int o = __offset(4);
    return (o != 0) ? __vector_len(o) : 0;
  }
  
  public ByteBuffer compiledMaterialAsByteBuffer() {
    return __vector_as_bytebuffer(4, 1);
  }
  
  public ByteBuffer compiledMaterialInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 4, 1);
  }
  
  public String sha1sum() {
    int o = __offset(6);
    return (o != 0) ? __string(o + this.bb_pos) : null;
  }
  
  public ByteBuffer sha1sumAsByteBuffer() {
    return __vector_as_bytebuffer(6, 1);
  }
  
  public ByteBuffer sha1sumInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 6, 1);
  }
  
  public CompiledMaterialDeclDef decl() {
    return decl(new CompiledMaterialDeclDef());
  }
  
  public CompiledMaterialDeclDef decl(CompiledMaterialDeclDef obj) {
    int o = __offset(8);
    return (o != 0) ? obj.__assign(__indirect(o + this.bb_pos), this.bb) : null;
  }
  
  public String compressedMaterial() {
    int o = __offset(10);
    return (o != 0) ? __string(o + this.bb_pos) : null;
  }
  
  public ByteBuffer compressedMaterialAsByteBuffer() {
    return __vector_as_bytebuffer(10, 1);
  }
  
  public ByteBuffer compressedMaterialInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 10, 1);
  }
  
  public static int createCompiledMaterialDef(FlatBufferBuilder builder, int compiled_materialOffset, int sha1sumOffset, int declOffset, int compressed_materialOffset) {
    builder.startObject(4);
    addCompressedMaterial(builder, compressed_materialOffset);
    addDecl(builder, declOffset);
    addSha1sum(builder, sha1sumOffset);
    addCompiledMaterial(builder, compiled_materialOffset);
    return endCompiledMaterialDef(builder);
  }
  
  public static void startCompiledMaterialDef(FlatBufferBuilder builder) {
    builder.startObject(4);
  }
  
  public static void addCompiledMaterial(FlatBufferBuilder builder, int compiledMaterialOffset) {
    builder.addOffset(0, compiledMaterialOffset, 0);
  }
  
  public static int createCompiledMaterialVector(FlatBufferBuilder builder, byte[] data) {
    return builder.createByteVector(data);
  }
  
  public static int createCompiledMaterialVector(FlatBufferBuilder builder, ByteBuffer data) {
    return builder.createByteVector(data);
  }
  
  public static void startCompiledMaterialVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(1, numElems, 1);
  }
  
  public static void addSha1sum(FlatBufferBuilder builder, int sha1sumOffset) {
    builder.addOffset(1, sha1sumOffset, 0);
  }
  
  public static void addDecl(FlatBufferBuilder builder, int declOffset) {
    builder.addOffset(2, declOffset, 0);
  }
  
  public static void addCompressedMaterial(FlatBufferBuilder builder, int compressedMaterialOffset) {
    builder.addOffset(3, compressedMaterialOffset, 0);
  }
  
  public static int endCompiledMaterialDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
