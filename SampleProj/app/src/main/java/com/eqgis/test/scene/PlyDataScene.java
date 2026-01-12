package com.eqgis.test.scene;

import android.content.Context;
import android.net.Uri;
import android.view.MotionEvent;
import android.widget.Toast;

import com.eqgis.eqr.gesture.NodeGestureController;
import com.eqgis.eqr.utils.ScaleTool;
import com.google.sceneform.HitTestResult;
import com.google.sceneform.Node;
import com.google.sceneform.NodeParent;
import com.google.sceneform.SceneView;
import com.google.sceneform.collision.Ray;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.ModelRenderable;
import com.google.sceneform.rendering.Renderable;

import java.util.function.Function;

/**
 * <p></p>
 * <pre>SampleCode:
 * </pre>
 *
 * @author tanyx 2026/1/5
 * @version 1.0
 **/
public class PlyDataScene implements ISampleScene{
//    private String plyPath = "temp/test_color.ply";
    private String plyPath = "temp/test_sh_0.ply";
//    private String plyPath = "temp/cactus_splat3_30kSteps_142k_splats.ply";
//    private String plyPath = "sofa.ply";
    public float distance = 3.6f;
    /**
     * 模型节点
     */
    private Node modelNode;

    /**
     * 光源节点
     */
    private SceneView sceneView;


    @Override
    public void create(Context context, NodeParent rootNode) {
        addData(context,rootNode);
    }

    @Override
    public void destroy(Context context) {
        if (modelNode.getRenderableInstance() != null){
            //销毁模型渲染实例
            modelNode.getRenderableInstance().destroy();
        }
        //断开节点
        modelNode.setParent(null);
    }

    @Override
    public void setSceneView(SceneView sceneView) {
        this.sceneView = sceneView;
    }

    /**
     * 加载模型
     */
    public void addData(Context context, NodeParent rootNode) {
        modelNode = new Node();
        modelNode.setEnabled(false);
        ModelRenderable
                .builder()
                .setSource(context, Uri.parse(plyPath))
                .setDataFormat(Renderable.RenderableDataFormat.PLY_SPLAT)
                .build()
                .thenApply(new Function<ModelRenderable, Object>() {
                    @Override
                    public Object apply(ModelRenderable modelRenderable) {
                        modelNode.setRenderable(modelRenderable);
                        //缩放成单位尺寸
                        Vector3 scaled = Vector3.one()
                                .scaled(ScaleTool.calculateUnitsScale(modelRenderable));
                        modelNode.setLocalScale(scaled);
                        Toast.makeText(context, "Scale: "+scaled, Toast.LENGTH_SHORT).show();

//                        modelNode.setLocalScale(Vector3.one().scaled(0.001f));
                        //当sceneView不为null时，则将在sceneView的中心作射线，在距离distance的位置加载模型
                        if (sceneView != null){
                            //这里需要短暂延时，避免width和height为0
                            sceneView.getHandler().postDelayed(()->{
                                int centerX = sceneView.getMeasuredWidth() / 2;
                                int centerY = sceneView.getMeasuredHeight() / 2;
                                Ray ray = sceneView.getScene().getCamera().screenPointToRay(centerX, centerY);
                                Vector3 point = ray.getPoint(distance);

                                modelNode.setLocalPosition(point);
                                modelNode.setEnabled(true);
                            },1000);
                        }else {
                            modelNode.setLocalPosition(new Vector3(0f, 0, -distance));
                            modelNode.setEnabled(true);
                        }
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

    public Node getModelNode() {
        return modelNode;
    }
}
