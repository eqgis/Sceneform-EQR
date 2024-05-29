package com.eqgis.eqr.core;

/**
 * EQ-Renderer
 * @version 1.0
 **/
public class Eqr {
    static {
        System.loadLibrary("eqr-core");
        boolean status = CoreNative.jni_CheckCoreStatus();
        if (!status){
            throw new IllegalStateException("had been expired.");
        }
    }

    /**
     * 获取核心库版本信息
     * @return String
     */
    public static String getCoreVersion(){
        return CoreNative.jni_GetVersion();
    }

    /**
     * 获取核心库的状态
     * @return boolean
     */
    public static boolean getCoreStatus(){
        return CoreNative.jni_CheckCoreStatus();
    }
}
