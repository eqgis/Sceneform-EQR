package com.eqgis.test.fragments.tutorial;

import android.view.MotionEvent;

import com.eqgis.ar.ARAnchor;
import com.eqgis.ar.ARHitResult;
import com.eqgis.ar.ARPlane;
import com.eqgis.ar.OnTapArPlaneListener;
import com.eqgis.eqr.layout.ARSceneLayout;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.utils.ScaleTool;
import com.google.sceneform.AnchorNode;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.ModelRenderable;

import java.util.ArrayList;
import java.util.List;

/**
 * AR 水平平面识别与锚点放置教程
 * <pre>
 *     使用 ARSceneLayout 显示检测平面，点击平面后创建 ARAnchor 并放置 GLTF 模型。
 *     平面识别依赖 ARCore/AREngine 的 6DoF Session，设备不支持时只展示原因，
 *     不使用 Camera 3DoF 伪装平面检测能力。
 * </pre>
 * @author tanyx
 */
public class ArPlaneDetectionLessonFragment extends BaseXrSceneLessonFragment {
    private final List<ARAnchor> anchors = new ArrayList<>();
    private XrCapabilityStatus capabilityStatus;
    private boolean planeDetectionEnabled;
    private ARSceneLayout activeArLayout;
    private ModelRenderable placementRenderable;

    @Override
    protected String getLessonTitle() {
        return "AR 平面识别";
    }

    @Override
    protected String getLessonDescription() {
        return "移动手机识别水平平面，点击检测网格后创建锚点并放置三维模型；该能力无法由 3DoF 降级替代。";
    }

    /**
     * 仅在原生 AR 能力就绪时创建 ARSceneLayout
     * @return AR 场景或静态说明场景
     */
    @Override
    protected SceneLayout createSceneLayout() {
        capabilityStatus = XrCapabilityStatus.inspect(requireContext());
        planeDetectionEnabled = capabilityStatus.isArReady();
        return planeDetectionEnabled
                ? new ARSceneLayout(requireContext())
                : new SceneLayout(requireContext());
    }

    /**
     * 配置平面检测、点击放置和不支持设备的不可降级提示
     * @param sceneLayout 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureXrScene(sceneLayout, !planeDetectionEnabled);
        if (!planeDetectionEnabled) {
            showStatusBanner(
                    "AR 平面识别不可用\n"
                            + capabilityStatus.getArUnavailableReason() + "\n"
                            + "该功能需要 6DoF 位姿、特征点与平面跟踪，Camera 3DoF 无法提供等价降级方案。",
                    true);
            //desc- 静态平面仅用于说明页面构图，不代表已执行真实环境平面识别。
            addReferencePlane(
                    new Vector3(0, -0.95f, -3.6f),
                    5.2f,
                    4.8f,
                    new Color(0.10f, 0.23f, 0.36f));
            addReferenceCube(
                    new Vector3(0, -0.48f, -3.2f),
                    0.85f,
                    new Color(1.0f, 0.55f, 0.10f));
            return;
        }

        activeArLayout = (ARSceneLayout) sceneLayout;
        activeArLayout.setPlaneRendererEnabled(true);
        activeArLayout.setOnSessionInitializationListener(session ->
                showStatusBanner(
                        capabilityStatus.getEngineName()
                                + " 平面检测已启动\n缓慢移动手机扫描环境，出现网格后点击平面放置模型。",
                        false));
        activeArLayout.setOnTapPlaneListener(new OnTapArPlaneListener() {
            @Override
            public void onTapPlane(ARHitResult hitResult,
                                   ARPlane plane,
                                   MotionEvent motionEvent) {
                placeModelOnPlane(hitResult);
            }
        });
        showStatusBanner(
                capabilityStatus.getEngineName()
                        + " 正在启动\n准备完成后，移动手机识别水平平面并点击网格。",
                false);
        loadXrRenderable("gltf/DamagedHelmet.glb", renderable -> {
            placementRenderable = renderable;
            showStatusBanner(
                    capabilityStatus.getEngineName()
                            + " 已就绪\n缓慢移动手机扫描环境，出现网格后点击平面放置模型。",
                    false);
        });
    }

    private void placeModelOnPlane(ARHitResult hitResult) {
        if (!isSceneActive() || activeArLayout == null) {
            return;
        }
        if (placementRenderable == null) {
            showStatusBanner("平面已命中，模型资源仍在加载，请稍后再次点击。", false);
            return;
        }
        ARAnchor anchor = hitResult.createAnchor();
        if (anchor == null) {
            showStatusBanner("锚点创建失败，请重新扫描并点击平面。", true);
            return;
        }
        AnchorNode modelNode = new AnchorNode(anchor);
        modelNode.setRenderable(placementRenderable);
        modelNode.setLocalScale(Vector3.one().scaled(
                ScaleTool.calculateUnitsScale(placementRenderable) * 0.42f));
        modelNode.setParent(activeArLayout.getRootNode());
        trackXrNode(modelNode);
        anchors.add(anchor);
        showStatusBanner(
                "已在平面锚点放置模型\n继续点击其他网格位置可添加更多模型。",
                false);
    }

    /**
     * 页面销毁前移除平面点击监听并停止跟踪所有锚点
     */
    @Override
    protected void onReleaseXrScene() {
        if (activeArLayout != null) {
            activeArLayout.setOnTapPlaneListener(null);
            activeArLayout.setOnSessionInitializationListener(null);
        }
        for (ARAnchor anchor : anchors) {
            anchor.detach();
        }
        anchors.clear();
        placementRenderable = null;
        activeArLayout = null;
        capabilityStatus = null;
        planeDetectionEnabled = false;
    }
}
