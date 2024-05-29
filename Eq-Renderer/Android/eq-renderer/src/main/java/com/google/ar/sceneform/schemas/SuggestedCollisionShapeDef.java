package com.google.ar.sceneform.schemas;

import com.google.ar.sceneform.lull.Vec3;
import com.google.ar.sceneform.flatbuffers.FlatBufferBuilder;
import com.google.ar.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SuggestedCollisionShapeDef extends Table {
  public static SuggestedCollisionShapeDef getRootAsSuggestedCollisionShapeDef(ByteBuffer _bb) {
    return getRootAsSuggestedCollisionShapeDef(_bb, new SuggestedCollisionShapeDef());
  }
  
  public static SuggestedCollisionShapeDef getRootAsSuggestedCollisionShapeDef(ByteBuffer _bb, SuggestedCollisionShapeDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public SuggestedCollisionShapeDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public int type() {
    int o = __offset(4);
    return (o != 0) ? this.bb.getInt(o + this.bb_pos) : 0;
  }
  
  public Vec3 center() {
    return center(new Vec3());
  }
  
  public Vec3 center(Vec3 obj) {
    int o = __offset(6);
    return (o != 0) ? obj.__assign(o + this.bb_pos, this.bb) : null;
  }
  
  public Vec3 size() {
    return size(new Vec3());
  }
  
  public Vec3 size(Vec3 obj) {
    int o = __offset(8);
    return (o != 0) ? obj.__assign(o + this.bb_pos, this.bb) : null;
  }
  
  public static void startSuggestedCollisionShapeDef(FlatBufferBuilder builder) {
    builder.startObject(3);
  }
  
  public static void addType(FlatBufferBuilder builder, int type) {
    builder.addInt(0, type, 0);
  }
  
  public static void addCenter(FlatBufferBuilder builder, int centerOffset) {
    builder.addStruct(1, centerOffset, 0);
  }
  
  public static void addSize(FlatBufferBuilder builder, int sizeOffset) {
    builder.addStruct(2, sizeOffset, 0);
  }
  
  public static int endSuggestedCollisionShapeDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
