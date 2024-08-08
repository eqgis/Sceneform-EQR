package com.eqgis.sceneform.rendering;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;

import com.eqgis.sceneform.SceneView;
import com.eqgis.sceneform.utilities.AndroidPreconditions;
import com.eqgis.sceneform.utilities.EnvironmentalHdrParameters;
import com.eqgis.sceneform.utilities.Preconditions;
import com.google.android.filament.Camera;
import com.google.android.filament.Entity;
import com.google.android.filament.IndirectLight;
import com.google.android.filament.Scene;
import com.google.android.filament.SwapChain;
import com.google.android.filament.TransformManager;
import com.google.android.filament.View.DynamicResolutionOptions;
import com.google.android.filament.Viewport;

import java.util.ArrayList;

/**
 * A rendering context.
 *
 * <p>Contains everything that will be drawn on a surface.
 *
 * @hide Not a public facing API for version 1.0
 */
public class Renderer implements EqUiHelper.RendererCallback {
  // Default camera settings are used everwhere that ARCore HDR Lighting (Deeplight) is disabled or
  // unavailable.
  private static final float DEFAULT_CAMERA_APERATURE = 4.0f;
  private static final float DEFAULT_CAMERA_SHUTTER_SPEED = 1.0f / 30.0f;
  private static final float DEFAULT_CAMERA_ISO = 320.0f;

  // HDR lighting camera settings are chosen to provide an exposure value of 1.0.  These are used
  // when ARCore HDR Lighting is enabled in Sceneform.
  private static final float ARCORE_HDR_LIGHTING_CAMERA_APERATURE = 1.0f;
  private static final float ARCORE_HDR_LIGHTING_CAMERA_SHUTTER_SPEED = 1.2f;
  private static final float ARCORE_HDR_LIGHTING_CAMERA_ISO = 100.0f;

  private static final Color DEFAULT_CLEAR_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.0f);

  // Limit resolution to 1080p for the minor edge. This is enough for Filament.
  public static int MAXIMUM_RESOLUTION = 1080;

  @Nullable private CameraProvider cameraProvider;
  private final SurfaceView surfaceView;
  private final ViewAttachmentManager viewAttachmentManager;

  private final ArrayList<RenderableInstance> renderableInstances = new ArrayList<>();
  private final ArrayList<LightInstance> lightInstances = new ArrayList<>();

  private Surface surface;
  @Nullable private SwapChain mSwapChain;
  private com.google.android.filament.View filamentView;
  private com.google.android.filament.Renderer renderer;
  private Camera filamentCamera;
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
    viewAttachmentManager = new ViewAttachmentManager(getContext(), view);
    initialize();
  }

//  /**
//   * Starts mirroring to the specified {@link Surface}.
//   *
//   * @hide
//   */
//  public void startMirroring(Surface surface, int left, int bottom, int width, int height) {
//    Mirror mirror = new Mirror();
//    mirror.surface = surface;
//    mirror.viewport = new Viewport(left, bottom, width, height);
//    mirror.swapChain = null;
//    synchronized (mirrors) {
//      mirrors.add(mirror);
//    }
//  }

