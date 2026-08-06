package com.eqgis.test.fragments.tutorial;

import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;

import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.utils.ScaleTool;
import com.google.sceneform.Node;
import com.google.sceneform.Scene;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.ModelRenderable;

/**
 * 节点手势控制教程
 * <pre>
 *     点击模型后通过 NodeGestureController 选择节点，
 *     使用单指旋转、双指平移和双指缩放控制模型自身变换。
 * </pre>
 * @author tanyx
 */
public class NodeGestureLessonFragment extends BaseInteractionLessonFragment {
    private static final String TAG = NodeGestureLessonFragment.class.getSimpleName();
    private final NodeGestureController gestureController = NodeGestureController.getInstance();
    private Scene.OnPeekTouchListener nodeTouchObserver;
    private Node modelNode;

    /**
     * 节点手势页面禁用默认相机手势，防止同一触摸事件同时改变相机和模型
     * @return 始终返回 false
     */
    @Override
    protected boolean isCameraGestureEnabled() {
        return false;
    }

    /**
     * 节点手势页面无需参数面板
     * @return 始终返回 false
     */
    @Override
    protected boolean shouldOverlayActions() {
        return false;
    }

    @Override
    protected String getLessonTitle() {
        return "节点手势控制";
    }

    @Override
    protected String getLessonDescription() {
        return "先点击选中 3D 模型，再用单指旋转、双指平移和双指缩放控制节点；当前页面使用天空盒背景。";
    }

    /**
     * 创建带天空盒的模型节点并绑定节点手势控制器
     * @param sceneLayout 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureInteractionScene(sceneLayout, true);
        gestureController.setFling(false)
                .unSelect();
        gestureController.init(requireContext().getApplicationContext())
                .setCamera(sceneLayout.getCamera())
                .setEnabled(true);

        //desc- PeekTouch 可在不截断 Node.OnTapListener 的前提下，把完整手势序列交给节点控制器。
        nodeTouchObserver = (hitTestResult, motionEvent) -> gestureController.onTouch(motionEvent);
        sceneLayout.getSceneView().getScene().addOnPeekTouchListener(nodeTouchObserver);
        loadGestureModel();
    }

    private void loadGestureModel() {
        ModelRenderable.builder()
                .setSource(requireContext(), Uri.parse("gltf/DamagedHelmet.glb"))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(renderable -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    modelNode = addRenderableNode(renderable, new Vector3(0, 0, -2.8f));
                    modelNode.setLocalScale(Vector3.one().scaled(
                            ScaleTool.calculateUnitsScale(renderable)));

                    //desc- 模型点击用于选中节点，双指平移需要以节点中心到相机的距离保持模型原始深度。
                    modelNode.setOnTapListener(new Node.OnTapListener() {
                        @Override
                        public void onTap(com.google.sceneform.HitTestResult hitTestResult,
                                          MotionEvent motionEvent) {
                            //desc- NodeGestureController 平移时需要节点中心深度，碰撞点距离会让模型中心突然跳到表面位置并靠近相机。
                            float nodeDistance = Vector3.subtract(
                                    modelNode.getWorldPosition(),
                                    sceneLayout.getCamera().getWorldPosition()).length();
                            gestureController.select(modelNode, nodeDistance);
                        }
                    });
                })
                .exceptionally(error -> {
                    Log.e(TAG, "加载节点手势模型失败", error);
                    return null;
                });
    }

    /**
     * 页面恢复时重新启用节点手势
     */
    @Override
    public void onResume() {
        super.onResume();
        if (isSceneActive()) {
            gestureController.setEnabled(true);
        }
    }

    /**
     * 页面暂停时停止节点惯性并禁用手势
     */
    @Override
    public void onPause() {
        gestureController.setFling(false).setEnabled(false);
        super.onPause();
    }

    /**
     * SceneLayout 销毁前解除 PeekTouch 和单例控制器持有的节点引用
     */
    @Override
    protected void onReleaseInteraction() {
        if (sceneLayout != null && nodeTouchObserver != null) {
            sceneLayout.getSceneView().getScene().removeOnPeekTouchListener(nodeTouchObserver);
        }
        nodeTouchObserver = null;
        gestureController.setFling(false).setEnabled(false);
        gestureController.unSelect();
        modelNode = null;
    }
}
