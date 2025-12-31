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
import com.eqgis.test.scene.GltfSampleScene;
import com.google.android.filament.RenderableManager;
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
 * 基础三维场景示例
 * <p>该示例展示了如何在常规的三维场景（非AR、非VR）中使用 SceneView 渲染GLTF模型，
 * 并通过手势控制器（{@link com.eqgis.eqr.gesture.NodeGestureController}）实现模型交互。</p>
 *
 * <p>主要特性包括：</p>
 * <ul>
 *     <li>基于 {@link com.eqgis.eqr.layout.SceneLayout} 初始化基础3D场景</li>
 *     <li>支持加载GLTF模型文件（异步加载）</li>
 *     <li>支持节点的旋转、缩放、平移等多点触控交互操作</li>
 *     <li>通过 {@link com.eqgis.eqr.geometry.GeometryUtils#makePlane(Vector3, Vector3, Material)}
 *     创建平面几何对象，用于模拟地面或参照物</li>
 *     <li>支持环境光照与相机视锥参数（FOV、远裁剪面）自定义</li>
 *     <li>示例中展示了如何使用 {@link com.google.sceneform.rendering.MaterialFactory}
 *     创建透明材质并应用到平面节点</li>
 * </ul>
 *
 * <p>本类主要用于3D渲染与模型交互逻辑的基础验证，也是AR与VR场景构建的前置演示样例。</p>
 */
public class BaseSceneActivity extends BaseActivity {
    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_scene);

        sceneLayout = findViewById(R.id.base_scene_layout);
        sceneLayout.init(this).addIndirectLight("enviroments/light/lightroom_ibl.ktx",50);
        sceneLayout.getCamera().setVerticalFovDegrees(45);
        sceneLayout.getCamera().setFarClipPlane(100);
//        sceneLayout.setTransparent(false);
//        sceneLayout.setSkybox("enviroments/pillars_2k_skybox.ktx");

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch aSwitch = findViewById(R.id.switch_primitive);
        aSwitch.setVisibility(View.VISIBLE);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                changePrimitiveType(b ? RenderableManager.PrimitiveType.LINES : RenderableManager.PrimitiveType.TRIANGLES);
            }
        });

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

    public void remove(View view) {
    }


    public void add(View view) {
    }

    private void addPlane(RootNode rootNode) {
        Node planeNode = new Node();
        planeNode.setParent(rootNode);
        CompletableFuture<Material> materialCompletableFuture = MaterialFactory.makeTransparentWithColor(
                rootNode.getScene().getView().getContext(),
                new Color(1, 1, 1,1)
        );
        materialCompletableFuture.thenAccept(new Consumer<Material>() {
            @Override
            public void accept(Material material) {
                ModelRenderable modelRenderable = GeometryUtils.makePlane( Vector3.one().scaled(1000),
                        new Vector3(0,-1.2f,0),material);
                Log.i("IKKYU", "accept: modelRenderable");
                planeNode.setRenderable(modelRenderable);
                boolean shadowReceiver = planeNode.getRenderable().isShadowReceiver();
                boolean shadowCaster = planeNode.getRenderable().isShadowCaster();
                Toast.makeText(rootNode.getScene().getView().getContext(),
                        "shadowReceiver:" + shadowReceiver + "\nshadowCaster:" + shadowCaster, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPlane2(RootNode rootNode){
        Node planeNode = new Node();
        planeNode.setParent(rootNode);
        ModelRenderable
                .builder()
                .setSource(this, Uri.parse("gltf/plane.glb"))
                .setIsFilamentGltf(true)
                .build()
                .thenApply(new Function<ModelRenderable, Object>() {
                    @Override
                    public Object apply(ModelRenderable modelRenderable) {
                        planeNode.setRenderable(modelRenderable);
                        //缩放成单位尺寸
                        planeNode.setLocalScale(Vector3.one()
                                .scaled(ScaleTool.calculateUnitsScale(modelRenderable)));
//                        modelNode.setLocalRotation(new Quaternion(Vector3.up(),30));
                        planeNode.setLocalScale(Vector3.one().scaled(1000));
                        planeNode.setLocalPosition(new Vector3(0,-0.7f,0));
                        return null;
                    }
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
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
//        deleteNode(sceneLayout.getRootNode());
    }
}