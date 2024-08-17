package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CubemapSamplerInit extends Table {
  public static CubemapSamplerInit getRootAsCubemapSamplerInit(ByteBuffer _bb) {
    return getRootAsCubemapSamplerInit(_bb, new CubemapSamplerInit());
  }
  
  public static CubemapSamplerInit getRootAsCubemapSamplerInit(ByteBuffer _bb, CubemapSamplerInit obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public CubemapSamplerInit __assign(int _i, ByteBuffer _bb) {
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
  
  public int usage() {
    int o = __offset(6);
    return (o != 0) ? this.bb.getInt(o + this.bb_pos) : 0;
  }
  
  public static int createCubemapSamplerInit(FlatBufferBuilder builder, int pathOffset, int usage) {
    builder.startObject(2);
    addUsage(builder, usage);
    addPath(builder, pathOffset);
    return endCubemapSamplerInit(builder);
  }
  
  public static void startCubemapSamplerInit(FlatBufferBuilder builder) {
    builder.startObject(2);
  }
  
  public static void addPath(FlatBufferBuilder builder, int pathOffset) {
    builder.addOffset(0, pathOffset, 0);
  }
  
  public static void addUsage(FlatBufferBuilder builder, int usage) {
    builder.addInt(1, usage, 0);
  }
  
  public static int endCubemapSamplerInit(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
