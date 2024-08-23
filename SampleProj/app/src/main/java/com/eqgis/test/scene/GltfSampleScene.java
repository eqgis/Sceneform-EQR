package com.eqgis.test.scene;

import android.content.Context;
import android.net.Uri;
import android.view.MotionEvent;
import android.widget.Toast;

import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.eqr.node.RootNode;
import com.eqgis.eqr.utils.PoseUtils;
import com.eqgis.eqr.utils.ScaleTool;
import com.google.sceneform.HitTestResult;
import com.google.sceneform.Node;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Light;
import com.google.sceneform.rendering.ModelRenderable;

import java.util.function.Function;

/**
 * GLTF示例场景
 * @author tanyx 2024/1/30
 * @version 1.0
 **/
public class GltfSampleScene implements ISampleScene{
    private String modelPath = "gltf/bee.glb";
    public float distance = 3.6f;


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
        if (lightNode != null){
            lightNode.setParent(null);
        }
    }

    /**
     * 加载模型
     */
    public void addGltf(Context context, RootNode rootNode) {
        modelNode = new Node();
        ModelRenderable
                .builder()
                .setSource(context, Uri.parse(modelPath))
                .setIsFilamentGltf(true)
                .build()
                .thenApply(new Function<ModelRenderable, Object>() {
                    @Override
                    public Object apply(ModelRenderable modelRenderable) {
                        modelNode.setRenderable(modelRenderable);
                        //缩放成单位尺寸
                        modelNode.setLocalScale(Vector3.one()
                                .scaled(ScaleTool.calculateUnitsScale(modelRenderable)));
//                        modelNode.setLocalRotation(new Quaternion(Vector3.up(),30));
                        modelNode.setLocalPosition(new Vector3(0f, 0, -distance));
                        modelNode.setParent(rootNode);
                        return null;
                    }
                });


        //给模型添加点击事件，多用于选中模型
        modelNode.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                Toast.makeText(context, "点击测试", Toast.LENGTH_SHORT).show();
            }
        });

        //手势控制器旋转节点
        NodeGestureController.getInstance().select(modelNode,distance);
    }

    private void addLight(RootNode rootNode) {
        lightNode = new Node();
        lightNode.setParent(rootNode);
//        Light.Builder builder = Light.builder(Light.Type.POINT);
//        Light light = builder.setColor(new Color(1, 0, 0, 1))
//                .setIntensity(/*光强：2000lm*/6000)
//                .build();
//        lightNode.setLight(light);
//        //平行光默认方向为(-Z)方向,此处旋转适当的角度，模拟室内日光灯角度
//        lightNode.setWorldRotation(/*欧拉角转四元数*/PoseUtils.toQuaternion(-45,0,-30));
        lightNode.setWorldPosition(new Vector3(0f,2.0f,-distance));
        Quaternion quaternion = Quaternion.rotationBetweenVectors(Vector3.forward(), new Vector3(0,-1,-0.2f));
        lightNode.setWorldRotation(quaternion);

        Light light = Light.builder(Light.Type.SPOTLIGHT)
                .setColorTemperature(/*3000*/4800)
                .setFalloffRadius(5.0f)
                .setInnerConeAngle(0.1f)
                .setOuterConeAngle(0.6f)
//                .setOuterConeAngle(0.5f)
                .setIntensity(/*10000*/30000)
                .setShadowCastingEnabled(/*启用光线投射，这会产生阴影，默认未启用*/true)
                .build();
        lightNode.setLight(light);
    }
}
