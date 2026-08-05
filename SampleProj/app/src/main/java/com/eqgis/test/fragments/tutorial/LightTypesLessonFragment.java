package com.eqgis.test.fragments.tutorial;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.Node;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Light;
import com.google.sceneform.rendering.MaterialFactory;

/**
 * 直接光类型教程
 * <pre>
 *     在同一个 Cube 和参考平面上切换平行光、点光源与聚光灯，比较位置、方向和衰减差异。
 * </pre>
 * @author tanyx
 */
public class LightTypesLessonFragment extends BaseMaterialCameraLessonFragment {
    private static final String TAG = LightTypesLessonFragment.class.getSimpleName();
    private Node lightNode;

    @Override
    protected String getLessonTitle() {
        return "灯光类型";
    }

    @Override
    protected String getLessonDescription() {
        return "切换平行光、点光源和聚光灯，观察光照方向、空间位置、衰减半径与锥角的区别。";
    }

    /**
     * 初始化灯光类型对比场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 12);
        createReferenceObjects();
        lightNode = new Node();
        addSceneNode(lightNode);
        updateLight(Light.Type.DIRECTIONAL);
    }

    /**
     * 初始化灯光类型下拉框
     * @param actionContainer 操作按钮容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        String[] names = {"平行光", "点光源", "聚光灯"};
        Light.Type[] types = {
                Light.Type.DIRECTIONAL,
                Light.Type.POINT,
                Light.Type.SPOTLIGHT
        };
        addLabeledSpinner(actionContainer, "灯光：", names, 0,
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        updateLight(types[position]);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
    }

    private void createReferenceObjects() {
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(0.92f, 0.92f, 0.95f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    Node cubeNode = addRenderableNode(
                            GeometryUtils.makeCube(new Vector3(0.9f, 0.9f, 0.9f), Vector3.zero(), material),
                            new Vector3(0, -0.28f, -3.0f));
                    if (cubeNode.getRenderableInstance() != null) {
                        cubeNode.getRenderableInstance().setShadowCaster(true);
                    }
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建灯光测试 Cube 失败", error);
                    return null;
                });
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(0.32f, 0.34f, 0.38f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    Node planeNode = addRenderableNode(
                            GeometryUtils.makePlane(new Vector3(4.2f, 1.0f, 4.0f), Vector3.zero(), material),
                            new Vector3(0, -0.78f, -3.0f));
                    if (planeNode.getRenderableInstance() != null) {
                        planeNode.getRenderableInstance().setShadowReceiver(true);
                    }
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建灯光测试平面失败", error);
                    return null;
                });
    }

    private void updateLight(Light.Type type) {
        if (!isSceneActive() || lightNode == null) {
            return;
        }
        Light.Builder builder = Light.builder(type)
                .setColorTemperature(5200)
                .setShadowCastingEnabled(true);
        Vector3 position = new Vector3(0, 1.2f, -1.8f);
        Quaternion rotation;
        if (type == Light.Type.DIRECTIONAL) {
            builder.setIntensity(1800);
            rotation = Quaternion.lookRotation(new Vector3(-0.35f, -1.0f, -0.45f), Vector3.up());
        } else if (type == Light.Type.POINT) {
            builder.setIntensity(5000).setFalloffRadius(4.5f);
            rotation = Quaternion.identity();
        } else {
            builder.setIntensity(9000)
                    .setFalloffRadius(5.0f)
                    .setInnerConeAngle(0.22f)
                    .setOuterConeAngle(0.58f);
            rotation = Quaternion.lookRotation(
                    Vector3.subtract(new Vector3(0, -0.25f, -3.0f), position),
                    Vector3.up());
        }
        lightNode.setLight(null);
        lightNode.setWorldPosition(position);
        lightNode.setWorldRotation(rotation);
        lightNode.setLight(builder.build());
    }
}
