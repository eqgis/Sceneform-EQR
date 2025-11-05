package com.eqgis.test.samples;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.eqr.layout.SceneViewType;
import com.eqgis.eqr.node.RootNode;
import com.eqgis.test.BaseActivity;
import com.eqgis.test.R;
import com.eqgis.test.scene.GltfSampleScene;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.ModelRenderable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * VR 三维场景示例（VRSceneActivity）
 * <p>
 * 本示例演示了如何在基于 SceneForm 的自定义引擎框架中创建一个基础 VR 场景，
 * 包含环境光照、摄像机配置、GLTF 模型加载、平面渲染与手势交互等功能。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *     <li>初始化 VR 模式的场景布局（{@link SceneViewType#VR}），并设置摄像机视锥角与远裁剪面；</li>
 *     <li>通过 {@link NodeGestureController} 实现节点的旋转、缩放与位移手势控制；</li>
 *     <li>调用 {@link GltfSampleScene} 加载并展示 GLTF 模型示例；</li>
 *     <li>动态创建一个半透明平面（地面），用于场景参考或模型放置；</li>
 * </ul>
 *
 * <h3>渲染与材质：</h3>
 * <ul>
 *     <li>使用 {@link MaterialFactory#makeTransparentWithColor(android.content.Context, Color)} 创建透明材质；</li>
 *     <li>通过 {@link GeometryUtils#makePlane(Vector3, Vector3, Material)} 绘制平面几何体；</li>
 *     <li>地面节点 {@link Node} 作为 RootNode 的子节点加入场景，支持实时渲染；</li>
 * </ul>
 *
 * <h3>技术要点：</h3>
 * <ul>
 *     <li>采用 {@link CompletableFuture} 异步创建材质，避免阻塞主线程；</li>
 *     <li>通过 {@link NodeGestureController#onTouch(MotionEvent)} 透传触摸事件，实现交互控制；</li>
 *     <li>可扩展为多模型、多节点 VR 展示场景；</li>
 * </ul>
 *
 * <h3>相关类与模块：</h3>
 * <ul>
 *     <li>{@link GeometryUtils}：几何体绘制工具类，支持平面、立方体、球体等基础形状生成；</li>
 *     <li>{@link NodeGestureController}：统一管理节点的手势交互行为（旋转、缩放、移动）；</li>
 *     <li>{@link GltfSampleScene}：GLTF 模型加载示例类，用于展示 3D 模型的加载与渲染；</li>
 *     <li>{@link RootNode}：场景根节点，所有渲染对象均挂载于此；</li>
 *     <li>{@link com.eqgis.eqr.utils.ScaleTool}：几何缩放与坐标变换的辅助工具类</li>
 * </ul>
 *
 * <h3>资源依赖：</h3>
 * <pre>
 * assets/
 * └── enviroments/
 *     ├── light/lightroom_ibl.ktx   // 间接光照资源
 *     └── pillars_2k_skybox.ktx     // 天空盒贴图（可选）
 * </pre>
 *
 * <p>
 * 本类可作为 VR 模式三维场景开发的基础模板，适合拓展至交互展览、虚拟陈列、3D 模型浏览等应用场景。
 * </p>
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
            }
        });
    }

}