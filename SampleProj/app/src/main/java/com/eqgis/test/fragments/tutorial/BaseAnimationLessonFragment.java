package com.eqgis.test.fragments.tutorial;

import android.animation.ObjectAnimator;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.utils.ScaleTool;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.ModelRenderable;
import com.google.sceneform.rendering.Renderable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 动画篇教程公共基类
 * <pre>
 *     统一管理动画对象、演示节点、异步模型加载和悬浮参数面板行布局。
 *     页面暂停时仅暂停正在播放的动画，恢复时不会误启动用户手动暂停的动画。
 * </pre>
 * @author tanyx
 */
public abstract class BaseAnimationLessonFragment extends BaseTutorialFragment {
    private static final String TAG = BaseAnimationLessonFragment.class.getSimpleName();
    private final List<Node> lessonNodes = new ArrayList<>();
    private final List<ObjectAnimator> lessonAnimators = new ArrayList<>();
    private final Set<ObjectAnimator> lifecyclePausedAnimators = new HashSet<>();

    /**
     * 将渲染对象挂载到教程场景
     * @param renderable 三维渲染对象
     * @param position 节点局部坐标
     * @return 已创建的场景节点
     */
    protected final Node addRenderableNode(Renderable renderable, Vector3 position) {
        Node node = new Node();
        node.setRenderable(renderable);
        node.setLocalPosition(position);
        addSceneNode(node);
        return node;
    }

    /**
     * 将节点挂载到教程场景并纳入生命周期管理
     * @param node 场景节点
     */
    protected final void addSceneNode(Node node) {
        if (!isSceneActive()) {
            return;
        }
        node.setParent(sceneLayout.getRootNode());
        lessonNodes.add(node);
    }

    /**
     * 注册属性动画，由当前 Fragment 统一暂停、恢复和销毁
     * @param animator Android 属性动画
     * @return 原动画对象
     */
    protected final ObjectAnimator trackAnimator(ObjectAnimator animator) {
        if (animator != null && !lessonAnimators.contains(animator)) {
            lessonAnimators.add(animator);
        }
        return animator;
    }

    /**
     * 取消并解除旧属性动画的生命周期管理
     * @param animator 需要移除的动画
     */
    protected final void untrackAnimator(ObjectAnimator animator) {
        if (animator == null) {
            return;
        }
        animator.cancel();
        lessonAnimators.remove(animator);
        lifecyclePausedAnimators.remove(animator);
    }

    /**
     * 在悬浮面板中添加一行参数控件
     * @param container 悬浮面板容器
     * @param labelText 参数标题
     * @param control SeekBar、Spinner 或 Switch 等控件
     * @return 用于实时更新参数值的标题 TextView
     */
    protected final TextView addControlRow(LinearLayout container, String labelText, View control) {
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(44));

        TextView label = new TextView(requireContext());
        label.setText(labelText);
        label.setTextColor(0xff333333);
        label.setTextSize(14);
        label.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(label, new LinearLayout.LayoutParams(
                dp(118),
                ViewGroup.LayoutParams.MATCH_PARENT));
        row.addView(control, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1));
        container.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return label;
    }

    /**
     * 异步创建彩色 Cube
     * @param position 节点局部坐标
     * @param size Cube 边长
     * @param color 材质颜色
     * @param consumer 节点创建成功回调
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
                    Log.e(TAG, "创建动画 Cube 失败", error);
                    return null;
                });
    }

    /**
     * 异步创建彩色球体
     * @param position 节点局部坐标
     * @param radius 球体半径
     * @param color 材质颜色
     * @param consumer 节点创建成功回调
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
                    Log.e(TAG, "创建动画球体失败", error);
                    return null;
                });
    }

    /**
     * 异步加载带内置动画的模型
     * @param position 节点局部坐标
     * @param scale 单位尺寸缩放后的附加比例
     * @param consumer 节点创建成功回调
     */
    protected final void loadAnimatedGlb(Vector3 position, float scale, Consumer<Node> consumer) {
        ModelRenderable.builder()
                .setSource(requireContext(), Uri.parse("gltf/Fox.glb"))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(renderable -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    Node node = addRenderableNode(renderable, position);
                    node.setLocalScale(Vector3.one().scaled(
                            ScaleTool.calculateUnitsScale(renderable) * scale));
                    consumer.accept(node);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "加载动画模型失败", error);
                    return null;
                });
    }

    /**
     * 页面不可见时暂停当前正在播放的属性动画
     */
    @Override
    public void onPause() {
        lifecyclePausedAnimators.clear();
        for (ObjectAnimator animator : lessonAnimators) {
            if (animator.isStarted() && !animator.isPaused()) {
                animator.pause();
                lifecyclePausedAnimators.add(animator);
            }
        }
        super.onPause();
    }

    /**
     * 页面恢复时仅恢复由生命周期暂停的属性动画
     */
    @Override
    public void onResume() {
        super.onResume();
        for (ObjectAnimator animator : lifecyclePausedAnimators) {
            if (animator.isPaused()) {
                animator.resume();
            }
        }
        lifecyclePausedAnimators.clear();
    }

    /**
     * SceneLayout 销毁前取消动画并解除节点资源
     */
    @Override
    protected void onBeforeDestroyScene() {
        for (ObjectAnimator animator : lessonAnimators) {
            animator.cancel();
            animator.setTarget(null);
        }
        lessonAnimators.clear();
        lifecyclePausedAnimators.clear();
        for (Node node : lessonNodes) {
            node.setRenderable(null);
            node.setParent(null);
        }
        lessonNodes.clear();
    }
}
