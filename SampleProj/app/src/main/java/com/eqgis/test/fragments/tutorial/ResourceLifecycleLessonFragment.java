package com.eqgis.test.fragments.tutorial;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * 生命周期与资源释放进阶教程。
 * <p>演示节点的创建、主动移除，以及 Fragment 到 SceneLayout 的安全销毁顺序。</p>
 * @author tanyx
 */
public class ResourceLifecycleLessonFragment extends BaseAdvancedLessonFragment {
    private final List<Node> demoNodes = new ArrayList<>();
    private TextView statusView;
    private int createdCount;
    private int removedCount;

    @Override
    protected String getLessonTitle() {
        return "生命周期与资源释放";
    }

    @Override
    protected String getLessonDescription() {
        return "观察 Node、Renderable、SceneLayout 与 Filament 资源的所有权。页面销毁时先停止回调并解绑节点，最后由 SceneLayout/SceneView 在安全帧释放全局渲染资源。";
    }

    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureAdvancedScene(sceneLayout, true);
        addDemoCube(new Vector3(-0.75f, 0.0f, -3.2f));
        addDemoCube(new Vector3(0.0f, 0.0f, -3.2f));
        addDemoCube(new Vector3(0.75f, 0.0f, -3.2f));
    }

    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        statusView = addPanelText(actionContainer, "正在创建演示节点…");
        addPanelText(actionContainer,
                "推荐顺序：\n1. 停止动画/帧监听/异步回调\n2. Node.setRenderable(null)\n3. Node.setParent(null)\n4. SceneLayout.destroy()\n\n不要在单个 Fragment 中主动调用 Renderer.destroyAllResources()，它属于最后一个 SceneView 的全局清理。"
        );
        addPanelButton(actionContainer, "添加一个节点").setOnClickListener(view -> {
            int index = demoNodes.size();
            float x = ((index % 5) - 2) * 0.55f;
            float y = (index / 5) * 0.55f;
            addDemoCube(new Vector3(x, y, -3.2f));
        });
        addPanelButton(actionContainer, "移除最后一个节点").setOnClickListener(view -> removeLastDemoNode());
        addPanelButton(actionContainer, "释放全部演示节点").setOnClickListener(view -> clearDemoNodes());
        updateStatus();
    }

    private void addDemoCube(Vector3 position) {
        float hue = (createdCount % 4) * 0.16f;
        Color color = new Color(0.12f + hue, 0.48f, 0.95f - hue);
        createCube(position, 0.42f, color, node -> {
            demoNodes.add(node);
            createdCount++;
            updateStatus();
        });
    }

    private void removeLastDemoNode() {
        if (demoNodes.isEmpty()) {
            return;
        }
        Node node = demoNodes.remove(demoNodes.size() - 1);
        removeManagedNode(node);
        removedCount++;
        updateStatus();
    }

    private void clearDemoNodes() {
        while (!demoNodes.isEmpty()) {
            Node node = demoNodes.remove(demoNodes.size() - 1);
            removeManagedNode(node);
            removedCount++;
        }
        updateStatus();
    }

    private void updateStatus() {
        if (statusView != null) {
            statusView.setText("已创建：" + createdCount
                    + "  已主动释放：" + removedCount
                    + "  场景中：" + demoNodes.size());
        }
    }

    @Override
    protected void onReleaseAdvancedScene() {
        demoNodes.clear();
        statusView = null;
    }
}
