package com.google.sceneform.rendering;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.sceneform.collision.Box;
import com.google.sceneform.collision.CollisionShape;
import com.google.sceneform.common.TransformProvider;
import com.google.sceneform.math.Matrix;
import com.google.sceneform.resources.ResourceRegistry;
import com.google.sceneform.utilities.AndroidPreconditions;
import com.google.sceneform.utilities.ChangeId;
import com.google.sceneform.utilities.LoadHelper;
import com.google.sceneform.utilities.Preconditions;
import com.google.android.filament.gltfio.FilamentAsset;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 渲染对象的抽象基类
 */
@SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"}) // CompletableFuture
public abstract class Renderable {
    // Renderable之间共享的数据，这是由于scenefrom中常用的copy机制，虽可共享数据，但不恰当的调用，容易导致内存泄漏
    private final IRenderableInternalData renderableData;

    protected boolean asyncLoadEnabled;

    private final Object registryId;

    // 每个渲染对象对应的数据
    private final ArrayList<Material> materialBindings = new ArrayList<>();
    private final ArrayList<String> materialNames = new ArrayList<>();
    private int renderPriority = RENDER_PRIORITY_DEFAULT;
    private boolean isShadowCaster = true;
    private boolean isShadowReceiver = true;
    //Assets中访问到的动画FPS
    private int animationFrameRate;
    @Nullable
    protected CollisionShape collisionShape;

    private final ChangeId changeId = new ChangeId();

    public static final int RENDER_PRIORITY_DEFAULT = 4;
    public static final int RENDER_PRIORITY_FIRST = 0;
    public static final int RENDER_PRIORITY_LAST = 7;
    // 数据的过期时间，缓存机制时用到
    private static final long DEFAULT_MAX_STALE_CACHE = TimeUnit.DAYS.toSeconds(14);
    // 默认的动画FPS
    public static final int DEFAULT_ANIMATION_FRAME_RATE = 24;

    /**
     * @hide
     */
    @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
    protected Renderable(Builder<? extends Renderable, ? extends Builder<?, ?>> builder) {
        Preconditions.checkNotNull(builder, "Parameter \"builder\" was null.");
        switch (builder.dataFormat){
            case GLTF2_0:
                renderableData = new RenderableInternalFilamentAssetData();
                break;
            case PLY:
                renderableData = new RenderableInternalPlyData();
                break;
            case PLY_3DGS:
                renderableData = new RenderableInternalGS3dData();
                break;
            case PLY_SPLAT:
                renderableData = new RenderableInternalSplatData();
                break;
            case DEFAULT_INTERNAL:
            default:
                renderableData = new RenderableInternalData();
                break;
        }

        if (builder.definition != null) {
            updateFromDefinition(builder.definition);
        }
        registryId = builder.registryId;
        asyncLoadEnabled = builder.asyncLoadEnabled;
        animationFrameRate = builder.animationFrameRate;
    }

    @SuppressWarnings("initialization")
    protected Renderable(Renderable other) {
        if (other.getId().isEmpty()) {
            throw new AssertionError("Cannot copy uninitialized Renderable.");
        }

        // 从other获取数据
        renderableData = other.renderableData;
        registryId = other.registryId;

        //cp材质
        Preconditions.checkState(other.materialNames.size() == other.materialBindings.size());
        for (int i = 0; i < other.materialBindings.size(); i++) {
            Material otherMaterial = other.materialBindings.get(i);
            materialBindings.add(otherMaterial.makeCopy());
            materialNames.add(other.materialNames.get(i));
        }

        renderPriority = other.renderPriority;
        isShadowCaster = other.isShadowCaster;
        isShadowReceiver = other.isShadowReceiver;

        //cp碰撞体
        if (other.collisionShape != null) {
            collisionShape = other.collisionShape.makeCopy();
        }

        asyncLoadEnabled = other.asyncLoadEnabled;
        animationFrameRate = other.animationFrameRate;

        changeId.update();
    }

    /**
     * 获取碰撞体形状
     */
    public @Nullable
    CollisionShape getCollisionShape() {
        return collisionShape;
    }

