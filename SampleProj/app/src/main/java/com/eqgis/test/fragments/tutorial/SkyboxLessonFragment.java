package com.eqgis.test.fragments.tutorial;

import android.util.Log;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.MaterialFactory;

/**
 * 天空盒教程
 * <pre>
 *     加载 KTX 天空盒作为场景背景，并配合同环境的 IBL 展示背景与物体反射的一致性。
 * </pre>
 * @author tanyx
 */
public class SkyboxLessonFragment extends BaseMaterialCameraLessonFragment {
    private static final String TAG = SkyboxLessonFragment.class.getSimpleName();

    @Override
    protected String getLessonTitle() {
        return "天空盒";
    }

    @Override
    protected String getLessonDescription() {
        return "加载 pillars KTX 天空盒作为远景背景。";
    }

    /**
     * 初始化天空盒场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 90);
        sceneLayout.setSkybox("enviroments/pillars_2k_skybox.ktx");
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(0.86f, 0.88f, 0.92f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    material.setFloat(MaterialFactory.MATERIAL_METALLIC, 1.0f);
                    material.setFloat(MaterialFactory.MATERIAL_ROUGHNESS, 0.12f);
                    addRenderableNode(
                            GeometryUtils.makeSphere(0.55f, Vector3.zero(), material),
                            new Vector3(-0.62f, 0, -3.0f));
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建天空盒反射球失败", error);
                    return null;
                });
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(0.14f, 0.48f, 0.92f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    material.setFloat(MaterialFactory.MATERIAL_METALLIC, 0.25f);
                    material.setFloat(MaterialFactory.MATERIAL_ROUGHNESS, 0.42f);
                    addRenderableNode(
                            GeometryUtils.makeCube(new Vector3(0.9f, 0.9f, 0.9f), Vector3.zero(), material),
                            new Vector3(0.72f, 0, -3.0f));
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建天空盒参考 Cube 失败", error);
                    return null;
                });
    }
}
