package com.eqgis.test.samples;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.test.BaseActivity;
import com.eqgis.test.R;
import com.eqgis.test.scene.GltfSampleScene;

/**
 * AR三维场景示例
 * <p>
 * 本示例展示了如何在AR场景中使用 SceneLayout 渲染GLTF模型，
 * 并通过手势控制器（{@link com.eqgis.eqr.gesture.NodeGestureController}）实现模型交互。
 * </p>
 *
 * <p>主要特性：</p>
 * <ul>
 *     <li>基于 {@link com.eqgis.eqr.layout.SceneLayout} 初始化AR三维场景</li>
 *     <li>支持环境光照设置，通过IBL文件增强光照效果</li>
 *     <li>通过手势控制器支持节点的旋转、缩放、平移等交互操作</li>
 *     <li>支持加载GLTF模型（异步加载）并绑定到场景根节点</li>
 *     <li>触摸屏事件回调可与手势控制器联动，实现交互体验</li>
 * </ul>
 *
 * <p>注意：</p>
 * <ul>
 *     <li>运行本类需设备支持 AR 功能，依赖 AREngine（华为） 或 ARCore（谷歌）</li>
 *     <li>在不支持的设备上，AR 功能和手势交互将无法正常使用</li>
 * </ul>
 *
 * <p>本类适合作为AR三维模型展示、交互验证和AR项目基础场景的示例。</p>
 */
public class ARSceneActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ar_scene);

        sceneLayout = findViewById(R.id.ar_scene_layout);
        sceneLayout.init(this);
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx",100);
        //节点手势控制器初始化
        NodeGestureController.getInstance()
                .setCamera(sceneLayout.getCamera())
                .init(this)
                .setEnabled(true);

        //加载GLTF模型
        sampleScene = new GltfSampleScene();
        sampleScene.create(getApplicationContext(),sceneLayout.getRootNode());
        sampleScene.setSceneView(sceneLayout.getSceneView());

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        View touchView = findViewById(R.id.touch_view);
        touchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                NodeGestureController.getInstance().onTouch(motionEvent);
                return true;
            }
        });
    }
}