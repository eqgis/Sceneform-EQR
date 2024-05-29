package com.eqgis.ar;

import com.google.ar.core.CameraConfig;
import com.google.ar.core.CameraConfigFilter;
import com.google.ar.core.Config;
import com.huawei.hiar.ARConfigBase;
import com.huawei.hiar.ARWorldTrackingConfig;

import java.util.EnumSet;

/**
 * AR配置类
 * <p>ARCore采用Config，华为AREngine采用ARWorldTrackingConfig</p>
 * @author tanyx
 */
public class ARConfig {
    Config coreConfig = null;
    ARWorldTrackingConfig hwConfig = null;

    /**
     * 构造函数
     * @param arSession ARSession对象
     */
    public ARConfig(ARSession arSession){
        if (arSession.coreSession!=null){
            coreConfig = new Config(arSession.coreSession);
            CameraConfigFilter filter = new CameraConfigFilter(arSession.coreSession);
            filter.setTargetFps(EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30));//限制帧率为30FPS
            // Return only camera configs that will not use the depth sensor.
            filter.setDepthSensorUsage(EnumSet.of(CameraConfig.DepthSensorUsage.DO_NOT_USE));

        }else{
            hwConfig = new ARWorldTrackingConfig(arSession.hwSession);
            //设置输入预览流分辨率，默认分辨率为（1440x1080），此外还支持宽高比为4:3的物理相机预览分辨率。
            //https://developer.huawei.com/consumer/cn/doc/graphics-References/config_base-0000001050119488#section1231643615527
            hwConfig.setPreviewSize(1440,1080);
            hwConfig.setPowerMode(ARConfigBase.PowerMode.NORMAL);//POWER_SAVING(1)省电模式 || ULTRA_POWER_SAVING(2)超级省电模式 || PERFORMANCE_FIRST(3)性能优先
        }
        arSession.arConfig = this;
    }

    /**
     * 设置每一帧更新的更新模式
     * @param updateMode
     */
    public void setUpdateMode(UpdateMode updateMode) {
        if (coreConfig!=null){
            coreConfig.setUpdateMode( UpdateMode.toARCore( updateMode) );
        }else{
            hwConfig.setUpdateMode( UpdateMode.toHuaWei( updateMode ) );
        }
    }

    /**
     * 获取当前的更新模式
     * @return
     */
    public UpdateMode getUpdateMode() {
        if (coreConfig!=null){
            return UpdateMode.forARCore( coreConfig.getUpdateMode() );
        }else{
            return UpdateMode.forHuaWei( hwConfig.getUpdateMode() );
        }
    }

//    public void setPowerMode( PowerMode powerMode) {
//        coreConfig.getP
//    }
//
//    public PowerMode getPowerMode() {
//
//    }

    /**
     * 获取平面检测的模式
     * @return 模式
     */
    public PlaneFindingMode getPlaneFindingMode(){
        if (coreConfig!=null){
            return PlaneFindingMode.forARCore( coreConfig.getPlaneFindingMode() );
        }else{
            return PlaneFindingMode.forHuaWei( hwConfig.getPlaneFindingMode());
        }
    }

    /**
     * 设置平面检测的模式
     * @param mode 模式
     */
    public void setPlaneFindingMode(PlaneFindingMode mode){
        if (coreConfig!=null){
            coreConfig.setPlaneFindingMode( PlaneFindingMode.toARCore(mode) );
        }else{
            hwConfig.setPlaneFindingMode( PlaneFindingMode.toHuaWei(mode) );
        }
    }

    /**
     * 设置当前的对焦模式
     * @param focusMode 对焦模式
     */
    public void setFocusMode(FocusMode focusMode) {
        if (coreConfig!=null){
            coreConfig.setFocusMode( FocusMode.toARCore(focusMode) );
        }else{
            hwConfig.setFocusMode( FocusMode.toHuaWei( focusMode ) );
        }
    }

    /**
     * 获取当前的对焦模式
     * @return 对焦模式
     */
    public  FocusMode getFocusMode() {
        if (coreConfig!=null){
            return FocusMode.forARCore( coreConfig.getFocusMode() );
        }else{
            return FocusMode.forHuaWei( hwConfig.getFocusMode() );
        }
    }

//    public void setEnableItem(long enableItem) {
//
//    }
//
//    public long getEnableItem() {
//
//    }