    /**
     * 设置碰撞体形状
     */
    public void setCollisionShape(@Nullable CollisionShape collisionShape) {
        this.collisionShape = collisionShape;
        changeId.update();
    }

    /**
     * 返回绑定到第一个子网格的材质
     */
    public Material getMaterial() {
        return getMaterial(0);
    }

    /**
     * 返回指定子索引的网格的材质
     */
    public Material getMaterial(int submeshIndex) {
        if (submeshIndex < materialBindings.size()) {
            return materialBindings.get(submeshIndex);
        }

        throw makeSubmeshOutOfRangeException(submeshIndex);
    }

    /**
     * 给第一个子网格设置材质
     */
    public void setMaterial(Material material) {
        setMaterial(0, material);
    }

    /**
     * 设置绑定到子网格的材质
     */
    public void setMaterial(int submeshIndex, Material material) {
        if (submeshIndex < materialBindings.size()) {
            materialBindings.set(submeshIndex, material);
            changeId.update();
        } else {
            throw makeSubmeshOutOfRangeException(submeshIndex);
        }
    }

    /**
     * 返回与指定子网格关联的名称
     * @throws IllegalArgumentException 若索引不正确，则抛出此异常
     */
    public String getSubmeshName(int submeshIndex) {
        Preconditions.checkState(materialNames.size() == materialBindings.size());
        if (submeshIndex >= 0 && submeshIndex < materialNames.size()) {
            return materialNames.get(submeshIndex);
        }

        throw makeSubmeshOutOfRangeException(submeshIndex);
    }

    /**
     * 获取渲染优先级
     * <p>优先级在0(首先呈现)和7(最后呈现)之间。默认值为4。</p>
     */
    public int getRenderPriority() {
        return renderPriority;
    }

    /**
     * 设置渲染优先级
     * <p>优先级在0(首先呈现)和7(最后呈现)之间。默认值为4。</p>
     */
    public void setRenderPriority(
            @IntRange(from = RENDER_PRIORITY_FIRST, to = RENDER_PRIORITY_LAST) int renderPriority) {
        this.renderPriority =
                Math.min(RENDER_PRIORITY_LAST, Math.max(RENDER_PRIORITY_FIRST, renderPriority));
        changeId.update();
    }

    /**
     * 如果配置为在其他可渲染对象上投射阴影，则返回true。
     */
    public boolean isShadowCaster() {
        return isShadowCaster;
    }

    /**
     * 设置渲染对象是否在场景中的其他渲染对象上投射阴影。
     */
    public void setShadowCaster(boolean isShadowCaster) {
        this.isShadowCaster = isShadowCaster;
        changeId.update();
    }

    /**
     * 如果配置为接收其他可渲染对象投射的阴影，则返回true。
     */
    public boolean isShadowReceiver() {
        return isShadowReceiver;
    }

    /**
     * 设置可渲染对象是否接收场景中其他可渲染对象投射的阴影。
     */
    public void setShadowReceiver(boolean isShadowReceiver) {
        this.isShadowReceiver = isShadowReceiver;
        changeId.update();
    }

    /**
     * 获取动画每秒播放的帧数
     * <p>默认使用Assets中访问得到的结果</p>
     */
    public int getAnimationFrameRate() {
        return animationFrameRate;
    }

    /**
     * 返回子网格数量
     * <p>至少有1个</p>
     */
    public int getSubmeshCount() {
        return renderableData.getMeshes().size();
    }

    /**
     * @hide
     */
    public ChangeId getId() {
        return changeId;
    }

    /**
     * @hide
     */
    public RenderableInstance createInstance(TransformProvider transformProvider) {
        return new RenderableInstance(transformProvider, this);
    }

    public void updateFromDefinition(IRenderableDefinition definition) {
        Preconditions.checkState(!definition.getSubmeshes().isEmpty());

        changeId.update();

        definition.applyDefinitionToData(renderableData, materialBindings, materialNames);

        collisionShape = new Box(renderableData.getSizeAabb(), renderableData.getCenterAabb());
    }

