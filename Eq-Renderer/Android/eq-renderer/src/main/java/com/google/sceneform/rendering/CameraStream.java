package com.google.sceneform.rendering;

import android.media.Image;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.sceneform.ARPlatForm;
import com.google.sceneform.CustomDepthImage;
import com.google.sceneform.utilities.AndroidPreconditions;
import com.google.sceneform.utilities.Preconditions;
import com.google.android.filament.EntityManager;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.IndexBuffer.Builder.IndexType;
import com.google.android.filament.MaterialInstance;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.Scene;
import com.google.android.filament.Texture;
import com.google.android.filament.VertexBuffer;
import com.google.android.filament.VertexBuffer.Builder;
import com.google.android.filament.VertexBuffer.VertexAttribute;
import com.eqgis.ar.ARFrame;
import com.eqgis.ar.ARSession;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * 相机视频流对象
 * <p>
 *     用于在filament中显示视频背景
 * </p>
 *
 * @hide 内部使用
 */
@SuppressWarnings("AndroidApiChecker") // CompletableFuture
public class CameraStream {
    public static final String MATERIAL_CAMERA_TEXTURE = "cameraTexture";
    public static final String MATERIAL_DEPTH_TEXTURE = "depthTexture";

    private static final String TAG = CameraStream.class.getSimpleName();

    private static final int POSITION_BUFFER_INDEX = 0;
    private static final int UV_BUFFER_INDEX = 1;//000
//    private static final int VERTEX_COUNT = 3;
//    private static final float[] CAMERA_VERTICES =
//            new float[]{
//                    -1.0f, 1.0f,
//                    1.0f, -1.0f,
//                    -3.0f, 1.0f,
//                    3.0f, 1.0f,
//                    1.0f};
//    private static final float[] CAMERA_UVS = new float[]{
//            0.0f, 0.0f,
//            0.0f, 2.0f,
//            2.0f, 0.0f};
//    private static final short[] INDICES = new short[]{0, 1, 2};

    private static final int FLOAT_SIZE_IN_BYTES = Float.SIZE / 8;
    private static final int UNINITIALIZED_FILAMENT_RENDERABLE = -1;//00

    private final Scene scene;
    private final int cameraTextureId;
    private final IndexBuffer cameraIndexBuffer;
    private final VertexBuffer cameraVertexBuffer;
    private final FloatBuffer cameraUvCoords;
    private final FloatBuffer transformedCameraUvCoords;
    private final IEngine engine;
    private int cameraStreamRenderable = UNINITIALIZED_FILAMENT_RENDERABLE;

    /** 启用深度API的状态，默认不启用{@link DepthMode#NO_DEPTH} */
    public DepthMode depthMode = DepthMode.NO_DEPTH;
    /** 启用深度遮挡 {@link DepthOcclusionMode#DEPTH_OCCLUSION_DISABLED} */
    private DepthOcclusionMode depthOcclusionMode = DepthOcclusionMode.DEPTH_OCCLUSION_DISABLED;

    @Nullable private ExternalTexture cameraTexture;
    @Nullable private DepthTexture depthTexture;

    @Nullable private Material cameraMaterial = null;
    @Nullable private Material occlusionCameraMaterial = null;
//    @Nullable private Material defaultCameraMaterial = null;

    private int renderablePriority = Renderable.RENDER_PRIORITY_LAST;

    private boolean isTextureInitialized = false;

    //added
    private static final short[] ARCORE_CAMERA_INDICES = new short[] {0, 1, 2};
    private static final short[] HUAWEI_CAMERA_INDICES = new short[] {0, 1, 2,3,2,1};
//    private static final short[] HUAWEI_CAMERA_INDICES = new short[] {0, 1, 2,1,2,3};

    private static final float[] ARCORE_CAMERA_VERTICES =
            new float[] {-1.0f, 1.0f, 1.0f,
                    -1.0f, -3.0f, 1.0f,
                    3.0f, 1.0f, 1.0f};
    private static final float[] ARCORE_CAMERA_UVS = new float[] {0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 0.0f};

    private static final float[] HUAWEI_CAMERA_VERTICES =
            new float[] {-1.0f, 1.0f, 1.0f,      -1.0f, -1.0f,1.0f,     1.0f, 1.0f,1.0f,    1.0f, -1.0f,1.0f};;
    private static final float[] HUAWEI_CAMERA_UVS = new float[] {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f};

