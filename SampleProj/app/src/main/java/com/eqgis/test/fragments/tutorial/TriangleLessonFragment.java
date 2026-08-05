package com.eqgis.test.fragments.tutorial;

import android.widget.LinearLayout;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.test.fragments.BaseSampleFragment;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.ModelRenderable;
import com.google.sceneform.rendering.RenderableDefinition;
import com.google.sceneform.rendering.Vertex;

import java.util.Arrays;
import java.util.Collections;

/**
 * 绘制三角形教程
 * <pre>
 *     本示例手动创建三个顶点、一个三角面索引和一个材质。
 *     用于说明 Mesh 最小构成，以及 RenderableDefinition 到 ModelRenderable 的创建流程。
 * </pre>
 * @author tanyx
 */
public class TriangleLessonFragment extends BaseSampleFragment {
    private Node triangleNode;

    @Override
    protected String getLessonTitle() {
        return "绘制三角形";
    }

    @Override
    protected String getLessonDescription() {
        return "本示例手动创建三个顶点和一个三角形索引，理解 Mesh 最小构成：顶点、法线、UV、索引和材质。";
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
     * 初始化三角形示例场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 60);
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(0.1f, 0.55f, 1.0f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    //desc- 三角形最小 Mesh 只需要三个顶点和一个三角面索引。
                    Vertex v0 = Vertex.builder()
                            .setPosition(new Vector3(-0.8f, -0.45f, -2.4f))
                            .setNormal(Vector3.back())
                            .setUvCoordinate(new Vertex.UvCoordinate(0, 0))
                            .build();
                    Vertex v1 = Vertex.builder()
                            .setPosition(new Vector3(0.8f, -0.45f, -2.4f))
                            .setNormal(Vector3.back())
                            .setUvCoordinate(new Vertex.UvCoordinate(1, 0))
                            .build();
                    Vertex v2 = Vertex.builder()
                            .setPosition(new Vector3(0, 0.65f, -2.4f))
                            .setNormal(Vector3.back())
                            .setUvCoordinate(new Vertex.UvCoordinate(0.5f, 1))
                            .build();
                    //desc- Submesh 负责把三角面索引和材质绑定到同一组几何数据上。
                    RenderableDefinition.Submesh submesh = RenderableDefinition.Submesh.builder()
                            .setTriangleIndices(Arrays.asList(0, 1, 2))
                            .setMaterial(material)
                            .build();
                    RenderableDefinition definition = RenderableDefinition.builder()
                            .setVertices(Arrays.asList(v0, v1, v2))
                            .setSubmeshes(Collections.singletonList(submesh))
                            .build();
                    ModelRenderable.builder()
                            .setSource(definition)
                            .build()
                            .thenAccept(renderable -> {
                                if (!isSceneActive()) {
                                    return;
                                }
                                triangleNode = new Node();
                                triangleNode.setRenderable(renderable);
                                triangleNode.setParent(sceneLayout.getRootNode());
                            });
                });
    }

    @Override
    protected void onBeforeDestroyScene() {
        if (triangleNode != null) {
            //desc- Fragment 切换时只解除节点挂载，避免旧帧继续访问已销毁材质。
            triangleNode.setParent(null);
            triangleNode = null;
        }
    }
}
