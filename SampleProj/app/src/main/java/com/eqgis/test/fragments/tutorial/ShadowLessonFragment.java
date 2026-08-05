package com.eqgis.test.fragments.tutorial;

import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.Node;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Light;
import com.google.sceneform.rendering.MaterialFactory;

/**
 * 实时阴影教程
 * <pre>
 *     使用投射阴影的平行光、阴影投射物和阴影接收平面展示实时阴影链路。
 * </pre>
 * @author tanyx
 */
public class ShadowLessonFragment extends BaseMaterialCameraLessonFragment {
    private static final String TAG = ShadowLessonFragment.class.getSimpleName();
    private Node lightNode;

    @Override
    protected String getLessonTitle() {
        return "实时阴影";
    }

    @Override
    protected String getLessonDescription() {
        return "开关平行光的阴影投射，理解 Light、ShadowCaster 与 ShadowReceiver 三者的配合。";
    }

    /**
     * 初始化阴影投射物、接收平面和主光源
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 10);
        createShadowCaster();
        createShadowReceiver();
        lightNode = new Node();
        addSceneNode(lightNode);
        updateShadowLight(true);
    }

    /**
     * 初始化阴影开关
     * @param actionContainer 操作按钮容器
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        Switch shadowSwitch = new Switch(requireContext());
        shadowSwitch.setText("启用实时阴影");
        shadowSwitch.setTextColor(0xff333333);
        shadowSwitch.setTextSize(14);
        shadowSwitch.setGravity(Gravity.CENTER_VERTICAL);
        shadowSwitch.setChecked(true);
        shadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateShadowLight(isChecked));
        actionContainer.addView(shadowSwitch, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void createShadowCaster() {
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(0.95f, 0.42f, 0.12f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    Node cubeNode = addRenderableNode(
                            GeometryUtils.makeCube(new Vector3(0.9f, 0.9f, 0.9f), Vector3.zero(), material),
                            new Vector3(0, -0.25f, -3.0f));
                    if (cubeNode.getRenderableInstance() != null) {
                        cubeNode.getRenderableInstance().setShadowCaster(true);
                    }
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建阴影投射 Cube 失败", error);
                    return null;
                });
    }

    private void createShadowReceiver() {
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(0.72f, 0.74f, 0.78f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    Node planeNode = addRenderableNode(
                            GeometryUtils.makePlane(new Vector3(4.5f, 1.0f, 4.5f), Vector3.zero(), material),
                            new Vector3(0, -0.72f, -3.0f));
                    if (planeNode.getRenderableInstance() != null) {
                        planeNode.getRenderableInstance().setShadowReceiver(true);
                    }
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建阴影接收平面失败", error);
                    return null;
                });
    }

    private void updateShadowLight(boolean enabled) {
        if (!isSceneActive() || lightNode == null) {
            return;
        }
        //desc- 重新构建 Light，使开关状态直接作用于 Filament 的实时阴影配置。
        Light light = Light.builder(Light.Type.DIRECTIONAL)
                .setColorTemperature(5200)
                .setIntensity(2200)
                .setShadowCastingEnabled(enabled)
                .build();
        lightNode.setLight(null);
        lightNode.setWorldRotation(Quaternion.lookRotation(
                new Vector3(-0.45f, -1.0f, -0.35f),
                Vector3.up()));
        lightNode.setLight(light);
    }
}
