package com.eqgis.test.scene;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.eqgis.eqr.node.RootNode;
import com.eqgis.eqr.utils.PoseUtils;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Light;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 简单的手势（点击）交互示例场景
 * @author tanyx 2024/1/31
 * @version 1.0
 **/
public class InteractiveScene implements ISampleScene{
    private String modelPath = "gltf/table.glb";

    /**
     * 模型节点
     */
    private Node modelNode;

    /**
     * 光源节点
     */
    private Node lightNode;

    private RootNode rootNode;

    private Context context;

    @Override
    public void create(Context context, RootNode rootNode) {
        this.context = context;
        this.rootNode = rootNode;

        //<editor-fold> - 环境光设置
        lightNode = new Node();
        lightNode.setParent(rootNode);
        Light.Builder builder = Light.builder(Light.Type.DIRECTIONAL);
        Light light = builder.setColor(new Color(1, 1, 1, 1))
                .setIntensity(/*光强：2000lm*/1200)
                .build();
        lightNode.setLight(light);
        //平行光默认方向为(-Z)方向,此处旋转适当的角度，模拟室内日光灯角度
        lightNode.setWorldRotation(/*欧拉角转四元数*/PoseUtils.toQuaternion(-45,0,-30));
        //</editor-fold>

        //<editor-fold> - 模型加载
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
                        modelNode.setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));
//                        modelNode.setLocalRotation(new Quaternion(Vector3.up(),30));
                        modelNode.setLocalPosition(new Vector3(0f, -0.2f, -0.3f));
                        modelNode.setParent(rootNode);
                        return null;
                    }
                });
        //</editor-fold>

        //<editor-fold> - 点击事件回调
        modelNode.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                //点击位置的节点对象
                Node node = hitTestResult.getNode();

                //获取点击对象的Node在世界坐标系下的空间位置、姿态
                Vector3 worldPosition = node.getWorldPosition();
                Quaternion worldRotation = node.getWorldRotation();
                Log.i(InteractiveScene.class.getSimpleName(), "对象节点: "+ worldRotation.toString() + "   " + worldPosition.toString());

                //点击位置与相机当前位置的距离
                float distance = hitTestResult.getDistance();
                //获取世界坐标系下的点击位置（射线检测：与点击对象包围盒的碰撞点位置）
                Vector3 point = hitTestResult.getPoint();
                Log.i(InteractiveScene.class.getSimpleName(), "点击位置与相机的距离是：" + distance + " 米" + point.toString());
                Toast.makeText(context, "碰撞点："+ point, Toast.LENGTH_SHORT).show();

                //添加标记
                addObj(point,/*采用与父节点相同的姿态*/worldRotation);
            }
        });
        //</editor-fold>
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
     * 添加标记对象
     * <p>添加模型使用ModelRenderable，添加安卓View使用ViewRenderable</p>
     * @param position 位置
     * @param rotation 姿态
     */
    public void addObj(Vector3 position,Quaternion rotation){
        //添加TextView，渲染文字
        TextView textView = new TextView(context);
        //Scenefrom默认情况下，250dp对应世界坐标系下的1m，
        textView.setTextSize(16);
        textView.setText("T");
        ViewRenderable.builder()
                .setView(context,textView)
                /*底部居中显示*/
                .setVerticalAlignment(ViewRenderable.VerticalAlignment.BOTTOM)
                .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                .build()
                .thenAccept(new Consumer<ViewRenderable>() {
                    @Override
                    public void accept(ViewRenderable viewRenderable) {
                        Node node = new Node();
                        node.setRenderable(viewRenderable);
                        node.setWorldScale(Vector3.one().scaled(0.25f));//比例
                        node.setWorldPosition(position);
//                        node.setLocalRotation(rotation);
                        node.setParent(rootNode);
                    }
                });
    }
}
