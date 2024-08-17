package com.eqgis.sceneform.schemas;

public final class SamplerWrapMode {
  public static final short ClampToEdge = 0;
  
  public static final short Repeat = 1;
  
  public static final short MirroredRepeat = 2;
  
  public static final String[] names = new String[] { "ClampToEdge", "Repeat", "MirroredRepeat" };
  
  public static String name(int e) {
    return names[e];
  }
}
