package com.google.ar.sceneform;

/**
 * AR-Platform
 */
public class ARPlatForm {

    //default-ARCore
    static Type TYPE = Type.AR_CORE;

    public static boolean isArCore(){
        return TYPE == Type.AR_CORE;//1
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
         * Set the occlusion material.
         */
        OCCLUSION_ENABLED,
        /**
         * <pre>
         * This is the default value
         * </pre>
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
