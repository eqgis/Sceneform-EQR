package com.eqgis.test.fragments.tutorial;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Renderable;

import java.util.ArrayList;
import java.util.List;

/**
 * 材质、光照与相机教程基类
 * <pre>
 *     统一管理教程节点的挂载和释放，并提供与现有教程一致的轻量操作控件。
 *     每个具体功能仍由独立 Fragment 和独立 SceneLayout 承载。
 * </pre>
 * @author tanyx
 */
public abstract class BaseMaterialCameraLessonFragment extends BaseTutorialFragment {
    private final List<Node> lessonNodes = new ArrayList<>();

    /**
     * 将渲染对象挂载到当前教程场景
     * @param renderable 三维渲染对象
     * @param position 世界坐标位置
     * @return 已挂载的节点；场景失效时返回未挂载节点
     */
    protected final Node addRenderableNode(Renderable renderable, Vector3 position) {
        Node node = new Node();
        node.setRenderable(renderable);
        node.setWorldPosition(position);
        addSceneNode(node);
        return node;
    }

    /**
     * 将普通节点或灯光节点挂载到当前教程场景
     * @param node 需要挂载的节点
     */
    protected final void addSceneNode(Node node) {
        if (!isSceneActive()) {
            return;
        }
        node.setParent(sceneLayout.getRootNode());
        lessonNodes.add(node);
    }

    /**
     * 在底部操作区添加带标题的下拉框
     * @param actionContainer 操作控件容器
     * @param labelText 下拉框标题
     * @param items 下拉选项
     * @param selection 默认选中位置
     * @param listener 选项监听器
     * @return 创建完成的下拉框
     */
    protected final Spinner addLabeledSpinner(LinearLayout actionContainer, String labelText,
                                               String[] items, int selection,
                                               AdapterView.OnItemSelectedListener listener) {
        TextView label = new TextView(requireContext());
        label.setText(labelText);
        label.setTextColor(0xff333333);
        label.setTextSize(14);
        label.setGravity(Gravity.CENTER_VERTICAL);
        actionContainer.addView(label, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        Spinner spinner = new Spinner(requireContext());
        configureTutorialSpinner(spinner);
        spinner.setAdapter(createTutorialSpinnerAdapter(items));
        spinner.setSelection(selection, false);
        spinner.setOnItemSelectedListener(listener);
        actionContainer.addView(spinner, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1));
        return spinner;
    }

    /**
     * 在 SceneLayout 销毁前释放灯光实例、渲染实例和节点关系
     */
    @Override
    protected void onBeforeDestroyScene() {
        for (Node node : lessonNodes) {
            node.setLight(null);
            node.setRenderable(null);
            node.setParent(null);
        }
        lessonNodes.clear();
    }
}
