package com.eqgis.test;

import android.os.Bundle;

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
        //加载GLTF模型
        sampleScene = new GltfSampleScene();
        sampleScene.create(this,sceneLayout.getRootNode());
    }
}