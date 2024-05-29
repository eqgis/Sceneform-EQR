package com.eqgis.ar;

/**
 * AR图片元数据
 * <p>提供对相机图像捕获结果metadata信息的访问。暂不引入这部分功能</p>
 */
public class ARImageMetadata {

    com.google.ar.core.ImageMetadata coreMetadata = null;
    com.huawei.hiar.ARImageMetadata hwMetadata = null;

    ARImageMetadata(com.google.ar.core.ImageMetadata coreobj,com.huawei.hiar.ARImageMetadata hwobj){
        if (coreobj==null && hwobj==null){
            throw new IllegalArgumentException();
        }
        coreMetadata = coreobj;
        hwMetadata = hwobj;
    }

    //没用到 暂时空着

}
