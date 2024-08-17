package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SamplerParamsDef extends Table {
  public static SamplerParamsDef getRootAsSamplerParamsDef(ByteBuffer _bb) {
    return getRootAsSamplerParamsDef(_bb, new SamplerParamsDef());
  }
  
  public static SamplerParamsDef getRootAsSamplerParamsDef(ByteBuffer _bb, SamplerParamsDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public SamplerParamsDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public int usageType() {
    int o = __offset(4);
    return (o != 0) ? (this.bb.getShort(o + this.bb_pos) & 0xFFFF) : 0;
  }
  
  public int magFilter() {
    int o = __offset(6);
    return (o != 0) ? (this.bb.getShort(o + this.bb_pos) & 0xFFFF) : 0;
  }
  
  public int minFilter() {
    int o = __offset(8);
    return (o != 0) ? (this.bb.getShort(o + this.bb_pos) & 0xFFFF) : 0;
  }
  
  public int wrapS() {
    int o = __offset(10);
    return (o != 0) ? (this.bb.getShort(o + this.bb_pos) & 0xFFFF) : 0;
  }
  
  public int wrapT() {
    int o = __offset(12);
    return (o != 0) ? (this.bb.getShort(o + this.bb_pos) & 0xFFFF) : 0;
  }
  
  public int wrapR() {
    int o = __offset(14);
    return (o != 0) ? (this.bb.getShort(o + this.bb_pos) & 0xFFFF) : 0;
  }
  
  public int anisotropyLog2() {
    int o = __offset(16);
    return (o != 0) ? (this.bb.get(o + this.bb_pos) & 0xFF) : 0;
  }
  
  public int compareMode() {
    int o = __offset(18);
    return (o != 0) ? (this.bb.getShort(o + this.bb_pos) & 0xFFFF) : 0;
  }
  
  public int compareFunc() {
    int o = __offset(20);
    return (o != 0) ? (this.bb.getShort(o + this.bb_pos) & 0xFFFF) : 0;
  }
  
  public static int createSamplerParamsDef(FlatBufferBuilder builder, int usage_type, int mag_filter, int min_filter, int wrap_s, int wrap_t, int wrap_r, int anisotropy_log2, int compare_mode, int compare_func) {
    builder.startObject(9);
    addCompareFunc(builder, compare_func);
    addCompareMode(builder, compare_mode);
    addWrapR(builder, wrap_r);
    addWrapT(builder, wrap_t);
    addWrapS(builder, wrap_s);
    addMinFilter(builder, min_filter);
    addMagFilter(builder, mag_filter);
    addUsageType(builder, usage_type);
    addAnisotropyLog2(builder, anisotropy_log2);
    return endSamplerParamsDef(builder);
  }
  
  public static void startSamplerParamsDef(FlatBufferBuilder builder) {
    builder.startObject(9);
  }
  
  public static void addUsageType(FlatBufferBuilder builder, int usageType) {
    builder.addShort(0, (short)usageType, 0);
  }
  
  public static void addMagFilter(FlatBufferBuilder builder, int magFilter) {
    builder.addShort(1, (short)magFilter, 0);
  }
  
  public static void addMinFilter(FlatBufferBuilder builder, int minFilter) {
    builder.addShort(2, (short)minFilter, 0);
  }
  
  public static void addWrapS(FlatBufferBuilder builder, int wrapS) {
    builder.addShort(3, (short)wrapS, 0);
  }
  
  public static void addWrapT(FlatBufferBuilder builder, int wrapT) {
    builder.addShort(4, (short)wrapT, 0);
  }
  
  public static void addWrapR(FlatBufferBuilder builder, int wrapR) {
    builder.addShort(5, (short)wrapR, 0);
  }
  
  public static void addAnisotropyLog2(FlatBufferBuilder builder, int anisotropyLog2) {
    builder.addByte(6, (byte)anisotropyLog2, 0);
  }
  
  public static void addCompareMode(FlatBufferBuilder builder, int compareMode) {
    builder.addShort(7, (short)compareMode, 0);
  }
  
  public static void addCompareFunc(FlatBufferBuilder builder, int compareFunc) {
    builder.addShort(8, (short)compareFunc, 0);
  }
  
  public static int endSamplerParamsDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
