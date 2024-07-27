package com.google.ar.sceneform;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import com.google.android.filament.gltfio.MaterialProvider;
import com.google.android.filament.gltfio.UbershaderLoader;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.EngineInstance;
import com.google.ar.sceneform.rendering.RenderableInternalFilamentAssetData;
import com.google.ar.sceneform.rendering.Renderer;
import com.google.ar.sceneform.rendering.ResourceManager;
import com.google.ar.sceneform.utilities.AndroidPreconditions;
import com.google.ar.sceneform.utilities.MovingAverageMillisecondsTracker;
import com.google.ar.sceneform.utilities.Preconditions;
import com.google.android.filament.ColorGrading;
import com.google.android.filament.View;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A Sceneform SurfaceView that manages rendering and interaction with the scene.
 */
public class SceneView extends SurfaceView implements Choreographer.FrameCallback {
    private static final String TAG = SceneView.class.getSimpleName();

    @Nullable
    private Renderer renderer = null;
    private final FrameTime frameTime = new FrameTime();

    private Scene scene;
    private volatile boolean debugEnabled = false;

    private boolean isInitialized = false;

    @Nullable
    private Color backgroundColor;

    // Used to track high-level performance metrics for Sceneform
    private final MovingAverageMillisecondsTracker frameTotalTracker =
            new MovingAverageMillisecondsTracker();
    private final MovingAverageMillisecondsTracker frameUpdateTracker =
            new MovingAverageMillisecondsTracker();
    private final MovingAverageMillisecondsTracker frameRenderTracker =
            new MovingAverageMillisecondsTracker();

    private List<OnTouchListener> mOnTouchListeners;

    /**
     * Constructs a SceneView object and binds it to an Android Context.
     *
     * @param context the Android Context to use
     * @see #SceneView(Context, AttributeSet)
     */
    @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
    public SceneView(Context context) {
        super(context);
        initialize();
    }

