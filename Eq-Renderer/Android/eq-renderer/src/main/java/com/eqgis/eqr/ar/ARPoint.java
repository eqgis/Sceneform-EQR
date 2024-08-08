package com.eqgis.eqr.ar;


import com.google.ar.core.Point;

/**
 * AR正在跟踪的点对象
 * @author tanyx
 */
public class ARPoint extends ARTrackable{
    ARPoint(Point coreobj, com.huawei.hiar.ARPoint hwobj) {
        super(coreobj, hwobj);
    }

    /**
     * 返回当前的朝向模式。
     * @return 当前的朝向模式。
     */
    public  OrientationMode getOrientationMode() {
        if (coretrackable!=null){
            Point corePoint = (Point)coretrackable;
            return OrientationMode.forARCore(corePoint.getOrientationMode());
        }else{
            com.huawei.hiar.ARPoint hwPoint = (com.huawei.hiar.ARPoint)hwtrackable;
            return OrientationMode.forHuawei(hwPoint.getOrientationMode());
        }

    }


    /**
     * 朝向模式
     */
    public static enum OrientationMode {
        UNKNOWN_MODE(-1),

        /**
         * 与世界坐标系的朝向一致，但会稍作调整。
         */
        INITIALIZED_TO_IDENTITY(0),

        /**
         * 朝向由估计的平面法向量决定，具体描述请参见getHitPose()。
         */
        ESTIMATED_SURFACE_NORMAL(1);

        private final int nativeCode;

        private OrientationMode(int code) {
            this.nativeCode = code;
        }

        static  OrientationMode forNumber(int nativeCode) {
             OrientationMode[] var1;
            int var2 = (var1 = values()).length;

            for(int var3 = 0; var3 < var2; ++var3) {
                 OrientationMode var4;
                if ((var4 = var1[var3]).nativeCode == nativeCode) {
                    return var4;
                }
            }

            return UNKNOWN_MODE;
        }

        static  OrientationMode forARCore(Point.OrientationMode mode){
            if (mode==Point.OrientationMode.INITIALIZED_TO_IDENTITY){
                return INITIALIZED_TO_IDENTITY;
            }else if(mode==Point.OrientationMode.ESTIMATED_SURFACE_NORMAL){
                return ESTIMATED_SURFACE_NORMAL;
            }else{
                return UNKNOWN_MODE;
            }
        }

        static  OrientationMode forHuawei(com.huawei.hiar.ARPoint.OrientationMode mode){
            if (mode==com.huawei.hiar.ARPoint.OrientationMode.INITIALIZED_TO_IDENTITY){
                return INITIALIZED_TO_IDENTITY;
            }else if(mode==com.huawei.hiar.ARPoint.OrientationMode.ESTIMATED_SURFACE_NORMAL){
                return ESTIMATED_SURFACE_NORMAL;
            }else{
                return UNKNOWN_MODE;
            }
        }

    }
}
