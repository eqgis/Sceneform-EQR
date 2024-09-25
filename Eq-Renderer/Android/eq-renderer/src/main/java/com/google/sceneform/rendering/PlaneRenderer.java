package com.google.sceneform.rendering;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.sceneform.math.Vector3;
import com.google.sceneform.ARPlatForm;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.eqgis.ar.ARFrame;
import com.eqgis.ar.ARPlane;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 平面渲染器
 * <p>
 *     用于AR模式下，渲染扫描到的平面
 * </p>
 */

@SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"}) // CompletableFuture
public class PlaneRenderer {
    /**
     * 材质文件中纹理参数名称
     */
    public static final String MATERIAL_TEXTURE = "texture";
    /**
     * 材质文件中UV比例参数
     */
    public static final String MATERIAL_UV_SCALE = "uvScale";
    /**
     * 材质文件中的颜色参数
     */
    public static final String MATERIAL_COLOR = "color";
    /**
     * 射灯半径
     */
    public static final String MATERIAL_SPOTLIGHT_RADIUS = "radius";
    private static final String TAG = PlaneRenderer.class.getSimpleName();
    /**
     * Float3材质参数控制网格可视化点
     */
    private static final String MATERIAL_SPOTLIGHT_FOCUS_POINT = "focusPoint";

    /**
     * 用于控制默认纹理的UV比例
     */
    private static final float BASE_UV_SCALE = 8.0f;

    private static final float DEFAULT_TEXTURE_WIDTH = 293;
    private static final float DEFAULT_TEXTURE_HEIGHT = 513;

    private static final float SPOTLIGHT_RADIUS = .5f;

    private final Renderer renderer;

    private final Map<Plane, PlaneVisualizer> visualizerMap = new HashMap<>();
    // Per-plane overrides
    private final Map<Plane, Material> materialOverrides = new HashMap<>();
    private final Map<com.huawei.hiar.ARPlane, PlaneVisualizer> visualizerMapAREngine = new HashMap<>();
    // Per-plane overrides
    private final Map<com.huawei.hiar.ARPlane, Material> materialOverridesAREngine = new HashMap<>();
    private CompletableFuture<Material> planeMaterialFuture;
    private Material shadowMaterial;
    private boolean isEnabled = false;
    private boolean isVisible = true;
    private boolean isShadowReceiver = true;
    private PlaneRendererMode planeRendererMode = PlaneRendererMode.RENDER_ALL;
    // Distance from the camera to last plane hit, default value is 4 meters (standing height).
    private float lastPlaneHitDistance = 4.0f;

    /**
     * 构造函数
     * @hide 仅用于AR模式创建平面，内部调用
     */
    @SuppressWarnings("initialization")
    public PlaneRenderer(Renderer renderer) {
        this.renderer = renderer;

        loadPlaneMaterial();
        loadShadowMaterial();
    }

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * 设置平面渲染器的启用状态
     */
    public void setEnabled(boolean enabled) {
        if (isEnabled != enabled) {
            isEnabled = enabled;

            for (PlaneVisualizer visualizer : visualizerMap.values()) {
                visualizer.setEnabled(isEnabled);
            }
        }
    }

    /**
     * 如果场景中的Renderables将阴影投射到平面上，则返回true。
     */
    public boolean isShadowReceiver() {
        return isShadowReceiver;
    }

    /**
     * 控制场景中的可渲染对象是否将阴影投射到平面上。
     * @param shadowReceiver 若设置为false，则没有平面接收阴影
     */
    public void setShadowReceiver(boolean shadowReceiver) {
        if (isShadowReceiver != shadowReceiver) {
            isShadowReceiver = shadowReceiver;

            for (PlaneVisualizer visualizer : visualizerMap.values()) {
                visualizer.setShadowReceiver(isShadowReceiver);
            }
        }
    }

    /**
     * 判断是否显示平面渲染结果
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * 设置平面的显隐状态
     * @param visible 若设置为false，则没有平面进行渲染
     */
    public void setVisible(boolean visible) {
        if (isVisible != visible) {
            isVisible = visible;

            for (PlaneVisualizer visualizer : visualizerMap.values()) {
                visualizer.setVisible(isVisible);
            }
        }
    }

