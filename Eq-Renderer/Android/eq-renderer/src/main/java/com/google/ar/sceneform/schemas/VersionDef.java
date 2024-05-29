package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class VersionDef extends Table {
  public static VersionDef getRootAsVersionDef(ByteBuffer _bb) {
    return getRootAsVersionDef(_bb, new VersionDef());
  }
  
  public static VersionDef getRootAsVersionDef(ByteBuffer _bb, VersionDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public VersionDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public float majorVersion() {
    int o = __offset(4);
    return (o != 0) ? this.bb.getFloat(o + this.bb_pos) : 0.0F;
  }
  
  public int minorVersion() {
    int o = __offset(6);
    return (o != 0) ? this.bb.getInt(o + this.bb_pos) : 0;
  }
  
  public static int createVersionDef(FlatBufferBuilder builder, float major_version, int minor_version) {
    builder.startObject(2);
    addMinorVersion(builder, minor_version);
    addMajorVersion(builder, major_version);
    return endVersionDef(builder);
  }
  
  public static void startVersionDef(FlatBufferBuilder builder) {
    builder.startObject(2);
  }
  
  public static void addMajorVersion(FlatBufferBuilder builder, float majorVersion) {
    builder.addFloat(0, majorVersion, 0.0D);
  }
  
  public static void addMinorVersion(FlatBufferBuilder builder, int minorVersion) {
    builder.addInt(1, minorVersion, 0);
  }
  
  public static int endVersionDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
