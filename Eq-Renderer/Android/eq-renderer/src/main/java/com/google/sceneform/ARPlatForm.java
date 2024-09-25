package com.google.sceneform;

/**
 * AR-Platform
 */
public class ARPlatForm {

    //default-ARCore
    static Type TYPE = Type.AR_CORE;

    public static boolean isArCoreOrNone(){
        //None是为了兼容CameraStream中采用背景纹理的情况
        return TYPE == Type.AR_CORE || TYPE == Type.NONE;//1
    }

    public static boolean isNone(){
        return TYPE == Type.NONE;//1
    }

    public static boolean isArEngine(){
        return TYPE == Type.AR_ENGINE;//2
    }

    public static Type getEngineType() {
        return TYPE;//1
    }

    /**
     * @hide
     * @param type
     */
    public static void setType(Type type){
        TYPE = type;
    }

    public enum Type{
        /**
         * ARCore
         */
        AR_CORE,
        /**
         * AREngine
         */
        AR_ENGINE,
        /**
         * None
         */
        NONE
    }

    //added by Ikkyu 2022/01/18
    public static OcclusionMode OCCLUSION_MODE = OcclusionMode.OCCLUSION_DISABLED;
    public enum OcclusionMode {
        /**
         * 启用遮挡
         * <p>AR实挡虚时启用，需要结合深度API</p>
         */
        OCCLUSION_ENABLED,
        /**
         * 禁用遮挡
         */
        OCCLUSION_DISABLED
    }

    public static void setCustomOcclusionEnabled(boolean status){
        if (status){
            OCCLUSION_MODE = OcclusionMode.OCCLUSION_ENABLED;
        }else {
            OCCLUSION_MODE = OcclusionMode.OCCLUSION_DISABLED;
        }
    }
    public static boolean isCustomOcclusionEnabled(){
        if (OCCLUSION_MODE == OcclusionMode.OCCLUSION_ENABLED){
            return true;
        }else {
            return false;
        }
    }
}
