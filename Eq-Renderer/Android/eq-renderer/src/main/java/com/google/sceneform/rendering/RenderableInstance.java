package com.google.sceneform.rendering;

import android.text.TextUtils;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import com.google.sceneform.animation.AnimatableModel;
import com.google.sceneform.animation.ModelAnimation;
import com.google.sceneform.common.TransformProvider;
import com.google.sceneform.math.Matrix;
import com.google.sceneform.utilities.AndroidPreconditions;
import com.google.sceneform.utilities.ChangeId;
import com.google.sceneform.utilities.Preconditions;
import com.google.android.filament.Entity;
import com.google.android.filament.EntityInstance;
import com.google.android.filament.EntityManager;
import com.google.android.filament.MaterialInstance;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.TransformManager;
import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.FilamentAsset;

import java.util.ArrayList;

/**
 * 渲染实例
 * <p>
 *     一个Renderable可以有多个RenderableInstances
 * </p>
 *
 * @hide
 */
@SuppressWarnings("AndroidJdkLibsChecker")
public class RenderableInstance implements AnimatableModel {
    private final TransformProvider transformProvider;
    private final Renderable renderable;
    @Nullable
    Renderer attachedRenderer;
    @Entity
    private int entity = 0;
    @Entity
    private int childEntity = 0;
    int renderableId = ChangeId.EMPTY_ID;

    @Nullable
    Animator filamentAnimator;

    private ArrayList<ModelAnimation> animations = new ArrayList<>();


    private int renderPriority = Renderable.RENDER_PRIORITY_DEFAULT;
    private boolean isShadowCaster = true;
    private boolean isShadowReceiver = true;

    private ArrayList<Material> materialBindings;
    private ArrayList<String> materialNames;


    @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
    public RenderableInstance(TransformProvider transformProvider, Renderable renderable) {
        Preconditions.checkNotNull(transformProvider, "Parameter \"transformProvider\" was null.");
        Preconditions.checkNotNull(renderable, "Parameter \"renderable\" was null.");
        this.transformProvider = transformProvider;
        this.renderable = renderable;
        this.materialBindings = new ArrayList<>(renderable.getMaterialBindings());
        this.materialNames = new ArrayList<>(renderable.getMaterialNames());
        entity = createFilamentEntity(EngineInstance.getEngine());


        //源模型->SFA->SFB这种加载方式，为早期scenefrom(1.15以及以前的版本使用)，现不再使用了
        //当前版本，仅支持通过gltfio库导入gltf2.0格式的模型
        if (renderable.getRenderableData() instanceof RenderableInternalFilamentAssetData) {
            createFilamentAssets(renderable);
        }
        //修改为prepareForDraw();时，第一帧统一创建

        ResourceManager.getInstance()
                .getRenderableInstanceCleanupRegistry()
                .register(this, new CleanupCallback(entity, childEntity));
    }

    private void createFilamentAssets(Renderable renderable) {
        RenderableInternalFilamentAssetData renderableData =
                (RenderableInternalFilamentAssetData) renderable.getRenderableData();
        FilamentAsset createdAsset = renderableData.createFilamentAsset(renderable);

        RenderableManager renderableManager = EngineInstance.getEngine().getRenderableManager();

        this.materialBindings.clear();
        this.materialNames.clear();
        for (int entity : createdAsset.getEntities()) {
            @EntityInstance int renderableInstance = renderableManager.getInstance(entity);
            if (renderableInstance == 0) {
                continue;
            }
            MaterialInstance materialInstance = renderableManager.getMaterialInstanceAt(renderableInstance, 0);
            materialNames.add(materialInstance.getName());

            MaterialInternalDataGltfImpl materialData = new MaterialInternalDataGltfImpl(materialInstance.getMaterial());
            Material material = new Material(materialData);
            material.updateGltfMaterialInstance(materialInstance);
            materialBindings.add(material);
        }

        TransformManager transformManager = EngineInstance.getEngine().getTransformManager();

        @EntityInstance int rootInstance = transformManager.getInstance(createdAsset.getRoot());
        @EntityInstance
        int parentInstance = transformManager.getInstance(childEntity == 0 ? entity : childEntity);

        transformManager.setParent(rootInstance, parentInstance);

        setRenderPriority(renderable.getRenderPriority());
        setShadowCaster(renderable.isShadowCaster());
        setShadowReceiver(renderable.isShadowReceiver());

        filamentAnimator = createdAsset.getInstance().getAnimator();
        animations = new ArrayList<>();
        for (int i = 0; i < filamentAnimator.getAnimationCount(); i++) {
            animations.add(new ModelAnimation(this, filamentAnimator.getAnimationName(i), i,
                    filamentAnimator.getAnimationDuration(i),
                    getRenderable().getAnimationFrameRate()));
        }
    }

