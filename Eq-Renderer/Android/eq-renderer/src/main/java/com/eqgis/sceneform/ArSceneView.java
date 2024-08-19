package com.eqgis.sceneform;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.eqgis.sceneform.math.Quaternion;
import com.eqgis.sceneform.math.Vector3;
import com.eqgis.sceneform.rendering.CameraStream;
import com.eqgis.sceneform.rendering.Color;
import com.eqgis.sceneform.rendering.EnvironmentalHdrLightEstimate;
import com.eqgis.sceneform.rendering.GLHelper;
import com.eqgis.sceneform.rendering.PlaneRenderer;
import com.eqgis.sceneform.rendering.Renderer;
import com.eqgis.sceneform.rendering.ThreadPools;
import com.eqgis.sceneform.utilities.AndroidPreconditions;
import com.eqgis.sceneform.utilities.Preconditions;
import com.eqgis.ar.ARAnchor;
import com.eqgis.ar.ARCamera;
import com.eqgis.ar.ARPlugin;
import com.eqgis.ar.ARFrame;
import com.eqgis.ar.ARLightEstimate;
import com.eqgis.ar.ARPose;
import com.eqgis.ar.ARSession;
import com.eqgis.ar.TrackingState;
import com.eqgis.ar.exceptions.ARCameraException;

