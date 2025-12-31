package com.google.sceneform.rendering;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eqgis.eqr.core.FilamentMaterialProviderManager;
import com.eqgis.eqr.core.FilamentPrimitiveUtilsNative;
import com.google.android.filament.EntityInstance;
import com.google.android.filament.EntityManager;
import com.google.android.filament.MaterialInstance;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.TransformManager;
import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.sceneform.animation.ModelAnimation;
import com.google.sceneform.collision.Box;
import com.google.sceneform.math.Vector3;
import com.google.android.filament.gltfio.UbershaderProvider;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.VertexBuffer;
import com.google.android.filament.gltfio.ResourceLoader;
import com.google.sceneform.utilities.LoadHelper;
import com.google.sceneform.utilities.SceneformBufferUtils;

import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Filament资产数据
 * <p>
 *     表示{@link Renderable}用于渲染glTF的数据。
 * </p>
 * */
@SuppressWarnings("AndroidJdkLibsChecker")
public class RenderableInternalFilamentAssetData implements IRenderableInternalData {
    //desc added by ikkyu 2022年3月19日
    private AssetLoader assetsLoader;//若加载gltf资源，会使用到该类

    private AtomicInteger count = new AtomicInteger(1);
    private FilamentAsset filamentAsset;

    Context context;
    Buffer gltfByteBuffer;
    boolean isGltfBinary;
    ResourceLoader resourceLoader;
    @Nullable Function<String, Uri> urlResolver;

    public void create(RenderableInstance instance) {
        Renderable renderable = instance.getRenderable();
        //updated by ikkyu
        assetsLoader = new AssetLoader(
                EngineInstance.getEngine().getFilamentEngine(),
                FilamentMaterialProviderManager.get(),
                EntityManager.get());

        //当前版本，不用再判断是否是glb了，直接通过createAssets即可创建。因此isGltfBinary的set方法，后续版本会删除。
        filamentAsset = assetsLoader.createAsset(gltfByteBuffer);

        if (renderable.collisionShape == null) {
            com.google.android.filament.Box box = filamentAsset.getBoundingBox();
            float[] halfExtent = box.getHalfExtent();
            float[] center = box.getCenter();
            renderable.collisionShape =
                    new Box(
                            new Vector3(halfExtent[0], halfExtent[1], halfExtent[2]).scaled(2.0f),
                            new Vector3(center[0], center[1], center[2]));
        }

        for (String uri : filamentAsset.getResourceUris()) {
            if (urlResolver == null) {
                Log.e(RenderableInternalFilamentAssetData.class.getSimpleName(),
                        "Failed to download uri " + uri + " no url resolver.");
                continue;
            }
            Uri dataUri = urlResolver.apply(uri);
            try {
                Callable<InputStream> callable = LoadHelper.fromUri(context, dataUri);
                resourceLoader.addResourceData(
                        uri, ByteBuffer.wrap(SceneformBufferUtils.inputStreamCallableToByteArray(callable)));
            } catch (Exception e) {
                Log.e(RenderableInternalFilamentAssetData.class.getSimpleName(),
                        "Failed to download data uri " + dataUri, e);
            }
        }

        if(renderable.asyncLoadEnabled) {
            resourceLoader.asyncBeginLoad(filamentAsset);
        } else {
            resourceLoader.loadResources(filamentAsset);
        }

    }

    public FilamentAsset getFilamentAsset(){
        return this.filamentAsset;
    }

    public void releaseAssets(AssetLoader assetLoader){
        if (assetLoader != null && this.filamentAsset != null){
            filamentAsset.releaseSourceData();
            assetLoader.destroyAsset(filamentAsset);
        }
    }

    public void addCount(){
        count.addAndGet(1);
    }

    public int decrementAndGet(){
        return count.decrementAndGet();
    }

    @Override
    public void setCenterAabb(Vector3 center) {
        // Not Implemented
    }

    @Override
    public Vector3 getCenterAabb() {
        // Not Implemented
        return Vector3.zero();
    }

    @Override
    public void setExtentsAabb(Vector3 halfExtents) {
        // Not Implemented
    }

    @Override
    public Vector3 getExtentsAabb() {
        throw new IllegalStateException("Not Implemented");
    }