    private static final int ARCORE_VERTEX_COUNT = 3;
    private static final int HUAWEI_VERTEX_COUNT = 4;

    /******************************************/

    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored", "initialization"})
    public CameraStream(int cameraTextureId, Renderer renderer) {
        scene = renderer.getFilamentScene();
        this.cameraTextureId = cameraTextureId;

        engine = EngineInstance.getEngine();

        if (/*1*/ARPlatForm.isArCore()){
            // create screen quad geometry to camera stream to
            ShortBuffer indexBufferData = ShortBuffer.allocate(ARCORE_CAMERA_INDICES.length);
            indexBufferData.put(ARCORE_CAMERA_INDICES);
            final int indexCount = indexBufferData.capacity();
            cameraIndexBuffer =
                    new IndexBuffer.Builder()
                            .indexCount(indexCount)
                            .bufferType(IndexType.USHORT)
                            .build(engine.getFilamentEngine());
            indexBufferData.rewind();
            Preconditions.checkNotNull(cameraIndexBuffer)
                    .setBuffer(engine.getFilamentEngine(), indexBufferData);

            // Note: ARCore expects the UV buffers to be direct or will assert in transformDisplayUvCoords.
            cameraUvCoords = createCameraUVBuffer();
            transformedCameraUvCoords = createCameraUVBuffer();

            FloatBuffer vertexBufferData = FloatBuffer.allocate(ARCORE_CAMERA_VERTICES.length);
            vertexBufferData.put(ARCORE_CAMERA_VERTICES);

            cameraVertexBuffer =
                    new Builder()
                            .vertexCount(ARCORE_VERTEX_COUNT)
                            .bufferCount(2)
                            .attribute(
                                    VertexAttribute.POSITION,
                                    0,
                                    VertexBuffer.AttributeType.FLOAT3,
                                    0,
                                    (ARCORE_CAMERA_VERTICES.length / ARCORE_VERTEX_COUNT) * FLOAT_SIZE_IN_BYTES)
                            .attribute(
                                    VertexAttribute.UV0,
                                    1,
                                    VertexBuffer.AttributeType.FLOAT2,
                                    0,
                                    (ARCORE_CAMERA_UVS.length / ARCORE_VERTEX_COUNT) * FLOAT_SIZE_IN_BYTES)
                            .build(engine.getFilamentEngine());

            vertexBufferData.rewind();
            Preconditions.checkNotNull(cameraVertexBuffer)
                    .setBufferAt(engine.getFilamentEngine(), POSITION_BUFFER_INDEX, vertexBufferData);

            adjustCameraUvsForOpenGL();
            cameraVertexBuffer.setBufferAt(
                    engine.getFilamentEngine(), UV_BUFFER_INDEX, transformedCameraUvCoords);

            CompletableFuture<Material> materialFuture =
                    Material.builder()
                            .setSource(
                                    renderer.getContext(),
                                    RenderingResources.GetSceneformResource(
                                            renderer.getContext(), RenderingResources.Resource.CAMERA_MATERIAL))
                            .build();

            materialFuture
                    .thenAccept(
                            material -> {
//                                defaultCameraMaterial = material;
//
//                                // Only set the camera material if it hasn't already been set to a custom material.
//                                if (cameraMaterial == null) {
//                                    setCameraMaterial(defaultCameraMaterial);
//                                }
                                float[] uvTransform = new float[]{
                                        1,0,0,0,
                                        0,1,0,0,
                                        0,0,1,0,
                                        0,0,0,1
                                };
                                material.getFilamentMaterialInstance()
                                        .setParameter(
                                                "uvTransform",
                                                MaterialInstance.FloatElement.FLOAT4,
                                                uvTransform,
                                                0,
                                                4);

                                // Only set the camera material if it hasn't already been set to a custom material.
                                if(cameraMaterial == null) {
                                    cameraMaterial = material;
                                }
                            })
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Unable to load camera stream materials.", throwable);
                                return null;
                            });


        }
        else{
            //创建一个矩形对象
            ShortBuffer indexBufferData = ShortBuffer.allocate(HUAWEI_CAMERA_INDICES.length);
            indexBufferData.put(HUAWEI_CAMERA_INDICES);
            final int indexCount = indexBufferData.capacity();
            cameraIndexBuffer =
                    new IndexBuffer.Builder()
                            .indexCount(indexCount)
                            .bufferType(IndexType.USHORT)
                            .build(engine.getFilamentEngine());
            indexBufferData.rewind();
            Preconditions.checkNotNull(cameraIndexBuffer)
                    .setBuffer(engine.getFilamentEngine(), indexBufferData);

            // UV
            cameraUvCoords = createCameraUVBuffer();
            transformedCameraUvCoords = createCameraUVBuffer();

            FloatBuffer vertexBufferData = FloatBuffer.allocate(HUAWEI_CAMERA_VERTICES.length);
            vertexBufferData.put(HUAWEI_CAMERA_VERTICES);

            cameraVertexBuffer =
                    new Builder()
                            .vertexCount(HUAWEI_VERTEX_COUNT)
                            .bufferCount(2)
                            .attribute(
                                    VertexAttribute.POSITION,
                                    0,
                                    VertexBuffer.AttributeType.FLOAT3,
                                    0,
                                    (HUAWEI_CAMERA_VERTICES.length / HUAWEI_VERTEX_COUNT) * FLOAT_SIZE_IN_BYTES)
                            .attribute(
                                    VertexAttribute.UV0,
                                    1,
                                    VertexBuffer.AttributeType.FLOAT2,
                                    0,
                                    (HUAWEI_CAMERA_UVS.length / HUAWEI_VERTEX_COUNT) * FLOAT_SIZE_IN_BYTES)
                            .build(engine.getFilamentEngine());

            vertexBufferData.rewind();
            Preconditions.checkNotNull(cameraVertexBuffer)
                    .setBufferAt(engine.getFilamentEngine(), POSITION_BUFFER_INDEX, vertexBufferData);

            adjustCameraUvsForOpenGL();
            cameraVertexBuffer.setBufferAt(
                    engine.getFilamentEngine(), UV_BUFFER_INDEX, transformedCameraUvCoords);

            CompletableFuture<Material> materialFuture =
                    Material.builder()
                            .setSource(
                                    renderer.getContext(),
                                    RenderingResources.GetSceneformResource(
                                            renderer.getContext(), RenderingResources.Resource.CAMERA_MATERIAL))
                            .build();

            materialFuture
                    .thenAccept(
                            material -> {
//                                defaultCameraMaterial = material;
//
//                                // Only set the camera material if it hasn't already been set to a custom material.
//                                if (cameraMaterial == null) {
//                                    setCameraMaterial(defaultCameraMaterial);
//                                }
                                float[] uvTransform = new float[]{
                                        1,0,0,0,
                                        0,1,0,0,
                                        0,0,1,0,
                                        0,0,0,1
                                };
                                material.getFilamentMaterialInstance()
                                        .setParameter(
                                                "uvTransform",
                                                MaterialInstance.FloatElement.FLOAT4,
                                                uvTransform,
                                                0,
                                                4);

                                // Only set the camera material if it hasn't already been set to a custom material.
                                if(cameraMaterial == null) {
                                    cameraMaterial = material;
                                }
                            })
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Unable to load camera stream materials.", throwable);
                                return null;
                            });
        }

