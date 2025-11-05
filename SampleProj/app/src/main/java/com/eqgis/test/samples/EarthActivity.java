package com.eqgis.test.samples;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.test.BaseActivity;
import com.eqgis.test.R;
import com.eqgis.test.utils.AssetImageLoader;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.ModelRenderable;
import com.google.sceneform.rendering.Texture;

/**
 * 地球场景示例（EarthActivity）
 * <p>
 * 本示例演示了在自定义 Sceneform 场景中，如何通过异步加载 assets 目录下的贴图，
 * 创建带有纹理的地球模型节点（球体），并结合手势控制器实现旋转、缩放等交互操作。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *     <li>初始化场景光照、天空盒、摄像机参数；</li>
 *     <li>通过 {@link Texture#builder()} 异步加载 assets/img/earth.png 贴图；</li>
 *     <li>使用 {@link MaterialFactory#makeOpaqueWithTexture(android.content.Context, Texture)} 创建带纹理材质；</li>
 *     <li>通过 {@link GeometryUtils} 工具类绘制球体模型（Earth Sphere）；</li>
 *     <li>将渲染对象绑定到 {@link Node} 节点并加入场景；</li>
 *     <li>借助 {@link NodeGestureController} 实现节点旋转、缩放、移动等手势控制；</li>
 * </ul>
 *
 * <h3>技术要点：</h3>
 * <ul>
 *     <li>采用异步链式调用（CompletableFuture）构建 Texture → Material → Renderable；</li>
 *     <li>仅在最终阶段操作 Node（需在主线程执行）；</li>
 *     <li>避免多层 runOnUiThread 调用，提高执行效率；</li>
 * </ul>
 *
 * <h3>相关类：</h3>
 * <ul>
 *     <li>{@link GeometryUtils}：几何体绘制工具类，支持球体、立方体、圆柱体生成；</li>
 *     <li>{@link NodeGestureController}：节点手势控制管理器，用于响应用户触摸事件；</li>
 *     <li>{@link AssetImageLoader}：从 assets 目录中加载 Bitmap 资源的工具类；</li>
 * </ul>
 *
 * <h3>资源依赖：</h3>
 * <pre>
 * assets/
 * ├── img/
 * │   └── earth.png               // 地球贴图
 * └── enviroments/
 *     ├── light/lightroom_ibl.ktx // 间接光照资源
 *     └── pillars_2k_skybox.ktx   // 天空盒贴图
 * </pre>
 *
 * <p>该示例适合作为 Sceneform + 自定义渲染节点的基础模板。</p>
 */
public class EarthActivity extends BaseActivity {

    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_scene);

        sceneLayout = findViewById(R.id.base_scene_layout);
        sceneLayout.init(this)
                .addIndirectLight("enviroments/light/lightroom_ibl.ktx",50);//添加间接光
        sceneLayout.getCamera().setVerticalFovDegrees(45);
        sceneLayout.getCamera().setFarClipPlane(100);
        sceneLayout.setSkybox("enviroments/pillars_2k_skybox.ktx");

        //节点手势控制器初始化
        NodeGestureController.getInstance()
                .setCamera(sceneLayout.getCamera())
                .init(this)
                .setEnabled(true);
        //透传Touch事件
        View touchView = findViewById(R.id.touch_view);
        touchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                NodeGestureController.getInstance().onTouch(motionEvent);
                return true;
            }
        });

        //创建地球
        createEarth(sceneLayout.getRootNode());
    }


    public void createEarth(Node parentNode) {
        Node tempNode = new Node();
        tempNode.setParent(parentNode);
        float distance = 2f;
        tempNode.setLocalPosition(new Vector3(0f, 0f, -distance));

        // 异步加载 Texture（此过程可在后台线程）
        Texture.builder()
                .setSource(AssetImageLoader.loadBitmapFromAssets(this, "img/earth.png"))
                .build()
                .thenCompose(texture ->
                        // 异步创建材质，不必手动切换到 UI 线程
                        MaterialFactory.makeOpaqueWithTexture(getApplicationContext(), texture)
                )
                .thenAccept(material -> {
                    // 材质参数设置（仍可在异步线程完成）
                    material.setFloat(MaterialFactory.MATERIAL_METALLIC, 0f);
                    material.setFloat(MaterialFactory.MATERIAL_ROUGHNESS, 0.8f);
                    material.setFloat(MaterialFactory.MATERIAL_REFLECTANCE, 0f);

                    // 创建渲染对象（Material + Mesh）（需要 Material，但也可异步创建）
                    ModelRenderable renderable = GeometryUtils.makeSphere(0.3f, new Vector3(0, 0, 0), material);

                    // 最后附加到 Node
                    tempNode.setRenderable(renderable);
                    //手势控制器选中节点（即完成手势适配）
                    NodeGestureController.getInstance().select(tempNode, distance);
                })
                .exceptionally(throwable -> {
                    Log.e("EarthActivity", "创建地球失败: ", throwable);
                    return null;
                });
    }
}