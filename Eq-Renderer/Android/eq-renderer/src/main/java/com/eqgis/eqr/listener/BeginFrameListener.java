package com.eqgis.eqr.listener;

/**
 * 帧绘制之前的监听事件
 **/

public interface BeginFrameListener{
    void onBeginFrame(long frameTimeNanos);
}