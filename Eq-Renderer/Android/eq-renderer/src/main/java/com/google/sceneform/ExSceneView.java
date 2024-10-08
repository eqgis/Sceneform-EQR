package com.google.sceneform;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.eqgis.eqr.listener.BeginFrameListener;
import com.eqgis.eqr.listener.InitializeListener;
import com.google.sceneform.rendering.ExternalTexture;
import com.google.sceneform.rendering.Renderer;
import com.google.sceneform.rendering.CameraStream;
import com.google.sceneform.utilities.Preconditions;

/**
 * 扩展纹理场景视图
 * @author ikkyu 2022/4/29 update 2022/07/19
 * @version 1.0
 **/
public class ExSceneView extends SceneView{
    @Nullable
    private CameraStream cameraStream;
    @Nullable
    private ExternalTexture externalTexture;
    private Renderer renderer;
    private BeginFrameListener beginFrameListener;
    private InitializeListener initializeListener;
    private boolean isInit = false;
    private int textureId = -1;

    /**
     * 构造函数
     *
     * @param context 安卓上下文
     */
    public ExSceneView(Context context) {
        super(context);
        initBaseParameter();
    }

    /**
     * 构造函数
     *
     * @param context 安卓上下文
     * @see #ExSceneView(Context, AttributeSet)
     */
    public ExSceneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBaseParameter();
    }

    /**
     * set BeginFrameListener
     * @param beginFrameListener
     */
    public void setBeginFrameListener(BeginFrameListener beginFrameListener) {
        this.beginFrameListener = beginFrameListener;
    }

    private void initBaseParameter() {
        //当前使用NONE类型
        ARPlatForm.setType(ARPlatForm.Type.NONE);
        renderer = Preconditions.checkNotNull(getRenderer());
        // 背景平面初始化
        cameraStream = new CameraStream(new int[]{textureId},renderer);
        externalTexture = new ExternalTexture();
    }

    /**
     * 每帧渲染前触发
     *
     * @return 如果场景在渲染前需要更新，则为True。
     * @hide
     */
    @Override
    public boolean onBeginFrame(long frameTimeNanos) {
        if (externalTexture == null)return true;

        // 初始化默认的纹理
        if (!cameraStream.isTextureInitialized()) {
            cameraStream.initializeTexture(textureId,externalTexture);
            if (!isInit && initializeListener != null){
                initializeListener.initializeTexture(externalTexture);
                isInit = true;
            }
        }

        //更新深度图
        if (ARPlatForm.OCCLUSION_MODE == ARPlatForm.OcclusionMode.OCCLUSION_ENABLED && super.customDepthImage != null){
            cameraStream.recalculateOcclusion(customDepthImage);//use
        }

        if (beginFrameListener!=null){
            beginFrameListener.onBeginFrame(frameTimeNanos);
        }
        return true;
    }

    /**
     * 获取拓展纹理
     * @return {@link ExternalTexture}
     */
    @Nullable
    public ExternalTexture getExternalTexture() {
        return externalTexture;
    }

    /**
     * 设置纹理初始化监听事件
     * @param initializeListener 监听事件
     */
    public void setInitializeListener(InitializeListener initializeListener) {
        this.initializeListener = initializeListener;
    }
}
