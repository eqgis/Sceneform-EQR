package com.eqgis.eqr.ar;

import android.util.Size;

/**
 * AR相机配置类
 * @author tanyx
 */
public class ARCameraConfig {
    com.google.ar.core.CameraConfig coreCameraConfig = null;
    com.huawei.hiar.ARCameraConfig hwCameraConfig = null;

    /**
     * 构造函数
     * @param coreobj ARCore的相机配置对象
     * @param hwobj 华为AREngine的相机配置对象
     */
    ARCameraConfig(com.google.ar.core.CameraConfig coreobj ,com.huawei.hiar.ARCameraConfig hwobj){
        if (coreobj==null && hwobj==null){
            throw new IllegalArgumentException();
        }
        coreCameraConfig = coreobj;
        hwCameraConfig = hwobj;
    }

    /**
     * 获取相机送到GPU流处理的图像帧尺寸，返回的是一个二维向量（width，height）。
     * @return 图像帧尺寸。
     */
    public Size getTextureDimensions() {
        if (coreCameraConfig!=null){
            return coreCameraConfig.getTextureSize();
        }else{
            return hwCameraConfig.getTextureDimensions();
        }
    }

    /**
     * 获取相机送到CPU流处理的图像帧尺寸，返回的是一个二维向量（width，height）。
     * @return 图像帧尺寸。
     */
    public Size getImageDimensions() {
        if (coreCameraConfig!=null){
            return coreCameraConfig.getImageSize();
        }else{
            return hwCameraConfig.getImageDimensions();
        }
    }

}
