package com.eqgis.sceneform.schemas;

import com.eqgis.sceneform.flatbuffers.FlatBufferBuilder;
import com.eqgis.sceneform.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class LightingCubeDef extends Table {
  public static LightingCubeDef getRootAsLightingCubeDef(ByteBuffer _bb) {
    return getRootAsLightingCubeDef(_bb, new LightingCubeDef());
  }
  
  public static LightingCubeDef getRootAsLightingCubeDef(ByteBuffer _bb, LightingCubeDef obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
  }
  
  public void __init(int _i, ByteBuffer _bb) {
    this.bb_pos = _i;
    this.bb = _bb;
    this.vtable_start = this.bb_pos - this.bb.getInt(this.bb_pos);
    this.vtable_size = this.bb.getShort(this.vtable_start);
  }
  
  public LightingCubeDef __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }
  
  public LightingCubeFaceDef faces(int j) {
    return faces(new LightingCubeFaceDef(), j);
  }
  
  public LightingCubeFaceDef faces(LightingCubeFaceDef obj, int j) {
    int o = __offset(4);
    return (o != 0) ? obj.__assign(__indirect(__vector(o) + j * 4), this.bb) : null;
  }
  
  public int facesLength() {
    int o = __offset(4);
    return (o != 0) ? __vector_len(o) : 0;
  }
  
  public static int createLightingCubeDef(FlatBufferBuilder builder, int facesOffset) {
    builder.startObject(1);
    addFaces(builder, facesOffset);
    return endLightingCubeDef(builder);
  }
  
  public static void startLightingCubeDef(FlatBufferBuilder builder) {
    builder.startObject(1);
  }
  
  public static void addFaces(FlatBufferBuilder builder, int facesOffset) {
    builder.addOffset(0, facesOffset, 0);
  }
  
  public static int createFacesVector(FlatBufferBuilder builder, int[] data) {
    builder.startVector(4, data.length, 4);
    for (int i = data.length - 1; i >= 0; ) {
      builder.addOffset(data[i]);
      i--;
    } 
    return builder.endVector();
  }
  
  public static void startFacesVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(4, numElems, 4);
  }
  
  public static int endLightingCubeDef(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
