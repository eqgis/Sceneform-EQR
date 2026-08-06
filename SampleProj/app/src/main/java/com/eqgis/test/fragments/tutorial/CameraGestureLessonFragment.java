package com.eqgis.test.fragments.tutorial;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;

/**
 * 相机手势控制教程
 * <pre>
 *     在带天空盒的场景中使用单指旋转相机、双指平移相机，
 *     并通过双指间距变化沿相机朝向前后移动。
 * </pre>
 * @author tanyx
 */
public class CameraGestureLessonFragment extends BaseInteractionLessonFragment {
    private TextView statusText;

    @Override
    protected String getLessonTitle() {
        return "相机手势控制";
    }

    @Override
    protected String getLessonDescription() {
        return "观察天空盒和多层 Cube：单指拖动旋转相机，双指同步拖动平移相机，双指缩放控制相机前后移动。";
    }

    /**
     * 创建带天空盒的相机手势参照场景
     * @param sceneLayout 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureInteractionScene(sceneLayout, true);
        createCube(new Vector3(-1.25f, 0.55f, -3.2f), 0.62f,
                new Color(0.08f, 0.52f, 1.0f), node -> { });
        createCube(new Vector3(0, 0.05f, -4.0f), 0.82f,
                new Color(0.24f, 0.82f, 0.48f), node -> { });
        createCube(new Vector3(1.35f, -0.45f, -5.0f), 1.0f,
                new Color(1.0f, 0.48f, 0.08f), node -> { });
        createSphere(new Vector3(-0.9f, -0.75f, -5.8f), 0.48f,
                new Color(0.76f, 0.32f, 0.96f), node -> { });
    }

    /**
     * 创建相机手势说明与复位操作
     * @param actionContainer 悬浮面板容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        statusText = addPanelText(actionContainer,
                "单指：旋转\n双指拖动：平移\n双指缩放：前后移动");
        addPanelButton(actionContainer, "复位相机").setOnClickListener(view -> {
            resetTutorialCamera();
            statusText.setText("相机已复位\n单指：旋转｜双指：平移/缩放");
        });
    }

    @Override
    protected void onReleaseInteraction() {
        statusText = null;
    }
}
