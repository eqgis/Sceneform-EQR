package com.eqgis.test.fragments.tutorial;

import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.HitTestResult;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;

import java.util.Locale;

/**
 * 碰撞检测点击教程
 * <pre>
 *     点击具备碰撞体的 Cube 或球体，通过 Node.OnTapListener 获取
 *     射线命中节点、碰撞点世界坐标以及碰撞点到相机的距离。
 * </pre>
 * @author tanyx
 */
public class CollisionClickLessonFragment extends BaseInteractionLessonFragment {
    private TextView collisionText;

    @Override
    protected String getLessonTitle() {
        return "碰撞检测点击";
    }

    @Override
    protected String getLessonDescription() {
        return "点击场景中的 Cube 或球体，悬浮面板会实时显示射线与碰撞体相交的世界坐标和距离。";
    }

    /**
     * 创建多个具备默认碰撞形状的几何体
     * @param sceneLayout 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureInteractionScene(sceneLayout, false);
        createCube(new Vector3(-0.9f, 0.15f, -3.1f), 1.05f,
                new Color(0.08f, 0.54f, 1.0f),
                node -> bindCollisionClick(node, "Cube"));
        createSphere(new Vector3(0.95f, -0.05f, -3.4f), 0.62f,
                new Color(1.0f, 0.42f, 0.12f),
                node -> bindCollisionClick(node, "Sphere"));
    }

    private void bindCollisionClick(Node node, String objectName) {
        node.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                Vector3 point = hitTestResult.getPoint();
                if (collisionText != null) {
                    collisionText.setText(String.format(
                            Locale.US,
                            "命中：%s\n位置：X %.3f  Y %.3f  Z %.3f\n距离：%.3f m",
                            objectName,
                            point.x,
                            point.y,
                            point.z,
                            hitTestResult.getDistance()));
                }
            }
        });
    }

    /**
     * 创建碰撞位置显示面板
     * @param actionContainer 悬浮面板容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        collisionText = addPanelText(actionContainer,
                "点击 Cube 或 Sphere\n碰撞位置将在这里更新");
    }

    @Override
    protected void onReleaseInteraction() {
        collisionText = null;
    }
}
