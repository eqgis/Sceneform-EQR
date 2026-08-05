package com.eqgis.test.fragments.tutorial;

import android.util.Log;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.MaterialFactory;

/**
 * PBR 材质属性教程
 * <pre>
 *     同时展示非金属、金属和透明材质，比较 metallic、roughness 与 alpha 的视觉差异。
 * </pre>
 * @author tanyx
 */
public class MaterialPropertiesLessonFragment extends BaseMaterialCameraLessonFragment {
    private static final String TAG = MaterialPropertiesLessonFragment.class.getSimpleName();

    @Override
    protected String getLessonTitle() {
        return "PBR 材质属性";
    }

    @Override
    protected String getLessonDescription() {
        return "比较非金属、金属和透明材质，观察 metallic、roughness 与 alpha 对表面效果的影响。";
    }

    /**
     * 初始化材质属性对比场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 100);
        createOpaqueSphere(
                new Color(0.1f, 0.48f, 1.0f),
                new Vector3(-1.0f, 0, -3.0f),
                0.0f,
                0.75f);
        createOpaqueSphere(
                new Color(0.92f, 0.68f, 0.22f),
                new Vector3(0, 0, -3.0f),
                1.0f,
                0.16f);
        MaterialFactory.makeTransparentWithColor(
                        requireContext(),
                        new Color(0.22f, 0.82f, 0.64f, 0.48f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    material.setFloat(MaterialFactory.MATERIAL_METALLIC, 0.0f);
                    material.setFloat(MaterialFactory.MATERIAL_ROUGHNESS, 0.28f);
                    addRenderableNode(
                            GeometryUtils.makeSphere(0.44f, Vector3.zero(), material),
                            new Vector3(1.0f, 0, -3.0f));
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建透明材质失败", error);
                    return null;
                });
    }

    private void createOpaqueSphere(Color color, Vector3 position, float metallic, float roughness) {
        MaterialFactory.makeOpaqueWithColor(requireContext(), color)
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    applyPbrParameters(material, metallic, roughness);
                    addRenderableNode(
                            GeometryUtils.makeSphere(0.44f, Vector3.zero(), material),
                            position);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建 PBR 材质失败", error);
                    return null;
                });
    }

    private void applyPbrParameters(Material material, float metallic, float roughness) {
        material.setFloat(MaterialFactory.MATERIAL_METALLIC, metallic);
        material.setFloat(MaterialFactory.MATERIAL_ROUGHNESS, roughness);
        material.setFloat(MaterialFactory.MATERIAL_REFLECTANCE, 0.5f);
    }
}