//    public void setLightingMode(int lightingMode) {
//        if (coreConfig!=null){
//            coreConfig.setLightEstimationMode(lightingMode);
//        }else{
//            hwConfig.setLightingMode(lightingMode);
//        }
//    }


//    void setSession(com.huawei.hiar.ARSession session) {
//
//    }

//    public void setImageInputMode(ImageInputMode mode) {
//        coreConfig.setAugmentedImageDatabase()
//    }

//    public  ImageInputMode getImageInputMode() {
//
//    }

//    public List<ARConfigBase.SurfaceType> getImageInputSurfaceTypes() {
//         coreConfig.getS
//    }
//
//    public List<Surface> getImageInputSurfaces() {
//
//    }

//    public void setPreviewSize(int width, int height) {
//        coreConfig.
//    }

    /**
     * 设置AR增强图像数据库
     * @param augImgDatabase AR增强图像数据库
     */
    public void setAugmentedImageDatabase( ARAugmentedImageDatabase augImgDatabase) {
        if (coreConfig!=null){
            if (augImgDatabase==null){
                coreConfig.setAugmentedImageDatabase(null);
            }else{
                coreConfig.setAugmentedImageDatabase(augImgDatabase.coredatabase);
            }

        }else{
            if (augImgDatabase==null){
                hwConfig.setAugmentedImageDatabase(null);
            }else{
                hwConfig.setAugmentedImageDatabase(augImgDatabase.hwdatabase);
            }

        }
    }

    /**
     * 获取AR增强图像数据库
     * @return AR增强图像数据库
     */
     public ARAugmentedImageDatabase getAugmentedImageDatabase() {
        if (coreConfig!=null){
            com.google.ar.core.AugmentedImageDatabase database = coreConfig.getAugmentedImageDatabase();
            if (database==null)return null;
            return new ARAugmentedImageDatabase(database,null);
        }else {
            com.huawei.hiar.ARAugmentedImageDatabase database = hwConfig.getAugmentedImageDatabase();
            if (database==null)return null;
            return new ARAugmentedImageDatabase(null,database);
        }
    }

    /**
     * 开启深度模式
     * <p>（针对支持深度API的设备）启用后，可接收深度数据</p>
     */
    public void openDepth() {
        if (coreConfig!=null){
            coreConfig.setDepthMode(Config.DepthMode.AUTOMATIC);
        }else {
//            hwConfig.setde
            hwConfig.setEnableItem(ARConfigBase.ENABLE_MESH | ARConfigBase.ENABLE_DEPTH);
        }
    }

//    public void setSemanticMode(int mode) {
//
//        coreConfig.setm
//    }
//
//    public int getSemanticMode() {
//
//    }


//    long getFaceDetectMode() {
//        long var1 = this.mSession.mNativeHandle;
//        long var3 = this.mNativeHandle;
//        return this.nativeGetFaceDetectMode(var1, var3);
//    }
//
//    void setFaceDetectMode(long faceDetectMode) {
//        long var3 = this.mSession.mNativeHandle;
//        long var5 = this.mNativeHandle;
//        this.nativeSetFaceDetectMode(var3, var5, faceDetectMode);
//    }


    /**
     * 对焦模式
     */
    public static enum FocusMode {
        /**
         * 未知
         */
        UNKNOWN(-1),

        /**
         * 固定对焦
         */
        FIXED_FOCUS(0),

        /**
         * 自动对焦
         */
        AUTO_FOCUS(1);

        final int mNativeCode;

        private FocusMode(int nativeCode) {
            this.mNativeCode = nativeCode;
        }

        static  FocusMode forNumber(int nativeCode) {
             FocusMode[] var1;
            int var2 = (var1 = values()).length;

            for(int var3 = 0; var3 < var2; ++var3) {
                 FocusMode var4;
                if ((var4 = var1[var3]).mNativeCode == nativeCode) {
                    return var4;
                }
            }

            return UNKNOWN;
        }

        static  FocusMode forARCore(Config.FocusMode mode) {
            if (mode == Config.FocusMode.AUTO){
                return AUTO_FOCUS;
            }else if(mode == Config.FocusMode.FIXED){
                return FIXED_FOCUS;
            }
            return UNKNOWN;
        }

        static  Config.FocusMode toARCore( FocusMode mode) {
            if (mode == AUTO_FOCUS){
                return Config.FocusMode.AUTO;
            }else if(mode == FIXED_FOCUS){
                return Config.FocusMode.FIXED;
            }
            return Config.FocusMode.FIXED;
        }

        static  FocusMode forHuaWei(com.huawei.hiar.ARConfigBase.FocusMode mode) {
            if (mode == com.huawei.hiar.ARConfigBase.FocusMode.AUTO_FOCUS){
                return AUTO_FOCUS;
            }else if(mode == com.huawei.hiar.ARConfigBase.FocusMode.FIXED_FOCUS){
                return FIXED_FOCUS;
            }
            return UNKNOWN;
        }

        static  com.huawei.hiar.ARConfigBase.FocusMode toHuaWei(FocusMode mode) {
            if (mode ==  AUTO_FOCUS){
                return com.huawei.hiar.ARConfigBase.FocusMode.AUTO_FOCUS;
            }else if(mode ==  FIXED_FOCUS){
                return com.huawei.hiar.ARConfigBase.FocusMode.FIXED_FOCUS;
            }
            return com.huawei.hiar.ARConfigBase.FocusMode.UNKNOWN;
        }

    }

