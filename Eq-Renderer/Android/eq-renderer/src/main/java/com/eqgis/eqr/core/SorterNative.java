package com.eqgis.eqr.core;

/**
 * @author tanyx 2026/1/12
 * @version 1.0
 **/
public class SorterNative {

    /**
     * 实测，20w点以内，JNI慢于Java
     */
    public static native void nSortByModelAndCameraMatrix(float[] centers,float[] nodeModelMat, float[] cameraModelMat,int[] out);
}