    @Override
    public Vector3 getSizeAabb() {
        // Not Implemented
        return Vector3.zero();
    }

    @Override
    public void setTransformScale(float scale) {
        // Not Implemented
    }

    @Override
    public float getTransformScale() {
        // Not Implemented
        return 1.0f;
    }

    @Override
    public void setTransformOffset(Vector3 offset) {
        // Not Implemented
    }

    @Override
    public Vector3 getTransformOffset() {
        // Not Implemented
        return Vector3.zero();
    }

    @Override
    public ArrayList<RenderableInternalData.MeshData> getMeshes() {
        // Not Implemented
        return new ArrayList<>(1);
    }

    @Override
    public void setIndexBuffer(@Nullable IndexBuffer indexBuffer) {
        // Not Implemented
    }

    @Nullable
    @Override
    public IndexBuffer getIndexBuffer() {
        // Not Implemented
        return null;
    }

    @Override
    public void setVertexBuffer(@Nullable VertexBuffer vertexBuffer) {
        // Not Implemented
    }

    @Nullable
    @Override
    public VertexBuffer getVertexBuffer() {
        // Not Implemented
        return null;
    }

    @Override
    public void setRawIndexBuffer(@Nullable IntBuffer rawIndexBuffer) {
        // Not Implemented
    }

    @Nullable
    @Override
    public IntBuffer getRawIndexBuffer() {
        // Not Implemented
        return null;
    }

    @Override
    public void setRawPositionBuffer(@Nullable FloatBuffer rawPositionBuffer) {
        // Not Implemented
    }

    @Nullable
    @Override
    public FloatBuffer getRawPositionBuffer() {
        // Not Implemented
        return null;
    }

    @Override
    public void setRawTangentsBuffer(@Nullable FloatBuffer rawTangentsBuffer) {
        // Not Implemented
    }

    @Nullable
    @Override
    public FloatBuffer getRawTangentsBuffer() {
        // Not Implemented
        return null;
    }

    @Override
    public void setRawUvBuffer(@Nullable FloatBuffer rawUvBuffer) {
        // Not Implemented
    }

    @Nullable
    @Override
    public FloatBuffer getRawUvBuffer() {
        // Not Implemented
        return null;
    }

    @Override
    public void setRawColorBuffer(@Nullable FloatBuffer rawColorBuffer) {
        // Not Implemented
    }

    @Nullable
    @Override
    public FloatBuffer getRawColorBuffer() {
        // Not Implemented
        return null;
    }

    @Override
    public void buildInstanceData(RenderableInstance instance, int renderedEntity) {
        TransformManager transformManager = EngineInstance.getEngine().getTransformManager();
        @EntityInstance int rootInstance = transformManager.getInstance(filamentAsset.getRoot());
        @EntityInstance
        int parentInstance = transformManager.getInstance(renderedEntity);
        transformManager.setParent(rootInstance, parentInstance);
    }

    @Override
    public void changePrimitiveType(RenderableInstance instance, RenderableManager.PrimitiveType type) {
        if (filamentAsset != null){
            IEngine engine = EngineInstance.getEngine();
            RenderableManager renderableManager = engine.getRenderableManager();
            for (int entity : filamentAsset.getEntities()) {
                @EntityInstance int renderableInstance = renderableManager.getInstance(entity);
                if (renderableInstance == 0) {
                    continue;
                }
                FilamentPrimitiveUtilsNative
                        .nRebuildPrimitiveGeometry(engine.getFilamentEngine().getNativeObject(),
                                filamentAsset.getNativeObject(), entity, type.getValue());
            }

        }
    }

    @Override
    public void dispose() {
        //desc- ikkyu loader源于gltfio.jar，AssetLoader主要用于加载gltf模型，在场景生成filamentAsset对象
        if (assetsLoader == null)return;
        if (resourceLoader != null){
            resourceLoader.asyncCancelLoad();
            resourceLoader.evictResourceData();
            resourceLoader.destroy();
            resourceLoader = null;
        }

        if (filamentAsset != null){
            filamentAsset.releaseSourceData();
            assetsLoader.destroyAsset(filamentAsset);
            assetsLoader.destroy();
            filamentAsset = null;
            assetsLoader = null;
        }
    }


}
