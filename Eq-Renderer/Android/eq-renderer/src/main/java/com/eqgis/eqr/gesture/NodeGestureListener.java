package com.eqgis.eqr.gesture;


import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.eqgis.eqr.animation.ValueAnimation;
import com.eqgis.eqr.animation.ValueAnimationEvaluator;
import com.eqgis.sceneform.Camera;
import com.eqgis.sceneform.Node;
import com.eqgis.sceneform.math.Quaternion;
import com.eqgis.sceneform.math.Vector3;


/**
 * 节点比例-手势监听器
 * @author tanyx 2021/09/07
 * @version 1.1
 */

class NodeGestureListener extends GestureDetector.SimpleOnGestureListener  implements ScaleGestureDetector.OnScaleGestureListener/*, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener */ {
    //移动距离阈值,像素
    private Node target;
    private Camera camera;
    private Vector3 lastRotationAxis = new Vector3(1,1,0);
    private Vector3 scaleTmp;

    //记录初始状态，以便用于复位操作
    private Vector3 originLocalScale;
    private Vector3 originLocalPosition;
    private Quaternion originLocalRotation;

    //旋转的惯性动画
    private ValueAnimation animation;
    private ValueAnimationEvaluator animationEvaluator;

    private boolean mIsFling = false;
    private boolean isScaling = false;
    private boolean isDoubleFingerScroll = false;
    private float distance = 1.0f;

    public NodeGestureListener() {

        //惯性动画初始化
        animation = new ValueAnimation();
        animationEvaluator = new ValueAnimationEvaluator(ValueAnimationEvaluator.Type.SIN);
        animation.setOnUpdateListener(new ValueAnimation.OnUpdateListener() {
            @Override
            public void onValueUpdate(float current, boolean running) {
                if (mIsFling){
                    Quaternion quaternion = Quaternion.axisAngle(lastRotationAxis, current * 0.00005f);
                    Quaternion localRotation = target.getLocalRotation();
                    target.setLocalRotation(Quaternion.multiply(quaternion,localRotation));
                }else {
                    animation.pause();
                }
            }
        });
    }

    /**
     * 设置相机
     * @param camera
     */
    public void setCamera(Camera camera){
        this.camera = camera;
    }

    /**
     * 更新操控的节点
     * @param node
     * @param distance
     */
    public void updateValue(Node node,float distance){
        this.target = node;
        this.distance = distance;
        if (node == null){
            //清除已存入的origin信息
            this.originLocalPosition = null;
            this.originLocalRotation = null;
            this.originLocalScale = null;
        }else {
            this.originLocalPosition = target.getLocalPosition();
            this.originLocalRotation = target.getLocalRotation();
            this.originLocalScale = target.getLocalScale();
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        isScaling = true;
        if (target == null)return false;
        float scaleFactor = detector.getScaleFactor();
        target.setLocalScale(scaleTmp.scaled(scaleFactor));
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        if (target == null)return false;
        scaleTmp = target.getLocalScale();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        isScaling = false;
    }

    public boolean isFling() {
        return mIsFling;
    }

    public void setFling(boolean enabled) {
        this.mIsFling = enabled;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (target == null)return false;

        if (e2.getPointerCount() == 2) {
            onDoubleFingerScroll(e1, e2, distanceX, distanceY);
        } else if (e2.getPointerCount() == 1) {
            onOneFingerScroll(distanceX, distanceY);
        }
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        onFling(velocityX,velocityY);
        return false;
    }

    private void onOneFingerScroll(float distanceX, float distanceY) {
        if (isScaling || isDoubleFingerScroll)return;
        //单指实现旋转
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        //原理：
        // 求AB两点（当前时刻的位置为B，上一时刻的位置为A）的垂线（这里是求旋转平面的法向量），
        // （将垂线的一个方向作为正方向，这里当作旋转平面的法向量）再将这个方向向量作为旋转轴。
        // 根据绕轴旋转定义四元数。
        // 这里将求垂线 => 简化成绕点旋转90度。因为求垂线后仍然需要考虑方向,而绕B点逆时针旋转90度即可得到B2。‘AB2’则是目标向量
        //注意：安卓设备坐标系与OpenGL右手坐标系的XY方向。
//            dev.romainguy.kotlin.math.Float3(
//                x = 0 - (e1.y - e2.y)/* + e2.x*/,
//                y = -/*取负是由于NDC坐标系和GL坐标系的Y轴正方向相反*/(e1.x - e2.x)/* + e2.y*/,
//                z = 0f
//            )

        //改成使用distanceX/Y，简化后如下：
        lastRotationAxis.x = -distanceY;
        lastRotationAxis.y = -distanceX;
        lastRotationAxis.z = 0f;
        //todo 这里后续修改,为改用射线检测实现精准控制
        Quaternion rotation = Quaternion.axisAngle(lastRotationAxis, (float) Math.toRadians(distance * 0.168f));
        Quaternion localRotation = target.getLocalRotation();
        target.setLocalRotation(Quaternion.multiply(rotation,localRotation));
    }

    private void onDoubleFingerScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        isDoubleFingerScroll = true;
        //计算当前Node的世界坐标系下的空间位置。
        Vector3 screenPoint = camera.worldToScreenPoint(target.getWorldPosition());
        screenPoint.x -= distanceX;
        screenPoint.y -= distanceY;
        //计算射线，再取得固定距离的空间点坐标.作为新的空间位置
        Vector3 newPosition = camera.screenPointToRay(screenPoint.x, screenPoint.y).getPoint(/*外部传入*/distance);
        target.setWorldPosition(newPosition);
    }

    private void onFling(float xVelocity, float yVelocity) {
        if (isDoubleFingerScroll)return;
        mIsFling = true;//状态更新

        float vDistance = (float) Math.min(5000f,Math.sqrt((xVelocity * xVelocity + yVelocity * yVelocity)));
        animation.updateValue(vDistance,0f);
        //根据速度标量取值惯性效果最长时间约3秒
        animation.setDuration((long) (vDistance * 0.32f));
        animation.init(animationEvaluator);
        animation.start();
    }

    public void resetNode(){
        if (target == null)return;
        target.setLocalRotation(originLocalRotation);
        target.setLocalPosition(originLocalPosition);
        target.setLocalScale(originLocalScale);
    }

    public void resetStatus(){
        isScaling = false;
        isDoubleFingerScroll = false;
    }

    public void rayTest(float x, float y) {
        //todo 精准指控的旋转，计划通过射线检测实现
    }
}
