package com.eqgis.eqr.gesture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;

import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;

/**
 * 节点手势控制器
 * @author tanyx 2024/7/25
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/
public class NodeGestureController {
    @SuppressLint("StaticFieldLeak")
    private static volatile NodeGestureController mInstance = null;

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private NodeGestureListener nodeGestureListener;
    private Context mContext;

    private boolean enabled = false;

    private boolean mIsInit = false;

    private NodeGestureController() {
        nodeGestureListener = new NodeGestureListener();
    }

    /**
     * 获取实例
     * @return this
     */
    public static NodeGestureController getInstance(){
        if (mInstance == null){
            synchronized (NodeGestureController.class){
                mInstance = new NodeGestureController();
            }
        }
        return mInstance;
    }

    /**
     * 参数初始化
     * @param context 上下文
     * @return this
     */
    public NodeGestureController init(Context context){
        this.mContext = context;
        scaleGestureDetector = new ScaleGestureDetector(mContext,nodeGestureListener);
        gestureDetector = new GestureDetector(mContext,nodeGestureListener);

        mIsInit = true;
        return mInstance;
    }

    /**
     * 设置场景相机
     * @param camera 场景相机
     */
    public NodeGestureController setCamera(Camera camera){
        nodeGestureListener.setCamera(camera);
        return this;
    }

    /**
     * 选择操作节点
     * @param node 节点
     * @param distance 节点距离相机的距离
     */
    public void select(Node node,float distance){
        synchronized (NodeGestureController.class){
            nodeGestureListener.updateValue(node,distance);
        }
    }

    /**
     * 取消已选中的节点
     */
    public void unSelect(){
        synchronized (NodeGestureController.class){
            nodeGestureListener.updateValue(null,1.0f);
        }
    }

    /**
     * 判断是否已启用
     * @return 状态值
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置启用状态
     * @param enabled 启用状态
     */
    public NodeGestureController setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * 设置滑动状态
     * @param flag 状态
     */
    public NodeGestureController setFling(boolean flag) {
        this.nodeGestureListener.setFling(flag);
        return this;
    }

    /**
     * 触摸事件
     * @param event 手势事件
     * @return 状态值，若手势控制器未启用，直接返回false
     */
    public boolean onTouch(MotionEvent event){
        if (!enabled)return true;
        if (!mIsInit)throw new IllegalStateException("uninitialized");

        if (event.getPointerCount() == 2){
            scaleGestureDetector.onTouchEvent(event);
        }

        nodeGestureListener.rayTest(event.getX(),event.getY());
        gestureDetector.onTouchEvent(event);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                nodeGestureListener.setFling(false);
                //三指点击复位
                if (event.getPointerCount() == 3){
                    resetNode();
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                nodeGestureListener.resetStatus();//重置状态
                break;
        }

        return true;
    }

    /**
     * 重置节点状态
     */
    public void resetNode(){
        nodeGestureListener.resetNode();
    }
}
