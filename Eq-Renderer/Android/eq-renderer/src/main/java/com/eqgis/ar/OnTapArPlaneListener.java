package com.eqgis.ar;

import android.view.MotionEvent;

import com.google.ar.sceneform.Node;

/**
 * AR平面出门监听
 **/
public interface OnTapArPlaneListener {
    /**
     *当点击AR识别到的平面时触发
     * @param hitResult 碰撞检测结果
     * @param plane AR平面
     * @param motionEvent 手势事件
     */
    void onTapPlane(ARHitResult hitResult, ARPlane plane, MotionEvent motionEvent);
}