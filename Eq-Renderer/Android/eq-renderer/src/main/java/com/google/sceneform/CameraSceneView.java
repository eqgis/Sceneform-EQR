package com.google.sceneform;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.google.sceneform.rendering.CameraStream;
import com.google.sceneform.rendering.ExternalTexture;
import com.google.sceneform.rendering.GLHelper;
import com.google.sceneform.rendering.Renderer;
import com.google.sceneform.utilities.Preconditions;

/**
 * 相机场景视图
 * <p>用于实现3Dof的AR场景</p>
 * @author tanyx 2024/9/25
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/
public class CameraSceneView extends SceneView{
    @Nullable
    private CameraStream cameraStream;
    @Nullable
    private ExternalTexture externalTexture;
    private Renderer renderer;
    private com.google.sceneform.ExSceneView.BeginFrameListener beginFrameListener;
    private com.google.sceneform.ExSceneView.InitializeListener initializeListener;
    private boolean isInit = false;
    private int textureId = GLHelper.createCameraTexture();

    public interface BeginFrameListener{
        void onBeginFrame(long frameTimeNanos);
    }

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

    /**
     * 构造函数
     *
     * @param context 安卓上下文
     */
    public CameraSceneView(Context context) {
        super(context);
        initBaseParameter();
    }

    /**
     * 构造函数
     *
     * @param context 安卓上下文
     * @see #CameraSceneView(Context, AttributeSet)
     */
    public CameraSceneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBaseParameter();
    }

    /**
     * set BeginFrameListener
     * @param beginFrameListener
     */
    public void setBeginFrameListener(com.google.sceneform.ExSceneView.BeginFrameListener beginFrameListener) {
        this.beginFrameListener = beginFrameListener;
    }

    private void initBaseParameter() {
        ARPlatForm.setType(ARPlatForm.Type.CAMERA);
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

//        float[] transformMatrix = getTransformMatrix(90, 0);
//        int matrixHandle = GLES30.glGetUniformLocation(program, "uMatrix");
//        GLES30.glUniformMatrix4fv(matrixHandle, 1, false, transformMatrix, 0);
//         绑定相机纹理并绘制
//        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

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
    public void setInitializeListener(com.google.sceneform.ExSceneView.InitializeListener initializeListener) {
        this.initializeListener = initializeListener;
    }

    private float[] getTransformMatrix(int sensorOrientation, int displayOrientation) {
//        float[] matrix = new float[16];
//        Matrix.setIdentityM(matrix, 0);

        // 计算总旋转角度
        int totalRotation = (sensorOrientation + displayOrientation) % 360;

        // 创建旋转矩阵
        float[] rotationMatrix = new float[16];
        Matrix.setRotateM(rotationMatrix, 0, totalRotation, 0, 0, 1);

        // 计算缩放以适应视口
//        float scaleX = (float) width / videoSize.getWidth();
//        float scaleY = (float) height / videoSize.getHeight();
//        float[] scaleMatrix = new float[16];
//        Matrix.setScaleM(scaleMatrix, 0, scaleX, scaleY, 1);

        // 组合矩阵
//        Matrix.multiplyMM(matrix, 0, rotationMatrix, 0, scaleMatrix, 0);

        return rotationMatrix;
    }

}
