package com.eqgis.eqr.ar;


import java.nio.FloatBuffer;

/**
 * AR平面对象
 * @author tanyx
 */
public class ARPlane extends ARTrackable{

    /**
     * 构造函数
     * @param coreobj ARCore的平面对象
     * @param hwobj 华为AREngine的平面对象
     */
    public ARPlane(com.google.ar.core.Plane coreobj, com.huawei.hiar.ARPlane hwobj) {
        super(coreobj, hwobj);
    }

//    public FloatBuffer getPolygon() {
//        if (AREngine.isUsingARCore()){
//            return coretrackable
//        }
//    }

    /**
     * 平面类型
     */
    public static enum PlaneType {
        /**
         * 水平向上（地面，桌面）。
         */
        HORIZONTAL_UPWARD_FACING(0),

        /**
         * 水平向下（天花板）。
         */
        HORIZONTAL_DOWNWARD_FACING(1),

        /**
         * 垂直平面。
         */
        VERTICAL_FACING(2),

        /**
         * 不支持的类型。
         */
        UNKNOWN_FACING(3);

        final int mNativeCode;

        private PlaneType(int nativeCode) {
            this.mNativeCode = nativeCode;
        }

        static  PlaneType forNumber(int nativeCode) {
            PlaneType[] var1;
            int var2 = (var1 = values()).length;

            for(int var3 = 0; var3 < var2; ++var3) {
                PlaneType var4;
                if ((var4 = var1[var3]).mNativeCode == nativeCode) {
                    return var4;
                }
            }

            return UNKNOWN_FACING;
        }

        static PlaneType forARCore(com.google.ar.core.Plane.Type type){
            if (type == com.google.ar.core.Plane.Type.HORIZONTAL_UPWARD_FACING){
                return HORIZONTAL_UPWARD_FACING;
            }else if(type == com.google.ar.core.Plane.Type.HORIZONTAL_DOWNWARD_FACING){
                return HORIZONTAL_DOWNWARD_FACING;
            }else if(type == com.google.ar.core.Plane.Type.VERTICAL){
                return VERTICAL_FACING;
            }else {
                return UNKNOWN_FACING;
            }
        }

        static PlaneType forHuawei(com.huawei.hiar.ARPlane.PlaneType type){
            if (type == com.huawei.hiar.ARPlane.PlaneType.HORIZONTAL_UPWARD_FACING){
                return HORIZONTAL_UPWARD_FACING;
            }else if(type == com.huawei.hiar.ARPlane.PlaneType.HORIZONTAL_DOWNWARD_FACING){
                return HORIZONTAL_DOWNWARD_FACING;
            }else if(type == com.huawei.hiar.ARPlane.PlaneType.VERTICAL_FACING){
                return VERTICAL_FACING;
            }else {
                return UNKNOWN_FACING;
            }
        }

    }

    /**
     * 获取从平面的局部坐标系到世界坐标系转换的pose。
     * @return ARPose
     */
    public ARPose getCenterPose() {
        if (coretrackable!=null){
            com.google.ar.core.Plane coreplane = (com.google.ar.core.Plane)coretrackable;
            com.google.ar.core.Pose corepose = coreplane.getCenterPose();
            if (corepose==null)return null;
            return new ARPose(corepose,null);
        }else{
            com.huawei.hiar.ARPlane hwplane = (com.huawei.hiar.ARPlane)hwtrackable;
            com.huawei.hiar.ARPose hwpose = hwplane.getCenterPose();
            if (hwpose==null)return null;
//            return new ARPose(null,hwpose);
            return ARPose.updatePoseOnAREngine(new ARPose(null,hwpose));
        }
    }

    /**
     * 获取平面的矩形边界沿平面局部坐标系X轴的长度，如矩形的宽度。
     * @return 平面的矩形边界沿平面局部坐标系X轴的长度。
     */
    public float getExtentX() {
        if (coretrackable!=null){
            com.google.ar.core.Plane coreplane = (com.google.ar.core.Plane)coretrackable;
            return coreplane.getExtentX();
        }else{
            com.huawei.hiar.ARPlane hwplane = (com.huawei.hiar.ARPlane)hwtrackable;
            return hwplane.getExtentX();
        }
    }

