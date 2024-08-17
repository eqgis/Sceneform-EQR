package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;
import com.eqgis.sceneform.lull.Vec3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class TransformDef extends Table {
  public static TransformDef getRootAsTransformDef(ByteBuffer _bb) {
    return getRootAsTransformDef(_bb, new TransformDef());
  }
  
  public static TransformDef getRootAsTransformDef(ByteBuffer _bb, TransformDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public TransformDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public float scale() {
    int o = __offset(4);
    return (o != 0) ? this.bb.getFloat(o + this.bb_pos) : 0.0F;
  }
  
  public Vec3 offset() {
    return offset(new Vec3());
  }
  
  public Vec3 offset(Vec3 obj) {
    int o = __offset(6);
    return (o != 0) ? obj.__assign(o + this.bb_pos, this.bb) : null;
  }
  
  public static void startTransformDef(FlatBufferBuilder builder) {
    builder.startObject(2);
  }
  
  public static void addScale(FlatBufferBuilder builder, float scale) {
    builder.addFloat(0, scale, 0.0D);
  }
  
  public static void addOffset(FlatBufferBuilder builder, int offsetOffset) {
    builder.addStruct(1, offsetOffset, 0);
  }
  
  public static int endTransformDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
