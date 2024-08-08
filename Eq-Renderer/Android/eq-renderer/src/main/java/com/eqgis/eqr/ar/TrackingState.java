package com.eqgis.eqr.ar;

/**
 * 跟踪状态
 */
public enum TrackingState {

    /**
     * 未知状态
     */
    UNKNOWN_STATE(-1),

    /**
     * 跟踪中
     */
    TRACKING(0),

    /**
     * 已暂停
     */
    PAUSED(1),

    /**
     * 已停止
     */
    STOPPED(2);

    final int mStateCode;

    private TrackingState(int stateCode) {
        this.mStateCode = stateCode;
    }

    static TrackingState forNumber(int stateCode) {
        TrackingState[] var1;
        int var2 = (var1 = values()).length;

        for(int var3 = 0; var3 < var2; ++var3) {
            TrackingState var4;
            if ((var4 = var1[var3]).mStateCode == stateCode) {
                return var4;
            }
        }

        return UNKNOWN_STATE;
    }

    static TrackingState fromHuawei(com.huawei.hiar.ARTrackable.TrackingState state){
        if(state==com.huawei.hiar.ARTrackable.TrackingState.TRACKING){
            return  TRACKING;
        }else if(state==com.huawei.hiar.ARTrackable.TrackingState.PAUSED){
            return PAUSED;
        }else if(state==com.huawei.hiar.ARTrackable.TrackingState.STOPPED){
            return STOPPED;
        }
        return UNKNOWN_STATE;
    }

    static TrackingState fromARCore(com.google.ar.core.TrackingState state){
        if(state==com.google.ar.core.TrackingState.TRACKING){
            return  TRACKING;
        }else if(state==com.google.ar.core.TrackingState.PAUSED){
            return PAUSED;
        }else if(state==com.google.ar.core.TrackingState.STOPPED){
            return STOPPED;
        }
        return UNKNOWN_STATE;
    }
}
