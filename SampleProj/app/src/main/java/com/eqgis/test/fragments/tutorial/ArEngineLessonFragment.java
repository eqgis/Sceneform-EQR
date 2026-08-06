package com.eqgis.test.fragments.tutorial;

import com.eqgis.eqr.layout.ARSceneLayout;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.layout.SceneViewType;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;

/**
 * ARCore 与 AREngine 场景教程
 * <pre>
 *     支持设备使用 ARSceneLayout 创建 6DoF AR Session；设备不兼容、AR 服务缺失或
 *     版本过低时，自动切换为 Camera2 相机背景与方向传感器驱动的 3DoF 场景，并通过
 *     常驻警告横幅明确显示检测原因和当前降级方案。
 * </pre>
 * @author tanyx
 */
public class ArEngineLessonFragment extends BaseXrSceneLessonFragment {
    private XrCapabilityStatus capabilityStatus;
    private boolean nativeArEnabled;
    private ARSceneLayout activeArLayout;

    @Override
    protected String getLessonTitle() {
        return "ARCore / AREngine";
    }

    @Override
    protected String getLessonDescription() {
        return "优先使用 ARCore 或华为 AREngine 提供 6DoF 跟踪；设备不支持时自动降级为 Camera 3DoF。";
    }

    /**
     * 根据设备能力创建原生 AR、Camera 3DoF 或无相机占位场景
     * @return 与当前设备能力匹配的场景布局
     */
    @Override
    protected SceneLayout createSceneLayout() {
        capabilityStatus = XrCapabilityStatus.inspect(requireContext());
        nativeArEnabled = capabilityStatus.isArReady();
        if (nativeArEnabled) {
            return new ARSceneLayout(requireContext());
        }
        if (capabilityStatus.canUseCameraFallback()) {
            return new SceneLayout(requireContext()).setSceneViewType(SceneViewType.CAMERA);
        }
        return new SceneLayout(requireContext());
    }

    /**
     * 创建 AR 模型；不支持设备展示相同模型并说明当前降级状态
     * @param sceneLayout 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        boolean cameraBackground = nativeArEnabled || capabilityStatus.canUseCameraFallback();
        configureXrScene(sceneLayout, !cameraBackground);

        if (nativeArEnabled) {
            activeArLayout = (ARSceneLayout) sceneLayout;
            activeArLayout.setPlaneRendererEnabled(false);
            activeArLayout.setOnSessionInitializationListener(session ->
                    showStatusBanner(
                            capabilityStatus.getEngineName()
                                    + " Session 已启动\n当前使用 6DoF：支持设备旋转与空间位置跟踪。",
                            false));
            showStatusBanner(
                    capabilityStatus.getEngineName()
                            + " 已就绪，正在启动 6DoF AR Session…",
                    false);
        } else if (capabilityStatus.canUseCameraFallback()) {
            String sensorNote = capabilityStatus.hasRotationSensor()
                    ? "方向传感器可用，可随手机姿态旋转视角。"
                    : "当前缺少旋转矢量传感器，相机背景可用，但视角无法随手机姿态更新。";
            showStatusBanner(
                    capabilityStatus.getEngineName() + " 不可用\n"
                            + capabilityStatus.getArUnavailableReason() + "\n"
                            + "当前已降级：Camera2 实时背景 + 3DoF 姿态。" + sensorNote,
                    true);
            addReferencePlane(
                    new Vector3(0, -1.0f, -3.7f),
                    5.2f,
                    4.4f,
                    new Color(0.08f, 0.20f, 0.32f));
        } else {
            showStatusBanner(
                    capabilityStatus.getEngineName() + " 无法启动\n"
                            + capabilityStatus.getArUnavailableReason() + "\n"
                            + "Camera 3DoF 降级也不可用，当前仅展示静态三维场景。",
                    true);
            addReferencePlane(
                    new Vector3(0, -1.0f, -3.7f),
                    5.2f,
                    4.4f,
                    new Color(0.08f, 0.20f, 0.32f));
        }

        loadXrRenderable("gltf/DamagedHelmet.glb", renderable ->
                addXrModel(renderable, new Vector3(0, -0.10f, -2.8f), 0.72f));
        addReferenceCube(
                new Vector3(-1.25f, -0.55f, -3.8f),
                0.55f,
                new Color(0.08f, 0.55f, 1.0f));
    }

    /**
     * 解除 AR Session 初始化监听，避免销毁后回调持有 Fragment
     */
    @Override
    protected void onReleaseXrScene() {
        if (activeArLayout != null) {
            activeArLayout.setOnSessionInitializationListener(null);
        }
        activeArLayout = null;
        capabilityStatus = null;
        nativeArEnabled = false;
    }
}
