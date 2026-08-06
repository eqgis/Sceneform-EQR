package com.eqgis.test.fragments.tutorial;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.Node;
import com.google.sceneform.Scene;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.ModelRenderable;

import java.util.ArrayList;
import java.util.List;

/**
 * 渲染性能诊断进阶教程。
 * <p>通过动态节点数量、阴影开关、实时 FPS 与 SceneView 内部耗时日志观察渲染负载。</p>
 * @author tanyx
 */
public class RenderPerformanceLessonFragment extends BaseAdvancedLessonFragment {
    private static final int MAX_BASE_COUNT = 100;
    private static final int[] COUNT_MULTIPLIERS = {1, 10, 100};
    private static final int NODE_BATCH_SIZE = 200;
    private static final float GRID_SPAN = 3.4f;

    private final List<Node> stressNodes = new ArrayList<>();
    private final Runnable objectCountTask = this::applyObjectCountBatch;
    private TextView statusView;
    private Scene.OnUpdateListener frameListener;
    private ModelRenderable sharedRenderable;
    private int baseCount = MAX_BASE_COUNT;
    private int countMultiplier = COUNT_MULTIPLIERS[0];
    private int desiredCount = baseCount * countMultiplier;
    private int frameCount;
    private long sampleStartNanos;
    private float currentFps;
    private boolean shadowsEnabled = true;
    private boolean debugEnabled;
    private boolean objectCountTaskPosted;

    @Override
    protected String getLessonTitle() {
        return "渲染性能诊断";
    }

