package com.eqgis.test.fragments.common;

import android.widget.Button;
import android.widget.LinearLayout;

import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.test.fragments.BaseSampleFragment;
import com.eqgis.test.scene.PlyDataScene;
import com.eqgis.test.scene.PlyGSDataScene;

/**
 * PLY点云数据 常用示例 Fragment
 * <pre>
 *     加载 PLY 点云。
 *     当前常用示例默认跳转原 Activity，本类保留给后续 Fragment 化迁移。
 * </pre>
 * @author tanyx
 */
public class PlyCommonFragment extends BaseSampleFragment {
    private boolean useGsData = false;

    @Override
    protected String getLessonTitle() {
        return "PLY点云数据场景";
    }

    @Override
    protected String getLessonDescription() {
        return "PLY数据解析，点云渲染效果。";
    }

    /**
     * 初始化 PLY/3DGS 场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 80);
        NodeGestureController.getInstance()
                .setCamera(sceneLayout.getCamera())
                .init(requireContext())
                .setEnabled(true);
        loadPlyScene();
    }

    /**
     * 初始化数据切换操作按钮
     * @param actionContainer 操作按钮容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        Button switchButton = new Button(requireContext());
        switchButton.setText("切换 PLY / 3DGS");
        switchButton.setOnClickListener(v -> {
            if (sampleScene != null) {
                sampleScene.destroy(requireContext());
            }
            useGsData = !useGsData;
            loadPlyScene();
        });
        actionContainer.addView(switchButton);
    }

    /**
     * 加载当前选择的数据场景
     */
    private void loadPlyScene() {
        sampleScene = useGsData ? new PlyGSDataScene() : new PlyDataScene();
        sampleScene.setSceneView(sceneLayout.getSceneView());
        sampleScene.create(requireContext(), sceneLayout.getRootNode());
    }
}
