package com.eqgis.eqr.layout;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;

import com.eqgis.eqr.ar.ARFrame;
import com.eqgis.eqr.ar.OnSessionInitializationListener;
import com.eqgis.eqr.ar.OnTapArPlaneListener;
import com.eqgis.eqr.ar.TrackingState;
import com.eqgis.sceneform.ArSceneView;
import com.eqgis.eqr.ar.ARConfig;
import com.eqgis.eqr.ar.ARPlugin;
import com.eqgis.eqr.ar.ARSession;
import com.eqgis.eqr.ar.exceptions.ARCameraException;
import com.eqgis.eqr.ar.exceptions.ARSessionException;

/**
 * AR场景布局控件
 * @author tanyx 2023/6/15
 * @version 1.0
 * </code>
 **/
public class ARSceneLayout extends SceneLayout{

    private ArSceneView arSceneView;
    private OnSessionInitializationListener onSessionInitializationListener;
    private ARSession session;
    private ARConfig arConfig;
    private ARSceneHelper arSceneHelper;

    //<editor-fold> 构造函数
    public ARSceneLayout(Context context) {
        super(context);
    }

    public ARSceneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ARSceneLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ARSceneLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    //</editor-fold>

    /**
     * 设置平面点击监听事件
     * @param onTapPlaneListener 平面点击监听事件
     */
    public void setOnTapPlaneListener(OnTapArPlaneListener onTapPlaneListener) {
        arSceneHelper.updateListener(onTapPlaneListener);
    }

    /**
     * 设置session初始化成功的监听事件
     * @param onSessionInitializationListener
     */
    public void setOnSessionInitializationListener(OnSessionInitializationListener onSessionInitializationListener) {
        this.onSessionInitializationListener = onSessionInitializationListener;
    }

    /**
     * 获取AR 会话
     * @return
     */
    public ARSession getSession() {
        return session;
    }

    /**
     * 获取AR 配置
     * @return
     */
    public ARConfig getArConfig() {
        return arConfig;
    }

    @Override
    public void resume() {
        if (arSceneView == null){
            return;
        }
        if (!(ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)){
            SceneLayoutUtils.displayError(context,"Unable to get permission of camera.",null);
        }
        if (arSceneView.getSession() == null) {
            try {
                Object[] objects = SceneLayoutUtils.createArSession((Activity) context, ARConfig.PlaneFindingMode.HORIZONTAL_ONLY);
                session = (ARSession) objects[0];
                arConfig = (ARConfig) objects[1];

                arSceneView.setupSession(session);
                if (this.onSessionInitializationListener != null) {
                    this.onSessionInitializationListener.onSessionInitialization(session);
                }
            } catch (ARSessionException e) {
                String text;
                if (ARPlugin.isHuawei()){
                    text = "Please update AREngine";
                }else {
                    text = "Please update ARCore";
                }
                SceneLayoutUtils.displayError(context,text,e);
            }
        }

        try {
            arSceneView.resume();
        } catch (ARCameraException e) {
            SceneLayoutUtils.displayError(context,"Unable to get camera",e);
        }
    }

    @Override
    public void pause() {
        if (session != null){
            session.pause();
        }
        super.pause();
    }

    @Override
    public void destroy() {
        if (arSceneHelper != null){
            arSceneView.getPlaneRenderer().setEnabled(false);
            arSceneHelper.destroy();
        }
        if (session != null){
            session.close();
            session = null;
        }
        super.destroy();
    }

    @Override
    protected void addLayout() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        arSceneView = new ArSceneView(context);
        arSceneView.setLayoutParams(layoutParams);
        this.addView(arSceneView);

        arSceneHelper = new ARSceneHelper(arSceneView)
                .addPlaneRenderer()
                .addPlaneTapDetector();

        sceneView = arSceneView;
    }

    /**
     * 获取AR帧
     * @return
     */
    public ARFrame getArFrame() {
        return arSceneView.getArFrame();
    }

    /**
     * 启动平面渲染
     * @param enabled
     */
    public void setPlaneRendererEnabled(boolean enabled){
        arSceneView.getPlaneRenderer().setEnabled(enabled);
    }

    /**
     * 判断是否启用平面渲染
     * @return
     */
    public boolean isPlaneRendererEnabled(){
        return arSceneView.getPlaneRenderer().isEnabled();
    }

    /**
     * 获取当前时刻的跟踪状态
     * @return 跟踪状态
     */
    public TrackingState getTrackingState(){
        ARFrame arFrame = arSceneView.getArFrame();
        if (arFrame == null){
            return TrackingState.UNKNOWN_STATE;
        }

        return arFrame.getCamera().getTrackingState();
    }
}
