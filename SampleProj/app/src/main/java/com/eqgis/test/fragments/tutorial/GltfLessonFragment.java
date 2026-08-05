package com.eqgis.test.fragments.tutorial;

import android.widget.LinearLayout;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.test.fragments.BaseSampleFragment;
import com.eqgis.test.scene.GltfSampleScene;

/**
 * GLTF/GLB 模型加载教程
 * <pre>
 *     本示例复用 GltfSampleScene，展示 ModelRenderable 加载 GLB 模型的基本流程。
 *     模型加载完成后会根据包围盒缩放到合适尺寸，便于观察完整模型。
 * </pre>
 * @author tanyx
 */
public class GltfLessonFragment extends BaseSampleFragment {
    @Override
    protected String getLessonTitle() {
        return "加载 GLTF/GLB 模型";
    }

    @Override
    protected String getLessonDescription() {
        return "使用 ModelRenderable 加载 GLB 模型，并通过 ScaleTool 将模型缩放到合适尺寸。";
    }

    /**
     * 初始化图元类型切换操作
     * @param actionContainer 操作按钮容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        addPrimitiveTypeSpinner(actionContainer);
    }

    /**
     * 初始化 GLTF 模型示例场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 80);
        sampleScene = new GltfSampleScene();
        sampleScene.setSceneView(sceneLayout.getSceneView());
        sampleScene.create(requireContext(), sceneLayout.getRootNode());
    }
}
