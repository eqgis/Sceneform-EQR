package com.eqgis.sceneform.schemas;

public final class ParameterInitDefType {
  public static final byte NONE = 0;
  
  public static final byte NullInit = 1;
  
  public static final byte ScalarInit = 2;
  
  public static final byte Vec3Init = 3;
  
  public static final byte Vec4Init = 4;
  
  public static final byte SamplerInit = 5;
  
  public static final byte Vec2Init = 6;
  
  public static final byte BoolInit = 7;
  
  public static final byte BoolVec2Init = 8;
  
  public static final byte BoolVec3Init = 9;
  
  public static final byte BoolVec4Init = 10;
  
  public static final byte IntInit = 11;
  
  public static final byte IntVec2Init = 12;
  
  public static final byte IntVec3Init = 13;
  
  public static final byte IntVec4Init = 14;
  
  public static final byte CubemapSamplerInit = 15;
  
  public static final byte ExternalSamplerInit = 16;
  
  public static final byte DoubleInit = 17;
  
  public static final byte DoubleVec2Init = 18;
  
  public static final byte DoubleVec3Init = 19;
  
  public static final byte DoubleVec4Init = 20;
  
  public static final String[] names = new String[] { 
      "NONE", "NullInit", "ScalarInit", "Vec3Init", "Vec4Init", "SamplerInit", "Vec2Init", "BoolInit", "BoolVec2Init", "BoolVec3Init", 
      "BoolVec4Init", "IntInit", "IntVec2Init", "IntVec3Init", "IntVec4Init", "CubemapSamplerInit", "ExternalSamplerInit", "DoubleInit", "DoubleVec2Init", "DoubleVec3Init", 
      "DoubleVec4Init" };
  
  public static String name(int e) {
    return names[e];
  }
}
