package com.eqgis.test.fragments.tutorial;

import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.geometry.Line3D;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.utils.ScreenPointTool;
import com.google.sceneform.HitTestResult;
import com.google.sceneform.Node;
import com.google.sceneform.Scene;
import com.google.sceneform.collision.Ray;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.ModelRenderable;

import java.util.ArrayList;
import java.util.List;

/**
 * 屏幕坐标、射线与空间测量进阶教程。
 * <p>将屏幕触点转换为世界射线，通过碰撞命中获得空间坐标，并测量两个命中点的距离。</p>
 * @author tanyx
 */
public class ScreenRayMeasureLessonFragment extends BaseAdvancedLessonFragment {
    private final List<Vector3> measuredPoints = new ArrayList<>();
    private final List<Node> markerNodes = new ArrayList<>();
    private Scene.OnPeekTouchListener peekTouchListener;
    private Node measureBoard;
    private ModelRenderable markerRenderable;
    private Material lineMaterial;
    private Line3D measurementLine;
    private TextView statusView;
    private float downX;
    private float downY;

    @Override
    protected String getLessonTitle() {
        return "屏幕坐标、射线与空间测量";
    }

    @Override
    protected String getLessonDescription() {
        return "点击测量板，将屏幕像素转换为 Camera Ray，再通过 Scene.hitTest() 得到世界坐标。连续选择两点后绘制 Line3D 并计算三维距离。";
    }

    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureAdvancedScene(sceneLayout, true);
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(0.18f, 0.22f, 0.29f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    measureBoard = addRenderableNode(
                            GeometryUtils.makeQuad(new Vector3(3.4f, 2.25f, 0.0f), Vector3.zero(), material),
                            new Vector3(0.0f, 0.0f, -4.0f));
                });
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(1.0f, 0.48f, 0.08f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    markerRenderable = GeometryUtils.makeSphere(0.055f, Vector3.zero(), material);
                    lineMaterial = material.makeCopy();
                });

        peekTouchListener = this::handleSceneTouch;
        sceneLayout.getSceneView().getScene().addOnPeekTouchListener(peekTouchListener);
    }

    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        statusView = addPanelText(actionContainer, "轻触深色测量板选择第一个空间点。拖动仍用于控制相机。"
        );
        addPanelButton(actionContainer, "重新测量").setOnClickListener(view -> {
            clearMeasurement();
            statusView.setText("测量已重置，请选择第一个空间点。"
            );
        });
        addPanelText(actionContainer,
                "转换链：屏幕像素 (x,y) → Camera.screenPointToRay → Scene.hitTest → 世界坐标 → Camera.worldToScreenPoint。射线检测依赖 Renderable 的 CollisionShape。"
        );
    }

    private void handleSceneTouch(HitTestResult hitTestResult, MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            return;
        }
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return;
        }
        float deltaX = event.getX() - downX;
        float deltaY = event.getY() - downY;
        if (deltaX * deltaX + deltaY * deltaY > dp(10) * dp(10)
                || hitTestResult.getNode() != measureBoard) {
            return;
        }
        Ray ray = ScreenPointTool.screenPointToRay(
                requireContext(), sceneLayout.getCamera(), (int) event.getX(), (int) event.getY());
        addMeasurementPoint(hitTestResult.getPoint(), event.getX(), event.getY(), ray);
    }

    private void addMeasurementPoint(Vector3 point, float screenX, float screenY, Ray ray) {
        if (markerRenderable == null || lineMaterial == null) {
            statusView.setText("测量标记材质仍在加载，请稍后再试。"
            );
            return;
        }
        if (measuredPoints.size() == 2) {
            clearMeasurement();
        }
        Vector3 storedPoint = new Vector3(point);
        measuredPoints.add(storedPoint);
        Node marker = addRenderableNode(markerRenderable, storedPoint);
        markerNodes.add(marker);

        Vector3 projected = sceneLayout.getCamera().worldToScreenPoint(storedPoint);
        String text = String.format(java.util.Locale.US,
                "屏幕：%.0f, %.0f px\n射线起点：%.2f, %.2f, %.2f\n射线方向：%.3f, %.3f, %.3f\n命中世界坐标：%.3f, %.3f, %.3f\n反投影：%.0f, %.0f px",
                screenX, screenY,
                ray.getOrigin().x, ray.getOrigin().y, ray.getOrigin().z,
                ray.getDirection().x, ray.getDirection().y, ray.getDirection().z,
                storedPoint.x, storedPoint.y, storedPoint.z,
                projected.x, projected.y);
        if (measuredPoints.size() == 2) {
            createMeasurementLine();
            float distance = Vector3.subtract(measuredPoints.get(1), measuredPoints.get(0)).length();
            text += String.format(java.util.Locale.US, "\n两点空间距离：%.3f m", distance);
        } else {
            text += "\n请选择第二个点。";
        }
        statusView.setText(text);
    }

    private void createMeasurementLine() {
        measurementLine = new Line3D()
                .setMaterial(lineMaterial)
                .setRadius(0.012f)
                .setEdgeNum(8)
                .setTextureMode(0)
                .setPointList(new ArrayList<>(measuredPoints));
        addManagedNode(measurementLine);
        measurementLine.refresh();
    }

    private void clearMeasurement() {
        for (Node marker : new ArrayList<>(markerNodes)) {
            removeManagedNode(marker);
        }
        markerNodes.clear();
        measuredPoints.clear();
        if (measurementLine != null) {
            measurementLine.dispose();
            removeManagedNode(measurementLine);
            measurementLine = null;
        }
    }

    @Override
    protected void onReleaseAdvancedScene() {
        if (sceneLayout != null && peekTouchListener != null) {
            sceneLayout.getSceneView().getScene().removeOnPeekTouchListener(peekTouchListener);
        }
        peekTouchListener = null;
        clearMeasurement();
        measureBoard = null;
        markerRenderable = null;
        lineMaterial = null;
        statusView = null;
    }
}