    /**
     * 获取异步资源加载进度
     * <p>以[0,1]中的百分比表示</p>
     * @return 进度值
     */
    public float getResourceLoadProgress(){
        if (renderable.getRenderableData() instanceof  RenderableInternalFilamentAssetData){
            RenderableInternalFilamentAssetData renderableData =
                    (RenderableInternalFilamentAssetData) renderable.getRenderableData();
            return renderableData.resourceLoader.asyncGetLoadProgress();
        }
        return 1.0f;
    }

    @Nullable
    public FilamentAsset getFilamentAsset() {
        if (renderable.getRenderableData() instanceof RenderableInternalFilamentAssetData){
            RenderableInternalFilamentAssetData renderableData =
                    (RenderableInternalFilamentAssetData) renderable.getRenderableData();
            return renderableData.getFilamentAsset();
        }
        return null;
    }

    /**
     * 获取filament的动画对象
     * <p>
     *     Animator由<code>FilamentAsset</code>拥有，可以用于两件事:
     *     <ul>
     *         <li>根据glTF <code>动画</code>定义更新<code>TransformManager</code>组件中的矩阵。
     *         <li>根据lTF <code>skin</code>定义更新<code>RenderableManager</code>组件中的骨矩阵。</li>
     *     </ul>
     * </p>
     */
    @Nullable
    Animator getFilamentAnimator() {
        return filamentAnimator;
    }

    /**
     * 获取渲染对象
     * @return {@link Readable}
     */
    public Renderable getRenderable() {
        return renderable;
    }

    public @Entity
    int getEntity() {
        return entity;
    }

    public @Entity
    int getRenderedEntity() {
        return (childEntity == 0) ? entity : childEntity;
    }

    void setModelMatrix(TransformManager transformManager, @Size(min = 16) float[] transform) {
        // Use entity, rather than childEntity; setting the latter would slam the local transform which
        // corrects for scaling and offset.
        @EntityInstance int instance = transformManager.getInstance(entity);
        transformManager.setTransform(instance, transform);
    }

    /**
     * 获取渲染优先级
     * <p>默认值为4</p>
     */
    public int getRenderPriority() {
        return renderPriority;
    }

    /**
     * 设置渲染优先级
     */
    public void setRenderPriority(@IntRange(from = Renderable.RENDER_PRIORITY_FIRST, to = Renderable.RENDER_PRIORITY_LAST) int renderPriority) {
        int[] entities = getFilamentAsset().getEntities();
        this.renderPriority = Math.min(Renderable.RENDER_PRIORITY_LAST, Math.max(Renderable.RENDER_PRIORITY_FIRST, renderPriority));
        RenderableManager renderableManager = EngineInstance.getEngine().getRenderableManager();
        for (int i = 0; i < entities.length; i++) {
            @EntityInstance int renderableInstance = renderableManager.getInstance(entities[i]);
            if (renderableInstance != 0) {
                renderableManager.setPriority(renderableInstance, this.renderPriority);
            }
        }
    }

    /**
     * 如果支持阴影投射，则返回true。
     * <p>将阴影投射到其他网格上</p>
     */
    public boolean isShadowCaster() {
        return isShadowCaster;
    }

