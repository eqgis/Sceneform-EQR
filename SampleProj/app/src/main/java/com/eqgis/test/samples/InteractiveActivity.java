package com.eqgis.test.samples;

import android.os.Bundle;

import com.eqgis.eqr.layout.EqSlamSceneLayout;
import com.eqgis.test.BaseActivity;
import com.eqgis.test.R;
import com.eqgis.test.scene.InteractiveScene;

/**
 * 简单交互示例
 * <p>
 * 这是一个基于 Sceneform 实现的简单交互场景示例{@link InteractiveScene}，用于演示如何在 3D 场景中实现
 * 模型加载、光照设置以及节点点击（Tap）交互功能。
 * </p>
 **/
public class InteractiveActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //普通三维场景(场景3选1)
        setContentView(R.layout.activity_base_scene);
        sceneLayout = findViewById(R.id.base_scene_layout);
        sceneLayout.init(this);
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
