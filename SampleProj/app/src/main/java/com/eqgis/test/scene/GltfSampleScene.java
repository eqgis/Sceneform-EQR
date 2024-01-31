package com.eqgis.test.scene;

import android.content.Context;
import android.net.Uri;

import com.eqgis.eqr.node.RootNode;
import com.eqgis.eqr.utils.PoseUtils;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Light;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.function.Function;

/**
 * GLTF示例场景
 * @author tanyx 2024/1/30
 * @version 1.0
 **/
public class GltfSampleScene implements ISampleScene{
    private String modelPath = "gltf/bee.glb";

    /**
     * 模型节点
     */
    private Node modelNode;

    /**
     * 光源节点
     */
    private Node lightNode;

    @Override
    public void create(Context context, RootNode rootNode) {
        addGltf(context, rootNode);

        //添加光源
        addLight(rootNode);
    }

    @Override
    public void destroy(Context context) {
        if (modelNode.getRenderableInstance() != null){
            //销毁模型渲染实例
            modelNode.getRenderableInstance().destroy();
        }
        //断开节点
        modelNode.setParent(null);
        lightNode.setParent(null);
    }

    /**
     * 加载模型
     */
    private void addGltf(Context context, RootNode rootNode) {
        modelNode = new Node();
        ModelRenderable
                .builder()
                .setSource(context, Uri.parse(modelPath))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenApply(new Function<ModelRenderable, Object>() {
                    @Override
                    public Object apply(ModelRenderable modelRenderable) {
                        modelNode.setRenderable(modelRenderable);
                        modelNode.setLocalScale(new Vector3(0.01f, 0.01f, 0.01f));
                        modelNode.setLocalRotation(new Quaternion(Vector3.up(),30));
                        modelNode.setLocalPosition(new Vector3(0f, -0.1f, -0.5f));
                        modelNode.setParent(rootNode);
                        return null;
                    }
                });
    }

    private void addLight(RootNode rootNode) {
        lightNode = new Node();
        lightNode.setParent(rootNode);
        Light.Builder builder = Light.builder(Light.Type.DIRECTIONAL);
        Light light = builder.setColor(new Color(1, 1, 1, 1))
                .setIntensity(/*光强：2000lm*/1200)
                .build();
        lightNode.setLight(light);
        //平行光默认方向为(-Z)方向,此处旋转适当的角度，模拟室内日光灯角度
        lightNode.setWorldRotation(/*欧拉角转四元数*/PoseUtils.toQuaternion(-45,0,-30));
    }
}