    /**
     * 获取渲染平面的材质
     */
    public CompletableFuture<Material> getMaterial() {
        return planeMaterialFuture;
    }

    /**
     * 获取平面的渲染模式
     *
     * @return {@link PlaneRendererMode}
     */
    public PlaneRendererMode getPlaneRendererMode() {
        return planeRendererMode;
    }

    /**
     * 设置平面的渲染模式
     * @param planeRendererMode {@link PlaneRendererMode}
     */
    public void setPlaneRendererMode(PlaneRendererMode planeRendererMode) {
        this.planeRendererMode = planeRendererMode;
    }

    /**
     * 更新渲染结果
     * <p>实时调用</p>
     * @param frame AR帧
     * @param viewWidth 视图宽度
     * @param viewHeight 视图高度
     */
    public void update(ARFrame frame, int viewWidth, int viewHeight) {
        if (ARPlatForm.isArCoreOrNone()){
            update(frame.getCoreFrame(),viewWidth,viewHeight);
        }else {
            update(frame.getHwFrame(),viewWidth,viewHeight);
        }
    }

    /**
     * 更新渲染结果
     * <p>实时调用</p>
     */
    private void update(Frame frame, int viewWidth, int viewHeight) {
        // Get a list of Plane-Trackables which are updated  on this frame.
        Collection<Plane> updatedPlanes = frame.getUpdatedTrackables(Plane.class);
        // Do a hittest on the current frame. The result is used to calculate
        // a focusPoint and to render the top most plane Trackable if
        // planeRendererMode is set to RENDER_TOP_MOST.
        HitResult hitResult = getHitResult(frame, viewWidth, viewHeight);
        // Calculate the focusPoint. It is used to determine the position of
        // the visualized grid.
        Vector3 focusPoint = getFocusPoint(frame, hitResult);

        @SuppressWarnings("nullness")
        @Nullable
        Material planeMaterial = planeMaterialFuture.getNow(null);
        if (planeMaterial != null) {
            planeMaterial.setFloat3(MATERIAL_SPOTLIGHT_FOCUS_POINT, focusPoint);
            planeMaterial.setFloat(MATERIAL_SPOTLIGHT_RADIUS, SPOTLIGHT_RADIUS);
        }

        if (planeRendererMode == PlaneRendererMode.RENDER_ALL && hitResult != null) {
            renderAllByARCore(updatedPlanes, planeMaterial);
        } else if (planeRendererMode == PlaneRendererMode.RENDER_TOP_MOST && hitResult != null) {
            Plane topMostPlane = (Plane) hitResult.getTrackable();
            Optional.ofNullable(topMostPlane)
                    .ifPresent(plane -> renderPlane(plane, planeMaterial));
        }

        // Check for not tracking Plane-Trackables and remove them.
        cleanupOldPlaneVisualizer();
    }

    /**
     * 更新渲染结果
     * <p>实时调用</p>
     */
    private void update(com.huawei.hiar.ARFrame frame, int viewWidth, int viewHeight) {
        // Get a list of Plane-Trackables which are updated  on this frame.
        Collection<com.huawei.hiar.ARPlane> updatedPlanes = frame.getUpdatedPlanes();
        // Do a hittest on the current frame. The result is used to calculate
        // a focusPoint and to render the top most plane Trackable if
        // planeRendererMode is set to RENDER_TOP_MOST.
        com.huawei.hiar.ARHitResult hitResult = getHitResult(frame, viewWidth, viewHeight);
        // Calculate the focusPoint. It is used to determine the position of
        // the visualized grid.
        Vector3 focusPoint = getFocusPoint(frame, hitResult);

        @SuppressWarnings("nullness")
        @Nullable
        Material planeMaterial = planeMaterialFuture.getNow(null);
        if (planeMaterial != null) {
            planeMaterial.setFloat3(MATERIAL_SPOTLIGHT_FOCUS_POINT, focusPoint);
            planeMaterial.setFloat(MATERIAL_SPOTLIGHT_RADIUS, SPOTLIGHT_RADIUS);
        }

        if (planeRendererMode == PlaneRendererMode.RENDER_ALL && hitResult != null) {
            renderAllByAREngine(updatedPlanes, planeMaterial);
        } else if (planeRendererMode == PlaneRendererMode.RENDER_TOP_MOST && hitResult != null) {
            com.huawei.hiar.ARPlane topMostPlane = (com.huawei.hiar.ARPlane) hitResult.getTrackable();
            Optional.ofNullable(topMostPlane)
                    .ifPresent(plane -> renderPlane(plane, planeMaterial));
        }

        // Check for not tracking Plane-Trackables and remove them.
        cleanupOldPlaneVisualizer();
    }