    /**
     * Sets whether the renderable casts shadow on other renderables in the scene.
     */
    public void setShadowCaster(boolean isShadowCaster) {
        this.isShadowCaster = isShadowCaster;
        RenderableManager renderableManager = EngineInstance.getEngine().getRenderableManager();
        @EntityInstance int renderableInstance = renderableManager.getInstance(getEntity());
        if (renderableInstance != 0) {
            renderableManager.setCastShadows(renderableInstance, isShadowCaster);
        }
        //TODO : Verify if we don't need to apply the parameter to child entities
//        int[] entities = getFilamentAsset().getEntities();
//        for (int i = 0; i < entities.length; i++) {
//            @EntityInstance int renderableInstance = renderableManager.getInstance(entities[i]);
//            if (renderableInstance != 0) {
//                renderableManager.setCastShadows(renderableInstance, isShadowCaster);
//            }
//        }
    }

    /**
     * 若支持其他对象投射的阴影，则返回true
     */
    public boolean isShadowReceiver() {
        return isShadowReceiver;
    }

    /**
     * 设置可渲染对象是否接收场景中其他可渲染对象投射的阴影。
     */
    public void setShadowReceiver(boolean isShadowReceiver) {
        this.isShadowReceiver = isShadowReceiver;
        RenderableManager renderableManager = EngineInstance.getEngine().getRenderableManager();
        @EntityInstance int renderableInstance = renderableManager.getInstance(getEntity());
        if (renderableInstance != 0) {
            renderableManager.setCastShadows(renderableInstance, isShadowCaster);
        }
        //TODO : Verify if we don't need to apply the parameter to child entities
//        for (int i = 0; i < entities.length; i++) {
//            @EntityInstance int renderableInstance = renderableManager.getInstance(entities[i]);
//            if (renderableInstance != 0) {
//                renderableManager.setReceiveShadows(renderableInstance, isShadowReceiver);
//            }
//        }
    }

    /**
     * 获取第一个子网格的材质
     */
    public Material getMaterial() {
        return getMaterial(0);
    }

    /**
     * 获取材质的数量
     */
    public int getMaterialsCount() {
        return materialBindings.size();
    }

    /**
     * 获取指定索引的网格的材质
     */
    public Material getMaterial(int index) {
        if (index < materialBindings.size()) {
            return materialBindings.get(index);
        }
        return null;
    }

    /**
     * 根据名称获取材质
     */
    public Material getMaterial(String name) {
        for(int i=0;i<materialBindings.size();i++) {
            if(TextUtils.equals(materialNames.get(i), name)) {
                return materialBindings.get(i);
            }
        }
        return null;
    }

    /**
     * 给第一个子网格设置材质
     */
    public void setMaterial(Material material) {
        setMaterial(0, material);
    }

    /**
     * 给指定索引的网格设置材质
     */
    public void setMaterial(@IntRange(from = 0) int primitiveIndex, Material material) {
        for (int i = 0; i < getFilamentAsset().getEntities().length; i++) {
            setMaterial(i, primitiveIndex, material);
        }
    }

    /**
     * 设置绑定到指定索引和entityIndex的材质
     */
    public void setMaterial(int entityIndex, @IntRange(from = 0) int primitiveIndex, Material material) {
        int[] entities = getFilamentAsset().getEntities();
        Preconditions.checkElementIndex(entityIndex, entities.length, "No entity found at the given index");
        materialBindings.set(entityIndex, material);
        RenderableManager renderableManager = EngineInstance.getEngine().getRenderableManager();
        @EntityInstance int renderableInstance = renderableManager.getInstance(entities[entityIndex]);
        if (renderableInstance != 0) {
            renderableManager.setMaterialInstanceAt(renderableInstance, primitiveIndex,
                    material.getFilamentMaterialInstance());
        }
    }

    /**
     * 获取指定索引的材质名称
     */
    public String getMaterialName(int index) {
        Preconditions.checkState(materialNames.size() == materialBindings.size());
        if (index >= 0 && index < materialNames.size()) {
            return materialNames.get(index);
        }
        return null;
    }

