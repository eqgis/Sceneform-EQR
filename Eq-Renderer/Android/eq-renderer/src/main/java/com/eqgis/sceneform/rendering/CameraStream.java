package com.eqgis.sceneform.rendering;

import android.media.Image;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.eqgis.sceneform.CustomDepthImage;
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
import com.eqgis.sceneform.ARPlatForm;
import com.eqgis.sceneform.utilities.AndroidPreconditions;
import com.eqgis.sceneform.utilities.Preconditions;
import com.eqgis.eqr.ar.ARCamera;
import com.eqgis.eqr.ar.ARCameraIntrinsics;
import com.eqgis.eqr.ar.ARConfig;
import com.eqgis.eqr.ar.ARFrame;
import com.eqgis.eqr.ar.ARSession;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * Displays the Camera stream using Filament.
 *
 * @hide Note: The class is hidden because it should only be used by the Filament Renderer and does
 * not expose a user facing API.
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

    /** By default the depthMode is set to {@link DepthMode#NO_DEPTH} */
    public DepthMode depthMode = DepthMode.NO_DEPTH;
    /** By default the depthOcclusionMode ist set to {@link DepthOcclusionMode#DEPTH_OCCLUSION_DISABLED} */
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
            // create screen quad geometry to camera stream to
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

            // Note: ARCore expects the UV buffers to be direct or will assert in transformDisplayUvCoords.
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

        // The ExternalTexture can't be created until we receive the first AR Core Frame so that we
        // can access the width and height of the camera texture. Return early if the ExternalTexture
        // hasn't been created yet so we don't start rendering until we have a valid texture. This will
        // be called again when the ExternalTexture is created.
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

        // The ExternalTexture can't be created until we receive the first AR Core Frame so that we
        // can access the width and height of the camera texture. Return early if the ExternalTexture
        // hasn't been created yet so we don't start rendering until we have a valid texture. This will
        // be called again when the ExternalTexture is created.
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
     * <pre>
     *     The {@link ARSession} holds the information if the
     *     DepthMode is configured or not. Based on
     *     that result different materials and textures are
     *     used for the camera.
     * </pre>
     *
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

        ARCamera arCamera = frame.getCamera();
        ARCameraIntrinsics intrinsics = arCamera.getCameraImageIntrinsics();
        int[] dimensions = intrinsics.getImageDimensions();
        int width ;//= dimensions[1];//1440;//
        int height ;//= dimensions[0];//1080;//
