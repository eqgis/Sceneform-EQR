package com.google.ar.sceneform.schemas;

public final class RenderFlags {
  public static final byte DoNotCastShadows = 1;
  
  public static final byte DoNotReceiveShadows = 2;
  
  public static final byte DisableFrustumCulling = 4;
  
  public static final String[] names = new String[] { "DoNotCastShadows", "DoNotReceiveShadows", "", "DisableFrustumCulling" };
  
  public static String name(int e) {
    return names[e - 1];
  }
}
