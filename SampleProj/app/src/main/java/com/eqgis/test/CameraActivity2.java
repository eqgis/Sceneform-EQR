package com.eqgis.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.eqr.layout.SceneViewType;
import com.eqgis.eqr.utils.PoseUtils;
import com.eqgis.test.scene.GltfSampleScene;
import com.google.sceneform.Camera;
import com.google.sceneform.FrameTime;
import com.google.sceneform.Scene;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;

/**
 * 通用3Dof的AR场景
 * <p>采用Camera2和方向传感器实现</p>
 */
public class CameraActivity2 extends BaseActivity {
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
