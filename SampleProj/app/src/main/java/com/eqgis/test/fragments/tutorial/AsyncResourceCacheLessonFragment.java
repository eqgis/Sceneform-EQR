package com.eqgis.test.fragments.tutorial;

import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.utils.ScaleTool;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.ModelRenderable;

/**
 * 异步加载与资源缓存进阶教程。
 * <p>对比 GLB 的异步/同步加载与 Renderable 注册表缓存，并防止销毁后的异步回调挂载资源。</p>
 * @author tanyx
 */
public class AsyncResourceCacheLessonFragment extends BaseAdvancedLessonFragment {
    private static final String TAG = AsyncResourceCacheLessonFragment.class.getSimpleName();
    private TextView statusView;
    private int mode;
    private int loadCount;
    private int requestGeneration;

    @Override
    protected String getLessonTitle() {
        return "异步加载与资源缓存";
    }

    @Override
    protected String getLessonDescription() {
        return "使用 ModelRenderable.Builder 比较异步加载、同步加载和 registryId 缓存。快速切换页面时通过 generation 与 isSceneActive() 丢弃晚到回调。";
    }

    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureAdvancedScene(sceneLayout, true);
    }

    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        statusView = addPanelText(actionContainer, "选择模式后加载 Fox.glb。首次加载与缓存命中的耗时会不同。"
        );
        String[] modes = {"异步 + 缓存", "异步 + 不缓存", "同步 + 缓存"};
        Spinner spinner = addPanelSpinner(actionContainer, "加载模式", modes);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mode = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        addPanelButton(actionContainer, "加载一个模型").setOnClickListener(view -> loadModel());
        addPanelButton(actionContainer, "清空场景并取消旧回调").setOnClickListener(view -> {
            requestGeneration++;
            clearManagedNodes();
            loadCount = 0;
            statusView.setText("已清空。本页不会主动清理全局缓存；缓存资源由最后一个 SceneView 的资源回收阶段处理。"
            );
        });
        addPanelText(actionContainer,
                "缓存开启时，Uri 作为 registryId，后续 Builder 可复用已加载 Renderable 的副本。关闭缓存适合一次性或需要隔离生命周期的资源。"
        );
    }

    private void loadModel() {
        if (!isSceneActive()) {
            return;
        }
        final int generation = requestGeneration;
        final boolean async = mode != 2;
        final boolean cache = mode != 1;
        final long startTime = SystemClock.elapsedRealtime();
        statusView.setText("正在加载：" + (async ? "异步" : "同步") + " / " + (cache ? "缓存开启" : "缓存关闭"));

        ModelRenderable.builder()
                .setSource(requireContext(), Uri.parse("gltf/Fox.glb"), cache)
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(async)
                .build()
                .thenAccept(renderable -> {
                    if (!isSceneActive() || generation != requestGeneration) {
                        return;
                    }
                    int index = loadCount++;
                    float x = ((index % 3) - 1) * 1.05f;
                    float y = (index / 3) * 0.85f - 0.35f;
                    Node node = addRenderableNode(renderable, new Vector3(x, y, -3.8f));
                    node.setLocalScale(Vector3.one().scaled(ScaleTool.calculateUnitsScale(renderable) * 0.9f));
                    long elapsed = SystemClock.elapsedRealtime() - startTime;
                    statusView.setText("第 " + loadCount + " 个模型完成：" + elapsed + " ms\n"
                            + (async ? "异步加载" : "同步加载") + "，" + (cache ? "缓存开启" : "缓存关闭"));
                })
                .exceptionally(error -> {
                    Log.e(TAG, "加载进阶教程模型失败", error);
                    if (isSceneActive() && generation == requestGeneration && statusView != null) {
                        statusView.setText("加载失败：" + error.getMessage());
                    }
                    return null;
                });
    }

    @Override
    protected void onReleaseAdvancedScene() {
        requestGeneration++;
        statusView = null;
    }
}