//        setupStandardCameraMaterial(renderer);
        setupOcclusionCameraMaterial(renderer);
    }

    private FloatBuffer createCameraUVBuffer() {
        FloatBuffer buffer;
        if (ARPlatForm.isArCore()/*2*/){
            buffer= ByteBuffer.allocateDirect(ARCORE_CAMERA_UVS.length * FLOAT_SIZE_IN_BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            buffer.put(ARCORE_CAMERA_UVS);
        }else {
            buffer= ByteBuffer.allocateDirect(HUAWEI_CAMERA_UVS.length * FLOAT_SIZE_IN_BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            buffer.put(HUAWEI_CAMERA_UVS);
        }

        buffer.rewind();

        return buffer;
    }

    void setupOcclusionCameraMaterial(Renderer renderer) {
        CompletableFuture<Material> materialFuture =
                Material.builder()
                        .setSource(
                                renderer.getContext(),
                                RenderingResources.GetSceneformResource(
                                        renderer.getContext(),
                                        RenderingResources.Resource.OCCLUSION_CAMERA_MATERIAL))
                        .build();
        materialFuture
                .thenAccept(
                        material -> {
                            float[] uvTransform = new float[]{
                                    1,0,0,0,
                                    0,1,0,0,
                                    0,0,1,0,
                                    0,0,0,1
                            };
//                            float[] uvTransform = Mat4.Companion.identity().toFloatArray();
                            material.getFilamentMaterialInstance()
                                    .setParameter(
                                            "uvTransform",
                                            MaterialInstance.FloatElement.FLOAT4,
                                            uvTransform,
                                            0,
                                            4);

                            // Only set the occlusion material if it hasn't already been set to a custom material.
                            if (occlusionCameraMaterial == null) {
                                occlusionCameraMaterial = material;
                            }
                        })
                .exceptionally(
                        throwable -> {
                            Log.e(TAG, "Unable to load camera stream materials.", throwable);
                            return null;
                        });
    }

    private void setCameraMaterial(Material material) {
        cameraMaterial = material;
        if (cameraMaterial == null)
            return;

        //ExternalTexture不能被创建，直到我们收到第一个ARFrame，以便我们
        //可以访问相机纹理的宽度和高度。如果ExternalTexture
        //尚未创建，不开始渲染，直到我们有一个有效的纹理。这将
        //创建ExternalTexture时再次调用。
        if (!isTextureInitialized()) {
            return;
        }

        cameraMaterial.setExternalTexture(
                MATERIAL_CAMERA_TEXTURE,
                Preconditions.checkNotNull(cameraTexture));
    }

    private void setOcclusionMaterial(Material material) {
        occlusionCameraMaterial = material;
        if (occlusionCameraMaterial == null)
            return;

        //ExternalTexture不能被创建，直到我们收到第一个ARFrame，以便我们
        //可以访问相机纹理的宽度和高度。如果ExternalTexture
        //尚未创建，不开始渲染，直到我们有一个有效的纹理。这将
        //创建ExternalTexture时再次调用。
        if (!isTextureInitialized()) {
            return;
        }

        occlusionCameraMaterial.setExternalTexture(
                MATERIAL_CAMERA_TEXTURE,
                Preconditions.checkNotNull(cameraTexture));
    }


    private void initOrUpdateRenderableMaterial(Material material) {
        if (!isTextureInitialized()) {
            return;
        }

        if (cameraStreamRenderable == UNINITIALIZED_FILAMENT_RENDERABLE) {
            initializeFilamentRenderable(material);
        } else {
            RenderableManager renderableManager = EngineInstance.getEngine().getRenderableManager();
            int renderableInstance = renderableManager.getInstance(cameraStreamRenderable);
            renderableManager.setMaterialInstanceAt(
                    renderableInstance, 0, material.getFilamentMaterialInstance());
        }
    }


    private void initializeFilamentRenderable(Material material) {
        // create entity id
        cameraStreamRenderable = EntityManager.get().create();

        // create the quad renderable (leave off the aabb)
        RenderableManager.Builder builder = new RenderableManager.Builder(1);
        builder
                .castShadows(false)
                .receiveShadows(false)
                .culling(false)
                // Always draw the camera feed last to avoid overdraw
                .priority(renderablePriority)
                .geometry(
                        0, RenderableManager.PrimitiveType.TRIANGLES, cameraVertexBuffer, cameraIndexBuffer)
                .material(0, Preconditions.checkNotNull(material).getFilamentMaterialInstance())
                .build(EngineInstance.getEngine().getFilamentEngine(), cameraStreamRenderable);

        // add to the scene
        scene.addEntity(cameraStreamRenderable);

        ResourceManager.getInstance()
                .getCameraStreamCleanupRegistry()
                .register(
                        this,
                        new CleanupCallback(
                                scene, cameraStreamRenderable, cameraIndexBuffer, cameraVertexBuffer));
    }

//    public void setCameraMaterialToDefault() {
//        if (defaultCameraMaterial != null) {
//            setCameraMaterial(defaultCameraMaterial);
//        } else {
//            // Default camera material hasn't been loaded yet, so just remove any custom material
//            // that has been set.
//            cameraMaterial = null;
//        }
//    }

    /**
     * 检查深度API启用状态
     * <p>需要手机支持深度API，请参考ARCore和AREngine的官方文档以查看支持Depth API的设备型号</p>
     * @param session {@link ARSession}
     */
    public void checkIfDepthIsEnabled(ARSession session) {
        depthMode = session.checkIfDepthIsEnabled();
    }


    public boolean isTextureInitialized() {
        return isTextureInitialized;
    }

    public void initializeTexture(ARFrame frame) {
        if (isTextureInitialized()) {
            return;
        }

//        ARCamera arCamera = frame.getCamera();
//        ARCameraIntrinsics intrinsics = arCamera.getCameraImageIntrinsics();
//        int[] dimensions = intrinsics.getImageDimensions();
//        int width ;//= dimensions[1];//1440;//
//        int height ;//= dimensions[0];//1080;//
////    int width = dimensions[0];//1440;//
////    int height = dimensions[1];//1080;//
//
//        if (ARPlatForm.isArCore()/*3*/){
//            width = dimensions[0];//1440;//
//            height = dimensions[1];//1080;//
//        }else{
////            width = dimensions[1];//1440;//
////            height = dimensions[0];//1080;//
//            //Ikkyu_memo, AREngine width must be > height
//            if (dimensions[0] == 0 && dimensions[1] == 0){
//                width = 1440;
//                height = 1080;
//            }else {
//                width = Math.max(dimensions[1],dimensions[0]);
//                height = Math.min(dimensions[1],dimensions[0]);
//            }
//        }
        //在使用1.22.x之后的filament，由于textureId的使用接口变更，这里不再需要w和h
        cameraTexture = new ExternalTexture(cameraTextureId);

        //updated by IKkyu 2022/01/22
        if (ARPlatForm.OCCLUSION_MODE != ARPlatForm.OcclusionMode.OCCLUSION_DISABLED
                ||(depthOcclusionMode == DepthOcclusionMode.DEPTH_OCCLUSION_ENABLED && (
                depthMode == DepthMode.DEPTH ||
                        depthMode == DepthMode.RAW_DEPTH))) {
            if (occlusionCameraMaterial != null) {
                isTextureInitialized = true;
                setOcclusionMaterial(occlusionCameraMaterial);
                initOrUpdateRenderableMaterial(occlusionCameraMaterial);
            }
        } else {
            if (cameraMaterial != null) {
                isTextureInitialized = true;
                setCameraMaterial(cameraMaterial);
                initOrUpdateRenderableMaterial(cameraMaterial);
            }
        }
    }

    /**
     * 纹理初始化
     * <p>
     *     本方法主要用于ExternalTexture做背景的场景，
     *     通过ExternalTexture的surface，我们可以绘制任何内容作为背景
     * </p>
     * Init BackgroundTexture , added by ikkyu 2022/04/29
     * @param externalTexture
     */
    public void initializeTexture(ExternalTexture externalTexture) {
        if (isTextureInitialized()) {
            return;
        }
        if (externalTexture == null)return;

        //用作相机纹理
        cameraTexture = externalTexture;

        if (ARPlatForm.OCCLUSION_MODE != ARPlatForm.OcclusionMode.OCCLUSION_DISABLED
                ||(depthOcclusionMode == DepthOcclusionMode.DEPTH_OCCLUSION_ENABLED && (
                depthMode == DepthMode.DEPTH ||
                        depthMode == DepthMode.RAW_DEPTH))) {
            if (occlusionCameraMaterial != null) {
                isTextureInitialized = true;
                setOcclusionMaterial(occlusionCameraMaterial);
                initOrUpdateRenderableMaterial(occlusionCameraMaterial);
            }
        } else {
            if (cameraMaterial != null) {
                isTextureInitialized = true;
                setCameraMaterial(cameraMaterial);
                initOrUpdateRenderableMaterial(cameraMaterial);
            }
        }
    }
    /**
     * <pre>
     *      更新深度图
     * </pre>
     *
     * @param depthImage {@link Image} 单通道的包含深度数据的安卓Image对象
     */
    public void recalculateOcclusion(Image depthImage) {
        if (occlusionCameraMaterial != null &&
                depthTexture == null) {
            depthTexture = new DepthTexture(
                    depthImage.getWidth(),
                    depthImage.getHeight());

            occlusionCameraMaterial.setDepthTexture(
                    MATERIAL_DEPTH_TEXTURE,
                    depthTexture);
        }

        if (occlusionCameraMaterial == null ||
                !isTextureInitialized ||
                depthImage == null) {
            return;
        }

        depthTexture.updateDepthTexture(depthImage);
    }

    /**
     * 更新深度图
     * added by Ikkyu 2022/01/22
     * modified by ikkyu 2022/10/24
     * @param depthImage 自定义的深度图数据对象
     */
    public void recalculateOcclusion(CustomDepthImage depthImage) {
        if (occlusionCameraMaterial != null &&
                depthTexture == null) {
            depthTexture = new DepthTexture(
                    depthImage.getWidth(),
                    depthImage.getHeight(), Texture.InternalFormat.RGBA8);

            occlusionCameraMaterial.setDepthTexture(
                    MATERIAL_DEPTH_TEXTURE,
                    depthTexture);
        }

        if (occlusionCameraMaterial == null ||
                !isTextureInitialized ||
                depthImage == null) {
            return;
        }
        try{
            depthTexture.updateDepthTexture(depthImage,Texture.Format.RGBA);
        }catch (BufferOverflowException e){
            depthTexture = null;
        }
    }


    public void recalculateCameraUvs(ARFrame frame) {
        IEngine engine = EngineInstance.getEngine();

        FloatBuffer cameraUvCoords = this.cameraUvCoords;
        FloatBuffer transformedCameraUvCoords = this.transformedCameraUvCoords;
        VertexBuffer cameraVertexBuffer = this.cameraVertexBuffer;
        frame.transformDisplayUvCoords(cameraUvCoords, transformedCameraUvCoords);
        adjustCameraUvsForOpenGL();
        cameraVertexBuffer.setBufferAt(
                engine.getFilamentEngine(), UV_BUFFER_INDEX, transformedCameraUvCoords);
    }


    private void adjustCameraUvsForOpenGL() {
        // Correct for vertical coordinates to match OpenGL
        if (ARPlatForm.isArCore()/*4*/){
            for (int i = 1; i < ARCORE_VERTEX_COUNT * 2; i += 2) {
                transformedCameraUvCoords.put(i, 1.0f - transformedCameraUvCoords.get(i));
            }
        }else{
            for (int i = 1; i < HUAWEI_VERTEX_COUNT * 2; i += 2) {
                transformedCameraUvCoords.put(i, 1.0f - transformedCameraUvCoords.get(i));
            }
        }
    }


    public int getRenderPriority() {
        return renderablePriority;
    }

    public void setRenderPriority(int priority) {
        renderablePriority = priority;
        if (cameraStreamRenderable != UNINITIALIZED_FILAMENT_RENDERABLE) {
            RenderableManager renderableManager = EngineInstance.getEngine().getRenderableManager();
            int renderableInstance = renderableManager.getInstance(cameraStreamRenderable);
            renderableManager.setPriority(renderableInstance, renderablePriority);
        }
    }

    /**
     * 根据设备支持的模式获取当前应用的深度模式。
     */
    public DepthMode getDepthMode() {
        return depthMode;
    }

    /**
     * 检查所选摄像机配置和AR配置是否支持该设备上提供的DepthOcclusionMode。当前支持的设备列表记录在ARCore支持的设备页面上。
     *
     * @param depthOcclusionMode 需要检查的深度模式。
     * @return 如果在AR会话配置中激活了深度模式，并且本设备支持提供的深度遮挡模式，则为True。
     */
    public boolean isDepthOcclusionModeSupported(DepthOcclusionMode depthOcclusionMode) {
        switch (depthOcclusionMode) {
            case DEPTH_OCCLUSION_ENABLED:
                return depthMode == DepthMode.DEPTH || depthMode == DepthMode.RAW_DEPTH;
            default:
                return true;
        }
    }

    /**
     * 获取当前深度遮挡模式
     *
     * @see #setDepthOcclusionMode
     * @see DepthOcclusionMode
     * @return 当前为camerastream定义的遮挡模式
     */
    public DepthOcclusionMode getDepthOcclusionMode() {
        return depthOcclusionMode;
    }


    /**
     * 设置深度API的调用模式
     * <pre>
     *     示例代码：
     *
     *     <code>
     *
     *     public void onAttachFragment(
     *         FragmentManager fragmentManager,
     *         Fragment fragment) {
     *         if (fragment.getId() == R.id.arFragment) {
     *             arFragment = (ArFragment) fragment;
     *             arFragment.setOnViewCreatedListener(this);
     *             arFragment.setOnSessionConfigurationListener(this);
     *         }
     *     }
     *
     *     public void onViewCreated(
     *         ArFragment arFragment,
     *         ArSceneView arSceneView) {
     *         arSceneView
     *            .getCameraStream()
     *            .setDepthModeUsage(CameraStream
     *               .setDepthOcclusionMode
     *               .DEPTH_OCCLUSION_DISABLED);
     *     }
     *     </code>
     *
     *     The default value for {@link DepthOcclusionMode} is {@link DepthOcclusionMode#DEPTH_OCCLUSION_DISABLED}.
     * </pre>
     *
     * @param depthOcclusionMode {@link DepthOcclusionMode}
     */
    public void setDepthOcclusionMode(DepthOcclusionMode depthOcclusionMode) {
        // Only set the occlusion material if the session config
        // has set the DepthMode to AUTOMATIC or RAW_DEPTH_ONLY,
        // otherwise set the standard camera material.
        if (isDepthOcclusionModeSupported(depthOcclusionMode)) {
            if (occlusionCameraMaterial != null) {
                setOcclusionMaterial(occlusionCameraMaterial);
                initOrUpdateRenderableMaterial(occlusionCameraMaterial);
            }
        } else {
            if (cameraMaterial != null) {
                setCameraMaterial(cameraMaterial);
                initOrUpdateRenderableMaterial(cameraMaterial);
            }
        }

        this.depthOcclusionMode = depthOcclusionMode;
    }


    /**
     * 深度API的数据类型
     */
    public enum DepthMode {
        /**
         * 无深度图数据
         * <pre>
         *     设备不支持深度API，或程序无需使用深度API时，使用这种Mode
         * </pre>
         */
        NO_DEPTH,

        /**
         * 使用自动获取的深度图数据
         * <pre>
         *     {@link ARSession} 配置为 AUTOMATIC
         * </pre>
         */
        DEPTH,

        /**
         * 使用原始的深度图数据
         * <pre>
         *     {@link ARSession} 配置为 RAW_DEPTH_ONLY
         * </pre>
         */
        RAW_DEPTH
    }


    /**
     * 深度遮挡模式
     */
    public enum DepthOcclusionMode {
        /**
         * 启用深度遮挡
         * <p>
         *     这会使用渲染遮挡效果的材质文件，以实现实挡虚的效果
         * </p>
         */
        DEPTH_OCCLUSION_ENABLED,
        /**
         * 禁用深度遮挡
         * <pre>
         *     这是默认模式，无虚实遮挡的效果，仅传入相机视频流数据
         * </pre>
         */
        DEPTH_OCCLUSION_DISABLED
    }

    /**
     * 清理回调
     * <p>
     *     gc后触发以清理filament的Native数据
     * </p>
     */
    public static final class CleanupCallback implements Runnable {
        private final Scene scene;
        private final int cameraStreamRenderable;
        private final IndexBuffer cameraIndexBuffer;
        private final VertexBuffer cameraVertexBuffer;

        public CleanupCallback(
                Scene scene,
                int cameraStreamRenderable,
                IndexBuffer cameraIndexBuffer,
                VertexBuffer cameraVertexBuffer) {
            this.scene = scene;
            this.cameraStreamRenderable = cameraStreamRenderable;
            this.cameraIndexBuffer = cameraIndexBuffer;
            this.cameraVertexBuffer = cameraVertexBuffer;
        }

        @Override
        public void run() {
            AndroidPreconditions.checkUiThread();

            IEngine engine = EngineInstance.getEngine();
            if (engine == null && !engine.isValid()) {
                return;
            }

            if (cameraStreamRenderable != UNINITIALIZED_FILAMENT_RENDERABLE) {
                try {
                    scene.removeEntity(cameraStreamRenderable);
                }catch (IllegalStateException e){
                    Log.w(TAG, "run: ", e);
                }
            }

            engine.destroyIndexBuffer(cameraIndexBuffer);
            engine.destroyVertexBuffer(cameraVertexBuffer);
        }
    }
}