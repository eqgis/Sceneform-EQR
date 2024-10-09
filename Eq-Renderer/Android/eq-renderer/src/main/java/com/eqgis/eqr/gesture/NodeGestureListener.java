package com.eqgis.eqr.gesture;


import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.eqgis.eqr.animation.ValueAnimation;
import com.eqgis.eqr.animation.ValueAnimationEvaluator;
import com.google.sceneform.Camera;
import com.google.sceneform.HitTestResult;
import com.google.sceneform.Node;
import com.google.sceneform.collision.Ray;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;


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
    private float lastRotationAngle = 0f;
    private float ROTATION_FACTOR = 5.0f;

    public NodeGestureListener() {

        //惯性动画初始化
        animation = new ValueAnimation();
        animationEvaluator = new ValueAnimationEvaluator(ValueAnimationEvaluator.Type.SIN);
        animation.setOnUpdateListener(new ValueAnimation.OnUpdateListener() {
            @Override
            public void onValueUpdate(float current, boolean running) {
                if (mIsFling){
                    Quaternion quaternion = Quaternion.axisAngle(lastRotationAxis, current);
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
//            float deltaY = e2.getY() - e1.getY();
//            float deltaX = e2.getX() - e1.getX();

//            Log.i("IKKYU---", "onScroll: e1:"+e1.getX()+" e2:"+e2.getX() +  " deltaX:"+deltaX + "  deltaY:"+deltaY + "  disX:"+distanceX + "  disY:"+distanceY);
            onOneFingerScroll(e2,distanceX, distanceY);
        }
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        onFling(velocityX,velocityY);
        return false;
    }

    /**
     * 根据distance实现旋转操作
     * @param distanceX
     * @param distanceY
     */
    @Deprecated
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

    /**
     * 单指旋转
     */
    private void onOneFingerScroll(MotionEvent currentEvent,float distanceX, float distanceY) {
        if (isScaling || isDoubleFingerScroll)return;
        //单指实现旋转(计算旋转轴和旋转角度)
        //原理：
        //上一触摸点转为空间坐标点A，当前触摸点转为空间触摸点B。记当前场景相机的位置为点O
        //那么向量OA与向量OB的叉积则是旋转轴

        //转为空间坐标（屏幕坐标->射线->固定距离的点）
        Vector3 pointA = camera.screenPointToRay(currentEvent.getX() - distanceX,
                currentEvent.getY() - distanceY).getPoint(/*外部传入*/distance);

        Vector3 pointB = camera.screenPointToRay(currentEvent.getX(),currentEvent.getY()).getPoint(distance);
        Vector3 pointO = camera.getWorldPosition();
        Vector3 oa = Vector3.subtract(pointA, pointO);
        Vector3 ob = Vector3.subtract(pointB, pointO);
        float c = Vector3.subtract(pointA, pointB).length();
        float a = oa.length();
        float b = ob.length();
        float cosC = (a * a + b * b - c * c) / (2 * a * b);
        //旋转轴
        Vector3 cross = Vector3.cross(oa, ob).normalized();

        //改成使用distanceX/Y，简化后如下：
        lastRotationAxis.x = cross.x;
        lastRotationAxis.y = cross.y;
        lastRotationAxis.z = cross.z;
        lastRotationAngle = (float) Math.toDegrees(Math.acos(cosC)) * ROTATION_FACTOR/*这里为一个经验系数*/;
        Quaternion rotation = Quaternion.axisAngle(lastRotationAxis, lastRotationAngle);
        Quaternion localRotation = target.getLocalRotation();
//        Log.i("IKKYU", "onOneFingerScroll: " + lastRotationAxis + "  q:"+Quaternion.multiply(rotation,localRotation));
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
        animation.updateValue(lastRotationAngle,0f);
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

    void rayTest(MotionEvent event) {
        //测试方法
        HitTestResult hitTestResult = target.getScene().hitTest(event);
        if (hitTestResult != null){
            Vector3 point = hitTestResult.getPoint();
            if (point.length() > 0.0001f){
                Log.i("IKKYU-GESTURE", "rayTest: ikkyu hitpoint >>>> " + point.toString());
            }
        }

        Vector3 worldPosition = target.getScene().getCamera().getWorldPosition();
        Quaternion worldRotation = target.getScene().getCamera().getWorldRotation();
        Log.i("IKKYU", "rayTest: CAMERA: "+worldRotation);
    }
}
