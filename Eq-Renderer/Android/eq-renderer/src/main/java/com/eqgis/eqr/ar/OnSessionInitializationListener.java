package com.eqgis.eqr.ar;

/**
 * ARSession对象初始化监听事件
 **/
public interface OnSessionInitializationListener {
    /**
     * 在ARSession执行resume成功后调用
     *@param session The AR Session.
     */
    void onSessionInitialization(ARSession session);
}
