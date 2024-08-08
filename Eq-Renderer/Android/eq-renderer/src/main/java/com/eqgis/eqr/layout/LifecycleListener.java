package com.eqgis.eqr.layout;

/**
 *  生命周期监听事件
 * @author tanyx 2023/12/12
 * @version 1.0
 **/
public interface LifecycleListener {
    void onResume();
    void onPause();
    void onDestroy();
    void onSceneInitComplete();
}
