package com.eqgis.test.fragments.tutorial;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.layout.SceneViewType;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;

/**
 * VR 场景 3DoF 教程
 * <pre>
 *     使用 VrSceneView 与旋转矢量传感器控制虚拟相机朝向，配合天空盒、GLTF 模型、
 *     平面和 Cube 构成纯虚拟沉浸场景。该页面不访问真实相机，也不提供位置跟踪。
 * </pre>
 * @author tanyx
 */
public class VrThreeDofLessonFragment extends BaseXrSceneLessonFragment {

    @Override
    protected String getLessonTitle() {
        return "VR 场景 3DoF";
    }

    @Override
    protected String getLessonDescription() {
        return "使用 VrSceneView 和方向传感器环视纯虚拟场景；天空盒提供沉浸背景，3DoF 仅跟踪设备旋转。";
    }

    /**
     * 创建由设备方向传感器驱动的 VR 场景
     * @return VR 类型 SceneLayout
     */
    @Override
    protected SceneLayout createSceneLayout() {
        return new SceneLayout(requireContext()).setSceneViewType(SceneViewType.VR);
    }

    /**
     * 创建天空盒、模型、Cube 和地面参照
     * @param sceneLayout 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureXrScene(sceneLayout, true);
        SensorManager sensorManager =
                (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        boolean rotationSensorReady = sensorManager != null
                && sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null;
        showStatusBanner(
                rotationSensorReady
                        ? "VR 3DoF 已启用\n转动手机即可环视天空盒与三维模型；当前模式不读取真实相机，也不跟踪设备位移。"
                        : "VR 场景已创建，但设备缺少旋转矢量传感器\n天空盒和模型仍可显示，视角无法随手机姿态更新。",
                !rotationSensorReady);

        addReferencePlane(
                new Vector3(0, -1.1f, -4.0f),
                7.0f,
                6.0f,
                new Color(0.06f, 0.12f, 0.22f));
        addReferenceCube(
                new Vector3(-1.35f, -0.50f, -3.7f),
                0.72f,
                new Color(0.08f, 0.58f, 1.0f));
        addReferenceCube(
                new Vector3(1.45f, -0.35f, -4.8f),
                0.90f,
                new Color(1.0f, 0.48f, 0.08f));
        loadXrRenderable("gltf/DamagedHelmet.glb", renderable ->
                addXrModel(renderable, new Vector3(0, -0.08f, -3.1f), 0.72f));
    }
}
