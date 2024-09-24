package com.eqgis.test;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.eqgis.ar.ARAnchor;
import com.eqgis.ar.ARHitResult;
import com.eqgis.ar.ARPlane;
import com.eqgis.ar.OnTapArPlaneListener;
import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.eqr.layout.ARSceneLayout;
import com.eqgis.eqr.utils.ScaleTool;
import com.eqgis.test.scene.GltfSampleScene;
import com.google.sceneform.AnchorNode;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.ModelRenderable;

import java.util.function.Function;

/**
 * AR三维场景
 */
public class ARScenePlaneActivity extends BaseActivity {
    private ARSceneLayout arSceneLayout;
    private String modelPath = "gltf/bee.glb";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ar_scene);
        ((TextView)findViewById(R.id.tips)).setText("Demo操作提示：\n1、移动手机识别平面\n2、点击平面加载模型");

        sceneLayout = findViewById(R.id.ar_scene_layout);
        sceneLayout.init(this);
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx",100);
        arSceneLayout = (ARSceneLayout) sceneLayout;
        arSceneLayout.setPlaneRendererEnabled(true);

        arSceneLayout.setOnTapPlaneListener(new OnTapArPlaneListener() {
            @Override
            public void onTapPlane(ARHitResult hitResult, ARPlane plane, MotionEvent motionEvent) {
                ARAnchor anchor = hitResult.createAnchor();
                AnchorNode modelNode = new AnchorNode(anchor);
                ModelRenderable
                        .builder()
                        .setSource(getApplicationContext(), Uri.parse(modelPath))
                        .setIsFilamentGltf(true)
                        .build()
                        .thenApply(new Function<ModelRenderable, Object>() {
                            @Override
                            public Object apply(ModelRenderable modelRenderable) {
                                modelNode.setRenderable(modelRenderable);
                                //缩放成单位尺寸
                                modelNode.setLocalScale(Vector3.one()
                                        .scaled(ScaleTool.calculateUnitsScale(modelRenderable) * 0.1f));
                                modelNode.setParent(arSceneLayout.getRootNode());

                                return null;
                            }
                        });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}