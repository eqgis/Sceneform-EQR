package com.eqgis.test.samples;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.eqr.node.RootNode;
import com.eqgis.eqr.utils.ScaleTool;
import com.eqgis.test.BaseActivity;
import com.eqgis.test.R;
import com.eqgis.test.scene.PlyDataScene;
import com.google.android.filament.RenderableManager;
import com.google.sceneform.Camera;
import com.google.sceneform.FrameTime;
import com.google.sceneform.Node;
import com.google.sceneform.Scene;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.IRenderableInternalData;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.ModelRenderable;
import com.google.sceneform.rendering.RenderableInternalSplatData;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Ply格式数据加载示例
 * <p>该示例展示了如何在常规的三维场景（非AR、非VR）中使用 SceneView 渲染Ply格式数据，
 * 并通过手势控制器（{@link NodeGestureController}）实现模型交互。</p>
 *
 * <p>当前支持Ply格式的Mesh、PointCloud、3DGS（部分能力）</p>
 */
public class PlyDataSceneActivity extends BaseActivity {
    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_scene);

        sceneLayout = findViewById(R.id.base_scene_layout);
        sceneLayout.init(this).addIndirectLight("enviroments/light/lightroom_ibl.ktx",50);
        sceneLayout.getCamera().setVerticalFovDegrees(45);
        sceneLayout.getCamera().setFarClipPlane(100);
        sceneLayout.setSkybox("enviroments/pillars_2k_skybox.ktx");

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch aSwitch = findViewById(R.id.switch_primitive);
        aSwitch.setVisibility(View.GONE);

        //节点手势控制器初始化
        NodeGestureController.getInstance()
                .setCamera(sceneLayout.getCamera())
                .init(this)
                .setEnabled(true);

        //加载数据
        sampleScene = new PlyDataScene();
        sampleScene.create(this,sceneLayout.getRootNode());

        View touchView = findViewById(R.id.touch_view);
        touchView.setOnTouchListener((view, motionEvent) -> {
            NodeGestureController.getInstance().onTouch(motionEvent);
            return true;
        });

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
    }
}