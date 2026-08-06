package com.eqgis.test.fragments.tutorial;

import android.animation.ValueAnimator;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.eqgis.eqr.geometry.Line3D;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.utils.MeshUtils;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.ModelRenderable;
import com.google.sceneform.rendering.RenderableDefinition;
import com.google.sceneform.rendering.Vertex;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 动态 Mesh 与 Line3D 进阶教程。
 * <p>根据参数重新生成顶点/索引网格，并原位刷新 Line3D 管线 Mesh。</p>
 * @author tanyx
 */
public class DynamicMeshLineLessonFragment extends BaseAdvancedLessonFragment {
    private Node waveNode;
    private Line3D line3D;
    private Material meshMaterial;
    private ModelRenderable waveRenderable;
    private RenderableDefinition waveDefinition;
    private TextView statusView;
    private ValueAnimator autoPlayAnimator;
    private float amplitude = 0.22f;
    private float lineRadius = 0.025f;
    private int edgeCount = 12;
    private float phase;
    private boolean autoPlaying = true;
    private boolean lifecyclePaused;

    @Override
    protected String getLessonTitle() {
        return "动态 Mesh 与 Line3D";
    }

    @Override
    protected String getLessonDescription() {
        return "运行时生成波浪面的 Vertex/triangleIndices，并使用 Line3D 将路径扩展为管线 Mesh。调节波幅、半径与截面边数后原位刷新几何数据。";
    }

    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureAdvancedScene(sceneLayout, true);
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(0.08f, 0.58f, 0.98f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    meshMaterial = material;
                    waveRenderable = buildWaveRenderable();
                    waveNode = addRenderableNode(
                            waveRenderable, new Vector3(0.0f, -0.45f, -3.6f));
                    createLine(material.makeCopy());
                    updateStatus();
                });
    }

    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        statusView = addPanelText(actionContainer, "正在创建动态网格…");
        bindFloatBar(addPanelSeekBar(actionContainer, "波幅：0.22 m", 46, 18), 0);
        bindFloatBar(addPanelSeekBar(actionContainer, "管线半径：0.025 m", 45, 20), 1);
        bindFloatBar(addPanelSeekBar(actionContainer, "截面边数：12", 21, 9), 2);

        Switch autoPlaySwitch = new Switch(requireContext());
        autoPlaySwitch.setText("自动播放");
        autoPlaySwitch.setTextColor(0xff333333);
        autoPlaySwitch.setTextSize(14);
        autoPlaySwitch.setGravity(Gravity.CENTER_VERTICAL);
        autoPlaySwitch.setChecked(autoPlaying);
        autoPlaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            autoPlaying = isChecked;
            updateAutoPlayState();
        });
        actionContainer.addView(autoPlaySwitch, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        addPanelButton(actionContainer, "推进波形并刷新").setOnClickListener(view -> {
            phase += 0.55f;
            refreshGeometry();
        });
        addPanelText(actionContainer,
                "动态几何要点：复用 Node 和材质；顶点数量不变时优先更新 Buffer；频繁重建 Renderable 会增加分配与回收压力。Line3D.refresh() 会在首次构建后复用 Mesh。"
        );
        updateAutoPlayState();
    }

    private void bindFloatBar(SeekBar seekBar, int type) {
        LinearLayout parent = (LinearLayout) seekBar.getParent();
        TextView label = (TextView) parent.getChildAt(parent.indexOfChild(seekBar) - 1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (type == 0) {
                    amplitude = 0.04f + progress * 0.01f;
                    label.setText(String.format(java.util.Locale.US, "波幅：%.2f m", amplitude));
                } else if (type == 1) {
                    lineRadius = 0.005f + progress * 0.001f;
                    label.setText(String.format(java.util.Locale.US, "管线半径：%.3f m", lineRadius));
                } else {
                    edgeCount = progress + 3;
                    label.setText("截面边数：" + edgeCount);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                refreshGeometry();
            }
        });
    }

    private void createLine(Material material) {
        line3D = new Line3D()
                .setMaterial(material)
                .setRadius(lineRadius)
                .setEdgeNum(edgeCount)
                .setTextureMode(0);
        addManagedNode(line3D);
        refreshLine();
    }

    private void refreshGeometry() {
        refreshAnimatedGeometry();
        updateStatus();
    }

    /** 复用已有 RenderableDefinition，刷新动画帧的波浪面与 Line3D。 */
    private void refreshAnimatedGeometry() {
        if (!isSceneActive() || waveRenderable == null || waveDefinition == null) {
            return;
        }
        waveDefinition.setVertices(buildWaveVertices());
        waveRenderable.updateFromDefinition(waveDefinition);
        refreshLine();
    }

    private void refreshLine() {
        if (line3D == null) {
            return;
        }
        ArrayList<Vector3> points = new ArrayList<>();
        for (int index = 0; index <= 20; index++) {
            float percent = index / 20f;
            float x = -1.35f + percent * 2.7f;
            float y = 0.58f + (float) Math.sin(percent * Math.PI * 3f + phase) * amplitude;
            points.add(new Vector3(x, y, -3.25f));
        }
        line3D.setRadius(lineRadius).setEdgeNum(edgeCount).setPointList(points);
        line3D.refresh();
    }

    private ModelRenderable buildWaveRenderable() {
        ArrayList<Vertex> vertices = buildWaveVertices();
        ArrayList<Integer> indices = buildWaveIndices();
        RenderableDefinition.Submesh submesh = RenderableDefinition.Submesh.builder()
                .setTriangleIndices(indices)
                .setMaterial(meshMaterial)
                .build();
        waveDefinition = RenderableDefinition.builder()
                .setVertices(vertices)
                .setSubmeshes(Arrays.asList(submesh))
                .build();
        ModelRenderable renderable = MeshUtils.makeRenderableByCustomMesh(
                meshMaterial, vertices, indices);
        renderable.setShadowCaster(false);
        return renderable;
    }

    /** @return 当前相位对应的波浪面顶点 */
    private ArrayList<Vertex> buildWaveVertices() {
        final int columns = 14;
        final int rows = 7;
        ArrayList<Vertex> vertices = new ArrayList<>((columns + 1) * (rows + 1));
        for (int row = 0; row <= rows; row++) {
            for (int column = 0; column <= columns; column++) {
                float u = column / (float) columns;
                float v = row / (float) rows;
                float x = (u - 0.5f) * 2.8f;
                float y = (v - 0.5f) * 1.25f;
                float z = (float) Math.sin(u * Math.PI * 4f + phase)
                        * (float) Math.cos(v * Math.PI * 2f) * amplitude;
                vertices.add(Vertex.builder()
                        .setPosition(new Vector3(x, y, z))
                        .setNormal(Vector3.back())
                        .setUvCoordinate(new Vertex.UvCoordinate(u, v))
                        .build());
            }
        }
        return vertices;
    }

    /** @return 波浪面固定拓扑的双面三角形索引 */
    private ArrayList<Integer> buildWaveIndices() {
        final int columns = 14;
        final int rows = 7;
        ArrayList<Integer> indices = new ArrayList<>(columns * rows * 12);
        int stride = columns + 1;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int a = row * stride + column;
                int b = a + 1;
                int c = a + stride;
                int d = c + 1;
                indices.add(a); indices.add(d); indices.add(b);
                indices.add(a); indices.add(c); indices.add(d);
                //desc- 教程网格采用双面索引，便于相机移动到背面观察三角形绕序。
                indices.add(b); indices.add(d); indices.add(a);
                indices.add(d); indices.add(c); indices.add(a);
            }
        }
        return indices;
    }

    /** 创建并启动循环属性动画，连续推进动态几何相位。 */
    private void ensureAutoPlayAnimator() {
        if (autoPlayAnimator != null) {
            return;
        }
        autoPlayAnimator = ValueAnimator.ofFloat(0.0f, (float) (Math.PI * 2.0));
        autoPlayAnimator.setDuration(3600L);
        autoPlayAnimator.setInterpolator(new LinearInterpolator());
        autoPlayAnimator.setRepeatCount(ValueAnimator.INFINITE);
        autoPlayAnimator.setRepeatMode(ValueAnimator.RESTART);
        autoPlayAnimator.addUpdateListener(animator -> {
            phase = (float) animator.getAnimatedValue();
            refreshAnimatedGeometry();
        });
    }

    /** 根据面板开关同步自动播放状态。 */
    private void updateAutoPlayState() {
        ensureAutoPlayAnimator();
        if (autoPlaying) {
            if (autoPlayAnimator.isPaused()) {
                autoPlayAnimator.resume();
            } else if (!autoPlayAnimator.isStarted()) {
                autoPlayAnimator.start();
            }
        } else if (autoPlayAnimator.isStarted() && !autoPlayAnimator.isPaused()) {
            autoPlayAnimator.pause();
        }
    }

    private void updateStatus() {
        if (statusView != null && meshMaterial != null) {
            statusView.setText(String.format(java.util.Locale.US,
                    "波浪面：120 顶点 / 196 面（双面索引）\nLine3D：21 路径点 / %d 边截面 / 半径 %.3f m",
                    edgeCount, lineRadius));
        }
    }

    /** 页面不可见时暂停自动播放。 */
    @Override
    public void onPause() {
        lifecyclePaused = autoPlayAnimator != null
                && autoPlayAnimator.isStarted()
                && !autoPlayAnimator.isPaused();
        if (lifecyclePaused) {
            autoPlayAnimator.pause();
        }
        super.onPause();
    }

    /** 页面恢复时仅恢复由生命周期暂停的自动播放。 */
    @Override
    public void onResume() {
        super.onResume();
        if (lifecyclePaused && autoPlaying && autoPlayAnimator != null
                && autoPlayAnimator.isPaused()) {
            autoPlayAnimator.resume();
        }
        lifecyclePaused = false;
    }

    @Override
    protected void onReleaseAdvancedScene() {
        if (autoPlayAnimator != null) {
            autoPlayAnimator.cancel();
            autoPlayAnimator.removeAllUpdateListeners();
        }
        autoPlayAnimator = null;
        lifecyclePaused = false;
        if (line3D != null) {
            line3D.dispose();
        }
        line3D = null;
        waveNode = null;
        waveRenderable = null;
        waveDefinition = null;
        meshMaterial = null;
        statusView = null;
    }
}
