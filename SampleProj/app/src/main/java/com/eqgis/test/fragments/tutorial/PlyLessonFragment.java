package com.eqgis.test.fragments.tutorial;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.test.fragments.BaseSampleFragment;
import com.eqgis.test.scene.PlyDataScene;

/**
 * PLY 模型加载教程
 * <pre>
 *     本示例复用 PlyDataScene，展示从 assets 加载 PLY 数据并交给 ModelRenderable 渲染的流程。
 *     可用于理解点云/Mesh 数据解析、包围盒计算和场景挂载。
 * </pre>
 * @author tanyx
 */
public class PlyLessonFragment extends BaseSampleFragment {
    @Override
    protected String getLessonTitle() {
        return "加载 PLY 模型";
    }

    @Override
    protected String getLessonDescription() {
        return "加载 assets 中的 PLY 数据，理解从数据解析到 ModelRenderable 渲染的基本流程。";
    }

    /**
     * 初始化 PLY 模型示例场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 80);
        sampleScene = new PlyDataScene();
        sampleScene.setSceneView(sceneLayout.getSceneView());
        sampleScene.create(requireContext(), sceneLayout.getRootNode());
    }
}
