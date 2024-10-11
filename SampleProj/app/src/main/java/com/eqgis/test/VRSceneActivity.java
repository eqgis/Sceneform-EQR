package com.eqgis.test;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.eqr.layout.SceneViewType;
import com.eqgis.eqr.node.RootNode;
import com.eqgis.eqr.utils.ScaleTool;
import com.eqgis.test.scene.GltfSampleScene;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.ModelRenderable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 基础三维场景
 */
public class VRSceneActivity extends BaseActivity {
    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_scene);

        sceneLayout = findViewById(R.id.base_scene_layout);
        sceneLayout.setSceneViewType(SceneViewType.VR).init(this).addIndirectLight("enviroments/light/lightroom_ibl.ktx",50);
        sceneLayout.getCamera().setVerticalFovDegrees(45);
        sceneLayout.getCamera().setFarClipPlane(100);
        //节点手势控制器初始化
        NodeGestureController.getInstance()
                .setCamera(sceneLayout.getCamera())
                .init(this)
                .setEnabled(true);

        //加载GLTF模型
        sampleScene = new GltfSampleScene();
        sampleScene.create(this,sceneLayout.getRootNode());

        View touchView = findViewById(R.id.touch_view);
        touchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                NodeGestureController.getInstance().onTouch(motionEvent);
                return true;
            }
        });
        addPlane(sceneLayout.getRootNode());
    }

    private void addPlane(RootNode rootNode) {
        Node planeNode = new Node();
        planeNode.setParent(rootNode);
        CompletableFuture<Material> materialCompletableFuture = MaterialFactory.makeTransparentWithColor(
                rootNode.getScene().getView().getContext(),
                new Color(1, 1, 1,0.1f)
        );
        materialCompletableFuture.thenAccept(new Consumer<Material>() {
            @Override
            public void accept(Material material) {
                ModelRenderable modelRenderable = GeometryUtils.makePlane( Vector3.one().scaled(1000),
                        new Vector3(0,-1.2f,0),material);
                Log.i("IKKYU", "accept: modelRenderable");
                planeNode.setRenderable(modelRenderable);
//                boolean shadowReceiver = planeNode.getRenderable().isShadowReceiver();
//                boolean shadowCaster = planeNode.getRenderable().isShadowCaster();
//                Toast.makeText(rootNode.getScene().getView().getContext(),
//                        "shadowReceiver:" + shadowReceiver + "\nshadowCaster:" + shadowCaster, Toast.LENGTH_SHORT).show();
            }
        });
    }

}