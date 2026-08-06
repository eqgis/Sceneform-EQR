package com.eqgis.test.fragments.tutorial;

import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
 * 交互篇教程公共基类
 * <pre>
 *     统一配置场景环境、创建可交互几何体、管理悬浮面板控件，
 *     并在 SceneLayout 销毁前解除节点点击监听和渲染资源引用。
 * </pre>
 * @author tanyx
 */
public abstract class BaseInteractionLessonFragment extends BaseTutorialFragment {
    private static final String TAG = BaseInteractionLessonFragment.class.getSimpleName();
    private final List<Node> lessonNodes = new ArrayList<>();

    /**
     * 配置交互教程的相机、环境光与可选天空盒
     * @param sceneLayout 当前场景布局
     * @param showSkybox true 表示显示天空盒
     */
    protected final void configureInteractionScene(SceneLayout sceneLayout, boolean showSkybox) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 50);
        if (showSkybox) {
            sceneLayout.setSkybox("enviroments/pillars_2k_skybox.ktx");
        }
        sceneLayout.getCamera().setVerticalFovDegrees(62);
        sceneLayout.getCamera().setFarClipPlane(100);
    }

    /**
     * 将渲染对象挂载到场景并纳入生命周期管理
     * @param renderable 渲染对象
     * @param position 节点局部坐标
     * @return 已创建节点
     */
    protected final Node addRenderableNode(Renderable renderable, Vector3 position) {
        Node node = new Node();
        node.setRenderable(renderable);
        node.setLocalPosition(position);
        addSceneNode(node);
        return node;
    }

    /**
     * 将已有节点挂载到当前教程场景
     * @param node 待挂载节点
     */
    protected final void addSceneNode(Node node) {
        if (!isSceneActive()) {
            return;
        }
        node.setParent(sceneLayout.getRootNode());
        lessonNodes.add(node);
    }

    /**
     * 异步创建彩色 Cube
     * @param position 节点位置
     * @param size Cube 边长
     * @param color 材质颜色
     * @param consumer 创建完成回调
     */
    protected final void createCube(Vector3 position, float size, Color color, Consumer<Node> consumer) {
        MaterialFactory.makeOpaqueWithColor(requireContext(), color)
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    Node node = addRenderableNode(
                            GeometryUtils.makeCube(Vector3.one().scaled(size), Vector3.zero(), material),
                            position);
                    consumer.accept(node);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建交互 Cube 失败", error);
                    return null;
                });
    }

    /**
     * 异步创建彩色球体
     * @param position 节点位置
     * @param radius 球体半径
     * @param color 材质颜色
     * @param consumer 创建完成回调
     */
    protected final void createSphere(Vector3 position, float radius, Color color, Consumer<Node> consumer) {
        MaterialFactory.makeOpaqueWithColor(requireContext(), color)
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    Node node = addRenderableNode(
                            GeometryUtils.makeSphere(radius, Vector3.zero(), material),
                            position);
                    consumer.accept(node);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建交互球体失败", error);
                    return null;
                });
    }

    /**
     * 在悬浮面板添加说明或状态文本
     * @param container 面板容器
     * @param text 初始文本
     * @return 状态 TextView
     */
    protected final TextView addPanelText(LinearLayout container, String text) {
        container.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(0xff333333);
        textView.setTextSize(14);
        textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        textView.setLineSpacing(dp(2), 1.0f);
        textView.setPadding(0, dp(4), 0, dp(8));
        container.addView(textView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return textView;
    }

    /**
     * 在悬浮面板添加操作按钮
     * @param container 面板容器
     * @param text 按钮文案
     * @return 已添加按钮
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
     * 子类解除额外触摸监听和控制器状态的回调
     */
    protected void onReleaseInteraction() {
    }

    /**
     * SceneLayout 销毁前统一解除交互监听和节点资源
     */
    @Override
    protected final void onBeforeDestroyScene() {
        onReleaseInteraction();
        for (Node node : lessonNodes) {
            node.setOnTapListener(null);
            node.setOnTouchListener(null);
            node.setRenderable(null);
            node.setParent(null);
        }
        lessonNodes.clear();
    }
}