    /**
     * 获取平面的矩形边界沿平面局部坐标系Z轴的长度，如矩形的高度。
     * @return 平面的矩形边界沿平面局部坐标系Z轴的长度。
     */
    public float getExtentZ() {
        if (coretrackable!=null){
            com.google.ar.core.Plane coreplane = (com.google.ar.core.Plane)coretrackable;
            return coreplane.getExtentZ();
        }else{
            com.huawei.hiar.ARPlane hwplane = (com.huawei.hiar.ARPlane)hwtrackable;
            return hwplane.getExtentZ();
        }
    }

    /**
     * 获取平面的类型
     * @return 平面类型
     */
    public  PlaneType getType() {
        if (coretrackable!=null){
            com.google.ar.core.Plane coreplane = (com.google.ar.core.Plane)coretrackable;
            return PlaneType.forARCore(coreplane.getType());
        }else{
            com.huawei.hiar.ARPlane hwplane = (com.huawei.hiar.ARPlane)hwtrackable;
            return PlaneType.forHuawei(hwplane.getType());
        }
    }

    //备注：这个方法有问题（不能通过强制转换得到plane），返回值始终为null，
    @Deprecated
    public ARPlane getSubsumedBy() {
        if (coretrackable!=null){
            com.google.ar.core.Plane coreplane = ((com.google.ar.core.Plane)coretrackable).getSubsumedBy();
            if(coreplane!=null){
                return new ARPlane(coreplane,null);
            }else{
                return null;
            }
        }else{
            com.huawei.hiar.ARPlane hwplane = ((com.huawei.hiar.ARPlane)hwtrackable).getSubsumedBy();
            if (hwplane!=null){
                return new ARPlane(null,hwplane );
            }else{
                return null;
            }
        }
    }


    /**
     * 判断传入的位姿（通过ARHitResult.getHitPose()获取）是否位于平面的多边形中。
     * @param pose ARPose
     * @return
     */
    public boolean isPoseInPolygon(ARPose pose) {
        if (coretrackable!=null){
            com.google.ar.core.Plane coreplane = (com.google.ar.core.Plane)coretrackable;
            return coreplane.isPoseInPolygon( pose.corepose );
        }else{
            com.huawei.hiar.ARPlane hwplane = (com.huawei.hiar.ARPlane)hwtrackable;
            return hwplane.isPoseInPolygon( pose.hwpose );
        }
    }

    /**
     * 判断传入的位姿（通过ARHitResult.getHitPose()获取）是否位于平面的矩形范围内，则返回true。
     * @param pose ARPose
     * @return
     */
    public boolean isPoseInExtents(ARPose pose) {
        if (coretrackable!=null){
            com.google.ar.core.Plane coreplane = (com.google.ar.core.Plane)coretrackable;
            return coreplane.isPoseInExtents( pose.corepose );
        }else{
            com.huawei.hiar.ARPlane hwplane = (com.huawei.hiar.ARPlane)hwtrackable;
            return hwplane.isPoseInExtents( pose.hwpose );
        }
    }

    /**
     * 获取检测平面的二维顶点数据
     * <p>格式为[x1, z1, x2, z2, ...]，这些值均在平面局部坐标系的x-z平面中定义，须经getCenterPose()转换到世界坐标系中。注意：在垂直平面中返回的值也是局部坐标系中的坐标[x1,z1,x2,z2,….]，需要使用getCenterPose()转换到世界坐标系。</p>
     * @return 检测平面的二维顶点数组。
     */
    public FloatBuffer getPlanePolygon() {
        if (coretrackable!=null){
            com.google.ar.core.Plane coreplane = (com.google.ar.core.Plane)coretrackable;
            return coreplane.getPolygon();
        }else{
            com.huawei.hiar.ARPlane hwplane = (com.huawei.hiar.ARPlane)hwtrackable;
            return hwplane.getPlanePolygon();
        }
    }
}
