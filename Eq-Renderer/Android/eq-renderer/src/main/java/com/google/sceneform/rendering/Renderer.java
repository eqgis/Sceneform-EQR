package com.google.sceneform.rendering;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;

import com.google.android.filament.Skybox;
import com.google.sceneform.utilities.EnvironmentalHdrParameters;
import com.google.android.filament.View;
import com.google.sceneform.SceneView;
import com.google.sceneform.utilities.AndroidPreconditions;
import com.google.sceneform.utilities.Preconditions;
import com.google.android.filament.Camera;
import com.google.android.filament.Entity;
import com.google.android.filament.IndirectLight;
import com.google.android.filament.Scene;
import com.google.android.filament.SwapChain;
import com.google.android.filament.TransformManager;
import com.google.android.filament.View.DynamicResolutionOptions;
import com.google.android.filament.Viewport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 渲染对象
 * <p>包含所有要渲染到surface上的内容</p>
 */
public class Renderer implements EqUiHelper.RendererCallback {
    //默认相机设置，在不使用ARCore/AREngine等外部相机参数的时候使用。
    private static final float DEFAULT_CAMERA_APERATURE = 4.0f;
    private static final float DEFAULT_CAMERA_SHUTTER_SPEED = 1.0f / 30.0f;
    private static final float DEFAULT_CAMERA_ISO = 320.0f;

    //选择HDR照明相机设置以提供1.0的曝光值。当启用ARCore HDR照明时用到
    private static final float ARCORE_HDR_LIGHTING_CAMERA_APERATURE = 1.0f;
    private static final float ARCORE_HDR_LIGHTING_CAMERA_SHUTTER_SPEED = 1.2f;
    private static final float ARCORE_HDR_LIGHTING_CAMERA_ISO = 100.0f;

    private static final Color DEFAULT_CLEAR_COLOR = new Color(0.0f, 0.0f, 0.0f, 1.0f);

    // 将分辨率限制在1440以内,可在初始化之前修改下面两个参数
    public static int MAXIMUM_RESOLUTION = 1440;
    public static boolean DYNAMIC_RESOLUTION_ENABLED = true;

    @Nullable private CameraProvider cameraProvider;
    private final SurfaceView surfaceView;
    private ViewAttachmentManager viewAttachmentManager;

    private final ArrayList<RenderableInstance> renderableInstances = new ArrayList<>();
    private final ArrayList<LightInstance> lightInstances = new ArrayList<>();

    private Surface surface;
    @Nullable private SwapChain swapChain;
    private com.google.android.filament.View view;
    private com.google.android.filament.View emptyView;
    private com.google.android.filament.Renderer renderer;
    private Camera camera;
    public Scene scene;
    private IndirectLight indirectLight;
    private boolean recreateSwapChain;

    private float cameraAperature;
    private float cameraShutterSpeed;
    private float cameraIso;

    private EqUiHelper filamentHelper;

    private final double[] cameraProjectionMatrix = new double[16];

    private EnvironmentalHdrParameters environmentalHdrParameters =
            EnvironmentalHdrParameters.makeDefault();

    private static class Mirror {
        @Nullable SwapChain swapChain;
        @Nullable Surface surface;
        Viewport viewport;
    }

    private final List<Mirror> mirrors = new ArrayList<>();

    /** @hide */
    public interface PreRenderCallback {
        void preRender(
                com.google.android.filament.Renderer renderer,
                SwapChain swapChain,
                Camera camera);
    }

    @Nullable private Runnable onFrameRenderDebugCallback = null;
    @Nullable private PreRenderCallback preRenderCallback;

    /** @hide */
    @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Renderer(SceneView view) {
        Preconditions.checkNotNull(view, "Parameter \"view\" was null.");
        // Enforce api level 24
        AndroidPreconditions.checkMinAndroidApiLevel();

        this.surfaceView = view;
        initialize();
    }

    /**
     * 开始将画面映射到指定平面{@link Surface}
     * @hide
     */
    public void startMirroring(Surface surface, int left, int bottom, int width, int height) {
        Mirror mirror = new Mirror();
        mirror.surface = surface;
        mirror.viewport = new Viewport(left, bottom, width, height);
        mirror.swapChain = null;
        synchronized (mirrors) {
            mirrors.add(mirror);
        }
    }

