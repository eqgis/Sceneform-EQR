package com.eqgis.test;

import android.content.Context;
import android.net.Uri;

import com.eqgis.eqr.node.RootNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.function.Function;

/**
 * gltf工具
 * @version 1.0
 **/
public class GltfUtils {
    /**
     * 在场景的根节点下添加模型
     * @param context
     * @param rootNode
     */
    public static void addGltf(Context context, RootNode rootNode) {
        ModelRenderable
                .builder()
                .setSource(context, Uri.parse("gltf/dragon.glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenApply(new Function<ModelRenderable, Object>() {
                    @Override
                    public Object apply(ModelRenderable modelRenderable) {
                        Node modelNode1 = new Node();
                        modelNode1.setRenderable(modelRenderable);
                        modelNode1.setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));
                        modelNode1.setLocalRotation(new Quaternion(Vector3.up(),30));
                        modelNode1.setLocalPosition(new Vector3(0f, -0.1f, -0.5f));
                        rootNode.addChild(modelNode1);
                        return null;
                    }
                });

    }
}
