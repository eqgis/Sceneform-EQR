package com.eqgis.test.samples;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.node.RootNode;
import com.eqgis.eqr.utils.ScaleTool;
import com.eqgis.test.R;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.ModelRenderable;

/**
 * 最简化3D模型加载示例
 * <p>
 * 本示例展示了如何在SceneLayout中加载单个GLTF模型，代码尽量精简，
 * 适合作为初学者快速了解3D模型渲染流程的入门示例。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>获取场景根节点（RootNode）</li>
 *     <li>创建节点（Node）并加载GLTF模型</li>
 *     <li>设置模型的缩放、位置</li>
 *     <li>将模型节点关联到场景根节点，实现可视化显示</li>
 * </ul>
 *
 * <p>适用场景：</p>
 * <ul>
 *     <li>初学者快速测试3D模型加载</li>
 *     <li>演示SceneLayout和ModelRenderable的最基础用法</li>
 * </ul>
 *
 */
public class SimplenessActivity extends AppCompatActivity {

    private String modelPath = "gltf/bee.glb";
    private SceneLayout sceneLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_scene);
        sceneLayout = findViewById(R.id.base_scene_layout);

        //获取场景的根节点
        RootNode rootNode = sceneLayout.getRootNode();
        //创建节点
        Node modelNode = new Node();

        ModelRenderable
                .builder()
                .setSource(this, Uri.parse(modelPath))
                .setIsFilamentGltf(true)
                .build()
                .thenApply(modelRenderable -> {
                    //在节点上设置Renderable
                    modelNode.setRenderable(modelRenderable);
                    //设置比例、位置、
                    modelNode.setLocalScale(/*缩放成单位尺寸*/Vector3.one()
                            .scaled(ScaleTool.calculateUnitsScale(modelRenderable)));
                    modelNode.setLocalPosition(new Vector3(0f, 0, -3));
                    //modelNode.setLocalRotation(new Quaternion(/*旋转轴*/new Vector3(0,0,1),/*旋转角度*/90));
                    //将节点关联到父节点上，才生效。也可使用rootNode.addChild(modelNode);
                    modelNode.setParent(rootNode);
                    return null;
                });
    }

}