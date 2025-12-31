package com.eqgis.test.samples;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import com.eqgis.ar.ARAnchor;
import com.eqgis.ar.ARHitResult;
import com.eqgis.ar.ARPlane;
import com.eqgis.ar.OnTapArPlaneListener;
import com.eqgis.eqr.layout.ARSceneLayout;
import com.eqgis.eqr.utils.ScaleTool;
import com.eqgis.test.BaseActivity;
import com.eqgis.test.R;
import com.google.sceneform.AnchorNode;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.ModelRenderable;

import java.util.function.Function;

/**
 * AR平面放置场景示例
 * <p>
 * 本示例展示了如何在AR环境中识别平面并在平面上放置3D模型。
 * 通过 {@link com.eqgis.eqr.layout.ARSceneLayout} 初始化AR场景，
 * 并使用 {@link OnTapArPlaneListener} 监听用户点击事件，在检测到的平面上加载模型。
 * </p>
 *
 * <p>主要特性：</p>
 * <ul>
 *     <li>基于AR平面检测识别真实环境中的平面</li>
 *     <li>用户点击平面时，在点击位置创建锚点并加载GLTF模型</li>
 *     <li>支持异步加载模型并自动缩放至合理尺寸</li>
 *     <li>可显示操作提示，引导用户进行平面识别和模型放置</li>
 * </ul>
 *
 * <p>注意：</p>
 * <ul>
 *     <li>运行本类需要设备支持AR功能，依赖AREngine或ARCore</li>
 *     <li>平面检测和模型放置功能在不支持的设备上将无法使用</li>
 *     <li>建议在光线充足、平面清晰的环境下测试，以获得最佳体验</li>
 * </ul>
 *
 * <p>本类适合作为AR应用中基于平面的3D模型交互示例或基础模板。</p>
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