//    int width = dimensions[0];//1440;//
//    int height = dimensions[1];//1080;//

        if (ARPlatForm.isArCore()/*3*/){
            width = dimensions[0];//1440;//
            height = dimensions[1];//1080;//
        }else{
//            width = dimensions[1];//1440;//
//            height = dimensions[0];//1080;//
            //Ikkyu_memo, AREngine width must be > height
            if (dimensions[0] == 0 && dimensions[1] == 0){
                width = 1440;
                height = 1080;
            }else {
                width = Math.max(dimensions[1],dimensions[0]);
                height = Math.min(dimensions[1],dimensions[0]);
            }
        }

        cameraTexture = new ExternalTexture(cameraTextureId, width, height);

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
     * Init BackgroundTexture , added by ikkyu 2022/04/29
     * @param externalTexture
     */
    public void initializeTexture(ExternalTexture externalTexture) {
        if (isTextureInitialized()) {
            return;
        }
        if (externalTexture == null)return;

        // External Camera Texture
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
     *      Update the DepthTexture.
     * </pre>
     *
     * @param depthImage {@link Image}
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
     * added by Ikkyu 2022/01/22
     * modified by ikkyu 2022/10/24
     * @param depthImage
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
     * Gets the currently applied depth mode depending on the device supported modes.
     */
    public DepthMode getDepthMode() {
        return depthMode;
    }

    /**
     * Checks whether the provided DepthOcclusionMode is supported on this device with the selected camera configuration and AR config.
     * The current list of supported devices is documented on the ARCore supported devices page.
     *
     * @param depthOcclusionMode The desired depth mode to check.
     * @return True if the depth mode has been activated on the AR session config
     * and the provided depth occlusion mode is supported on this device.
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
     * Gets the current Depth Occlusion Mode
     *
     * @see #setDepthOcclusionMode
     * @see DepthOcclusionMode
     * @return the occlusion mode currently defined for the CarmeraStream
     */
    public DepthOcclusionMode getDepthOcclusionMode() {
        return depthOcclusionMode;
    }


    /**
     * <pre>
     *     Set the DepthModeUsage to {@link DepthOcclusionMode#DEPTH_OCCLUSION_ENABLED} to set the
     *     occlusion {@link com.google.android.filament.Material}. This will process the incoming DepthImage to
     *     occlude virtual objects behind real world objects. If the {@link ARSession} configuration
     *     for the {@link com.google.ar.core.Config.DepthMode} is set to {@link ARConfig.DepthMode#DISABLED},
     *     the standard camera {@link Material} is used.
     *
     *     Set the DepthModeUsage to {@link DepthOcclusionMode#DEPTH_OCCLUSION_DISABLED} to set the
     *     standard camera {@link com.google.android.filament.Material}.
     *
     *     A good place to set the DepthModeUsage is inside of the onViewCreated() function call.
     *     To make sure that this function is called in your code set the correct listener on
     *     your Ar Fragment
     *
     *     <code>public void onAttachFragment(
     *         FragmentManager fragmentManager,
     *         Fragment fragment
     *     ) {
     *         if (fragment.getId() == R.id.arFragment) {
     *             arFragment = (ArFragment) fragment;
     *             arFragment.setOnViewCreatedListener(this);
     *             arFragment.setOnSessionConfigurationListener(this);
     *         }
     *     }
     *
     *     public void onViewCreated(
     *         ArFragment arFragment,
     *         ArSceneView arSceneView
     *     ) {
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
     * The DepthMode Enum is used to reflect the {@link ARSession} configuration
     * for the DepthMode to decide if the occlusion material should be set and if
     * frame.acquireDepthImage() or frame.acquireRawDepthImage() should be called to get
     * the input data for the depth texture.
     */
    public enum DepthMode {
        /**
         * <pre>
         * The {@link ARSession} is not configured to use the Depth-API
         *
         * This is the default value
         * </pre>
         */
        NO_DEPTH,
        /**
         * The {@link ARSession} is configured to use the DepthMode AUTOMATIC
         */
        DEPTH,
        /**
         * The {@link ARSession} is configured to use the DepthMode RAW_DEPTH_ONLY
         */
        RAW_DEPTH
    }


    /**
     * Independent from the {@link ARSession} configuration, the user can decide with the
     * DeptModeUsage which {@link com.google.android.filament.Material} should be set to the
     * CameraStream renderable.
     */
    public enum DepthOcclusionMode {
        /**
         * Set the occlusion material. If the {@link ARSession} is not
         * configured properly the standard camera material is used.
         * Valid {@link ARSession} configuration for the DepthMode are
         * {@link ARConfig.DepthMode#AUTOMATIC} and {@link ARConfig.DepthMode#RAW_DEPTH_ONLY}.
         */
        DEPTH_OCCLUSION_ENABLED,
        /**
         * <pre>
         * Use this value if the standard camera material should be applied to
         * the CameraStream Renderable even if the {@link ARSession} configuration has set
         * the DepthMode to {@link ARConfig.DepthMode#AUTOMATIC} or
         * {@link ARConfig.DepthMode#RAW_DEPTH_ONLY}. This Option is useful, if you
         * want to use the DepthImage or RawDepthImage or just the DepthPoints without the
         * occlusion effect.
         *
         * This is the default value
         * </pre>
         */
        DEPTH_OCCLUSION_DISABLED
    }

    /**
     * Cleanup filament objects after garbage collection
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
            AndroidPreconditions.checkEngineThread();

            IEngine engine = EngineInstance.getEngine();
            if (engine == null && !engine.isValid()) {
                return;
            }

            if (cameraStreamRenderable != UNINITIALIZED_FILAMENT_RENDERABLE) {
                scene.remove(cameraStreamRenderable);
            }

            engine.destroyIndexBuffer(cameraIndexBuffer);
            engine.destroyVertexBuffer(cameraVertexBuffer);
        }
    }

    /**实现与相机纹理的颜色混合*/
    public void updateBlendColor(Color blendColor){
        if (cameraMaterial != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cameraMaterial.setFloat4("blendColor",blendColor);
            }
        }
    }
}