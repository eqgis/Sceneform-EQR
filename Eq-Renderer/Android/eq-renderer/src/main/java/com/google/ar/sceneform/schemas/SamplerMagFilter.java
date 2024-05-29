package com.google.ar.sceneform.schemas;

public final class SamplerMagFilter {
  public static final short Nearest = 0;
  
  public static final short Linear = 1;
  
  public static final String[] names = new String[] { "Nearest", "Linear" };
  
  public static String name(int e) {
    return names[e];
  }
}
