package com.eqgis.test.fragments.tutorial;

import android.view.Gravity;
import android.widget.FrameLayout;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.layout.SceneViewType;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;

/**
 * 实时相机流与三维叠加教程
 * <pre>
 *     使用 CameraSceneView 将 Camera2 预览流渲染为二维背景，设备方向传感器负责更新
 *     三维相机姿态，并在真实画面前叠加 Cube 与 Plane。
 * </pre>
 * @author tanyx
 */
public class CameraStreamLessonFragment extends BaseVideoLessonFragment {

    @Override
    protected String getLessonTitle() {
        return "实时相机流";
    }

    @Override
    protected String getLessonDescription() {
        return "使用 Camera2 与 ExternalTexture 显示实时相机背景，并由方向传感器控制三维相机姿态。";
    }

    /**
     * CameraSceneView 已使用设备方向传感器更新相机，不叠加教程触摸相机手势
     * @return false
     */
    @Override
    protected boolean isCameraGestureEnabled() {
        return false;
    }

    /**
     * 当前页面没有运行时参数，不显示空的悬浮面板
     * @return false
     */
    @Override
    protected boolean shouldOverlayActions() {
        return false;
    }

    /**
     * 创建 Camera2 背景场景
     * @return CAMERA 类型场景布局
     */
    @Override
    protected SceneLayout createSceneLayout() {
        return new SceneLayout(requireContext()).setSceneViewType(SceneViewType.CAMERA);
    }

    /**
     * 在实时相机背景前添加三维 Cube 和 Plane
     * @param sceneLayout 当前教程场景
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureVideoScene(sceneLayout);
        if (sceneLayout.getSceneView().getLayoutParams() instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams layoutParams =
                    (FrameLayout.LayoutParams) sceneLayout.getSceneView().getLayoutParams();
            //desc- CameraSceneView 会按 4:3 放大 SurfaceView，顶部对齐可避免居中裁切区域越界覆盖教程文字。
            layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            sceneLayout.getSceneView().setLayoutParams(layoutParams);
        }
        addDecorationPlane(
                new Vector3(0, -0.95f, -4.0f),
                4.8f,
                4.0f,
                new Color(0.10f, 0.28f, 0.42f));
        addDecorationCube(
                new Vector3(-0.75f, -0.42f, -3.2f),
                0.75f,
                new Color(1.0f, 0.48f, 0.08f));
        addDecorationCube(
                new Vector3(0.85f, -0.52f, -4.0f),
                0.62f,
                new Color(0.08f, 0.62f, 1.0f));
    }
}
