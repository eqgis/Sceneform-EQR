package com.eqgis.test.fragments.tutorial;

import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.HitTestResult;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.MaterialFactory;

import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 三维节点射线拾取教程
 * <pre>
 *     绘制多个具备碰撞形状的 Cube，Scene 通过屏幕触点生成射线，
 *     Node.OnTapListener 接收最近命中节点并切换该节点的材质颜色。
 * </pre>
 * @author tanyx
 */
public class NodePickingLessonFragment extends BaseInteractionLessonFragment {
    private static final Color SELECTED_COLOR = new Color(1.0f, 0.86f, 0.12f);
    private final Map<Node, Color> baseColors = new IdentityHashMap<>();
    private Node selectedNode;
    private TextView statusText;

    @Override
    protected String getLessonTitle() {
        return "3D Node 交互";
    }

    @Override
    protected String getLessonDescription() {
        return "点击多个 Cube 中的任意一个，Ray hit-test 会返回最近命中的 Node，并把该 Cube 切换为指定颜色。";
    }

    /**
     * 创建用于 Ray hit-test 的多个 Cube
     * @param sceneLayout 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureInteractionScene(sceneLayout, false);
        createPickableCube("Cube 1", new Vector3(-1.15f, 0.58f, -3.2f),
                new Color(0.08f, 0.52f, 1.0f));
        createPickableCube("Cube 2", new Vector3(0, 0.58f, -3.65f),
                new Color(0.24f, 0.82f, 0.48f));
        createPickableCube("Cube 3", new Vector3(1.15f, 0.58f, -4.1f),
                new Color(1.0f, 0.46f, 0.08f));
        createPickableCube("Cube 4", new Vector3(-0.62f, -0.62f, -3.15f),
                new Color(0.72f, 0.28f, 0.96f));
        createPickableCube("Cube 5", new Vector3(0.72f, -0.62f, -3.55f),
                new Color(0.96f, 0.24f, 0.42f));
    }

    private void createPickableCube(String name, Vector3 position, Color color) {
        createCube(position, 0.68f, color, node -> {
            node.setName(name);
            baseColors.put(node, color);
            node.setOnTapListener(new Node.OnTapListener() {
                @Override
                public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                    selectNode(node, hitTestResult);
                }
            });
        });
    }

    private void selectNode(Node node, HitTestResult hitTestResult) {
        if (selectedNode != null && selectedNode != node) {
            setNodeColor(selectedNode, baseColors.get(selectedNode));
        }
        selectedNode = node;
        setNodeColor(node, SELECTED_COLOR);
        Vector3 point = hitTestResult.getPoint();
        if (statusText != null) {
            statusText.setText(String.format(
                    Locale.US,
                    "已选中：%s\n命中点：%.2f, %.2f, %.2f",
                    node.getName(),
                    point.x,
                    point.y,
                    point.z));
        }
    }

    private void setNodeColor(Node node, Color color) {
        if (node == null || color == null || node.getRenderableInstance() == null) {
            return;
        }
        Material material = node.getRenderableInstance().getMaterial();
        if (material != null) {
            material.setFloat4(MaterialFactory.MATERIAL_COLOR, color);
        }
    }

    /**
     * 创建拾取状态面板
     * @param actionContainer 悬浮面板容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        statusText = addPanelText(actionContainer,
                "点击任意 Cube\n最近命中的 Node 将变为黄色");
    }

    @Override
    protected void onReleaseInteraction() {
        selectedNode = null;
        baseColors.clear();
        statusText = null;
    }
}
