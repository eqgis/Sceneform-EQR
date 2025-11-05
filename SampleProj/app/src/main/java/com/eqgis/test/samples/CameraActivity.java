package com.eqgis.test.samples;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.eqr.layout.SceneViewType;
import com.eqgis.test.BaseActivity;
import com.eqgis.test.R;
import com.eqgis.test.scene.GltfSampleScene;

/**
 * 通用3Dof的AR场景
 * <p>基于Camera与设备方向传感器实现的三自由度（3DoF）增强现实场景。</p>
 * <p>该类负责初始化基于摄像头背景的SceneView环境，支持环境光照、模型加载、
 * 以及通过手势控制器(NodeGestureController)实现的模型旋转与缩放交互。</p>
 * <p>主要特性包括：</p>
 * <ul>
 *     <li>使用相机视频流作为视频背景，实现现实场景叠加渲染</li>
 *     <li>通过方向传感器计算设备姿态，实现相机视角控制</li>
 *     <li>支持GLTF模型异步加载与显示</li>
 *     <li>支持多点触控的旋转、缩放、拖拽交互</li>
 * </ul>
 */
public class CameraActivity extends BaseActivity {
    private TextView tipsView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        sceneLayout = findViewById(R.id.camera_scene_layout);
        tipsView = findViewById(R.id.tips);

        //选择相机类型后，初始化控件
        sceneLayout.setSceneViewType(SceneViewType.CAMERA).init(this);
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx",100);

        //节点手势控制器初始化
        NodeGestureController.getInstance()
                .setCamera(sceneLayout.getCamera())
                .init(this)
                .setEnabled(true);

        //加载GLTF模型
        sampleScene = new GltfSampleScene();
        sampleScene.create(getApplicationContext(),sceneLayout.getRootNode());
        sampleScene.setSceneView(sceneLayout.getSceneView());

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        View touchView = findViewById(R.id.touch_view);
        touchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                NodeGestureController.getInstance().onTouch(motionEvent);
                return true;
            }
        });

//        //仅供调测时使用
//        sceneLayout.addSceneUpdateListener(new Scene.OnUpdateListener() {
//            @Override
//            public void onUpdate(FrameTime frameTime) {
//                Camera camera = sceneLayout.getCamera();
//                Quaternion cameraRotation = camera.getWorldRotation();
//                Vector3 eulerAngle = PoseUtils.toEulerAngle(cameraRotation);
//                tipsView.setText("欧拉角：" + eulerAngle.toString() + "\n" +
//                        "");
//            }
//        });
    }
}
