package com.eqgis.test.fragments.tutorial;

import android.net.Uri;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.utils.ScaleTool;
import com.eqgis.test.SampleLesson;
import com.eqgis.test.fragments.LessonHostFragment;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.ModelRenderable;
import com.google.sceneform.rendering.Renderable;
import com.google.sceneform.rendering.RenderableDefinition;
import com.google.sceneform.rendering.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 基础几何主题复用场景页
 * <pre>
 *     该类保留给需要在同一 SceneLayout 中切换多个几何示例的场景。
 *     当前教程入口默认使用一个功能一个 Fragment，因此本类暂不作为默认路径。
 * </pre>
 * @author tanyx
 */
public class GeometryTopicFragment extends BaseTutorialFragment implements LessonHostFragment {
    private final List<Node> lessonNodes = new ArrayList<>();
    private SampleLesson pendingLesson;

    /**
     * 创建基础几何复用场景页
     * @param lesson {@link SampleLesson} 初始展示的功能示例
     * @return 基础几何主题 Fragment
     */
    public static GeometryTopicFragment newInstance(SampleLesson lesson) {
        GeometryTopicFragment fragment = new GeometryTopicFragment();
        fragment.pendingLesson = lesson;
        return fragment;
    }

    @Override
    protected String getLessonTitle() {
        return "基础几何与 Mesh";
    }

    @Override
    protected String getLessonDescription() {
        return "本主题复用同一个 SceneLayout。切换教程时只替换场景内容，避免频繁销毁和重建 Filament Engine。";
    }

    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 80);
        if (pendingLesson != null) {
            showLesson(pendingLesson);
        }
    }

    /**
     * 展示指定几何教程功能
     * @param lesson {@link SampleLesson} 功能示例数据
     */
    @Override
    public void showLesson(SampleLesson lesson) {
        pendingLesson = lesson;
        if (!isSceneActive()) {
            return;
        }
        clearLessonNodes();
        switch (lesson.getId()) {
            case "lesson_triangle":
                showTriangle();
                break;
            case "lesson_plane":
                showPlane();
                break;
            case "lesson_cube":
                showCube();
                break;
            case "lesson_gltf":
                showGltf();
                break;
            case "lesson_ply":
                showPly();
                break;
            default:
                showTriangle();
                break;
        }
    }

    @Override
    protected void onBeforeDestroyScene() {
        clearLessonNodes();
    }

    private void clearLessonNodes() {
        for (Node node : lessonNodes) {
            node.setParent(null);
        }
        lessonNodes.clear();
    }

    private void addNode(Node node) {
        if (!isSceneActive()) {
            return;
        }
        //desc- 所有示例节点统一记录，便于切换功能时成组解除挂载。
        node.setParent(sceneLayout.getRootNode());
        lessonNodes.add(node);
    }

    /**
     * 展示手动构建的三角形 Mesh
     */
    private void showTriangle() {
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(0.1f, 0.55f, 1.0f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    //desc- 手动声明顶点属性，便于观察 Mesh 顶点、法线和 UV 的最小组合。
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
                                Node node = new Node();
                                node.setRenderable(renderable);
                                addNode(node);
                            });
                });
    }

    private void showPlane() {
        MaterialFactory.makeTransparentWithColor(requireContext(), new Color(0.2f, 0.8f, 0.5f, 0.65f))
                .thenAccept(material -> {
                    Node node = new Node();
                    node.setRenderable(GeometryUtils.makePlane(
                            new Vector3(2.0f, 2.0f, 1.0f),
                            new Vector3(0, -0.35f, -2.6f),
                            material));
                    addNode(node);
                });
    }

    private void showCube() {
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(1.0f, 0.55f, 0.1f))
                .thenAccept(material -> {
                    Node node = new Node();
                    node.setRenderable(GeometryUtils.makeCube(
                            new Vector3(0.8f, 0.8f, 0.8f),
                            new Vector3(0, 0, -2.8f),
                            material));
                    addNode(node);
                });
    }

    private void showGltf() {
        Node node = new Node();
        ModelRenderable.builder()
                .setSource(requireContext(), Uri.parse("gltf/bee.glb"))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(renderable -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    node.setRenderable(renderable);
                    node.setLocalScale(Vector3.one().scaled(ScaleTool.calculateUnitsScale(renderable)));
                    node.setLocalPosition(new Vector3(0, 0, -3.0f));
                    addNode(node);
                });
    }

    private void showPly() {
        Node node = new Node();
        ModelRenderable.builder()
                .setSource(requireContext(), Uri.parse("sofa.ply"))
                .setDataFormat(Renderable.RenderableDataFormat.PLY)
                .build()
                .thenAccept(renderable -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    node.setRenderable(renderable);
                    node.setLocalScale(Vector3.one().scaled(ScaleTool.calculateUnitsScale(renderable)));
                    node.setLocalPosition(new Vector3(0, 0, -3.2f));
                    addNode(node);
                });
    }
}