//  /**
//   * Stops mirroring to the specified {@link Surface}.
//   *
//   * @hide
//   */
//  public void stopMirroring(Surface surface) {
//    synchronized (mirrors) {
//      for (Mirror mirror : mirrors) {
//        if (mirror.surface == surface) {
//          mirror.surface = null;
//        }
//      }
//    }
//  }

  /**
   * Access to the underlying Filament renderer.
   *
   * @hide
   */
  public com.google.android.filament.Renderer getFilamentRenderer() {
    return renderer;
  }

  /**
   * Access to the underlying Filament view.
   *
   * @hide
   */
  public com.google.android.filament.View getFilamentView() {
    return filamentView;
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
   * Inverts winding for front face rendering.
   *
   * @hide Used internally by ArSceneView
   */

  public void setFrontFaceWindingInverted(Boolean inverted) {
    filamentView.setFrontFaceWindingInverted(inverted);
  }

  /**
   * Checks whether winding is inverted for front face rendering.
   *
   * @hide Used internally by ViewRenderable
   */

  public boolean isFrontFaceWindingInverted() {
    return filamentView.isFrontFaceWindingInverted();
  }

  /** @hide */
  public void setCameraProvider(@Nullable CameraProvider cameraProvider) {
    this.cameraProvider = cameraProvider;
  }

  /** @hide */
  public void onPause() {
    viewAttachmentManager.onPause();
  }

  /** @hide */
  public void onResume() {
    viewAttachmentManager.onResume();
  }

  /**@hide */
  public void onDestroy(){
    viewAttachmentManager.onDestroy();
  }

  /**
   * Sets a callback to happen after each frame is rendered. This can be used to log performance
   * metrics for a given frame.
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
  public void render(long frameTimeNanos) {
    synchronized (this) {
      if (recreateSwapChain) {
        final IEngine engine = EngineInstance.getEngine();
        if (mSwapChain != null) {
          engine.destroySwapChain(mSwapChain);
        }
        mSwapChain = engine.createSwapChain(surface);
        recreateSwapChain = false;
        Log.i("IKKYU", "render: createSwapChain ");
      }
      if (filamentHelper.isReadyToRender() || EngineInstance.isHeadlessMode()) {
        updateInstances();
        updateLights();

        CameraProvider cameraProvider = this.cameraProvider;
        if (cameraProvider != null) {

          @Nullable SwapChain swapChainLocal = mSwapChain;
          if (swapChainLocal == null) {
//          throw new AssertionError("Internal Error: Failed to get swap chain");
            Log.i("IKKYU", "render: 'swapChainLocal == null' will createSwapChain ");
            recreateSwapChain = true;//下帧重新创建交换链
            return;
          }

//        try {
//        }catch (IllegalStateException e){
//          Log.e(Renderer.class.getSimpleName(), "Ikkyu-Render: " + swapChainLocal.getNativeObject(), e);
//          recreateSwapChain = true;
//        }
          // Render the scene, unless the renderer wants to skip the frame.
          // This means you are sending frames too quickly to the GPU
          if (renderer.beginFrame(swapChainLocal, frameTimeNanos)) {
            final float[] projectionMatrixData = cameraProvider.getProjectionMatrix().data;
            for (int i = 0; i < 16; ++i) {
              cameraProjectionMatrix[i] = projectionMatrixData[i];
            }

            filamentCamera.setModelMatrix(cameraProvider.getWorldModelMatrix().data);
            filamentCamera.setCustomProjection(
                    cameraProjectionMatrix,
                    cameraProvider.getNearClipPlane(),
                    cameraProvider.getFarClipPlane());

            if (preRenderCallback != null) {
              preRenderCallback.preRender(renderer, swapChainLocal, filamentCamera);
            }

            if (cameraProvider.isActive()){
              renderer.render(filamentView);
            }
            if (onFrameRenderDebugCallback != null) {
              onFrameRenderDebugCallback.run();
            }
            renderer.endFrame();
          }

          reclaimReleasedResources();
        }
      }
    }

  }

  /** @hide */
  public void dispose() {
    filamentHelper.detach(); // call this before destroying the Engine (it could call back)

    final IEngine engine = EngineInstance.getEngine();
    if (indirectLight != null) {
      engine.destroyIndirectLight(indirectLight);
    }
    engine.destroyRenderer(renderer);
    engine.destroyView(filamentView);
    reclaimReleasedResources();
  }

  public void dispose2(){
    filamentHelper.detach();
    final IEngine engine = EngineInstance.getEngine();
    if (indirectLight != null) {
      engine.destroyIndirectLight(indirectLight);
    }

    engine.destroyRenderer(renderer);
    engine.destroyView(filamentView);
//    engine.destroyView(emptyView);
    engine.destroyCamera(filamentCamera);
    if (scene.getSkybox()!=null){
      engine.destroySkybox(scene.getSkybox());
    }
    engine.destroyScene(scene);
    if (mSwapChain != null){
      engine.destroySwapChain(mSwapChain);
      engine.flushAndWait();
      mSwapChain = null;
    }
    reclaimReleasedResources();
  }

  public Context getContext() {
    return getSurfaceView().getContext();
  }

  /**
   * Set the Light Probe used for reflections and indirect light.
   *
   * @hide the scene level API is publicly exposed, this is used by the Scene internally.
   */
  public void setLightProbe(LightProbe lightProbe) {
    if (lightProbe == null) {
      throw new AssertionError("Passed in an invalid light probe.");
    }
    final IndirectLight latestIndirectLight = lightProbe.buildIndirectLight();
    if (latestIndirectLight != null) {
      scene.setIndirectLight(latestIndirectLight);
      if (indirectLight != null && indirectLight != latestIndirectLight) {
        final IEngine engine = EngineInstance.getEngine();
        engine.destroyIndirectLight(indirectLight);
      }
      indirectLight = latestIndirectLight;
    }
  }

  /**
   * 设置间接光
   * @param light
   */
  public void setIndirectLight(IndirectLight light){
    if (indirectLight != null) {
      scene.setIndirectLight(light);
      IEngine engine = EngineInstance.getEngine();
      engine.destroyIndirectLight(indirectLight);
    }else {
      scene.setIndirectLight(light);
    }
    indirectLight = light;
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
    synchronized (this){
      @Nullable SwapChain swapChainLocal = mSwapChain;
      if (swapChainLocal != null) {
        final IEngine engine = EngineInstance.getEngine();
        engine.destroySwapChain(swapChainLocal);
        // Required to ensure we don't return before Filament is done executing the
        // destroySwapChain command, otherwise Android might destroy the Surface
        // too early
        engine.flushAndWait();
        mSwapChain = null;
      }
    }
  }

  /** @hide Only used for scuba testing for now. */
  public void setDynamicResolutionEnabled(boolean isEnabled) {
    // Enable dynamic resolution. By default it will scale down to 25% of the screen area
    // (i.e.: 50% on each axis, e.g.: reducing a 1080p image down to 720p).
    // This can be changed in the options below.
    // TODO: This functionality should probably be exposed to the developer eventually.
    DynamicResolutionOptions options = new DynamicResolutionOptions();
    options.enabled = isEnabled;
    filamentView.setDynamicResolutionOptions(options);
  }

  /** @hide Only used for scuba testing for now. */
  @VisibleForTesting
  public void setAntiAliasing(com.google.android.filament.View.AntiAliasing antiAliasing) {
    filamentView.setAntiAliasing(antiAliasing);
  }

  /** @hide Only used for scuba testing for now. */
  @VisibleForTesting
  public void setDithering(com.google.android.filament.View.Dithering dithering) {
    filamentView.setDithering(dithering);
  }

  /** @hide Used internally by ArSceneView. */

  public void setPostProcessingEnabled(boolean enablePostProcessing) {return ;}



  /** @hide Used internally by ArSceneView */

  public void setRenderQuality(com.google.android.filament.View.RenderQuality renderQuality) {return ;}



  /**
   * Sets a high performance configuration for the filament view. Disables MSAA, disables
   * post-process, disables dynamic resolution, sets quality to 'low'.
   *
   * @hide Used internally by ArSceneView
   */

  public void enablePerformanceMode() {return ;}









  /**
   * Getter to help convert between filament and Environmental HDR.
   *
   * @hide This may be removed in the future
   */
  public EnvironmentalHdrParameters getEnvironmentalHdrParameters() {
    return environmentalHdrParameters;
  }

  /**
   * Setter to help convert between filament and Environmental HDR.
   *
   * @hide This may be removed in the future
   */
  public void setEnvironmentalHdrParameters(EnvironmentalHdrParameters environmentalHdrParameters) {
    this.environmentalHdrParameters = environmentalHdrParameters;
  }

  /** @hide UiHelper.RendererCallback implementation */
  @Override
  public void onResized(int width, int height) {
    filamentView.setViewport(new Viewport(0, 0, width, height));
//    emptyView.setViewport(new Viewport(0, 0, width, height));
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
    return viewAttachmentManager;
  }

  @SuppressWarnings("AndroidApiChecker") // CompletableFuture
  private void initialize() {
    AndroidPreconditions.checkEngineThread();
    SurfaceView surfaceView = getSurfaceView();

    filamentHelper = new EqUiHelper(EqUiHelper.ContextErrorPolicy.DONT_CHECK);
    filamentHelper.setRenderCallback(this);
    //使用TextureView时，需要实现透明背景
//    filamentHelper.setOpaque(false);
//    filamentHelper.setMediaOverlay(true);//TODO CHECK 影响SurfaceView
    filamentHelper.attachTo(surfaceView);

    IEngine engine = EngineInstance.getEngine();

    renderer = engine.createRenderer();
    scene = engine.createScene();
    filamentView = engine.createView();
//    emptyView = engine.createView();
    filamentCamera = engine.createCamera();
    setUseHdrLightEstimate(false);

    setDefaultClearColor();
    filamentView.setCamera(filamentCamera);
    filamentView.setScene(scene);

    setDynamicResolutionEnabled(true);

//    emptyView.setCamera(engine.createCamera());
//    emptyView.setScene(engine.createScene());
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
    filamentCamera.setExposure(cameraAperature, cameraShutterSpeed, cameraIso);
  }

  /**
   * Returns the exposure setting for renderering.
   *
   * @hide This is support deeplight API which is not stable yet.
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
      renderableInstance.prepareForDraw();

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
   * Releases rendering resources ready for garbage collection
   *
   * @return Count of resources currently in use
   */
  public static long reclaimReleasedResources() {
    return ResourceManager.getInstance().reclaimReleasedResources();
  }

  /** Immediately releases all rendering resources, even if in use. */
  public static void destroyAllResources() {
    ResourceManager.getInstance().destroyAllResources();
    EngineInstance.destroyEngine();
  }
}