    /**
     * @hide
     */
    public Matrix getWorldModelMatrix() {
        return renderable.getFinalModelMatrix(transformProvider.getWorldModelMatrix());
    }

    /**
     * 获取模型动画
     *  @param animationIndex 从0开始的索引值
     */
    @Override
    public ModelAnimation getAnimation(int animationIndex) {
        Preconditions.checkElementIndex(animationIndex, getAnimationCount(), "No animation found at the given index");
        return animations.get(animationIndex);
    }

    /**
     * 获取模型动画的数量
     */
    @Override
    public int getAnimationCount() {
        return animations.size();
    }

    // We use our own {@link android.view.Choreographer} to update the animations so just return
    // false (not applied)
    @Override
    public boolean applyAnimationChange(ModelAnimation animation) {
        return false;
    }

    private void setupSkeleton(IRenderableInternalData renderableInternalData) {
        return;
    }

    /**
     * 更改图元类型
     */
    public void changePrimitive(RenderableManager.PrimitiveType primitiveType){
        IRenderableInternalData renderableData = renderable.getRenderableData();
        if (renderableData != null){
            renderableData.changePrimitiveType(this, primitiveType);
        }
    }

    /**
     * @hide
     */
    public void prepareForDraw() {
        renderable.prepareForDraw();

        ChangeId changeId = renderable.getId();
        if (changeId.checkChanged(renderableId)) {
            IRenderableInternalData renderableInternalData = renderable.getRenderableData();
            setupSkeleton(renderableInternalData);
            renderableInternalData.buildInstanceData(this, getRenderedEntity());
            renderableId = changeId.get();
            // 第一次渲染，所以总是更新蒙皮skinning，即使我们没有动画和没有skinModifier
            updateSkinning();
        } else {
            // 如果渲染是动画或有一个皮肤修改器已经改变了，只更新蒙皮skinning
            if (updateAnimations(false)) {
                updateSkinning();
            }
        }
    }

    private void attachFilamentAssetToRenderer() {
        FilamentAsset currentFilamentAsset = getFilamentAsset();
        if (currentFilamentAsset != null) {
            int[] entities = currentFilamentAsset.getEntities();
            Preconditions.checkNotNull(attachedRenderer)
                    .getFilamentScene()
                    .addEntity(currentFilamentAsset.getRoot());
            Preconditions.checkNotNull(attachedRenderer)
                    .getFilamentScene()
                    .addEntities(currentFilamentAsset.getEntities());
            Preconditions.checkNotNull(attachedRenderer).getFilamentScene().addEntities(entities);
        }
    }

    /**
     * @hide
     */
    public void attachToRenderer(Renderer renderer) {
        renderer.addInstance(this);
        attachedRenderer = renderer;
        renderable.attachToRenderer(renderer);
        attachFilamentAssetToRenderer();
    }

    void detachFilamentAssetFromRenderer() {
        FilamentAsset currentFilamentAsset = getFilamentAsset();
        if (currentFilamentAsset != null) {
            int[] entities = currentFilamentAsset.getEntities();
            for (int entity : entities) {
                Preconditions.checkNotNull(attachedRenderer).getFilamentScene().removeEntity(entity);
            }
            int root = currentFilamentAsset.getRoot();
            Preconditions.checkNotNull(attachedRenderer).getFilamentScene().removeEntity(root);
        }
    }


