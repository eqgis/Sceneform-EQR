package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class LightingCubeFaceDef extends Table {
  public static LightingCubeFaceDef getRootAsLightingCubeFaceDef(ByteBuffer _bb) {
    return getRootAsLightingCubeFaceDef(_bb, new LightingCubeFaceDef());
  }
  
  public static LightingCubeFaceDef getRootAsLightingCubeFaceDef(ByteBuffer _bb, LightingCubeFaceDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public LightingCubeFaceDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public int data(int j) {
    int o = __offset(4);
    return (o != 0) ? (this.bb.get(__vector(o) + j * 1) & 0xFF) : 0;
  }
  
  public int dataLength() {
    int o = __offset(4);
    return (o != 0) ? __vector_len(o) : 0;
  }
  
  public ByteBuffer dataAsByteBuffer() {
    return __vector_as_bytebuffer(4, 1);
  }
  
  public ByteBuffer dataInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 4, 1);
  }
  
  public static int createLightingCubeFaceDef(FlatBufferBuilder builder, int dataOffset) {
    builder.startObject(1);
    addData(builder, dataOffset);
    return endLightingCubeFaceDef(builder);
  }
  
  public static void startLightingCubeFaceDef(FlatBufferBuilder builder) {
    builder.startObject(1);
  }
  
  public static void addData(FlatBufferBuilder builder, int dataOffset) {
    builder.addOffset(0, dataOffset, 0);
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
  
  public static int endLightingCubeFaceDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