    /**
     * 创建一个克隆对象
     */
    public abstract Renderable makeCopy();

    public IRenderableInternalData getRenderableData() {
        return renderableData;
    }

    ArrayList<Material> getMaterialBindings() {
        return materialBindings;
    }

    ArrayList<String> getMaterialNames() {
        return materialNames;
    }

    /**
     * 子类重写
     * <p>针对View的渲染，需要重新</p>
     * <p>针对gltf模型的渲染，这里需要处理resource异步加载的更新操作</p>
     */
    void prepareForDraw() {
        if (getRenderableData() instanceof RenderableInternalFilamentAssetData) {
            RenderableInternalFilamentAssetData renderableData =
                    (RenderableInternalFilamentAssetData) getRenderableData();
            // 资源加载器异步更新，下面的方法配合resourceLoader.asyncBeginLoad方法使用
            renderableData.resourceLoader.asyncUpdateLoad();
        }
    }

    void attachToRenderer(Renderer renderer) {
    }

    void detatchFromRenderer() {
    }

    /**
     * 根据传入的矩阵获取用于渲染{@link Renderable}的最终模型矩阵。
     * 默认实现只是简单地遍历原始矩阵。
     * 警告:不要修改原始矩阵!如果最终矩阵与原始矩阵不相同，则必须返回一个新实例。
     *
     * @hide
     */
    public Matrix getFinalModelMatrix(final Matrix originalMatrix) {
        Preconditions.checkNotNull(originalMatrix, "Parameter \"originalMatrix\" was null.");
        return originalMatrix;
    }

    private IllegalArgumentException makeSubmeshOutOfRangeException(int submeshIndex) {
        return new IllegalArgumentException(
                "submeshIndex ("
                        + submeshIndex
                        + ") is out of range. It must be less than the submeshCount ("
                        + getSubmeshCount()
                        + ").");
    }


    /**
     * 移除没有实例的数据，释放内存
     */
    public void tryDestroyData(){
        if (renderableData instanceof RenderableInternalFilamentAssetData
        && ((RenderableInternalFilamentAssetData)renderableData).decrementAndGet() == 1){
            ResourceRegistry<ModelRenderable> registry = ResourceManager.getInstance().getModelRenderableRegistry();
            registry.remove(registryId);
            renderableData.dispose();
        }
    }

    /**
     * 用于以编程方式构造{@link Renderable}。生成器数据是存储的，而不是复制的。在构建调用之前或之间修改数据时要小心。
     */
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"}) // CompletableFuture
    abstract static class Builder<T extends Renderable, B extends Builder<T, B>> {
        /**
         * @hide
         */
        @Nullable
        protected Object registryId = null;
        /**
         * @hide
         */
        @Nullable
        protected Context context = null;

        @Nullable
        private Uri sourceUri = null;
        @Nullable
        private Callable<InputStream> inputStreamCreator = null;
        @Nullable
        private RenderableDefinition definition = null;
//        private boolean isGltf = false;
//        private boolean isFilamentAsset = false;
        private RenderableDataFormat dataFormat = RenderableDataFormat.DEFAULT_INTERNAL;
        private boolean asyncLoadEnabled = false;
        @Nullable
        private LoadGltfListener loadGltfListener;
        @Nullable
        private Function<String, Uri> uriResolver = null;
        @Nullable
        private byte[] materialsBytes = null;

        private int animationFrameRate = DEFAULT_ANIMATION_FRAME_RATE;

        /**
         * 构造函数
         */
        protected Builder() {
        }

        public B setSource(Context context, Callable<InputStream> inputStreamCreator) {
            Preconditions.checkNotNull(inputStreamCreator);
            this.sourceUri = null;
            this.inputStreamCreator = inputStreamCreator;
            this.context = context;
            return getSelf();
        }

        public B setSource(Context context, Uri sourceUri) {
            return setRemoteSourceHelper(context, sourceUri, /*原值：true，改为false*/false);
        }

        public B setSource(Context context, Uri sourceUri, boolean enableCaching) {
//            return null;
            //updated by ikkyu 2022/03/19，我也不知道为什么，此处源码是return null; 修改如下：
            return setRemoteSourceHelper(context, sourceUri, enableCaching);
        }

