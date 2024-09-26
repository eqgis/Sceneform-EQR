package com.eqgis.eqr.listener;

import com.google.sceneform.rendering.ExternalTexture;

/**
 * 纹理初始化监听事件
 */
public interface InitializeListener{
    /**
     * 当纹理初始化成功是触发回调
     * @param externalTexture 扩展纹理
     */
    void initializeTexture(ExternalTexture externalTexture);
}