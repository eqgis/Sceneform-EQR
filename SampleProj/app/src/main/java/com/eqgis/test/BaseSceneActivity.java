package com.eqgis.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.eqr.layout.LifecycleListener;
import com.eqgis.test.scene.GltfSampleScene;
import com.eqgis.sceneform.Node;

import java.util.List;

/**
 * 基础三维场景
 */
public class BaseSceneActivity extends BaseActivity {
    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_scene);

        sceneLayout = findViewById(R.id.base_scene_layout);

        sceneLayout.setLifecycleListener(new LifecycleListener() {
            @Override
            public void onResume() {

            }

            @Override
            public void onPause() {

            }

            @Override
            public void onDestroy() {

            }

            @Override
            public void onSceneInitComplete() {
                sceneLayout.getCamera().setVerticalFovDegrees(45);
                sceneLayout.setTransparent(true);
                //节点手势控制器初始化
                NodeGestureController.getInstance()
                        .setCamera(sceneLayout.getCamera())
                        .init(BaseSceneActivity.this)
                        .setEnabled(true);

                //加载GLTF模型
                sampleScene = new GltfSampleScene();
                sampleScene.create(BaseSceneActivity.this,sceneLayout.getRootNode());

                View touchView = findViewById(R.id.touch_view);
                touchView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        NodeGestureController.getInstance().onTouch(motionEvent);
                        return true;
                    }
                });
            }
        });
    }

    public void remove(View view) {
        List<Node> children = sceneLayout.getRootNode().getChildren();
        if (children.size() == 0){
            Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
            return;
        }
        Node node = children.get(children.size() - 1);
        node.destroy();
//        node.getRenderableInstance().detachFromRenderer();
//        node.getRenderableInstance().destroy();
//        node.getRenderableInstance().destroyGltfAsset();
    }


    public void add(View view) {
        synchronized (BaseSceneActivity.class){
            ((GltfSampleScene)sampleScene).distance += 0.1f;
            ((GltfSampleScene)sampleScene).addGltf(this,sceneLayout.getRootNode());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sceneLayout.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sceneLayout.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
//        deleteNode(sceneLayout.getRootNode());
        sceneLayout.destroy();
    }
}