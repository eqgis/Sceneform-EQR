package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RuntimeAssetDef extends Table {
  public static RuntimeAssetDef getRootAsRuntimeAssetDef(ByteBuffer _bb) {
    return getRootAsRuntimeAssetDef(_bb, new RuntimeAssetDef());
  }
  
  public static RuntimeAssetDef getRootAsRuntimeAssetDef(ByteBuffer _bb, RuntimeAssetDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public RuntimeAssetDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public int renderFlags() {
    int o = __offset(4);
    return (o != 0) ? (this.bb.get(o + this.bb_pos) & 0xFF) : 0;
  }
  
  public int renderPriority() {
    int o = __offset(6);
    return (o != 0) ? (this.bb.get(o + this.bb_pos) & 0xFF) : 4;
  }
  
  public static int createRuntimeAssetDef(FlatBufferBuilder builder, int render_flags, int render_priority) {
    builder.startObject(2);
    addRenderPriority(builder, render_priority);
    addRenderFlags(builder, render_flags);
    return endRuntimeAssetDef(builder);
  }
  
  public static void startRuntimeAssetDef(FlatBufferBuilder builder) {
    builder.startObject(2);
  }
  
  public static void addRenderFlags(FlatBufferBuilder builder, int renderFlags) {
    builder.addByte(0, (byte)renderFlags, 0);
  }
  
  public static void addRenderPriority(FlatBufferBuilder builder, int renderPriority) {
    builder.addByte(1, (byte)renderPriority, 4);
  }
  
  public static int endRuntimeAssetDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