import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * AR模式的场景视图
 * <p>
 *     当前支持ARCore和AREngine。
 *     默认底层自动根据设备型号判断使用ARCore还是AREngine
 * </p>
 * 对于2020年之前发布的部分华为手机，既支持ARCore，也支持AREngine。
 * 默认使用AREngine，若要强制使用ARCore，
 * 需在初始化前执行{@link ARPlugin#enforceARCore()}
 */
@SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
public class ArSceneView extends SceneView {
    private static final String TAG = ArSceneView.class.getSimpleName();
    private static final String REPORTED_ENGINE_TYPE = "Sceneform";
    private static final float DEFAULT_PIXEL_INTENSITY = 1.0f;
    private static final Color DEFAULT_COLOR_CORRECTION = new Color(1, 1, 1);

    /**
     * 当相机移动了这个距离，我们创建一个新的锚，我们连接Hdr灯光
     */
    private static final float RECREATE_LIGHTING_ANCHOR_DISTANCE = 0.5f;
    private final Color lastValidColorCorrection = new Color(DEFAULT_COLOR_CORRECTION);
    private final float[] colorCorrectionPixelIntensity = new float[4];
    // pauseResumeTask is modified on the main thread only.  It may be completed on background
    // threads however.
    private final SequentialTask pauseResumeTask = new SequentialTask();
    private int cameraTextureId;
    @Nullable private ARSession session;
    @Nullable private ARFrame currentFrame;
    //    @Nullable private ARConfig cachedConfig;
//    private int minArCoreVersionCode;
    private Display display;
    private CameraStream cameraStream;
    private PlaneRenderer planeRenderer;
    private Image depthImage;
    private boolean lightEstimationEnabled = true;
    private boolean isLightDirectionUpdateEnabled = true;
    @Nullable private Consumer<EnvironmentalHdrLightEstimate> onNextHdrLightingEstimate = null;
    private float lastValidPixelIntensity = DEFAULT_PIXEL_INTENSITY;
    @Nullable private ARAnchor lastValidEnvironmentalHdrAnchor;
    @Nullable private float[] lastValidEnvironmentalHdrAmbientSphericalHarmonics;
    @Nullable private float[] lastValidEnvironmentalHdrMainLightDirection;
    @Nullable private float[] lastValidEnvironmentalHdrMainLightIntensity;

    /**
     * 构造函数
     * @param context the Android Context to use
     * @see #ArSceneView(Context, AttributeSet)
     */
    @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
    public ArSceneView(Context context) {
        // SceneView will initialize the scene, renderer, and camera.
        super(context);
        Renderer renderer = Preconditions.checkNotNull(getRenderer());
        renderer.enablePerformanceMode();
        initializeAr();
    }

    /**
     * 构造函数
     * @param context the Android Context to use
     * @param attrs   the Android AttributeSet to associate with
     */
    @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
    public ArSceneView(Context context, AttributeSet attrs) {
        // SceneView will initialize the scene, renderer, and camera.
        super(context, attrs);
        Renderer renderer = Preconditions.checkNotNull(getRenderer());
        renderer.enablePerformanceMode();
        initializeAr();
    }

    private static boolean loadUnifiedJni() {
        return false;
    }

    private static native void nativeReportEngineType(
            ARSession session, String engineType, String engineVersion);

    /**
     * 配置ARSession
     * @param session ARSession
     * @see #ArSceneView(Context, AttributeSet)
     */
    public void setupSession(ARSession session) {
        if (this.session != null) {
            Log.w(TAG, "The session has already been setup, cannot set it up again.");
            return;
        }
        // Enforce api level 24
        AndroidPreconditions.checkMinAndroidApiLevel();

        this.session = session;

        Renderer renderer = Preconditions.checkNotNull(getRenderer());
        int width = renderer.getDesiredWidth();
        int height = renderer.getDesiredHeight();
        if (width != 0 && height != 0) {
            session.setDisplayGeometry(display.getRotation(), width, height);
        }

//        initializeFacingDirection(session);//AREngine的cameraConfig不支持获取朝向，暂时注释掉

        // Set the correct Texture configuration on the camera stream
        cameraStream.checkIfDepthIsEnabled(session);

        // Session needs access to a texture id for updating the camera stream.
        // Filament and the Main thread each have their own gl context that share resources for this.
        session.setCameraTextureName(cameraTextureId);
    }

//    private void initializeFacingDirection(ARSession session) {
//        if (session.getCameraConfig().getFacingDirection() == FacingDirection.FRONT) {
//            Renderer renderer = Preconditions.checkNotNull(getRenderer());
//            renderer.setFrontFaceWindingInverted(true);
//        }
//    }

    /**
     * 对ARSession执行resume操作
     * @throws Exception if the camera can not be opened
     */
    @Override
    public void resume(){
        //更新屏幕旋转角度
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int rotation = wm.getDefaultDisplay().getRotation();
        ARCamera.setSurfaceRotation(rotation);
        try {
            resumeSession();
        } catch (ARCameraException e) {
            throw new ARCameraException(e);
        }
        resumeScene();
    }

    /**
     * 通过异步的方式对ARSession执行resume操作
     */
    public CompletableFuture<Void> resumeAsync(Executor executor) {
        final WeakReference<ArSceneView> currentSceneView = new WeakReference<>(this);
        pauseResumeTask.appendRunnable(
                () -> {
                    ArSceneView arSceneView = currentSceneView.get();
                    if (arSceneView == null) {
                        return;
                    }
                    try {
                        arSceneView.resumeSession();
                    } catch (ARCameraException e) {
                        throw new RuntimeException(e);
                    }
                },
                executor);

        return pauseResumeTask.appendRunnable(
                () -> {
                    ArSceneView arSceneView = currentSceneView.get();
                    if (arSceneView == null) {
                        return;
                    }
                    arSceneView.resumeScene();
                },
                ThreadPools.getMainExecutor());
    }

    /**
     * 恢复ARSession
     */
    private void resumeSession() throws ARCameraException {
        ARSession session = this.session;
        if (session != null) {
            reportEngineType();
            session.resume();
        }
    }

    /**
     * 恢复场景
     */
    private void resumeScene() {
        try {
            super.resume();
        } catch (Exception ex) {
            // This exception should not be possible from here
            throw new IllegalStateException(ex);
        }
    }

    /**
     * 暂停场景
     * <p>
     *     暂停渲染和ARSession
     * </p>
     */
    @Override
    public void pause() {
        pauseScene();
        pauseSession();
    }

    /**
     * 通过异步的方式暂停场景
     */
    public CompletableFuture<Void> pauseAsync(Executor executor) {
        final WeakReference<ArSceneView> currentSceneView = new WeakReference<>(this);
        pauseResumeTask.appendRunnable(
                () -> {
                    ArSceneView arSceneView = currentSceneView.get();
                    if (arSceneView == null) {
                        return;
                    }
                    arSceneView.pauseScene();
                },
                ThreadPools.getMainExecutor());

        return pauseResumeTask
                .appendRunnable(
                        () -> {
                            ArSceneView arSceneView = currentSceneView.get();
                            if (arSceneView == null) {
                                return;
                            }
                            arSceneView.pauseSession();
                        },
                        executor)
                .thenAcceptAsync(
                        // Ensure the final completed future is on the main thread.
                        notUsed -> {
                        },
                        ThreadPools.getMainExecutor());
    }

    /**
     * 暂停ARSession
     */
    private void pauseSession() {
        if (session != null) {
            session.pause();
        }
    }

    /**
     * 暂停场景
     */
    private void pauseScene() {
        super.pause();
        if (depthImage != null) {
            depthImage.close();
            depthImage = null;
        }
    }

    /**
     * @hide
     */
    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (session != null) {
            int width = right - left;
            int height = bottom - top;
            session.setDisplayGeometry(display.getRotation(), width, height);
        }
    }

    /**
     * @return 如果启用了光线估计，则返回true。
     */
    public boolean isLightEstimationEnabled() {
        return lightEstimationEnabled;
    }

    /**
     * 启用基于相机馈送的光线估计。太阳的颜色和强度是间接的
     * 光将由ARCore的光估计提供的值调制。中的点亮对象
     * 场景将受到影响。
     * @param enable 设置为true以启用光估计或false使用默认估计，
     *               表示像素强度为1.0，颜色校正值为白色(1.0,1.0,1.0)。
     */
    public void setLightEstimationEnabled(boolean enable) {
        lightEstimationEnabled = enable;
        if (!lightEstimationEnabled) {
            // Update the light probe with the current best light estimate.
            getScene().setLightEstimate(DEFAULT_COLOR_CORRECTION, DEFAULT_PIXEL_INTENSITY);
            lastValidPixelIntensity = DEFAULT_PIXEL_INTENSITY;
            lastValidColorCorrection.set(DEFAULT_COLOR_CORRECTION);
        }
    }

    /**
     * 获取AR会话
     */
    @Nullable
    public ARSession getSession() {
        return session;
    }

    /**
     * 获取AR帧
     */
    @Nullable
    @UiThread
    public ARFrame getArFrame() {
        return currentFrame;
    }

    /**
     * 获取平面渲染对象
     */
    public PlaneRenderer getPlaneRenderer() {
        return planeRenderer;
    }

    /**
     * 返回CameraStream，用于控制是否启用或禁用遮挡。
     * <p>内部方法</p>
     */
    public CameraStream getCameraStream() { return cameraStream; }

    /**
     * 渲染前触发
     * @hide
     */
    @SuppressWarnings("AndroidApiChecker")
    @Override
    public boolean onBeginFrame(long frameTimeNanos) {
        // No session, no drawing.
        ARSession session = this.session;
        if (session == null) {
            return false;
        }

        if (!pauseResumeTask.isDone()) {
            return false;
        }

        ensureUpdateMode();

        //从ARCore更新Frame。
        boolean updated = true;
        try {
            ARFrame frame = session.update();
            // No frame, no drawing.
            if (frame == null) {
                return false;
            }

            //配置相机纹理
            if (!cameraStream.isTextureInitialized()) {
                cameraStream.initializeTexture(frame);
            }

            //重新计算UV
            if (shouldRecalculateCameraUvs(frame)) {
                cameraStream.recalculateCameraUvs(frame);
            }

            if (currentFrame != null && currentFrame.getTimestampNs() == frame.getTimestampNs()) {
                updated = false;
            }

            currentFrame = frame;
        } catch (ARCameraException e) {
            Log.w(TAG, "Exception updating AR session", e);
            return false;
        }

        // No camera, no drawing.
        ARCamera currentArCamera = currentFrame.getCamera();
        if (currentArCamera == null) {
            getScene().setUseHdrLightEstimate(false);
            return false;
        }

        //如果ARCore会话已更改，则更新
        if (updated) {
            //更新相机跟踪姿态
            //todo
//            getScene().getCamera().updateTrackedPose(currentArCamera);
            //updated by Ikkyu(tanyx)
            if (customPoseUpdateListener != null){
                customPoseUpdateListener.onUpdate(currentArCamera);
            }else {
                getScene().getCamera().updateTrackedPose(currentArCamera);
            }

            ARFrame frame = currentFrame;
            if (frame != null) {
                if (ARPlatForm.OCCLUSION_MODE == ARPlatForm.OcclusionMode.OCCLUSION_ENABLED && super.customDepthImage != null){
                    //added by Ikkyu 2022/01/24，custom DepthImage
//                    cameraStream.depthMode = CameraStream.DepthMode.DEPTH;
                    cameraStream.recalculateOcclusion(customDepthImage);//use
                }else {
                    if(cameraStream.getDepthOcclusionMode() == CameraStream.DepthOcclusionMode.DEPTH_OCCLUSION_ENABLED) {
                        if (cameraStream.getDepthMode() == CameraStream.DepthMode.DEPTH) {
                            try (Image depthImage = currentFrame.acquireDepthImage()) {
                                cameraStream.recalculateOcclusion(depthImage);
                            } catch (Exception e) {
                                Log.w(TAG, "Exception acquiring RawDepthImage", e);
                            }
                        }
                        //备注：AREngine不支持获取raw
                        else if (cameraStream.getDepthMode() == CameraStream.DepthMode.RAW_DEPTH) {
                            try (Image depthImage = currentFrame.acquireRawDepthImage()) {
                                cameraStream.recalculateOcclusion(depthImage);
                            } catch (Exception e) {
                                Log.w(TAG, "Exception acquiring RawDepthImage", e);
                            }
                        }
                    }
                }

                //更新光照估计
                updateLightEstimate(frame);
                //更新平面渲染对象
                if (planeRenderer.isEnabled()) {
                    planeRenderer.update(frame, getWidth(), getHeight());
                }
            }
        }

        return updated;
    }

    /***********************=====added by Ikkyu(tanyx)=====TOP============****************/
    /**
     * 自定义姿态更新监听
     */
    private CustomPoseUpdateListener customPoseUpdateListener;
    public interface CustomPoseUpdateListener {
        /**
         * <p>
         *     备注：实现的方法中应该包括{@link Camera#updateTrackedPose(ARCamera, Vector3, Quaternion)}的调用
         * </p>
         * @param arCamera
         * @return
         */
        ARPose onUpdate(ARCamera arCamera);
    }

    public void setCustomPoseUpdateListener(CustomPoseUpdateListener customPoseUpdateListener) {
        this.customPoseUpdateListener = customPoseUpdateListener;
    }

    /***********************=====added by Ikkyu(tanyx)=====BOTTOM=========****************/
    @Override
    public void doFrame(long frameTimeNanos) {
        super.doFrame(frameTimeNanos);
    }

    private boolean shouldRecalculateCameraUvs(ARFrame frame) {
        return frame.hasDisplayGeometryChanged();
    }

    /**
     * 从帧中获得AR光估计，然后更新场景。
     */
    private void updateLightEstimate(ARFrame frame) {
        // Just return if Light Estimation is disabled.
        if (!lightEstimationEnabled || getSession() == null) {
            return;
        }

        // Update the Light Probe with the new light estimate.
        ARLightEstimate estimate = frame.getLightEstimate();

        if (isEnvironmentalHdrLightingAvailable()) {
            if (frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
                updateHdrLightEstimate(
                        estimate, Preconditions.checkNotNull(getSession()), frame.getCamera());
            }
        } else {
            updateNormalLightEstimate(estimate);
        }
    }

    /**
     * 根据环境HDR照明估算，检查每一帧的阳光是否被更新。
     *
     * @return true if the sunlight direction is updated every frame, false otherwise.
     */

    public boolean isLightDirectionUpdateEnabled() {
        return isLightDirectionUpdateEnabled;
    }

    /**
     * 设置环境HDR照明产生的阳光方向是否更新
     * 每帧。如果为false，则光照方向将更新一次，然后不再更新
     * 改变。
     * <p>这可用于关闭阴影方向更新时，他们分散或不需要的。
     * <p>默认状态为true，每帧更新阳光方向。
     */

    public void setLightDirectionUpdateEnabled(boolean isLightDirectionUpdateEnabled) {
        this.isLightDirectionUpdateEnabled = isLightDirectionUpdateEnabled;
    }

    /**
     * 如果ARCore相机配置了
     * Config.LightEstimationMode.ENVIRONMENTAL_HDR。当环境HDR照明模式启用时，
     * 产生的光线估计将应用于Sceneform场景。
     * @return true如果HDR照明在Sceneform中启用，因为ARCore HDR照明估计是启用。
     */

    public boolean isEnvironmentalHdrLightingAvailable() {
        return session.isHDR();
    }

    /**
     * 捕获光估计的值
     * @hide
     */
    public void captureLightingValues(
            Consumer<EnvironmentalHdrLightEstimate> onNextHdrLightingEstimate) {
        this.onNextHdrLightingEstimate = onNextHdrLightingEstimate;
    }

    void updateHdrLightEstimate(
            ARLightEstimate estimate, ARSession session, ARCamera camera) {
        if (estimate.getState() != ARLightEstimate.State.VALID) {
            return;
        }
        getScene().setUseHdrLightEstimate(true);

        //如果还没有获得方向，就不应该跳过更新方向。
        if (isLightDirectionUpdateEnabled || lastValidEnvironmentalHdrMainLightDirection == null) {
            boolean needsNewAnchor = false;

            //如果当前的锚为hdr光方向没有跟踪，或者我们已经移动太远
            //那么我们就需要一个新的锚来确定光的方向。
            if (lastValidEnvironmentalHdrAnchor == null
                    || lastValidEnvironmentalHdrAnchor.getTrackingState() != TrackingState.TRACKING) {
                needsNewAnchor = true;
            } else {
                ARPose cameraPose = camera.getPose();
                Vector3 cameraPosition = new Vector3(cameraPose.tx(), cameraPose.ty(), cameraPose.tz());
                ARPose anchorPose = Preconditions.checkNotNull(lastValidEnvironmentalHdrAnchor).getPose();
                Vector3 anchorPosition = new Vector3(anchorPose.tx(), anchorPose.ty(), anchorPose.tz());
                needsNewAnchor =
                        Vector3.subtract(cameraPosition, anchorPosition).length()
                                > RECREATE_LIGHTING_ANCHOR_DISTANCE;
            }

            //如果我们需要一个新的锚点，我们就会破坏当前的锚点，并尝试创建一个新的锚点。如果
            //ARCore会话正在跟踪，如果失败，我们将停止更新深度估计，直到我们再次开始跟踪。
            if (needsNewAnchor) {
                if (lastValidEnvironmentalHdrAnchor != null) {
                    lastValidEnvironmentalHdrAnchor.detach();
                    lastValidEnvironmentalHdrAnchor = null;
                }
                lastValidEnvironmentalHdrMainLightDirection = null;
                if (camera.getTrackingState() == TrackingState.TRACKING) {
                    try {
                        lastValidEnvironmentalHdrAnchor = session.createAnchor(camera.getPose());
                    } catch (Exception e) {
                        // Hopefully this exception is not truly fatal.
                        Log.e(TAG, "Error trying to create environmental hdr anchor", e);
                    }
                }
            }

            //如果我们有一个有效的锚点，我们根据当前的光估计更新锚点相关的局部方向。
            try {
                if (lastValidEnvironmentalHdrAnchor != null) {
                    float[] mainLightDirection = estimate.getEnvironmentalHdrMainLightDirection();
                    if (mainLightDirection != null) {
                        ARPose anchorPose = Preconditions.checkNotNull(lastValidEnvironmentalHdrAnchor).getPose();
                        lastValidEnvironmentalHdrMainLightDirection =
                                anchorPose.inverse().rotateVector(mainLightDirection);
                    }
                }
            }catch (Exception e){
                Log.w(TAG, "updateHdrLightEstimate: ", e);
            }
        }

        try{
            float[] sphericalHarmonics = estimate.getEnvironmentalHdrAmbientSphericalHarmonics();
            if (sphericalHarmonics != null) {
                lastValidEnvironmentalHdrAmbientSphericalHarmonics = sphericalHarmonics;
            }

            float[] mainLightIntensity = estimate.getEnvironmentalHdrMainLightIntensity();
            if (mainLightIntensity != null) {
                lastValidEnvironmentalHdrMainLightIntensity = mainLightIntensity;
            }
        }catch (Exception e){
            //若不支持HDR，则捕获
            Log.w(TAG, "updateHdrLightEstimate: Not support HDR", e);
        }

        if (lastValidEnvironmentalHdrAnchor == null
                || lastValidEnvironmentalHdrMainLightIntensity == null
                || lastValidEnvironmentalHdrAmbientSphericalHarmonics == null
                || lastValidEnvironmentalHdrMainLightDirection == null) {
            return;
        }

        float mainLightIntensityScalar =
                Math.max(
                        1.0f,
                        Math.max(
                                Math.max(
                                        lastValidEnvironmentalHdrMainLightIntensity[0],
                                        lastValidEnvironmentalHdrMainLightIntensity[1]),
                                lastValidEnvironmentalHdrMainLightIntensity[2]));

        final Color mainLightColor =
                new Color(
                        lastValidEnvironmentalHdrMainLightIntensity[0] / mainLightIntensityScalar,
                        lastValidEnvironmentalHdrMainLightIntensity[1] / mainLightIntensityScalar,
                        lastValidEnvironmentalHdrMainLightIntensity[2] / mainLightIntensityScalar);

        try {
            Image[] cubeMap = estimate.acquireEnvironmentalHdrCubeMap();

            //我们计算相对于跟踪锚的当前位置的世界空间方向。
            ARPose anchorPose = Preconditions.checkNotNull(lastValidEnvironmentalHdrAnchor).getPose();
            float[] currentLightDirection =
                    anchorPose.rotateVector(
                            Preconditions.checkNotNull(lastValidEnvironmentalHdrMainLightDirection));

            if (onNextHdrLightingEstimate != null) {
                EnvironmentalHdrLightEstimate lightEstimate =
                        new EnvironmentalHdrLightEstimate(
                                lastValidEnvironmentalHdrAmbientSphericalHarmonics,
                                currentLightDirection,
                                mainLightColor,
                                mainLightIntensityScalar,
                                cubeMap);
                onNextHdrLightingEstimate.accept(lightEstimate);
                onNextHdrLightingEstimate = null;
            }

            getScene()
                    .setEnvironmentalHdrLightEstimate(
                            lastValidEnvironmentalHdrAmbientSphericalHarmonics,
                            currentLightDirection,
                            mainLightColor,
                            mainLightIntensityScalar,
                            cubeMap);
            for (Image cubeMapImage : cubeMap) {
                cubeMapImage.close();
            }
        }catch (Exception e){
            //若不支持HDR，则捕获
            Log.w(TAG, "updateHdrLightEstimate: Not support HDR", e);
        }
    }

    private void updateNormalLightEstimate(ARLightEstimate estimate) {
        getScene().setUseHdrLightEstimate(false);
        //验证估计是否有效
        float pixelIntensity = lastValidPixelIntensity;
        // Only update the estimate if it is valid.
        if (estimate.getState() == ARLightEstimate.State.VALID) {
            estimate.getColorCorrection(colorCorrectionPixelIntensity, 0);
            pixelIntensity = Math.max(colorCorrectionPixelIntensity[3], 0.0f);
            lastValidColorCorrection.set(
                    colorCorrectionPixelIntensity[0],
                    colorCorrectionPixelIntensity[1],
                    colorCorrectionPixelIntensity[2]);
        }
        //用当前最佳光估计更新光探针数据。
        getScene().setLightEstimate(lastValidColorCorrection, pixelIntensity);
        //更新最后的有效估计。
        lastValidPixelIntensity = pixelIntensity;
    }

    private void initializeAr() {
        //updated by ikkyu 替换兼容之前的修改，
        setPlatForm();

        display = getContext().getSystemService(WindowManager.class).getDefaultDisplay();

        initializePlaneRenderer();
        initializeCameraStream();
    }

    /**
     * 设置AR平台
     * 默认为根据设备厂商选择
     */
    private void setPlatForm() {
        //AREngine.enforceARCore();//若要强制使用ARCore，必须在此之前调用
        if (!ARPlugin.isARApkReady(getContext())){
            ARPlugin.installARApk(getContext());
        }
    }

    private void initializePlaneRenderer() {
        Renderer renderer = Preconditions.checkNotNull(getRenderer());
        planeRenderer = new PlaneRenderer(renderer);
    }

    private void initializeCameraStream() {
        cameraTextureId = GLHelper.createCameraTexture();
        Renderer renderer = Preconditions.checkNotNull(getRenderer());
        cameraStream = new CameraStream(cameraTextureId, renderer);
    }

    private void ensureUpdateMode() {
        if (session == null) {
            return;
        }

        // Check the update mode.//不检查，不更新，O(∩_∩)O哈哈~
//        if (minArCoreVersionCode >= ArCoreVersion.VERSION_CODE_1_3) {
//            if (cachedConfig == null) {
//                cachedConfig = session.getConfig();
//            } else {
//                session.getConfig(cachedConfig);
//            }
//
//            Config.UpdateMode updateMode = cachedConfig.getUpdateMode();
//            if (updateMode != Config.UpdateMode.LATEST_CAMERA_IMAGE) {
//                throw new RuntimeException(
//                        "Invalid ARCore UpdateMode "
//                                + updateMode
//                                + ", Sceneform requires that the ARCore session is configured to the "
//                                + "UpdateMode LATEST_CAMERA_IMAGE.");
//            }
//        }
    }

    private void reportEngineType() {
        return;
    }
}
