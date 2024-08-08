package com.eqgis.sceneform;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.eqgis.sceneform.rendering.CameraStream;
import com.eqgis.sceneform.rendering.Color;
import com.eqgis.sceneform.rendering.ExternalTexture;
import com.eqgis.sceneform.rendering.Renderer;
import com.eqgis.sceneform.utilities.Preconditions;

/**
 * ExternalTexture SceneView
 * <pre>SampleCode:
 * </pre>
 *
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

    //用于与背景进行混合的颜色值，结合”mat_blend_camera“材质文件使用
    private Color blendColor = new Color(1,0.705f,0.203f,1);

    public interface BeginFrameListener{
        void onBeginFrame(long frameTimeNanos);
    }

    /**
     * Constructs a SceneView object and binds it to an Android Context.
     *
     * @param context the Android Context to use
     */
    public ExSceneView(Context context) {
        super(context);
        initBaseParameter();
    }

    /**
     * Constructs a SceneView object and binds it to an Android Context.
     *
     * @param context the Android Context to use
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
        renderer = Preconditions.checkNotNull(getRenderer());
        // Initialize Plane Renderer
        cameraStream = new CameraStream(-1,renderer);
        externalTexture = new ExternalTexture();
    }

    /**
     * Update view-specific logic before for each display frame.
     *
     * @return true if the scene should be updated before rendering.
     * @hide
     */
    @Override
    public boolean onBeginFrame(long frameTimeNanos) {
        if (externalTexture == null)return true;

        // Setup Camera Stream if needed.
        if (!cameraStream.isTextureInitialized()) {
            cameraStream.initializeTexture(externalTexture);
        }else {
            cameraStream.updateBlendColor(blendColor);
        }

        //update depthImage
        if (ARPlatForm.OCCLUSION_MODE == ARPlatForm.OcclusionMode.OCCLUSION_ENABLED && super.customDepthImage != null){
            cameraStream.recalculateOcclusion(customDepthImage);//use
        }

        if (beginFrameListener!=null){
            beginFrameListener.onBeginFrame(frameTimeNanos);
        }
        return true;
    }

    /**
     * get ExternalTexture
     * @return {@link ExternalTexture}
     */
    @Nullable
    public ExternalTexture getExternalTexture() {
        return externalTexture;
    }

    public void setBlendColor(float r,float g,float b, float a){
        this.blendColor.r = r;
        this.blendColor.g = g;
        this.blendColor.b = b;
        this.blendColor.a = a;
    }
}