    /**
     * 渲染AREngine检测到的所有平面
     * @param updatedPlanes {@link Collection}<{@link Plane}>
     * @param planeMaterial {@link Material}
     */
    private void renderAllByARCore(
            Collection<Plane> updatedPlanes,
            Material planeMaterial
    ) {
        for (Plane plane : updatedPlanes) {
            renderPlane(plane, planeMaterial);
        }
    }

    /**
     * 渲染AREngine检测到的所有平面
     * @param updatedPlanes {@link Collection}<{@link Plane}>
     * @param planeMaterial {@link Material}
     */
    private void renderAllByAREngine(
            Collection<com.huawei.hiar.ARPlane> updatedPlanes,
            Material planeMaterial
    ) {
        for (com.huawei.hiar.ARPlane plane : updatedPlanes) {
            renderPlane(plane, planeMaterial);
        }
    }

    /**
     * 渲染平面
     * @param plane         {@link Plane}
     * @param planeMaterial {@link Material}
     */
    private void renderPlane(Plane plane, Material planeMaterial) {
        PlaneVisualizer planeVisualizer;

        // Find the plane visualizer if it already exists.
        // If not, create a new plane visualizer for this plane.
        if (visualizerMap.containsKey(plane)) {
            planeVisualizer = visualizerMap.get(plane);
        } else {
            planeVisualizer = new PlaneVisualizer(new ARPlane(plane,null), renderer);
            Material overrideMaterial = materialOverrides.get(plane);
            if (overrideMaterial != null) {
                planeVisualizer.setPlaneMaterial(overrideMaterial);
            } else if (planeMaterial != null) {
                planeVisualizer.setPlaneMaterial(planeMaterial);
            }
            if (shadowMaterial != null) {
                planeVisualizer.setShadowMaterial(shadowMaterial);
            }
            planeVisualizer.setShadowReceiver(isShadowReceiver);
            planeVisualizer.setVisible(isVisible);
            planeVisualizer.setEnabled(isEnabled);
            visualizerMap.put(plane, planeVisualizer);
        }

        // Update the plane visualizer.
        Optional.ofNullable(planeVisualizer)
                .ifPresent(PlaneVisualizer::updatePlane);
    }
    private void renderPlane(com.huawei.hiar.ARPlane plane, Material planeMaterial) {
        PlaneVisualizer planeVisualizer;

        // Find the plane visualizer if it already exists.
        // If not, create a new plane visualizer for this plane.
        if (visualizerMapAREngine.containsKey(plane)) {
            planeVisualizer = visualizerMapAREngine.get(plane);
        } else {
            planeVisualizer = new PlaneVisualizer(new ARPlane(null,plane), renderer);
            Material overrideMaterial = materialOverridesAREngine.get(plane);
            if (overrideMaterial != null) {
                planeVisualizer.setPlaneMaterial(overrideMaterial);
            } else if (planeMaterial != null) {
                planeVisualizer.setPlaneMaterial(planeMaterial);
            }
            if (shadowMaterial != null) {
                planeVisualizer.setShadowMaterial(shadowMaterial);
            }
            planeVisualizer.setShadowReceiver(isShadowReceiver);
            planeVisualizer.setVisible(isVisible);
            planeVisualizer.setEnabled(isEnabled);
            visualizerMapAREngine.put(plane, planeVisualizer);
        }

        // Update the plane visualizer.
        Optional.ofNullable(planeVisualizer)
                .ifPresent(PlaneVisualizer::updatePlane);
    }

