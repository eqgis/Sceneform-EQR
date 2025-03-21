package com.eqgis.test;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.node.RootNode;
import com.eqgis.eqr.utils.ScaleTool;
import com.google.sceneform.Node;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.ModelRenderable;

/**
 * 最简单的示例
 * <pre>
 *     演示如何加载3D模型。
 *     最简的代码示例！！！！！！
 * </pre>
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