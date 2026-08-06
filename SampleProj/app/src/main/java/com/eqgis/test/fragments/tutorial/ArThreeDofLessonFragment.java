package com.eqgis.test.fragments.tutorial;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.layout.SceneViewType;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;

/**
 * Camera 3DoF 增强现实降级方案教程
 * <pre>
 *     使用 CameraSceneView 将 Camera2 预览作为背景，并由旋转矢量传感器更新三维相机姿态。
 *     该方案无需 ARCore/AREngine，但只跟踪旋转，不提供空间位置、平面、锚点和尺度估计。
 * </pre>
 * @author tanyx
 */
public class ArThreeDofLessonFragment extends BaseXrSceneLessonFragment {
    private XrCapabilityStatus capabilityStatus;
    private boolean cameraSceneEnabled;

    @Override
    protected String getLessonTitle() {
        return "AR 3DoF";
    }

    @Override
    protected String getLessonDescription() {
        return "不依赖 AR 服务：使用 Camera2 作为背景、方向传感器控制视角，是 ARCore/AREngine 不可用时的轻量降级方案。";
    }

    /**
     * 相机权限可用时创建 CameraSceneView，否则使用静态场景说明原因
     * @return Camera 3DoF 或静态 SceneLayout
     */
    @Override
    protected SceneLayout createSceneLayout() {
        capabilityStatus = XrCapabilityStatus.inspect(requireContext());
        cameraSceneEnabled = capabilityStatus.canUseCameraFallback();
        return cameraSceneEnabled
                ? new SceneLayout(requireContext()).setSceneViewType(SceneViewType.CAMERA)
                : new SceneLayout(requireContext());
    }

    /**
     * 创建 Camera 3DoF 参照模型与降级能力说明
     * @param sceneLayout 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureXrScene(sceneLayout, !cameraSceneEnabled);
        if (cameraSceneEnabled) {
            showStatusBanner(
                    capabilityStatus.hasRotationSensor()
                            ? "Camera 3DoF 已启用\nCamera2 提供实时背景，旋转矢量传感器控制视角；不支持位置跟踪、平面识别和锚点。"
                            : "Camera2 背景已启用，但设备缺少旋转矢量传感器\n当前只能显示相机画面，无法获得完整 3DoF 姿态。",
                    !capabilityStatus.hasRotationSensor());
        } else {
            showStatusBanner(
                    "Camera 3DoF 无法启动\n"
                            + capabilityStatus.getCameraUnavailableReason()
                            + "\n当前仅展示静态三维场景。",
                    true);
        }

        addReferencePlane(
                new Vector3(0, -1.0f, -3.8f),
                5.4f,
                4.8f,
                new Color(0.07f, 0.18f, 0.30f));
        addReferenceCube(
                new Vector3(-1.15f, -0.55f, -3.4f),
                0.62f,
                new Color(0.08f, 0.58f, 1.0f));
        loadXrRenderable("gltf/DamagedHelmet.glb", renderable ->
                addXrModel(renderable, new Vector3(0.55f, -0.08f, -3.0f), 0.65f));
    }

    @Override
    protected void onReleaseXrScene() {
        capabilityStatus = null;
        cameraSceneEnabled = false;
    }
}