    /**
     * 删除不再跟踪的旧平面
     * 更新所有剩余平面的材质参数。
     */
    private void cleanupOldPlaneVisualizer() {
        if (ARPlatForm.isArCoreOrNone()){
            Iterator<Map.Entry<Plane, PlaneVisualizer>> iter = visualizerMap.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry<Plane, PlaneVisualizer> entry = iter.next();
                Plane plane = entry.getKey();
                PlaneVisualizer planeVisualizer = entry.getValue();

                // If this plane was subsumed by another plane or it has permanently stopped tracking,
                // remove it.
                if (plane.getSubsumedBy() != null || plane.getTrackingState() == TrackingState.STOPPED) {
                    planeVisualizer.release();
                    iter.remove();
                    continue;
                }
            }
        }else {
            Iterator<Map.Entry<com.huawei.hiar.ARPlane, PlaneVisualizer>> iter = visualizerMapAREngine.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry<com.huawei.hiar.ARPlane, PlaneVisualizer> entry = iter.next();
                com.huawei.hiar.ARPlane plane = entry.getKey();
                PlaneVisualizer planeVisualizer = entry.getValue();

                // If this plane was subsumed by another plane or it has permanently stopped tracking,
                // remove it.
                if (plane.getSubsumedBy() != null || plane.getTrackingState() == com.huawei.hiar.ARTrackable.TrackingState.STOPPED) {
                    planeVisualizer.release();
                    iter.remove();
                    continue;
                }
            }
        }
    }


    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    private void loadShadowMaterial() {
        Material.builder()
                .setSource(
                        renderer.getContext(),
                        RenderingResources.GetSceneformResource(
                                renderer.getContext(), RenderingResources.Resource.PLANE_SHADOW_MATERIAL))
                .build()
                .thenAccept(
                        material -> {
                            shadowMaterial = material;
                            for (PlaneVisualizer visualizer : visualizerMap.values()) {
                                visualizer.setShadowMaterial(shadowMaterial);
                            }
                        })
                .exceptionally(
                        throwable -> {
                            Log.e(TAG, "Unable to load plane shadow material.", throwable);
                            return null;
                        });
    }

    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    private void loadPlaneMaterial() {
        Texture.Sampler sampler =
                Texture.Sampler.builder()
                        .setMinMagFilter(Texture.Sampler.MagFilter.LINEAR)
                        .setWrapMode(Texture.Sampler.WrapMode.REPEAT)
                        .build();

        CompletableFuture<Texture> textureFuture =
                Texture.builder()
                        .setSource(
                                renderer.getContext(),
                                RenderingResources.GetSceneformResource(
                                        renderer.getContext(), RenderingResources.Resource.PLANE))
                        .setSampler(sampler)
                        .build();

        planeMaterialFuture =
                Material.builder()
                        .setSource(
                                renderer.getContext(),
                                RenderingResources.GetSceneformResource(
                                        renderer.getContext(), RenderingResources.Resource.PLANE_MATERIAL))
                        .build()
                        .thenCombine(
                                textureFuture,
                                (material, texture) -> {
                                    material.setTexture(MATERIAL_TEXTURE, texture);
                                    material.setFloat3(MATERIAL_COLOR, 1.0f, 1.0f, 1.0f);

                                    // TODO: Don't use hardcoded width and height... Need api for getting
                                    // width and
                                    // height from the Texture class.
                                    float widthToHeightRatio = DEFAULT_TEXTURE_WIDTH / DEFAULT_TEXTURE_HEIGHT;
                                    float scaleX = BASE_UV_SCALE;
                                    float scaleY = scaleX * widthToHeightRatio;
                                    material.setFloat2(MATERIAL_UV_SCALE, scaleX, scaleY);

                                    for (Map.Entry<Plane, PlaneVisualizer> entry : visualizerMap.entrySet()) {
                                        if (!materialOverrides.containsKey(entry.getKey())) {
                                            entry.getValue().setPlaneMaterial(material);
                                        }
                                    }
                                    return material;
                                });
    }

    /**
     * 获取射线检测结果
     * <p>ARCore的frame</p>
     * @param frame  {@link Frame}
     * @param width  int
     * @param height int
     * @return {@link HitResult}
     */
    @Nullable
    private HitResult getHitResult(Frame frame, int width, int height) {
        // If we hit a plane, return the hit point.
        List<HitResult> hits = frame.hitTest(width / 2f, height / 2f);
        if (hits != null && !hits.isEmpty()) {
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                Pose hitPose = hit.getHitPose();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitPose)) {
                    return hit;
                }
            }
        }
        return null;
    }

    /**
     * 获取射线检测结果
     * @param frame {@link com.huawei.hiar.ARFrame}
     * @param width int
     * @param height int
     * @return {@link HitResult}
     */
    private com.huawei.hiar.ARHitResult getHitResult(com.huawei.hiar.ARFrame frame, int width, int height) {
        // If we hit a plane, return the hit point.
        List<com.huawei.hiar.ARHitResult> hits = frame.hitTest(width / 2f, height / 2f);
        if (hits != null && !hits.isEmpty()) {
            for (com.huawei.hiar.ARHitResult hit : hits) {
                com.huawei.hiar.ARTrackable trackable = hit.getTrackable();
                com.huawei.hiar.ARPose hitPose = hit.getHitPose();
                if (trackable instanceof com.huawei.hiar.ARPlane && ((com.huawei.hiar.ARPlane) trackable).isPoseInPolygon(hitPose)) {
                    return hit;
                }
            }
        }
        return null;
    }

    /**
     * 计算当前的焦点
     * 焦点可作为当前平面的中心位置
     *
     * @param frame {@link Frame}
     * @param hit   {@link HitResult}
     * @return {@link Vector3}
     */
    private Vector3 getFocusPoint(Frame frame, HitResult hit) {
        if (hit != null) {
            Pose hitPose = hit.getHitPose();
            lastPlaneHitDistance = hit.getDistance();
            return new Vector3(hitPose.tx(), hitPose.ty(), hitPose.tz());
        }

        // If we didn't hit anything, project a point in front of the camera so that the spotlight
        // rolls off the edge smoothly.
        Pose cameraPose = frame.getCamera().getPose();
        Vector3 cameraPosition = new Vector3(cameraPose.tx(), cameraPose.ty(), cameraPose.tz());
        float[] zAxis = cameraPose.getZAxis();
        Vector3 backwards = new Vector3(zAxis[0], zAxis[1], zAxis[2]);

        return Vector3.add(cameraPosition, backwards.scaled(-lastPlaneHitDistance));
    }
    private Vector3 getFocusPoint(com.huawei.hiar.ARFrame frame, com.huawei.hiar.ARHitResult hit) {
        if (hit != null) {
            com.huawei.hiar.ARPose hitPose = hit.getHitPose();
            lastPlaneHitDistance = hit.getDistance();
            return new Vector3(hitPose.tx(), hitPose.ty(), hitPose.tz());
        }

        // If we didn't hit anything, project a point in front of the camera so that the spotlight
        // rolls off the edge smoothly.
        com.huawei.hiar.ARPose cameraPose = frame.getCamera().getPose();
        Vector3 cameraPosition = new Vector3(cameraPose.tx(), cameraPose.ty(), cameraPose.tz());
        float[] zAxis = cameraPose.getZAxis();
        Vector3 backwards = new Vector3(zAxis[0], zAxis[1], zAxis[2]);

        return Vector3.add(cameraPosition, backwards.scaled(-lastPlaneHitDistance));
    }


    /**
     * 平面渲染模式
     */
    public enum PlaneRendererMode {
        /**
         * 渲染所有平面
         */
        RENDER_ALL,
        /**
         * 只渲染位于最顶部的平面
         */
        RENDER_TOP_MOST
    }
}