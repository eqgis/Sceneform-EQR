package com.eqgis.test.scene;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.eqgis.eqr.node.RootNode;
import com.eqgis.eqr.utils.PoseUtils;
import com.google.sceneform.Camera;
import com.google.sceneform.HitTestResult;
import com.google.sceneform.Node;
import com.google.sceneform.NodeParent;
import com.google.sceneform.SceneView;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Light;
import com.google.sceneform.rendering.ModelRenderable;
import com.google.sceneform.rendering.ViewRenderable;

import java.util.function.Consumer;
import java.util.function.Function;
/**
 * InteractiveScene
 * <p>
 * 这是一个基于 Sceneform 实现的简单交互场景示例，用于演示如何在 3D 场景中实现
 * 模型加载、光照设置以及节点点击（Tap）交互功能。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *     <li>加载一个 GLTF 格式的 3D 模型文件（默认路径："gltf/table.glb"）</li>
 *     <li>为场景添加一个可调节角度与强度的平行光源（模拟日光）</li>
 *     <li>实现模型点击事件的响应，打印节点姿态与射线碰撞信息</li>
 *     <li>在点击位置动态添加一个文字标记（ViewRenderable）</li>
 * </ul>
 *
 * <h3>使用说明：</h3>
 * <ol>
 *     <li>实现接口 {@link ISampleScene}，由外部的 Scene 容器调用 {@link #create(Context, NodeParent)} 初始化。</li>
 *     <li>调用 {@link #destroy(Context)} 可释放渲染资源，防止内存泄漏。</li>
 *     <li>场景中的所有节点（模型节点、光照节点、UI节点）均挂载在外部传入的 {@code rootNode} 下。</li>
 * </ol>
 *
 * <h3>示例交互：</h3>
 * 当用户点击模型时，系统将计算射线与包围盒的碰撞点，
 * 在该位置生成一个带有 "T" 的标记对象，并输出点击信息至日志与 Toast。
 *
 * <h3>适用场景：</h3>
 * 本类常用于 SLAM、AR 及三维互动演示系统的入门示例，
 * 展示如何通过 Sceneform 实现模型渲染与节点交互。
 *
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

    private NodeParent rootNode;

    private Context context;

    @Override
    public void create(Context context, NodeParent rootNode) {
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

    @Override
    public void  setSceneView(SceneView sceneView){

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
