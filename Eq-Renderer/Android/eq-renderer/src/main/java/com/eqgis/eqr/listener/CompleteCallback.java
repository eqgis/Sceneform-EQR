package com.eqgis.eqr.listener;

/**
 * 完成情况的回调事件
 * @author tanyx
 **/
public interface CompleteCallback {

    /**
     * 成功回调
     * @param object 返回对象
     */
    void onSuccess(Object object);

    /**
     * 失败回调
     * @param errorMessage 错误信息
     */
    void onFailed(String errorMessage);
}