        public B setSource(Context context, int resource) {
            this.inputStreamCreator = LoadHelper.fromResource(context, resource);
            this.context = context;

            Uri uri = LoadHelper.resourceToUri(context, resource);
            this.sourceUri = uri;
            this.registryId = uri;
            return getSelf();
        }

        /**
         * 设置自定义渲染对象{@link RenderableDefinition}.
         */
        public B setSource(RenderableDefinition definition) {
            this.definition = definition;
            registryId = null;
            sourceUri = null;
            return getSelf();
        }

        public B setRegistryId(@Nullable Object registryId) {
            this.registryId = registryId;
            return getSelf();
        }

        /**
         * 设置当前渲染对象为gltf
         * @param isFilamentGltf 若需要渲染gltf，则需要设置为true
         * @return this
         */
        @Deprecated
        public B setIsFilamentGltf(boolean isFilamentGltf) {
            if (isFilamentGltf){
                dataFormat = RenderableDataFormat.GLTF2_0;
            }
//            this.isFilamentAsset = isFilamentGltf;
            return getSelf();
        }
        /**
         * 设置当前渲染对象的数据类型
         * @return this
         */
        public B setDataFormat (RenderableDataFormat format) {
            this.dataFormat = format;
            return getSelf();
        }

        /**
         * 启用异步的方式加载资源
         * <p>
         *     注意：虽然设置异步，但是仍会阻塞一会儿UI线程。比起不启用异步，阻塞UI线程的时间大大缩短
         *     这是{@link com.google.android.filament.gltfio.ResourceLoader#asyncBeginLoad(FilamentAsset)}仍会阻塞UI线程
         * </p>
         */
        public B setAsyncLoadEnabled(boolean asyncLoadEnabled) {
            this.asyncLoadEnabled = asyncLoadEnabled;
            return getSelf();
        }

        /**
         * 设置动画每秒播放的帧数
         * @param frameRate 每秒帧数
         */
        public B setAnimationFrameRate(int frameRate) {
            this.animationFrameRate = frameRate;
            return getSelf();
        }

        /**
         * 判断是否已传入资源
         * @hide
         */
        public Boolean hasSource() {
            return sourceUri != null || inputStreamCreator != null || definition != null;
        }

        /**
         * 创建{@link Renderable}
         * @return {@link Renderable}
         */
        public CompletableFuture<T> build() {
            try {
                checkPreconditions();
            } catch (Throwable failedPrecondition) {
                CompletableFuture<T> result = new CompletableFuture<>();
                result.completeExceptionally(failedPrecondition);
                FutureHelper.logOnException(
                        getRenderableClass().getSimpleName(),
                        result,
                        "Unable to load Renderable registryId='" + registryId + "'");
                return result;
            }

            // 先检查
            Object registryId = this.registryId;
            if (registryId != null) {
                // See if a renderable has already been registered by this id, if so re-use it.
                ResourceRegistry<T> registry = getRenderableRegistry();
                CompletableFuture<T> renderableFuture = registry.get(registryId);
                if (renderableFuture != null) {
                    return renderableFuture.thenApply(
                            renderable -> getRenderableClass().cast(renderable.makeCopy()));
                }
            }

            T renderable = makeRenderable();

            if (definition != null) {
                return CompletableFuture.completedFuture(renderable);
            }

            // 非空判断
            Callable<InputStream> inputStreamCreator = this.inputStreamCreator;
            if (inputStreamCreator == null) {
                CompletableFuture<T> result = new CompletableFuture<>();
                result.completeExceptionally(new AssertionError("Input Stream Creator is null."));
                FutureHelper.logOnException(
                        getRenderableClass().getSimpleName(),
                        result,
                        "Unable to load Renderable registryId='" + registryId + "'");
                return result;
            }

            CompletableFuture<T> result = null;
            switch (dataFormat){
                case GLTF2_0:
                    if (context != null) {
                        result = loadRenderableFromFilamentGltf(context, renderable);
                    } else {
                        throw new AssertionError("Gltf Renderable.Builder must have a valid context.");
                    }
                    break;
                case PLY:
                case PLY_3DGS:
                case PLY_SPLAT:
                    if (context != null) {
                        result = loadRenderableFromUniversalData(context, renderable);
                    } else {
                        throw new AssertionError("Gltf Renderable.Builder must have a valid context.");
                    }
                    break;
                default:
                    throw new IllegalArgumentException();
            }
//            if (isFilamentAsset) {
//                if (context != null) {
//                    result = loadRenderableFromFilamentGltf(context, renderable);
//                } else {
//                    throw new AssertionError("Gltf Renderable.Builder must have a valid context.");
//                }
//            } else if (isGltf) {
//                if (context != null) {
//                    result = loadRenderableFromGltf(context, renderable, this.materialsBytes);
//                } else {
//                    throw new AssertionError("Gltf Renderable.Builder must have a valid context.");
//                }
//            } else {
//                throw new IllegalArgumentException();
//            }

            if (registryId != null) {
                ResourceRegistry<T> registry = getRenderableRegistry();
                registry.register(registryId, result);
            }

            FutureHelper.logOnException(
                    getRenderableClass().getSimpleName(),
                    result,
                    "Unable to load Renderable registryId='" + registryId + "'");
            return result.thenApply(
                    resultRenderable -> getRenderableClass().cast(resultRenderable.makeCopy()));
        }