//    public static enum PowerMode {
//        UNKNOWN(-1),
//        NORMAL(0),
//        POWER_SAVING(1),
//        ULTRA_POWER_SAVING(2),
//        PERFORMANCE_FIRST;
//
//        final int mNativeCode;
//
//        private PowerMode(int nativeCode) {
//            this.mNativeCode = nativeCode;
//        }
//
//        static ARConfigBase.PowerMode forNumber(int nativeCode) {
//            ARConfigBase.PowerMode[] var1;
//            int var2 = (var1 = values()).length;
//
//            for(int var3 = 0; var3 < var2; ++var3) {
//                ARConfigBase.PowerMode var4;
//                if ((var4 = var1[var3]).mNativeCode == nativeCode) {
//                    return var4;
//                }
//            }
//
//            return UNKNOWN;
//        }
//
//        static {
//            ARConfigBase.PowerMode var0;
//            ARConfigBase.PowerMode var10000 = var0 = new ARConfigBase.PowerMode;
//            var10000.<init>(3);
//            PERFORMANCE_FIRST = var10000;
//        }
//    }

    public static enum UpdateMode {
        /**
         * 未知
         */
        UNKNOWN(-1),

        /**
         * ARsession的update()方法在新的帧可用时才返回。
         */
        BLOCKING(0),

        /**
         * ARsession的update()方法立刻返回（如果没有新的帧，就返回上一帧）。
         */
        LATEST_CAMERA_IMAGE(1);

        final int mNativeCode;

        private UpdateMode(int nativeCode) {
            this.mNativeCode = nativeCode;
        }

        static  UpdateMode forNumber(int nativeCode) {
             UpdateMode[] var1;
            int var2 = (var1 = values()).length;

            for(int var3 = 0; var3 < var2; ++var3) {
                 UpdateMode var4;
                if ((var4 = var1[var3]).mNativeCode == nativeCode) {
                    return var4;
                }
            }

            return UNKNOWN;
        }

        static  UpdateMode forARCore(Config.UpdateMode mode) {
            if (mode == Config.UpdateMode.BLOCKING){
                return BLOCKING;
            }else if(mode == Config.UpdateMode.LATEST_CAMERA_IMAGE){
                return LATEST_CAMERA_IMAGE;
            }
            return UNKNOWN;
        }

        static  Config.UpdateMode toARCore( UpdateMode mode) {
            if (mode == BLOCKING){
                return Config.UpdateMode.BLOCKING;
            }else if(mode == LATEST_CAMERA_IMAGE){
                return Config.UpdateMode.LATEST_CAMERA_IMAGE;
            }
            return Config.UpdateMode.LATEST_CAMERA_IMAGE;
        }

        static  UpdateMode forHuaWei(com.huawei.hiar.ARConfigBase.UpdateMode mode) {
            if (mode == com.huawei.hiar.ARConfigBase.UpdateMode.BLOCKING){
                return BLOCKING;
            }else if(mode == com.huawei.hiar.ARConfigBase.UpdateMode.LATEST_CAMERA_IMAGE){
                return LATEST_CAMERA_IMAGE;
            }
            return UNKNOWN;
        }

        static  com.huawei.hiar.ARConfigBase.UpdateMode toHuaWei(UpdateMode mode) {
            if (mode ==  BLOCKING){
                return com.huawei.hiar.ARConfigBase.UpdateMode.BLOCKING;
            }else if(mode ==  LATEST_CAMERA_IMAGE){
                return com.huawei.hiar.ARConfigBase.UpdateMode.LATEST_CAMERA_IMAGE;
            }
            return com.huawei.hiar.ARConfigBase.UpdateMode.UNKNOWN;
        }

    }

    public static enum PlaneFindingMode {
        UNKNOWN(-1),
        /**
         * 禁用平面识别
         */
        DISABLED(0),

        /**
         * 启用平面识别（包括水平面和垂直面）
         */
        ENABLE(3),

        /**
         * 仅启用水平面识别
         */
        HORIZONTAL_ONLY(1),

        /**
         * 仅启用垂直面识别
         */
        VERTICAL_ONLY(2);

        final int mNativeCode;

        private PlaneFindingMode(int nativeCode) {
            this.mNativeCode = nativeCode;
        }

        static PlaneFindingMode forNumber(int nativeCode) {
            PlaneFindingMode[] var1;
            int var2 = (var1 = values()).length;

            for(int var3 = 0; var3 < var2; ++var3) {
                PlaneFindingMode var4;
                if ((var4 = var1[var3]).mNativeCode == nativeCode) {
                    return var4;
                }
            }

            return UNKNOWN;
        }

        static PlaneFindingMode forARCore(Config.PlaneFindingMode mode){
            if (mode == Config.PlaneFindingMode.DISABLED){
                return DISABLED;
            }else if(mode == Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL){
                return ENABLE;
            }else if(mode == Config.PlaneFindingMode.HORIZONTAL){
                return HORIZONTAL_ONLY;
            }else if(mode == Config.PlaneFindingMode.VERTICAL){
                return VERTICAL_ONLY;
            }
            return UNKNOWN;
        }

        static Config.PlaneFindingMode toARCore(PlaneFindingMode mode){
            if (mode == DISABLED){
                return Config.PlaneFindingMode.DISABLED;
            }else if(mode == ENABLE){
                return Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL;
            }else if(mode == HORIZONTAL_ONLY){
                return Config.PlaneFindingMode.HORIZONTAL;
            }else if(mode == VERTICAL_ONLY){
                return Config.PlaneFindingMode.VERTICAL;
            }
            return Config.PlaneFindingMode.DISABLED;
        }

        static PlaneFindingMode forHuaWei(com.huawei.hiar.ARConfigBase.PlaneFindingMode mode){
            if (mode == com.huawei.hiar.ARConfigBase.PlaneFindingMode.DISABLED){
                return DISABLED;
            }else if(mode == com.huawei.hiar.ARConfigBase.PlaneFindingMode.ENABLE){
                return ENABLE;
            }else if(mode == com.huawei.hiar.ARConfigBase.PlaneFindingMode.HORIZONTAL_ONLY){
                return HORIZONTAL_ONLY;
            }else if(mode == com.huawei.hiar.ARConfigBase.PlaneFindingMode.VERTICAL_ONLY){
                return VERTICAL_ONLY;
            }
            return UNKNOWN;
        }

        static com.huawei.hiar.ARConfigBase.PlaneFindingMode toHuaWei(PlaneFindingMode mode){
            if (mode == DISABLED){
                return com.huawei.hiar.ARConfigBase.PlaneFindingMode.DISABLED;
            }else if(mode == ENABLE){
                return com.huawei.hiar.ARConfigBase.PlaneFindingMode.ENABLE;
            }else if(mode == HORIZONTAL_ONLY){
                return com.huawei.hiar.ARConfigBase.PlaneFindingMode.HORIZONTAL_ONLY;
            }else if(mode == VERTICAL_ONLY){
                return com.huawei.hiar.ARConfigBase.PlaneFindingMode.VERTICAL_ONLY;
            }
            return com.huawei.hiar.ARConfigBase.PlaneFindingMode.UNKNOWN;
        }

    }

    /**
     * 深度模式
     * <p>ARCore支持RAW_DEPTH</p>
     */
    public enum DepthMode {
        /**
         * 禁用深度数据
         */
        DISABLED,

        /**
         * 自动接收深度数据
         */
        AUTOMATIC,

        /**
         * 仅用RAW的深度数据
         * <p>仅用于ARCore</p>
         */
        RAW_DEPTH_ONLY;
    }
}
