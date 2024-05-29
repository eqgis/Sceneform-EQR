package com.google.ar.sceneform.schemas;

public final class SamplerUsageType {
  public static final short Color = 0;
  
  public static final short Normal = 1;
  
  public static final short Data = 2;
  
  public static final short Lookup = 3;
  
  public static final String[] names = new String[] { "Color", "Normal", "Data", "Lookup" };
  
  public static String name(int e) {
    return names[e];
  }
}