    /**
     * 停止将画面映射到指定平面{@link Surface}
     *
     * @hide
     */
    public void stopMirroring(Surface surface) {
        synchronized (mirrors) {
            for (Mirror mirror : mirrors) {
                if (mirror.surface == surface) {
                    mirror.surface = null;
                }
            }
        }
    }

    /**
     * * 获取filament的渲染器对象
     *
     * @hide
     */
    public com.google.android.filament.Renderer getFilamentRenderer() {
        return renderer;
    }

    /**
     * 获取filament的视图对象
     *
     * @hide
     */
    public com.google.android.filament.View getFilamentView() {
        return view;
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    /** @hide */
    public void setClearColor(Color color) {
        com.google.android.filament.Renderer.ClearOptions options = new com.google.android.filament.Renderer.ClearOptions();
        options.clear = true;
        if(color.a > 0) {
            options.clearColor[0] = color.r;
            options.clearColor[1] = color.g;
            options.clearColor[2] = color.b;
            options.clearColor[3] = color.a;
        }
        renderer.setClearOptions(options);
    }

    /** @hide */
    public void setDefaultClearColor() {
        setClearColor(DEFAULT_CLEAR_COLOR);
    }

    /**
     * 反转渲染
     *
     * @hide ArSceneView中使用
     */

    public void setFrontFaceWindingInverted(Boolean inverted) {
        view.setFrontFaceWindingInverted(inverted);
    }

    /**
     *检查是否启用反转渲染
     *
     * @hide ViewRenderable中用到
     */

    public boolean isFrontFaceWindingInverted() {
        return view.isFrontFaceWindingInverted();
    }

    /** @hide */
    public void setCameraProvider(@Nullable CameraProvider cameraProvider) {
        this.cameraProvider = cameraProvider;
    }

    /** @hide */
    public void onPause() {
        if (viewAttachmentManager != null){
            viewAttachmentManager.onPause();
        }
    }

    /** @hide */
    public void onResume() {
        if (viewAttachmentManager != null){
            viewAttachmentManager.onResume();
        }
    }

    /**
     * 设置每个帧渲染后发生的回调。这可以用于记录性能
     * 给定帧的度量。
     */
    public void setFrameRenderDebugCallback(Runnable onFrameRenderDebugCallback) {
        this.onFrameRenderDebugCallback = onFrameRenderDebugCallback;
    }

    private Viewport getLetterboxViewport(Viewport srcViewport, Viewport destViewport) {
        boolean letterBoxSides =
                (destViewport.width / (float) destViewport.height)
                        > (srcViewport.width / (float) srcViewport.height);
        float scale =
                letterBoxSides
                        ? (destViewport.height / (float) srcViewport.height)
                        : (destViewport.width / (float) srcViewport.width);
        int width = (int) (srcViewport.width * scale);
        int height = (int) (srcViewport.height * scale);
        int left = (destViewport.width - width) / 2;
        int bottom = (destViewport.height - height) / 2;
        return new Viewport(left, bottom, width, height);
    }

    /** @hide */
    public void setPreRenderCallback(@Nullable PreRenderCallback preRenderCallback) {
        this.preRenderCallback = preRenderCallback;
    }

    /** @hide */
    public void render(long frameTimeNanos, boolean debugEnabled) {
        synchronized (this) {
            if (recreateSwapChain) {
                final IEngine engine = EngineInstance.getEngine();
                if (swapChain != null) {
                    engine.destroySwapChain(swapChain);
                }
                swapChain = engine.createSwapChain(surface);
                recreateSwapChain = false;
            }
        }
        synchronized (mirrors) {
            Iterator<Mirror> mirrorIterator = mirrors.iterator();
            while (mirrorIterator.hasNext()) {
                Mirror mirror = mirrorIterator.next();
                if (mirror.surface == null) {
                    if (mirror.swapChain != null) {
                        final IEngine engine = EngineInstance.getEngine();
                        engine.destroySwapChain(Preconditions.checkNotNull(mirror.swapChain));
                    }
                    mirrorIterator.remove();
                } else if (mirror.swapChain == null) {
                    final IEngine engine = EngineInstance.getEngine();
                    mirror.swapChain = engine.createSwapChain(Preconditions.checkNotNull(mirror.surface));
                }
            }
        }

        if (filamentHelper.isReadyToRender() || EngineInstance.isHeadlessMode()) {
            updateInstances();
            updateLights();

            CameraProvider cameraProvider = this.cameraProvider;
            if (cameraProvider != null) {

                @Nullable SwapChain swapChainLocal = swapChain;
                if (swapChainLocal == null) {
                    throw new AssertionError("Internal Error: Failed to get swap chain");
                }

                //渲染场景
                if (renderer.beginFrame(swapChainLocal, frameTimeNanos)) {
                    final float[] projectionMatrixData = cameraProvider.getProjectionMatrix().data;
                    for (int i = 0; i < 16; ++i) {
                        cameraProjectionMatrix[i] = projectionMatrixData[i];
                    }

                    camera.setModelMatrix(cameraProvider.getWorldModelMatrix().data);
                    camera.setCustomProjection(
                            cameraProjectionMatrix,
                            cameraProvider.getNearClipPlane(),
                            cameraProvider.getFarClipPlane());

                    if (preRenderCallback != null) {
                        preRenderCallback.preRender(renderer, swapChainLocal, camera);
                    }

                    //目前，filament不提供禁用摄像头的功能
                    //使用null相机渲染视图不会清除viewport。
                    //当相机被禁用时，渲染一个空视图，作为绕行方式。
                    com.google.android.filament.View currentView =
                            cameraProvider.isActive() ? view : emptyView;
                    renderer.render(currentView);

                    synchronized (mirrors) {
                        for (Mirror mirror : mirrors) {
                            if (mirror.swapChain != null) {
                                renderer.mirrorFrame(
                                        mirror.swapChain,
                                        getLetterboxViewport(currentView.getViewport(), mirror.viewport),
                                        currentView.getViewport(),
                                        com.google.android.filament.Renderer.MIRROR_FRAME_FLAG_COMMIT
                                                | com.google.android.filament.Renderer
                                                .MIRROR_FRAME_FLAG_SET_PRESENTATION_TIME
                                                | com.google.android.filament.Renderer.MIRROR_FRAME_FLAG_CLEAR);
                            }
                        }
                    }
                    if (onFrameRenderDebugCallback != null) {
                        onFrameRenderDebugCallback.run();
                    }
                    renderer.endFrame();
                }

                reclaimReleasedResources();//每帧调用
            }
        }
    }

    /**
     * 设置天空盒
     * @param skybox 天空和
     */
    public void setSkybox(Skybox skybox){
        if (skybox != null){
            scene.setSkybox(skybox);
            Log.i("IKKYU ", "setSkybox: "+skybox.toString());
        }
    }

    /**
     * 获取天空盒
     * @return 天空盒
     */
    public Skybox getSkybox(){
        return this.scene.getSkybox();
    }

    /**
     * 设置间接光
     * <p>
     *     间接光会产生一个照明,这些照明时从场景中其它物体上反射而形成的。
     *     该节点会向场景中添加间接光,不会使用光线跟踪。
     * </p>
     * @param light 间接光
     */
    public void setIndirectLight(IndirectLight light) {
        this.indirectLight = light;
        if (light != null){
            scene.setIndirectLight(light);
        }
    }

    /**
     * 获取间接光对象
     * @return 间接光
     */
    public IndirectLight getIndirectLight(){
        return indirectLight;
    }

    /**
     * 释放所有资源
     */
    public void destroyEntities() {
        filamentHelper.detach();
        final IEngine engine = EngineInstance.getEngine();

        //desc-1 先销毁 Renderable Entity（关键）
        int[] entities = scene.getEntities();
        for (int entity : entities) {
            if (engine.getRenderableManager().hasComponent(entity)) {
                if (scene.hasEntity(entity)) {
                    scene.removeEntity(entity);
                }
                engine.destroyEntity(entity);
            }
        }

        //desc-2 再销毁光照 / 天空盒
        if (indirectLight != null) {
            engine.destroyIndirectLight(indirectLight);
            indirectLight = null;
        }

        //desc-3 再销毁 Renderer / View / Camera
        engine.destroyRenderer(renderer);
        engine.destroyView(view);
        engine.destroyView(emptyView);
        engine.destroyCamera(camera);

        //desc-4 Scene 自己持有的 Skybox
        if (scene.getSkybox() != null) {
            engine.destroySkybox(scene.getSkybox());
        }
        //desc-6 SwapChain
        if (swapChain != null) {
            engine.destroySwapChain(swapChain);
            swapChain = null;
        }

    }

    public Context getContext() {
        return getSurfaceView().getContext();
    }

    /** @hide */
    public void setDesiredSize(int width, int height) {
        int minor = Math.min(width, height);
        int major = Math.max(width, height);
        if (minor > MAXIMUM_RESOLUTION) {
            major = (major * MAXIMUM_RESOLUTION) / minor;
            minor = MAXIMUM_RESOLUTION;
        }
        if (width < height) {
            int t = minor;
            minor = major;
            major = t;
        }

        filamentHelper.setDesiredSize(major, minor);
    }

    /** @hide */
    public int getDesiredWidth() {
        return filamentHelper.getDesiredWidth();
    }

    /** @hide */
    public int getDesiredHeight() {
        return filamentHelper.getDesiredHeight();
    }

    /** @hide UiHelper.RendererCallback implementation */
    @Override
    public void onNativeWindowChanged(Surface surface) {
        synchronized (this) {
            this.surface = surface;
            recreateSwapChain = true;
        }
    }

    /** @hide UiHelper.RendererCallback implementation */
    @Override
    public void onDetachedFromSurface() {
        @Nullable SwapChain swapChainLocal = swapChain;
        if (swapChainLocal != null) {
            final IEngine engine = EngineInstance.getEngine();
            engine.destroySwapChain(swapChainLocal);
            //确保在filament执行完之前不会执行destroySwapChain命令，否则Android可能会销毁Surface
            engine.flushAndWait();
            swapChain = null;
        }
    }

    /** @hide */
    private void setDynamicResolutionEnabled(boolean isEnabled) {
        //启用动态解析。默认情况下，它将缩小到屏幕面积的25%
        //(即:每个轴上的50%)
        //这可以在下面的选项中修改。
        DynamicResolutionOptions options = new DynamicResolutionOptions();
        options.enabled = isEnabled;
        view.setDynamicResolutionOptions(options);
    }

    /** @hide  */
    @VisibleForTesting
    public void setAntiAliasing(com.google.android.filament.View.AntiAliasing antiAliasing) {
        view.setAntiAliasing(antiAliasing);
    }

    /** @hide  */
    @VisibleForTesting
    public void setDithering(com.google.android.filament.View.Dithering dithering) {
        view.setDithering(dithering);
    }

    /**
     * 为filament设置高性能配置。禁用FXAA，禁用
     * 后处理，禁用动态分辨率，设置质量为“低”。
     *  @hide 由ArSceneView内部使用
     */

    public void enablePerformanceMode() {
        view.setAntiAliasing(View.AntiAliasing.NONE);
        View.RenderQuality quality = new View.RenderQuality();
        quality.hdrColorBuffer = View.QualityLevel.LOW;
        view.setRenderQuality(quality);
    }

    /**
     * 获取环境HDR参数
     * 用于辅助 filament 和 环境 HDR之间的转换
     * @hide 后续可能不在需要
     */
    public EnvironmentalHdrParameters getEnvironmentalHdrParameters() {
        return environmentalHdrParameters;
    }

    /**
     * 设置环境HDR参数
     * 用于辅助 filament 和 环境 HDR之间的转换
     * @hide 后续可能不在需要
     */
    public void setEnvironmentalHdrParameters(EnvironmentalHdrParameters environmentalHdrParameters) {
        this.environmentalHdrParameters = environmentalHdrParameters;
    }

    /** @hide UiHelper.RendererCallback implementation */
    @Override
    public void onResized(int width, int height) {
        view.setViewport(new Viewport(0, 0, width, height));
        emptyView.setViewport(new Viewport(0, 0, width, height));
    }


    public void onReleaseData() {
        //do nothing
    }

    /** @hide */
    void addLight(LightInstance instance) {
        @Entity int entity = instance.getEntity();
        scene.addEntity(entity);
        lightInstances.add(instance);
    }

    /** @hide */
    void removeLight(LightInstance instance) {
        @Entity int entity = instance.getEntity();
        scene.remove(entity);
        lightInstances.remove(instance);
    }

    private void addModelInstanceInternal(RenderableInstance instance) {return ;}

    private void removeModelInstanceInternal(RenderableInstance instance) {return ;}

    /** @hide */
    public void addInstance(RenderableInstance instance) {
        scene.addEntity(instance.getRenderedEntity());
        Log.d("Renderer IKKYU Test", "Scene ：count："+scene.getEntityCount()+" addInstance: "+ instance.getRenderedEntity());
        addModelInstanceInternal(instance);
        renderableInstances.add(instance);
    }

    /** @hide */
    public void removeInstance(RenderableInstance instance) {
        removeModelInstanceInternal(instance);
        scene.removeEntity(instance.getRenderedEntity());
//    scene.remove(instance.getRenderedEntity());
        renderableInstances.remove(instance);
    }

    @NonNull
    public Scene getFilamentScene() {
        return scene;
    }

    ViewAttachmentManager getViewAttachmentManager() {
        synchronized (Renderer.class){
            if (viewAttachmentManager == null){
                viewAttachmentManager = new ViewAttachmentManager(getContext(), surfaceView);
                viewAttachmentManager.onResume();
            }
        }
        return viewAttachmentManager;
    }

    @SuppressWarnings("AndroidApiChecker") // CompletableFuture
    private void initialize() {
        SurfaceView surfaceView = getSurfaceView();

        filamentHelper = new EqUiHelper(EqUiHelper.ContextErrorPolicy.DONT_CHECK);
        filamentHelper.setRenderCallback(this);
        //使用TextureView时，需要实现透明背景
//    filamentHelper.setOpaque(false);//使用SurfaceView，需注释，否则层级会出现问题
//    filamentHelper.setMediaOverlay(true);//CHECK 影响SurfaceView
        filamentHelper.attachTo(surfaceView);

        IEngine engine = EngineInstance.getEngine();

        renderer = engine.createRenderer();
        scene = engine.createScene();
        view = engine.createView();
        emptyView = engine.createView();
        camera = engine.createCamera();
        setUseHdrLightEstimate(false);

        setDefaultClearColor();
        view.setCamera(camera);
        view.setScene(scene);

        setDynamicResolutionEnabled(DYNAMIC_RESOLUTION_ENABLED);

        emptyView.setCamera(engine.createCamera());
        emptyView.setScene(engine.createScene());
    }

    public void setUseHdrLightEstimate(boolean useHdrLightEstimate) {
        if (useHdrLightEstimate) {
            cameraAperature = ARCORE_HDR_LIGHTING_CAMERA_APERATURE;
            cameraShutterSpeed = ARCORE_HDR_LIGHTING_CAMERA_SHUTTER_SPEED;
            cameraIso = ARCORE_HDR_LIGHTING_CAMERA_ISO;
        } else {
            cameraAperature = DEFAULT_CAMERA_APERATURE;
            cameraShutterSpeed = DEFAULT_CAMERA_SHUTTER_SPEED;
            cameraIso = DEFAULT_CAMERA_ISO;
        }
        // Setup the Camera Exposure values.
        camera.setExposure(cameraAperature, cameraShutterSpeed, cameraIso);
    }

    /**
     * 返回渲染的曝光设置。
     * @hide 这是支持深度显示的API，目前还不稳定。
     */
    public float getExposure() {
        float e = (cameraAperature * cameraAperature) / cameraShutterSpeed * 100.0f / cameraIso;
        return 1.0f / (1.2f * e);
    }

    private void updateInstances() {
        final IEngine engine = EngineInstance.getEngine();
        final TransformManager transformManager = engine.getTransformManager();
        transformManager.openLocalTransformTransaction();

        for (RenderableInstance renderableInstance : renderableInstances) {
            renderableInstance.prepareForDraw(cameraProvider);

            float[] transform = renderableInstance.getWorldModelMatrix().data;
            renderableInstance.setModelMatrix(transformManager, transform);
        }

        transformManager.commitLocalTransformTransaction();
    }

    private void updateLights() {
        for (LightInstance lightInstance : lightInstances) {
            lightInstance.updateTransform();
        }
    }

    /**
     * 释放准备进行垃圾收集的渲染资源
     * @return 资源计数
     */
    public static long reclaimReleasedResources() {
        return ResourceManager.getInstance().reclaimReleasedResources();
    }

    /** 立即释放所有渲染资源，即使正在使用。 */
    public static void destroyAllResources() {
        ResourceManager.getInstance().destroyAllResources();
        EngineInstance.destroyEngine();
    }
}