    /**
     * 销毁实例对象
     * <p>
     *     释放图像内存和资源缓存
     * </p>
     * @author Ikkyu
     */
    public void destroy() {
        //释放源数据，主要包含一些URI信息|Mesh缓存（VB+IB）
        FilamentAsset filamentAsset = getFilamentAsset();

        EntityManager entityManager = EntityManager.get();

        Renderer rendererToDetach = attachedRenderer;
        if (rendererToDetach != null) {
            //desc- ikkyu （对于gltf模型，直接执行destroyGltfAsset();即可释放所有关联的对象）
            {
                if (filamentAsset != null) {
                    int[] entities = filamentAsset.getEntities();
                    rendererToDetach.scene.removeEntities(entities);
                    entityManager.destroy(entities);

                    int root = filamentAsset.getRoot();
                    rendererToDetach.scene.removeEntity(root);
                    entityManager.destroy(root);
                }
            }
            rendererToDetach.removeInstance(this);
            renderable.detatchFromRenderer();//as View
        }

        //(RenderableInternalFilamentAssetData) renderable.getRenderableData();
        if (renderable.getRenderableData() instanceof RenderableInternalFilamentAssetData) {
            RenderableInternalFilamentAssetData renderableData =
                    (RenderableInternalFilamentAssetData) renderable.getRenderableData();
            renderableData.resourceLoader.asyncCancelLoad();
            renderableData.resourceLoader.evictResourceData();
//            renderableData.resourceLoader.destroy();
        }
        renderable.getRenderableData().dispose();//Other RenderableData dispose

        for (Material material : renderable.getMaterialBindings()) {
            material.internalMaterialInstance.dispose();
        }

        if (childEntity != 0) {
            entityManager.destroy(childEntity);
        }
        if (entity != 0) {
            entityManager.destroy(entity);
        }
        //</editor-fold>
    }

    /**
     * @hide
     */
    public void detachFromRenderer() {
        Renderer rendererToDetach = attachedRenderer;
        if (rendererToDetach != null) {
            detachFilamentAssetFromRenderer();
            //todo
            renderable.getRenderableData().dispose();
            rendererToDetach.removeInstance(this);
            renderable.detatchFromRenderer();
        }
    }

    /**
     * 应用动画更改<code>如果fore==true</code>或动画被修改
     *
     * @param force 强制更新，即使动画时间位置没有改变。
     * @return 如果进行了动画更新操作，则为True。
     */
    public boolean updateAnimations(boolean force) {
        boolean hasUpdate = false;
        for (int i = 0; i < getAnimationCount(); i++) {
            ModelAnimation animation = getAnimation(i);
            if (force || animation.isDirty()) {
                if (getFilamentAnimator() != null) {
                    getFilamentAnimator().applyAnimation(i, animation.getTimePosition());
                }
                animation.setDirty(false);
                hasUpdate = true;
            }
        }
        return hasUpdate;
    }


    /**
     * 计算所有骨节点的根到节点转换。
     * 用例 <code>TransformManager</code> and <code>RenderableManager</code>.
     *
     * <p>NOTE: 这个操作与 <code>animation</code>不同</p>
     */
    private void updateSkinning() {
        if (getFilamentAnimator() != null) {
            getFilamentAnimator().updateBoneMatrices();
        }
    }

    public void setBlendOrderAt(int index, int blendOrder) {
        RenderableManager renderableManager = EngineInstance.getEngine().getRenderableManager();
        @EntityInstance int renderableInstance = renderableManager.getInstance(getRenderedEntity());
        renderableManager.setBlendOrderAt(renderableInstance, index, blendOrder);
    }

    @Entity
    private static int createFilamentEntity(IEngine engine) {
        EntityManager entityManager = EntityManager.get();
        @Entity int entity = entityManager.create();
        TransformManager transformManager = engine.getTransformManager();
        transformManager.create(entity);
        return entity;
    }

    /**
     * Cleanup回调
     */
    private static final class CleanupCallback implements Runnable {
        private final int childEntity;
        private final int entity;

        CleanupCallback(int childEntity, int entity) {
            this.childEntity = childEntity;
            this.entity = entity;
        }

        @Override
        public void run() {
            AndroidPreconditions.checkUiThread();

            IEngine engine = EngineInstance.getEngine();

            if (engine == null || !engine.isValid()) {
                return;
            }

            RenderableManager renderableManager = engine.getRenderableManager();

            if (childEntity != 0) {
                renderableManager.destroy(childEntity);
            }
            if (entity != 0) {
                renderableManager.destroy(entity);
            }
        }
    }
}