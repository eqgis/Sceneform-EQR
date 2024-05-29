package com.google.ar.sceneform.schemas;

public final class SamplerCompareMode {
  public static final short None = 0;
  
  public static final short CompareToTexture = 1;
  
  public static final String[] names = new String[] { "None", "CompareToTexture" };
  
  public static String name(int e) {
    return names[e];
  }
}
