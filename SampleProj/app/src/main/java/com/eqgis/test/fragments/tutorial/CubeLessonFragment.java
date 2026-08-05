package com.eqgis.test.fragments.tutorial;

import android.widget.LinearLayout;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.test.fragments.BaseSampleFragment;
import com.google.sceneform.Node;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.MaterialFactory;

/**
 * 绘制 Cube 教程
 * <pre>
 *     本示例通过 GeometryUtils 创建立方体，并放置到相机前方。
 *     用于观察立方体顶点、面、法线和不透明材质在三维空间中的表现。
 * </pre>
 * @author tanyx
 */
public class CubeLessonFragment extends BaseSampleFragment {
    private Node cubeNode;

    @Override
    protected String getLessonTitle() {
        return "绘制 Cube";
    }

    @Override
    protected String getLessonDescription() {
        return "使用几何工具创建立方体，观察顶点、面、法线和材质在三维空间中的效果。";
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
     * 初始化立方体示例场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 80);
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(1.0f, 0.55f, 0.1f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    cubeNode = new Node();
                    cubeNode.setRenderable(GeometryUtils.makeCube(
                            new Vector3(0.8f, 0.8f, 0.8f),
                            Vector3.zero(),
                            material));
                    cubeNode.setWorldPosition(new Vector3(0, 0, -2.8f));
                    cubeNode.setWorldRotation(Quaternion.multiply(new Quaternion(Vector3.up(),45),new Quaternion(Vector3.right(),30)));
                    cubeNode.setParent(sceneLayout.getRootNode());
                });
    }

    @Override
    protected void onBeforeDestroyScene() {
        if (cubeNode != null) {
            //desc- 解除节点挂载后再销毁 SceneLayout，避免切换页面时访问已失效 Renderable。
            cubeNode.setParent(null);
            cubeNode = null;
        }
    }
}
