package com.eqgis.test.fragments.tutorial;

import android.widget.LinearLayout;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.test.fragments.BaseSampleFragment;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.MaterialFactory;

/**
 * 创建平面教程
 * <pre>
 *     本示例通过 GeometryUtils 创建一个透明平面。
 *     用于说明平面 Mesh 的尺寸、位置、UV、法线和透明材质效果。
 * </pre>
 * @author tanyx
 */
public class PlaneLessonFragment extends BaseSampleFragment {
    private Node planeNode;

    @Override
    protected String getLessonTitle() {
        return "创建平面";
    }

    @Override
    protected String getLessonDescription() {
        return "创建一个带透明材质的平面，用于理解平面 Mesh、UV 和场景中的参考面。";
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
     * 初始化平面示例场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 60);
        MaterialFactory.makeTransparentWithColor(requireContext(), new Color(0.2f, 0.8f, 0.5f, 0.65f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    planeNode = new Node();
                    planeNode.setRenderable(GeometryUtils.makePlane(
                            new Vector3(2.0f, 2.0f, 1.0f),
                            new Vector3(0, -0.35f, -2.6f),
                            material));
                    planeNode.setParent(sceneLayout.getRootNode());
                });
    }

    @Override
    protected void onBeforeDestroyScene() {
        if (planeNode != null) {
            //desc- 切换 Fragment 前解除平面节点挂载，由 SceneLayout 统一释放渲染资源。
            planeNode.setParent(null);
            planeNode = null;
        }
    }
}
