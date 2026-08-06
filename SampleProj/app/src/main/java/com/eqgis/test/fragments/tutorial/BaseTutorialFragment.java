package com.eqgis.test.fragments.tutorial;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eqgis.eqr.gesture.CameraGestureController;
import com.eqgis.test.fragments.BaseSampleFragment;

/**
 * 教程渲染 Fragment 基类
 * <pre>
 *     在普通教程页面的 SceneLayout 初始化完成后统一绑定相机手势，
 *     单指旋转相机、双指在当前视图平面内平移相机，并通过双指捏合控制相机前后移动。
 * </pre>
 * @author tanyx
 */
public abstract class BaseTutorialFragment extends BaseSampleFragment {
    private CameraGestureController cameraGestureController;

    /**
     * 当前教程是否启用默认相机手势
     * <p>节点手势等需要独占单指/双指事件的页面可覆盖并返回 false。</p>
     * @return true 表示自动绑定相机手势
     */
    protected boolean isCameraGestureEnabled() {
        return true;
    }

    /**
     * 将相机恢复到当前教程创建手势控制器时的初始位置与姿态
     */
    protected final void resetTutorialCamera() {
        if (cameraGestureController != null) {
            cameraGestureController.resetCamera();
        }
    }

    /**
     * 教程操作控件统一悬浮在三维场景左上角
     * @return 始终返回 true
     */
    @Override
    protected boolean shouldOverlayActions() {
        return true;
    }

    /**
     * SceneLayout 创建完成后绑定相机手势
     * @param view 当前 Fragment 根视图
     * @param savedInstanceState 保存的页面状态
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isCameraGestureEnabled()
                || sceneLayout == null
                || sceneLayout.getSceneView() == null
                || sceneLayout.getCamera() == null) {
            return;
        }
        cameraGestureController = new CameraGestureController(sceneLayout.getCamera());
        cameraGestureController.attachTo(sceneLayout.getSceneView());
    }

    /**
     * 销毁教程视图前解绑相机手势，再释放 SceneLayout
     */
    @Override
    public void onDestroyView() {
        if (cameraGestureController != null) {
            cameraGestureController.detach();
            cameraGestureController = null;
        }
        super.onDestroyView();
    }
}
