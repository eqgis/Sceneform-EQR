package com.eqgis.test.fragments.common;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.test.fragments.BaseSampleFragment;
import com.eqgis.test.scene.InteractiveScene;

/**
 * 交互常用示例 Fragment
 * <pre>
 *     复用 InteractiveScene 演示模型点击、射线命中和 ViewRenderable 标记。
 *     当前常用示例默认跳转原 Activity，本类保留给后续 Fragment 化迁移。
 * </pre>
 * @author tanyx
 */
public class InteractiveCommonFragment extends BaseSampleFragment {
    @Override
    protected String getLessonTitle() {
        return "手势交互";
    }

    @Override
    protected String getLessonDescription() {
        return "演示模型点击、射线命中、ViewRenderable 标记和基础触摸交互。点击模型后会在命中点生成文本标记。";
    }

    /**
     * 初始化交互示例场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 80);
        sampleScene = new InteractiveScene();
        sampleScene.setSceneView(sceneLayout.getSceneView());
        sampleScene.create(requireContext(), sceneLayout.getRootNode());
    }
}
