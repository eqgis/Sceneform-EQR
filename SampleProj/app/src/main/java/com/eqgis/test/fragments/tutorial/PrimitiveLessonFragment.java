package com.eqgis.test.fragments.tutorial;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.eqgis.eqr.layout.SceneLayout;
import com.google.android.filament.RenderableManager;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.ModelRenderable;
import com.google.sceneform.rendering.RenderableDefinition;
import com.google.sceneform.rendering.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 基本图元绘制教程。
 * <p>
 * 使用同一个教程页分别构建点、独立线段、连续折线、独立三角形和三角带，展示不同
 * {@link RenderableManager.PrimitiveType} 对顶点索引的解释方式。
 * </p>
 * <pre>{@code
 * RenderableDefinition.builder()
 *         .setVertices(vertices)
 *         .setSubGeometries(Collections.singletonList(subGeometry))
 *         .setPrimitiveType(RenderableManager.PrimitiveType.LINE_STRIP)
 *         .build();
 * }</pre>
 *
 * @author tanyx
 * @since 2026/8/7
 * @version 1.0
 */
public class PrimitiveLessonFragment extends BaseTutorialFragment {
    private static final String TAG = PrimitiveLessonFragment.class.getSimpleName();
    private static final float DEFAULT_POINT_SIZE = 18.0f;
    private static final float DEFAULT_LINE_WIDTH = 6.0f;
    private static final Color WHITE_VERTEX_COLOR = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    private static final String[] PRIMITIVE_NAMES = {
            "POINTS（点）",
            "LINES（独立线段）",
            "LINE_STRIP（连续折线）",
            "TRIANGLES（独立三角形）",
            "TRIANGLE_STRIP（三角带）"
    };
    private static final RenderableManager.PrimitiveType[] PRIMITIVE_TYPES = {
            RenderableManager.PrimitiveType.POINTS,
            RenderableManager.PrimitiveType.LINES,
            RenderableManager.PrimitiveType.LINE_STRIP,
            RenderableManager.PrimitiveType.TRIANGLES,
            RenderableManager.PrimitiveType.TRIANGLE_STRIP
    };

    private Node primitiveNode;
    private Material pointMaterial;
    private Material lineMaterial;
    private Material coloredMaterial;
    private TextView statusView;
    private LinearLayout pointSizeContainer;
    private LinearLayout lineWidthContainer;
    private RenderableManager.PrimitiveType selectedPrimitive =
            RenderableManager.PrimitiveType.POINTS;
    private float pointSize = DEFAULT_POINT_SIZE;
    private float lineWidth = DEFAULT_LINE_WIDTH;
    private int renderGeneration;

    @Override
    protected String getLessonTitle() {
        return "绘制基本图元";
    }

    @Override
    protected String getLessonDescription() {
        return "切换 POINTS、LINES、LINE_STRIP、TRIANGLES 和 TRIANGLE_STRIP，观察 Filament 如何按不同拓扑解释顶点与索引。";
    }

