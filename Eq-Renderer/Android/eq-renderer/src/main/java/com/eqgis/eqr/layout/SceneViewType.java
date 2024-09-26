package com.eqgis.eqr.layout;

/**
 * 场景视图类型
 * @author tanyx 2024/9/26
 * @version 1.0
 **/
public enum SceneViewType {
    /**
     * 普通3D视图
     * <p>对应使用SceneView</p>
     */
    BASE,
    /**
     * 扩展场景视图
     * <p>对应使用ExSceneView</p>
     */
    EXTENSION,
    /**
     * 相机视图
     * <p>对应使用CameraSceneView</p>
     */
    CAMERA,
    /**
     * VR视图
     */
    VR
}