        protected void checkPreconditions() {
            AndroidPreconditions.checkUiThread();

            if (!hasSource()) {
                throw new AssertionError("ModelRenderable must have a source.");
            }
        }

        private B setRemoteSourceHelper(Context context, Uri sourceUri, boolean enableCaching) {
            Preconditions.checkNotNull(sourceUri);
            this.sourceUri = sourceUri;
            this.context = context;
            this.registryId = sourceUri;
            //配置缓存
            if (enableCaching) {
                this.setCachingEnabled(context);
            }

            Map<String, String> connectionProperties = new HashMap<>();
            if (!enableCaching) {
                connectionProperties.put("Cache-Control", "no-cache");
            } else {
                connectionProperties.put("Cache-Control", "max-stale=" + DEFAULT_MAX_STALE_CACHE);
            }
            this.inputStreamCreator =
                    LoadHelper.fromUri(
                            context, Preconditions.checkNotNull(this.sourceUri), connectionProperties);
            return getSelf();
        }

//        private CompletableFuture<T> loadRenderableFromGltf(
//                @NonNull Context context, T renderable, @Nullable byte[] materialsBytes) {
//            return null;
//        }

        private CompletableFuture<T> loadRenderableFromFilamentGltf(
                @NonNull Context context, T renderable) {
            LoadRenderableFromFilamentGltfTask<T> loader =
                    new LoadRenderableFromFilamentGltfTask<>(
                            renderable, context, Preconditions.checkNotNull(sourceUri), uriResolver);
            return loader.downloadAndProcessRenderable(Preconditions.checkNotNull(inputStreamCreator));
        }
        private CompletableFuture<T> loadRenderableFromUniversalData(
                @NonNull Context context, T renderable) {
            LoadRenderableFromUniversalDataTask<T> loader =
                    new LoadRenderableFromUniversalDataTask<>(
                            renderable, context, Preconditions.checkNotNull(sourceUri), uriResolver);
            return loader.downloadAndProcessRenderable(Preconditions.checkNotNull(inputStreamCreator));
        }

        private void setCachingEnabled(Context context) {
            return;
        }

        protected abstract T makeRenderable();

        protected abstract Class<T> getRenderableClass();

        protected abstract ResourceRegistry<T> getRenderableRegistry();

        protected abstract B getSelf();
    }

    public enum RenderableDataFormat{
        /**
         * 作为GLTF格式进行渲染
         */
        GLTF2_0,
        /**
         * 作为PLY模型格式进行渲染
         */
        PLY,
        PLY_3DGS,
        PLY_SPLAT,
        /**
         * 使用RenderableDefinition渲染
         */
        DEFAULT_INTERNAL
    }
}
