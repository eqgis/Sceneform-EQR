package com.eqgis.test;

import android.os.Bundle;

import com.eqgis.eqr.layout.EqSlamSceneLayout;


public class SlamSceneActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //启用全屏
        EqSlamSceneLayout.setFullScreenEnable(this,true);

        setContentView(R.layout.activity_slam_scene);
        sceneLayout = findViewById(R.id.slam_scene_layout);
        /*EqSlamSceneLayout eqSlamSceneLayout = (EqSlamSceneLayout) sceneLayout;
        //关闭特征点绘制
        eqSlamSceneLayout.setDrawPoints(false);*/
        //加载GLTF模型
        GltfUtils.addGltf(this,sceneLayout.getRootNode());
    }
}