    /**
     * 初始化图元类型与点尺寸控制面板。
     *
     * @param actionContainer 操作按钮容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        actionContainer.setOrientation(LinearLayout.VERTICAL);
        statusView = createPanelText(getPrimitiveDescription(selectedPrimitive));
        actionContainer.addView(statusView);

        LinearLayout primitiveRow = new LinearLayout(requireContext());
        primitiveRow.setGravity(Gravity.CENTER_VERTICAL);
        primitiveRow.setMinimumHeight(dp(44));

        TextView primitiveLabel = createPanelText("图元类型：");
        primitiveRow.addView(primitiveLabel, new LinearLayout.LayoutParams(
                dp(88),
                ViewGroup.LayoutParams.MATCH_PARENT));

        Spinner primitiveSpinner = new Spinner(requireContext());
        configureTutorialSpinner(primitiveSpinner);
        primitiveSpinner.setAdapter(createTutorialSpinnerAdapter(PRIMITIVE_NAMES));
        primitiveSpinner.setSelection(0, false);
        primitiveSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPrimitive = PRIMITIVE_TYPES[position];
                updatePanelForPrimitive();
                renderSelectedPrimitive();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        primitiveRow.addView(primitiveSpinner, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f));
        actionContainer.addView(primitiveRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        pointSizeContainer = createPointSizeControls();
        actionContainer.addView(pointSizeContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        lineWidthContainer = createLineWidthControls();
        lineWidthContainer.setVisibility(View.GONE);
        actionContainer.addView(lineWidthContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView hintView = createPanelText(
                "切换时会重建与目标拓扑匹配的索引。点尺寸和线宽均使用屏幕像素；宽线由专用材质在屏幕空间展开。"
        );
        hintView.setTextColor(0xff6e6e73);
        actionContainer.addView(hintView);
    }

    /**
     * 加载点、线图元专用材质和普通颜色材质，并创建默认图元。
     *
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 60);
        MaterialFactory.makePointsWithColor(
                        requireContext(), new Color(0.0f, 0.48f, 1.0f), DEFAULT_POINT_SIZE)
                .thenCombine(
                        MaterialFactory.makeLinesWithColor(
                                requireContext(),
                                new Color(0.0f, 0.48f, 1.0f),
                                DEFAULT_LINE_WIDTH),
                        (points, lines) -> new Material[]{points, lines})
                .thenCombine(
                        MaterialFactory.makeOpaqueWithColor(
                                requireContext(), new Color(0.0f, 0.48f, 1.0f)),
                        (primitiveMaterials, colored) -> new Material[]{
                                primitiveMaterials[0], primitiveMaterials[1], colored
                        })
                .thenAccept(materials -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    pointMaterial = materials[0];
                    lineMaterial = materials[1];
                    coloredMaterial = materials[2];
                    pointMaterial.setFloat(MaterialFactory.VERTEX_POINT_SIZE, pointSize);
                    lineMaterial.setFloat(MaterialFactory.LINE_WIDTH, lineWidth);
                    renderSelectedPrimitive();
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建基本图元材质失败", error);
                    return null;
                });
    }

    /**
     * 创建点尺寸控制区域。
     *
     * @return 仅在 POINTS 模式显示的控制区域
     */
    private LinearLayout createPointSizeControls() {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);