    /**
     * Constructs a SceneView object and binds it to an Android Context.
     *
     * @param context the Android Context to use
     * @param attrs   the Android AttributeSet to associate with
     */
    @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
    public SceneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // this makes sure that the view's onTouchListener is called.
        if (!super.onTouchEvent(motionEvent)) {
            scene.onTouchEvent(motionEvent);
            // We must always return true to guarantee that this view will receive all touch events.
            // TODO: Update Scene.onTouchEvent to return if it was handled.
            for (OnTouchListener listener:mOnTouchListeners) {
                listener.onTouch(null,motionEvent);
            }
            return true;
        }
        return true;
    }

    /**
     * Set the background to a given {@link Drawable}, or remove the background. If the background is
     * a {@link ColorDrawable}, then the background color of the {@link Scene} is set to {@link
     * ColorDrawable#getColor()} (the alpha of the color is ignored). Otherwise, default to the
     * behavior of {@link SurfaceView#setBackground(Drawable)}.
     */
    @Override
    public void setBackground(@Nullable Drawable background) {
        if (background instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable) background;
            backgroundColor = new Color(colorDrawable.getColor());
            if (renderer != null) {
                renderer.setClearColor(backgroundColor);
            }
        } else {
            backgroundColor = null;
            if (renderer != null) {
                renderer.setDefaultClearColor();
            }
            super.setBackground(background);
        }
    }

    /**
     * Set the background to transparent.
     */
    public void setTransparent(boolean transparent) {
        setBackgroundColor(android.graphics.Color.TRANSPARENT);//Add this line.Avoid this method being invalid.--IkkyuTed
        setZOrderOnTop(transparent);
        getHolder().setFormat(transparent ? PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE);
        renderer.getFilamentView().setBlendMode(transparent ? View.BlendMode.TRANSLUCENT : View.BlendMode.OPAQUE);
    }

    /**
     * @hide
     */
    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int width = right - left;
        int height = bottom - top;
        Preconditions.checkNotNull(renderer).setDesiredSize(width, height);
    }

    /**
     * Resume Sceneform, which resumes the rendering thread.
     * <p>
     * Typically called from onResume().
     *
     * @throws CameraNotAvailableException if the camera can not be opened
     */
    public void resume() throws Exception {
        if (renderer != null) {
            renderer.onResume();
        }
        // Start the drawing when the renderer is resumed.  Remove and re-add the callback
        // to avoid getting called twice.
        Choreographer.getInstance().removeFrameCallback(this);
        Choreographer.getInstance().postFrameCallback(this);
    }

    /**
     * Pause Sceneform, which pauses the rendering thread.
     *
     * <p>Typically called from onPause().
     */
    public void pause() {
        Choreographer.getInstance().removeFrameCallback(this);
        if (renderer != null) {
            renderer.onPause();
        }
    }

    /**
     * Required to exit Sceneform.
     *
     * <p>Typically called from onDestroy().
     */
    public void destroy() {
        pause();

        if (renderer != null) {

            //todo check_free memory_: add this method to release memory
            try {
//                reclaimReleasedResources();//'renderer.dispose()' has call this method.

                renderer.dispose2();//包含renderer和filamentView、indirectLight
            }catch (IllegalStateException e){
                Log.w(TAG, "destroy: ", e);
            }finally {
                renderer = null;
            }
        }

        ResourceManager.getInstance().destroyAllResources();
//        EngineInstance.destroyEngine();
//        destroyAllResources();
    }

    /**
     * Immediately releases all rendering resources, even if in use.
     *
     * <p>Use this if nothing more will be rendered in this scene or any other, and the memory must be
     * released immediately.
     */
    public static void destroyAllResources() {
        Renderer.destroyAllResources();
    }

    /**
     * Releases rendering resources ready for garbage collection
     *
     * <p>Called every frame to collect unused resources. May be called manually to release resources
     * after rendering has stopped.
     *
     * @return Count of resources currently in use
     */
    public static long reclaimReleasedResources() {
        return Renderer.reclaimReleasedResources();
    }

    /**
     * If enabled, provides various visualizations for debugging.
     *
     * @param enable True to enable debugging visualizations, false to disable it.
     */
    public void enableDebug(boolean enable) {
        debugEnabled = enable;
    }

    /**
     * Indicates whether debugging is enabled for this view.
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Returns the renderer used for this view, or null if the renderer is not setup.
     *
     * @hide Not a public facing API for version 1.0
     */
    @Nullable
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Returns the Sceneform Scene created by this view.
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * To capture the contents of this view, designate a {@link Surface} onto which this SceneView
     * should be mirrored. Use {@link android.media.MediaRecorder#getSurface()}, {@link
     * android.media.MediaCodec#createInputSurface()} or {@link
     * android.media.MediaCodec#createPersistentInputSurface()} to obtain the input surface for
     * recording. This will incur a rendering performance cost and should only be set when capturing
     * this view. To stop the additional rendering, call stopMirroringToSurface.
     *
     * @param surface the Surface onto which the rendered scene should be mirrored.
     * @param left    the left edge of the rectangle into which the view should be mirrored on surface.
     * @param bottom  the bottom edge of the rectangle into which the view should be mirrored on
     *                surface.
     * @param width   the width of the rectangle into which the SceneView should be mirrored on surface.
     * @param height  the height of the rectangle into which the SceneView should be mirrored on
     *                surface.
     */
    public void startMirroringToSurface(
            Surface surface, int left, int bottom, int width, int height) {
        if (renderer != null) {
            renderer.startMirroring(surface, left, bottom, width, height);
        }
    }

    /**
     * When capturing is complete, call this method to stop mirroring the SceneView to the specified
     * {@link Surface}. If this is not called, the additional performance cost will remain.
     *
     * <p>The application is responsible for calling {@link Surface#release()} on the Surface when
     * done.
     */
    public void stopMirroringToSurface(Surface surface) {
        if (renderer != null) {
            renderer.stopMirroring(surface);
        }
    }

    /**
     * Initialize the renderer. This creates the Renderer and sets the camera.
     *
     * @see #SceneView(Context, AttributeSet)
     */
    private void initialize() {
        if (isInitialized) {
            Log.w(TAG, "SceneView already initialized.");
            return;
        }

        if (!AndroidPreconditions.isMinAndroidApiLevel()) {
            Log.e(TAG, "Sceneform requires Android N or later");
            renderer = null;
        } else {
            renderer = new Renderer(this);

            //added by Ikkyu 2022/03/31 Restored deprecated tone mapping
            renderer.getFilamentView().setColorGrading(
                    new ColorGrading.Builder().toneMapping(
                            ColorGrading.ToneMapping.FILMIC
                    ).build(EngineInstance.getEngine().getFilamentEngine()));

            if (backgroundColor != null) {
                renderer.setClearColor(backgroundColor);
            }
            scene = new Scene(this);
            renderer.setCameraProvider(scene.getCamera());
        }
        isInitialized = true;
        mOnTouchListeners=new ArrayList<>();
    }

    /**
     * Update view-specific logic before for each display frame.
     *
     * @return true if the scene should be updated before rendering.
     * @hide
     */
    protected boolean onBeginFrame(long frameTimeNanos) {
        //desc-added by Ikkyu 2022年1月18日，@Testing 可能不生效
//        if (customDepthImage != null && ARPlatForm.OCCLUSION_MODE == ARPlatForm.OcclusionMode.OCCLUSION_ENABLED){
//            recalculateOcclusion(customDepthImage);
//        }
        return true;
    }

    /**
     * Callback that occurs for each display frame. Updates the scene and reposts itself to be called
     * by the choreographer on the next frame.
     *
     * @hide
     */
    @SuppressWarnings("AndroidApiChecker")
    @Override
    public void doFrame(long frameTimeNanos) {
        // Always post the callback for the next frame.
        Choreographer.getInstance().postFrameCallback(this);
        doFrameNoRepost(frameTimeNanos);
    }

    /**
     * Callback that occurs for each display frame. Updates the scene but does not post a callback
     * request to the choreographer for the next frame. This is used for testing where on-demand
     * renders are needed.
     *
     * @hide
     */
    public void doFrameNoRepost(long frameTimeNanos) {
        // TODO: Display the tracked performance metrics in debug mode.
        if (debugEnabled) {
            frameTotalTracker.beginSample();
        }

        if (onBeginFrame(frameTimeNanos)) {
            doUpdate(frameTimeNanos);
            doRender(frameTimeNanos);
        }

        if (debugEnabled) {
            frameTotalTracker.endSample();
            if ((System.currentTimeMillis() / 1000) % 60 == 0) {
                Log.d(TAG, " PERF COUNTER: frameRender: " + frameRenderTracker.getAverage());
                Log.d(TAG, " PERF COUNTER: frameTotal: " + frameTotalTracker.getAverage());
                Log.d(TAG, " PERF COUNTER: frameUpdate: " + frameUpdateTracker.getAverage());
            }
        }
    }

    private void doUpdate(long frameTimeNanos) {
        if (debugEnabled) {
            frameUpdateTracker.beginSample();
        }

        frameTime.update(frameTimeNanos);

        scene.dispatchUpdate(frameTime);

        if (debugEnabled) {
            frameUpdateTracker.endSample();
        }
    }

    private void doRender(long frameTimeNanos) {
        Renderer renderer = this.renderer;
        if (renderer == null) {
            return;
        }

        if (debugEnabled) {
            frameRenderTracker.beginSample();
        }

        renderer.render(frameTimeNanos, debugEnabled);

        if (debugEnabled) {
            frameRenderTracker.endSample();
        }
    }

    /**
     * ADD BY IKKYU========================
     */

    /**
     * {@link SceneView#updateDepthImageData(byte[], int, int)}
     */
    protected CustomDepthImage customDepthImage;
//    static final String MATERIAL_CAMERA_TEXTURE = "cameraTexture";
//    static final String MATERIAL_DEPTH_TEXTURE = "depthTexture";
//    @Nullable private DepthTexture depthTexture;
//    @Nullable private Material occlusionCameraMaterial = null;

//    /**
//     * 更新深度图
//     * @param depthImage
//     */
//    void updateDepthImage(CustomDepthImage depthImage){
//        this.customDepthImage = depthImage;
//    }

    /**
     * 更新深度图数据
     * @param data
     * @param width
     * @param height
     */
    public void updateDepthImageData(byte[] data,int width,int height){
        if (data == null){
            this.customDepthImage = null;
            return;
        }
        if (this.customDepthImage == null){
            this.customDepthImage = new CustomDepthImage(data,width,height);
            return;
        }
        customDepthImage.setBytes(data);
        customDepthImage.setWidth(width);
        customDepthImage.setHeight(height);
    }

//    void setupOcclusionCameraMaterial(Renderer renderer) {
//        CompletableFuture<Material> materialFuture =
//                Material.builder()
//                        .setSource(
//                                renderer.getContext(),
//                                RenderingResources.GetSceneformResource(
//                                        renderer.getContext(),
//                                        RenderingResources.Resource.OCCLUSION_CAMERA_MATERIAL))
//                        .build();
//        materialFuture
//                .thenAccept(
//                        material -> {
//                            float[] uvTransform = new float[]{
//                                    1,0,0,0,
//                                    0,1,0,0,
//                                    0,0,1,0,
//                                    0,0,0,1
//                            };
////                            float[] uvTransform = Mat4.Companion.identity().toFloatArray();
//                            material.getFilamentMaterialInstance()
//                                    .setParameter(
//                                            "uvTransform",
//                                            MaterialInstance.FloatElement.FLOAT4,
//                                            uvTransform,
//                                            0,
//                                            4);
//
//                            // Only set the occlusion material if it hasn't already been set to a custom material.
//                            if (occlusionCameraMaterial == null) {
//                                occlusionCameraMaterial = material;
//                            }
//                        })
//                .exceptionally(
//                        throwable -> {
//                            Log.e(TAG, "Unable to load camera stream materials.", throwable);
//                            return null;
//                        });
//    }

   //增加外部手势事件处理
    public void addOnTouchListener(OnTouchListener listener){
        mOnTouchListeners.add(listener);
    }

}