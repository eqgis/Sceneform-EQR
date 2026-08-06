package com.eqgis.test.fragments.tutorial;

import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.Renderable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 进阶专题教程公共基类。
 * <pre>
 *     统一配置相机与环境光，提供面板控件和基础几何体创建方法，
 *     并确保节点在 SceneLayout 销毁前先解除监听、Renderable 与父节点引用。
 * </pre>
 * @author tanyx
 */
public abstract class BaseAdvancedLessonFragment extends BaseTutorialFragment {
    private static final String TAG = BaseAdvancedLessonFragment.class.getSimpleName();
    private final List<Node> managedNodes = new ArrayList<>();

    /**
     * 配置进阶教程通用场景。
     * @param sceneLayout 当前场景布局
     * @param showSkybox 是否显示天空盒
     */
    protected final void configureAdvancedScene(SceneLayout sceneLayout, boolean showSkybox) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 55);
        if (showSkybox) {
            sceneLayout.setSkybox("enviroments/pillars_2k_skybox.ktx");
        }
        sceneLayout.getCamera().setVerticalFovDegrees(62);
        sceneLayout.getCamera().setFarClipPlane(100);
    }

    /**
     * 将节点挂载到场景并纳入页面生命周期管理。
     * @param node 待挂载节点
     * @return 原节点
     */
    protected final <T extends Node> T addManagedNode(T node) {
        if (!isSceneActive()) {
            return node;
        }
        node.setParent(sceneLayout.getRootNode());
        managedNodes.add(node);
        return node;
    }

    /**
     * 创建并挂载一个可渲染节点。
     * @param renderable 渲染对象
     * @param position 节点局部坐标
     * @return 已创建节点
     */
    protected final Node addRenderableNode(Renderable renderable, Vector3 position) {
        Node node = new Node();
        node.setRenderable(renderable);
        node.setLocalPosition(position);
        return addManagedNode(node);
    }

    /**
     * 从场景和托管列表移除节点。
     * @param node 待移除节点
     */
    protected final void removeManagedNode(Node node) {
        if (node == null) {
            return;
        }
        node.setOnTapListener(null);
        node.setOnTouchListener(null);
        node.setRenderable(null);
        node.setParent(null);
        managedNodes.remove(node);
    }

    /** 清空当前页面托管的所有节点。 */
    protected final void clearManagedNodes() {
        for (int index = managedNodes.size() - 1; index >= 0; index--) {
            Node node = managedNodes.get(index);
            node.setOnTapListener(null);
            node.setOnTouchListener(null);
            node.setRenderable(null);
            node.setParent(null);
        }
        managedNodes.clear();
    }

    /** @return 当前托管节点数量 */
    protected final int getManagedNodeCount() {
        return managedNodes.size();
    }

    /**
     * 异步创建彩色 Cube。
     * @param position 节点位置
     * @param size 边长
     * @param color 颜色
     * @param consumer 创建成功回调
     */
    protected final void createCube(Vector3 position, float size, Color color, Consumer<Node> consumer) {
        MaterialFactory.makeOpaqueWithColor(requireContext(), color)
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    consumer.accept(addRenderableNode(
                            GeometryUtils.makeCube(Vector3.one().scaled(size), Vector3.zero(), material),
                            position));
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建进阶教程 Cube 失败", error);
                    return null;
                });
    }

    /**
     * 异步创建彩色球体。
     * @param position 节点位置
     * @param radius 半径
     * @param color 颜色
     * @param consumer 创建成功回调
     */
    protected final void createSphere(Vector3 position, float radius, Color color, Consumer<Node> consumer) {
        MaterialFactory.makeOpaqueWithColor(requireContext(), color)
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    consumer.accept(addRenderableNode(
                            GeometryUtils.makeSphere(radius, Vector3.zero(), material), position));
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建进阶教程 Sphere 失败", error);
                    return null;
                });
    }

    /**
     * 向悬浮面板添加说明或状态文本。
     * @param container 面板容器
     * @param text 初始文本
     * @return 文本控件
     */
    protected final TextView addPanelText(LinearLayout container, String text) {
        container.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(0xff333333);
        textView.setTextSize(13);
        textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        textView.setLineSpacing(dp(2), 1.0f);
        textView.setPadding(0, dp(3), 0, dp(7));
        container.addView(textView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return textView;
    }

    /**
     * 向悬浮面板添加按钮。
     * @param container 面板容器
     * @param text 按钮文案
     * @return 按钮
     */
    protected final Button addPanelButton(LinearLayout container, String text) {
        container.setOrientation(LinearLayout.VERTICAL);
        Button button = new Button(requireContext());
        button.setText(text);
        button.setAllCaps(false);
        container.addView(button, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return button;
    }

    /**
     * 添加带标题的 SeekBar。
     * @param container 面板容器
     * @param title 标题
     * @param max 最大进度
     * @param progress 初始进度
     * @return SeekBar
     */
    protected final SeekBar addPanelSeekBar(LinearLayout container, String title, int max, int progress) {
        addPanelText(container, title);
        SeekBar seekBar = new SeekBar(requireContext());
        seekBar.setMax(max);
        seekBar.setProgress(progress);
        container.addView(seekBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return seekBar;
    }

    /**
     * 添加教程风格下拉框。
     * @param container 面板容器
     * @param title 标题
     * @param items 选项
     * @return Spinner
     */
    protected final Spinner addPanelSpinner(LinearLayout container, String title, String[] items) {
        addPanelText(container, title);
        Spinner spinner = new Spinner(requireContext());
        configureTutorialSpinner(spinner);
        spinner.setAdapter(createTutorialSpinnerAdapter(items));
        container.addView(spinner, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return spinner;
    }

    /** 子类解除帧监听、异步任务或其它页面级资源的回调。 */
    protected void onReleaseAdvancedScene() {
    }

    /** SceneLayout 销毁前统一释放页面资源。 */
    @Override
    protected final void onBeforeDestroyScene() {
        onReleaseAdvancedScene();
        clearManagedNodes();
    }
}
