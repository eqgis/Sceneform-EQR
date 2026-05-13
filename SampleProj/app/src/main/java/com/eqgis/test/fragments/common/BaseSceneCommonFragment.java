package com.eqgis.test.fragments.common;

import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.test.fragments.BaseSampleFragment;
import com.eqgis.test.scene.GltfSampleScene;

/**
 * 普通 3D 常用示例 Fragment
 * <pre>
 *     使用 SceneLayout 加载 GLTF 示例模型，并启用基础相机手势控制。
 *     当前常用示例默认跳转原 Activity，本类保留给后续 Fragment 化迁移。
 * </pre>
 * @author tanyx
 */
public class BaseSceneCommonFragment extends BaseSampleFragment {
    @Override
    protected String getLessonTitle() {
        return "普通 3D 场景";
    }

    @Override
    protected String getLessonDescription() {
        return "使用 SceneLayout 创建普通三维场景，加载 GLTF 模型，并添加环境光与基础相机参数。";
    }

    /**
     * 初始化普通 3D 场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 50);
        sceneLayout.getCamera().setVerticalFovDegrees(45);
        sceneLayout.getCamera().setFarClipPlane(100);
        NodeGestureController.getInstance()
                .setCamera(sceneLayout.getCamera())
                .init(requireContext())
                .setEnabled(true);

        sampleScene = new GltfSampleScene();
        sampleScene.setSceneView(sceneLayout.getSceneView());
        sampleScene.create(requireContext(), sceneLayout.getRootNode());
    }
}
