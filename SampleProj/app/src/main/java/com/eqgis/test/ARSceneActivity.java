package com.eqgis.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.test.scene.GltfSampleScene;

/**
 * AR三维场景
 */
public class ARSceneActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ar_scene);

        sceneLayout = findViewById(R.id.ar_scene_layout);
        sceneLayout.init(this);
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
    }
}