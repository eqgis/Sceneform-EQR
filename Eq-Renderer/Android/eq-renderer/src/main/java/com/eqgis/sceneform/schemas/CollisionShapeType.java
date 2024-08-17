package com.eqgis.sceneform.schemas;

public final class CollisionShapeType {
  public static final int Box = 0;
  
  public static final int Sphere = 1;
  
  public static final String[] names = new String[] { "Box", "Sphere" };
  
  public static String name(int e) {
    return names[e];
  }
}