    @Override
    protected String getLessonDescription() {
        return "实时统计 FPS，并调节共享 Renderable 的实例数量与阴影开销。可开启 SceneView 内部耗时日志，在 Logcat 观察 render、update 和 total 的移动平均值。";
    }

    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureAdvancedScene(sceneLayout, true);
        sampleStartNanos = System.nanoTime();
        frameListener = frameTime -> sampleFrame();
        sceneLayout.addSceneUpdateListener(frameListener);

        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(0.12f, 0.55f, 0.96f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    sharedRenderable = GeometryUtils.makeCube(
                            Vector3.one().scaled(0.25f), Vector3.zero(), material);
                    applyObjectCount(desiredCount);
                });
    }

    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        statusView = addPanelText(actionContainer, "正在采样渲染帧…");
        SeekBar countBar = addPanelSeekBar(
                actionContainer,
                createCountLabelText(),
                MAX_BASE_COUNT - 1,
                baseCount - 1);
        TextView countLabel = (TextView) actionContainer.getChildAt(actionContainer.getChildCount() - 2);
        countBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                baseCount = progress + 1;
                applySelectedObjectCount(countLabel);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        Spinner multiplierSpinner = addPanelSpinner(
                actionContainer,
                "数量级",
                new String[]{"x1", "x10", "x100"});
        multiplierSpinner.setSelection(0, false);
        multiplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                countMultiplier = COUNT_MULTIPLIERS[position];
                applySelectedObjectCount(countLabel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button shadowButton = addPanelButton(actionContainer, "关闭实时阴影");
        shadowButton.setOnClickListener(view -> {
            shadowsEnabled = !shadowsEnabled;
            if (sharedRenderable != null) {
                sharedRenderable.setShadowCaster(shadowsEnabled);
                sharedRenderable.setShadowReceiver(shadowsEnabled);
            }
            shadowButton.setText(shadowsEnabled ? "关闭实时阴影" : "开启实时阴影");
            updateStatus();
        });

        Button debugButton = addPanelButton(actionContainer, "开启内部耗时日志");
        debugButton.setOnClickListener(view -> {
            debugEnabled = !debugEnabled;
            if (isSceneActive()) {
                sceneLayout.getSceneView().enableDebug(debugEnabled);
            }
            debugButton.setText(debugEnabled ? "关闭内部耗时日志" : "开启内部耗时日志");
        });
        addPanelText(actionContainer,
                "诊断建议：先固定相机和场景，再分别对比节点数、材质数、透明混合、阴影、后处理和纹理尺寸。FPS 只说明结果，耗时分段才有助于定位瓶颈。"
        );
    }

    /**
     * 应用 SeekBar 基数与下拉框数量级的乘积。
     *
     * @param countLabel 显示基数、倍率和最终实例数的面板文本
     */
    private void applySelectedObjectCount(TextView countLabel) {
        desiredCount = baseCount * countMultiplier;
        countLabel.setText(createCountLabelText());
        applyObjectCount(desiredCount);
    }

    /** @return 当前实例数量控制文案 */
    private String createCountLabelText() {
        return "实例数量：" + baseCount + " × " + countMultiplier + " = " + desiredCount;
    }

    private void applyObjectCount(int count) {
        if (sharedRenderable == null || !isSceneActive()) {
            return;
        }
        desiredCount = count;
        scheduleObjectCountTask();
    }

    /** 按帧分批增删实例，避免切换到高数量级时长时间阻塞主线程。 */
    private void applyObjectCountBatch() {
        objectCountTaskPosted = false;
        if (sharedRenderable == null || !isSceneActive()) {
            return;
        }

        int operationCount = 0;
        while (stressNodes.size() > desiredCount && operationCount < NODE_BATCH_SIZE) {
            Node node = stressNodes.remove(stressNodes.size() - 1);
            removeManagedNode(node);
            operationCount++;
        }
        while (stressNodes.size() < desiredCount && operationCount < NODE_BATCH_SIZE) {
            Node node = addRenderableNode(sharedRenderable,
                    new Vector3(0.0f, 0.0f, -4.2f));
            stressNodes.add(node);
            operationCount++;
        }

        if (stressNodes.size() == desiredCount) {
            layoutStressNodes();
        } else {
            scheduleObjectCountTask();
        }
        updateStatus();
    }

    /** 将实例数量调整任务安排到下一帧。 */
    private void scheduleObjectCountTask() {
        if (objectCountTaskPosted || sceneLayout == null) {
            return;
        }
        objectCountTaskPosted = true;
        sceneLayout.postOnAnimation(objectCountTask);
    }

    /**
     * 将当前实例排布为居中的自适应网格。
     * <p>最大 10000 个实例对应 100×100 网格，并缩放节点以保持在相机可见范围内。</p>
     */
    private void layoutStressNodes() {
        int count = stressNodes.size();
        if (count == 0) {
            return;
        }
        int columns = (int) Math.ceil(Math.sqrt(count));
        int rows = (int) Math.ceil((double) count / columns);
        float spacing = Math.min(0.34f, GRID_SPAN / Math.max(columns - 1, 1));
        float nodeScale = Math.min(1.0f, spacing / 0.3f);
        float centerColumn = (columns - 1) * 0.5f;
        float centerRow = (rows - 1) * 0.5f;

        //desc- 数量级变化后重新排布全部实例，避免新增节点超出相机视锥而无法形成有效压力。
        for (int index = 0; index < count; index++) {
            int column = index % columns;
            int row = index / columns;
            Node node = stressNodes.get(index);
            node.setLocalPosition(new Vector3(
                    (column - centerColumn) * spacing,
                    (row - centerRow) * spacing,
                    -4.2f));
            node.setLocalScale(Vector3.one().scaled(nodeScale));
        }
    }

    private void sampleFrame() {
        frameCount++;
        long now = System.nanoTime();
        long elapsed = now - sampleStartNanos;
        if (elapsed < 750_000_000L) {
            return;
        }
        currentFps = frameCount * 1_000_000_000f / elapsed;
        frameCount = 0;
        sampleStartNanos = now;
        updateStatus();
    }

    private void updateStatus() {
        if (statusView == null) {
            return;
        }
        Runtime runtime = Runtime.getRuntime();
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
        statusView.setText(String.format(java.util.Locale.US,
                "FPS：%.1f\n实例：%d / %d（共享 1 个 Renderable）\nJava Heap：%d MB\n阴影：%s",
                currentFps, stressNodes.size(), desiredCount, usedMb,
                shadowsEnabled ? "开启" : "关闭"));
    }

    @Override
    protected void onReleaseAdvancedScene() {
        if (sceneLayout != null && frameListener != null) {
            sceneLayout.removeSceneUpdateListener(frameListener);
            sceneLayout.getSceneView().enableDebug(false);
        }
        if (sceneLayout != null) {
            sceneLayout.removeCallbacks(objectCountTask);
        }
        objectCountTaskPosted = false;
        frameListener = null;
        stressNodes.clear();
        sharedRenderable = null;
        statusView = null;
    }
}
