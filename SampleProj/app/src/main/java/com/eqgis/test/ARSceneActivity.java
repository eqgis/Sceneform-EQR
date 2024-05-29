package com.eqgis.test;

import android.os.Bundle;
import android.widget.Toast;

import com.eqgis.ar.ARCamera;
import com.eqgis.ar.ARPlugin;
import com.eqgis.test.scene.GltfSampleScene;
import com.google.ar.sceneform.ARPlatForm;
import com.huawei.hiar.AREnginesApk;

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