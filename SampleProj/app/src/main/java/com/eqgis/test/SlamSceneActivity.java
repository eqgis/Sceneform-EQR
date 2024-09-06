package com.eqgis.test;

import android.os.Bundle;

import com.eqgis.eqr.layout.EqSlamSceneLayout;
import com.eqgis.test.scene.GltfSampleScene;


public class SlamSceneActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //启用全屏
        EqSlamSceneLayout.setFullScreenEnable(this,true);

        setContentView(R.layout.activity_slam_scene);
        sceneLayout = findViewById(R.id.slam_scene_layout);
        sceneLayout.init(this);
        /*EqSlamSceneLayout eqSlamSceneLayout = (EqSlamSceneLayout) sceneLayout;
        //关闭特征点绘制
        eqSlamSceneLayout.setDrawPoints(false);*/
        //加载GLTF模型
        sampleScene = new GltfSampleScene();
        sampleScene.create(this,sceneLayout.getRootNode());
    }
}