        TextView pointSizeLabel = createPanelText("点尺寸：18 px");
        container.addView(pointSizeLabel, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        SeekBar pointSizeBar = new SeekBar(requireContext());
        pointSizeBar.setMax(28);
        pointSizeBar.setProgress(14);
        pointSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pointSize = progress + 4.0f;
                pointSizeLabel.setText("点尺寸：" + (int) pointSize + " px");
                if (pointMaterial != null) {
                    pointMaterial.setFloat(MaterialFactory.VERTEX_POINT_SIZE, pointSize);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        container.addView(pointSizeBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return container;
    }

    /**
     * 创建线宽控制区域。
     *
     * @return 仅在 LINES 和 LINE_STRIP 模式显示的控制区域
     */
    private LinearLayout createLineWidthControls() {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);

        TextView lineWidthLabel = createPanelText("线宽：6 px");
        container.addView(lineWidthLabel, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        SeekBar lineWidthBar = new SeekBar(requireContext());
        lineWidthBar.setMax(29);
        lineWidthBar.setProgress(5);
        lineWidthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lineWidth = progress + 1.0f;
                lineWidthLabel.setText("线宽：" + (int) lineWidth + " px");
                if (lineMaterial != null) {
                    lineMaterial.setFloat(MaterialFactory.LINE_WIDTH, lineWidth);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        container.addView(lineWidthBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return container;
    }

    /**
     * 创建统一风格的面板文字。
     *
     * @param text 展示文案
     * @return 已配置字号和颜色的 TextView
     */
    private TextView createPanelText(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(0xff333333);
        textView.setTextSize(14);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setLineSpacing(dp(2), 1.0f);
        return textView;
    }

    /** 根据当前图元更新状态说明与点、线尺寸区域可见性。 */
    private void updatePanelForPrimitive() {
        if (statusView != null) {
            statusView.setText(getPrimitiveDescription(selectedPrimitive));
        }
        if (pointSizeContainer != null) {
            pointSizeContainer.setVisibility(
                    selectedPrimitive == RenderableManager.PrimitiveType.POINTS
                            ? View.VISIBLE
                            : View.GONE);
        }
        if (lineWidthContainer != null) {
            lineWidthContainer.setVisibility(isLinePrimitive(selectedPrimitive)
                    ? View.VISIBLE
                    : View.GONE);
        }
    }

    /** 创建当前选中拓扑对应的 Renderable，并替换场景中的旧节点。 */
    private void renderSelectedPrimitive() {
        if (!isSceneActive()
                || pointMaterial == null
                || lineMaterial == null
                || coloredMaterial == null) {
            return;
        }
        final int generation = ++renderGeneration;
        final RenderableManager.PrimitiveType primitiveType = selectedPrimitive;
        PrimitiveGeometryData sourceMesh = createPrimitive(primitiveType);
        boolean linePrimitive = isLinePrimitive(primitiveType);
        PrimitiveGeometryData renderMesh = linePrimitive
                ? createWideLineMesh(sourceMesh, primitiveType)
                : sourceMesh;
        RenderableManager.PrimitiveType renderPrimitiveType = linePrimitive
                ? RenderableManager.PrimitiveType.TRIANGLES
                : primitiveType;
        Material material;
        if (primitiveType == RenderableManager.PrimitiveType.POINTS) {
            material = pointMaterial;
        } else if (linePrimitive) {
            material = lineMaterial;
        } else {
            material = coloredMaterial;
        }

        clearPrimitiveNode();
        RenderableDefinition.SubGeometry subGeometry = RenderableDefinition.SubGeometry.builder()
                .setTriangleIndices(renderMesh.indices)
                .setMaterial(material)
                .build();
        RenderableDefinition definition = RenderableDefinition.builder()
                .setVertices(renderMesh.vertices)
                .setSubGeometries(Collections.singletonList(subGeometry))
                .setPrimitiveType(renderPrimitiveType)
                .build();
        ModelRenderable.builder()
                .setSource(definition)
                .build()
                .thenAccept(renderable -> {
                    if (!isSceneActive() || generation != renderGeneration) {
                        return;
                    }
                    renderable.setShadowCaster(false);
                    renderable.setShadowReceiver(false);
                    primitiveNode = new Node();
                    primitiveNode.setRenderable(renderable);
                    primitiveNode.setParent(sceneLayout.getRootNode());
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建基本图元 Renderable 失败", error);
                    return null;
                });
    }

    /**
     * 根据图元类型生成可清晰观察其拓扑特征的顶点和索引。
     *
     * @param primitiveType 目标 Filament 图元类型
     * @return 与目标图元拓扑匹配的 Mesh 数据
     */
    private PrimitiveGeometryData createPrimitive(RenderableManager.PrimitiveType primitiveType) {
        switch (primitiveType) {
            case POINTS:
                return createPoints();
            case LINES:
                return createLines();
            case LINE_STRIP:
                return createLineStrip();
            case TRIANGLES:
                return createTriangles();
            case TRIANGLE_STRIP:
                return createTriangleStrip();
            default:
                throw new IllegalArgumentException("Unsupported primitive type: " + primitiveType);
        }
    }

    /** @return 由独立点组成的波浪点阵 */
    private PrimitiveGeometryData createPoints() {
        PrimitiveGeometryData mesh = new PrimitiveGeometryData();
        final int rows = 5;
        final int columns = 9;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                float x = (column - (columns - 1) / 2.0f) * 0.27f;
                float y = (row - (rows - 1) / 2.0f) * 0.27f;
                float z = -3.0f + (float) Math.sin(column * 0.75f + row * 0.55f) * 0.2f;
                addVertex(mesh, x, y, z);
            }
        }
        //desc- Filament 的 POINTS 同样要求 IndexBuffer，因此每个顶点都写入一个对应索引。
        addSequentialIndices(mesh);
        return mesh;
    }

    /** @return 由互不相连的线段组成的网格 */
    private PrimitiveGeometryData createLines() {
        PrimitiveGeometryData geometry = new PrimitiveGeometryData();
        addLineSegment(geometry, -1.1f, -0.65f, 1.1f, -0.65f);
        addLineSegment(geometry, 1.1f, -0.65f, 1.1f, 0.65f);
        addLineSegment(geometry, 1.1f, 0.65f, -1.1f, 0.65f);
        addLineSegment(geometry, -1.1f, 0.65f, -1.1f, -0.65f);
        addLineSegment(geometry, -1.1f, -0.65f, 1.1f, 0.65f);
        addLineSegment(geometry, -1.1f, 0.65f, 1.1f, -0.65f);
        addSequentialIndices(geometry);
        return geometry;
    }

    /** @return 由单条连续折线组成的网格 */
    private PrimitiveGeometryData createLineStrip() {
        PrimitiveGeometryData mesh = new PrimitiveGeometryData();
        final int count = 11;
        for (int index = 0; index < count; index++) {
            float x = -1.25f + index * 0.25f;
            float y = (float) Math.sin(index * 0.9f) * 0.62f;
            addVertex(mesh, x, y, -3.0f);
        }
        addSequentialIndices(mesh);
        return mesh;
    }

    /**
     * 将 LINES 或 LINE_STRIP 输入拓扑展开为线材质需要的三角形。
     *
     * @param sourceMesh 原始线端点和索引
     * @param primitiveType 原始线图元类型
     * @return 包含 COLOR 与 CUSTOM0 顶点属性的三角形宽线
     */
    private PrimitiveGeometryData createWideLineMesh(
            PrimitiveGeometryData sourceMesh, RenderableManager.PrimitiveType primitiveType) {
        PrimitiveGeometryData result = new PrimitiveGeometryData();
        if (primitiveType == RenderableManager.PrimitiveType.LINES) {
            for (int index = 0; index + 1 < sourceMesh.indices.size(); index += 2) {
                addWideLineSegment(
                        result,
                        sourceMesh.vertices.get(sourceMesh.indices.get(index)).getPosition(),
                        sourceMesh.vertices.get(sourceMesh.indices.get(index + 1)).getPosition());
            }
        } else if (primitiveType == RenderableManager.PrimitiveType.LINE_STRIP) {
            for (int index = 0; index + 1 < sourceMesh.indices.size(); index++) {
                addWideLineSegment(
                        result,
                        sourceMesh.vertices.get(sourceMesh.indices.get(index)).getPosition(),
                        sourceMesh.vertices.get(sourceMesh.indices.get(index + 1)).getPosition());
            }
        } else {
            throw new IllegalArgumentException("Unsupported line primitive type: " + primitiveType);
        }
        return result;
    }

    /**
     * 生成一条由两个三角形组成的屏幕空间宽线段。
     * <p>
     * 每个端点复制为左右两个顶点，CUSTOM0.xyz 保存另一端点，CUSTOM0.w 保存偏移符号。
     * 终点观察方向与起点相反，因此终点的左右符号也需要反转。
     * </p>
     *
     * @param mesh 目标 Mesh
     * @param start 线段起点
     * @param end 线段终点
     */
    private void addWideLineSegment(PrimitiveGeometryData mesh, Vector3 start, Vector3 end) {
        int baseIndex = mesh.vertices.size();
        addWideLineVertex(mesh, start, end, 1.0f);
        addWideLineVertex(mesh, start, end, -1.0f);
        addWideLineVertex(mesh, end, start, -1.0f);
        addWideLineVertex(mesh, end, start, 1.0f);

        mesh.indices.add(baseIndex);
        mesh.indices.add(baseIndex + 1);
        mesh.indices.add(baseIndex + 2);
        mesh.indices.add(baseIndex + 2);
        mesh.indices.add(baseIndex + 1);
        mesh.indices.add(baseIndex + 3);
    }

    /** 添加线材质要求的单个顶点及 CUSTOM0 属性。 */
    private void addWideLineVertex(
            PrimitiveGeometryData mesh, Vector3 position, Vector3 otherEndpoint, float side) {
        mesh.vertices.add(Vertex.builder()
                .setPosition(position)
                .setColor(WHITE_VERTEX_COLOR)
                .setCustom0(new Vertex.Float4(
                        otherEndpoint.x, otherEndpoint.y, otherEndpoint.z, side))
                .build());
    }

    /** @return 由三个互不共享顶点的三角形组成的网格 */
    private PrimitiveGeometryData createTriangles() {
        PrimitiveGeometryData mesh = new PrimitiveGeometryData();
        addVertex(mesh, -1.15f, -0.6f, -3.0f);
        addVertex(mesh, -0.35f, -0.6f, -3.0f);
        addVertex(mesh, -0.75f, 0.55f, -3.0f);
        addVertex(mesh, -0.4f, -0.6f, -3.05f);
        addVertex(mesh, 0.4f, -0.6f, -3.05f);
        addVertex(mesh, 0.0f, 0.55f, -3.05f);
        addVertex(mesh, 0.35f, -0.6f, -3.1f);
        addVertex(mesh, 1.15f, -0.6f, -3.1f);
        addVertex(mesh, 0.75f, 0.55f, -3.1f);
        addSequentialIndices(mesh);
        return mesh;
    }

    /** @return 由交替上下顶点构成的连续三角带网格 */
    private PrimitiveGeometryData createTriangleStrip() {
        PrimitiveGeometryData mesh = new PrimitiveGeometryData();
        final int columns = 6;
        for (int column = 0; column < columns; column++) {
            float x = -1.2f + column * 0.48f;
            float z = -3.0f + (float) Math.sin(column * 0.8f) * 0.16f;
            //desc- 首列先上后下，使三角带第一个三角形朝向相机，后续绕序由 TRIANGLE_STRIP 自动交替。
            addVertex(mesh, x, 0.55f, z);
            addVertex(mesh, x, -0.55f, z);
        }
        addSequentialIndices(mesh);
        return mesh;
    }

    /**
     * 添加普通线段的两个端点。
     *
     * @param mesh 目标 Mesh
     * @param startX 起点 X
     * @param startY 起点 Y
     * @param endX 终点 X
     * @param endY 终点 Y
     */
    private void addLineSegment(
            PrimitiveGeometryData mesh, float startX, float startY, float endX, float endY) {
        addVertex(mesh, startX, startY, -3.0f);
        addVertex(mesh, endX, endY, -3.0f);
    }

    /**
     * 添加一个朝向相机的顶点。
     *
     * @param mesh 目标 Mesh
     * @param x 模型局部坐标 X
     * @param y 模型局部坐标 Y
     * @param z 模型局部坐标 Z
     */
    private void addVertex(PrimitiveGeometryData mesh, float x, float y, float z) {
        float u = Math.max(0.0f, Math.min(1.0f, (x + 1.4f) / 2.8f));
        float v = Math.max(0.0f, Math.min(1.0f, (y + 0.8f) / 1.6f));
        mesh.vertices.add(Vertex.builder()
                .setPosition(new Vector3(x, y, z))
                .setNormal(Vector3.back())
                //desc- 普通颜色材质声明了 UV0，即使仅使用纯色也必须提供该顶点属性。
                .setUvCoordinate(new Vertex.UvCoordinate(u, v))
                .build());
    }

    /**
     * 为 Mesh 中的全部顶点生成从 0 开始的一一对应索引。
     *
     * @param mesh 目标 Mesh
     */
    private void addSequentialIndices(PrimitiveGeometryData mesh) {
        for (int index = 0; index < mesh.vertices.size(); index++) {
            mesh.indices.add(index);
        }
    }

    /**
     * 返回当前图元的索引解释说明。
     *
     * @param primitiveType Filament 图元类型
     * @return 面板状态说明
     */
    private String getPrimitiveDescription(RenderableManager.PrimitiveType primitiveType) {
        switch (primitiveType) {
            case POINTS:
                return "POINTS：每个索引绘制一个独立点。";
            case LINES:
                return "LINES：每两个索引定义一条独立线段，并由专用材质展开为可调宽线。";
            case LINE_STRIP:
                return "LINE_STRIP：相邻索引首尾连接，并由专用材质展开为连续宽线。";
            case TRIANGLES:
                return "TRIANGLES：每三个索引绘制一个独立三角形。";
            case TRIANGLE_STRIP:
                return "TRIANGLE_STRIP：从第三个索引开始，每增加一个索引形成一个三角形。";
            default:
                return primitiveType.name();
        }
    }

    /** @return 当前类型是否为需要专用线材质的线图元 */
    private boolean isLinePrimitive(RenderableManager.PrimitiveType primitiveType) {
        return primitiveType == RenderableManager.PrimitiveType.LINES
                || primitiveType == RenderableManager.PrimitiveType.LINE_STRIP;
    }

    /** 解除旧图元节点及 Renderable，避免切换时残留渲染实例。 */
    private void clearPrimitiveNode() {
        if (primitiveNode == null) {
            return;
        }
        primitiveNode.setRenderable(null);
        primitiveNode.setParent(null);
        primitiveNode = null;
    }

    /** 销毁场景前取消异步挂载并解除渲染资源引用。 */
    @Override
    protected void onBeforeDestroyScene() {
        renderGeneration++;
        clearPrimitiveNode();
        pointMaterial = null;
        lineMaterial = null;
        coloredMaterial = null;
        statusView = null;
        pointSizeContainer = null;
        lineWidthContainer = null;
    }

    /** 基本图元构建所需的顶点与索引容器。 */
    private static final class PrimitiveGeometryData {
        final List<Vertex> vertices = new ArrayList<>();
        final List<Integer> indices = new ArrayList<>();
    }
}
