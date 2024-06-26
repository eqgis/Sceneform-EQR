package com.eqgis.eqr.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.eqgis.eqr.core.Eqr;
import com.eqgis.eqr.node.RootNode;
import com.eqgis.exception.NotSupportException;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

import java.util.List;

/**
 * 场景布局控件
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/
public class SceneLayout extends FrameLayout{
    private LifecycleListener lifecycleListener;

    protected Context context;
    public SceneView sceneView;

    //场景根节点
    private RootNode rootNode;

    //手势检测器
    private GestureDetector gestureDetector;

    //<editor-fold> 构造函数
    public SceneLayout(Context context) {
        super(context);
        init(context);
    }

    public SceneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SceneLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SceneLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    //</editor-fold>

    private void init(Context context){
        Eqr.getCoreStatus();
        this.context = context;

        //添加布局
        addLayout();

        rootNode = new RootNode();
        rootNode.setParent(sceneView.getScene());
    }

    /**
     * 添加布局
     * @return SceneView
     */
    protected void addLayout() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        sceneView = new SceneView(context);
        sceneView.setLayoutParams(layoutParams);
        this.addView(sceneView);
    }

    /**
     * 获取场景中的相机实体
     * @return
     */
    public Camera getCamera(){
        return sceneView.getScene().getCamera();
    }

    /**
     * 获取场景中的根节点
     * @return
     */
    public RootNode getRootNode(){
        return rootNode;
    }

    /**
     * 移除节点
     * @param node
     */
    public void removeNode(Node node){
        List<Node> children = node.getChildren();
        if (children.size() == 0){
            //不移除根节点
            if (node instanceof RootNode)return;

            if(node.getRenderableInstance() != null){
                //销毁node节点的图形资源占用
                node.getRenderableInstance().destroy();
            }
            node.setParent(null);
            node.setEnabled(false);
            return;
        }
        while (children.size()!=0){
            removeNode(children.get(0));
        }
    }

    /**
     * 唤醒
     */
    public void resume() {
        if (sceneView ==null){
            return;
        }

        try {
            sceneView.resume();
        } catch (Exception e) {
            Log.e(SceneLayout.class.getSimpleName(), "onResume: ", e);
        }finally {
            if (lifecycleListener != null)
                lifecycleListener.onResume();
        }
    }

    /**
     * 暂停
     */
    public void pause(){
        if (sceneView !=null) {
            sceneView.pause();
        }
        if (lifecycleListener != null)
            lifecycleListener.onPause();
    }

    /**
     * 销毁
     */
    public void destroy(){
        if (sceneView !=null) {
            //deleteNode(rootNode);
            sceneView.destroy();
        }
        if (lifecycleListener != null)
            lifecycleListener.onDestroy();
    }

    /**
     * 添加场景更新监听事件
     * @param onUpdateListener 更新监听事件
     */
    public void addSceneUpdateListener(Scene.OnUpdateListener onUpdateListener){
        if (onUpdateListener == null)return;
        sceneView.getScene().addOnUpdateListener(onUpdateListener);
    }

    /**
     * 移除场景更新监听事件
     * @param onUpdateListener
     */
    public void removeSceneUpdateListener(Scene.OnUpdateListener onUpdateListener){
        if (onUpdateListener == null)return;
        sceneView.getScene().removeOnUpdateListener(onUpdateListener);
    }

    /**
     * 设置视图是否为背景透明
     * @param transparent boolean
     */
    public void setTransparent(boolean transparent){
        if (this instanceof ARSceneLayout){
            throw new NotSupportException("The method was not supported.");
        }
        sceneView.setTransparent(transparent);
    }

    /**
     * 设置生命周期监听事件
     * @param lifecycleListener
     */
    protected void setLifecycleListener(LifecycleListener lifecycleListener){
        this.lifecycleListener = lifecycleListener;
    }

    /**
     * 获取某位置朝向相机时的元素的旋转角度
     * @param position
     * @return
     */
    public Quaternion getLookRotation(Vector3 position) {
        final Vector3 cameraPosition = sceneView.getScene().getCamera().getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, position);
        return Quaternion.lookRotation(direction, Vector3.up());
    }
}
