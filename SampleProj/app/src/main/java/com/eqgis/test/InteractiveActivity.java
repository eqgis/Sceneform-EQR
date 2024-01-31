package com.eqgis.test;

import android.os.Bundle;

import com.eqgis.eqr.layout.EqSlamSceneLayout;
import com.eqgis.test.scene.InteractiveScene;

/**
 * 简单交互示例
 * @author tanyx 2024/1/31
 * @version 1.0
 **/
public class InteractiveActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //普通三维场景(场景3选1)
        setContentView(R.layout.activity_base_scene);
        sceneLayout = findViewById(R.id.base_scene_layout);
        sceneLayout.setTransparent(true);//场景视图背景透明

        //AR三维场景(场景3选1)
//        setContentView(R.layout.activity_ar_scene);
//        sceneLayout = findViewById(R.id.ar_scene_layout);

        //启用SLAM 做的 AR场景(场景3选1)
//        EqSlamSceneLayout.setFullScreenEnable(this,true);
//        setContentView(R.layout.activity_slam_scene);
//        sceneLayout = findViewById(R.id.slam_scene_layout);

        //加载场景
        sampleScene = new InteractiveScene();
        sampleScene.create(this,sceneLayout.getRootNode());
    }
}
