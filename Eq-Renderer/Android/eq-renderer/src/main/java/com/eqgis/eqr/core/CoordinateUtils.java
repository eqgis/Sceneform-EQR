package com.eqgis.eqr.core;


import com.eqgis.eqr.Location;
import com.google.ar.sceneform.math.Vector3;

/**
 * 坐标工具
 */
class CoordinateUtils {

    /**
     * 计算地理坐标的夹角
     * @param a 顶点A
     * @param o 顶点O
     * @param c 顶点C
     * @return 角度
     */
    public static double computeGeoAngle(Location a,Location o,Location c){
        return CoordinateUtilsNative.computeGeoAngle(a,o,c);
    }

    /**
     * 将目标的场景坐标转换为地理坐标
     * <p>CGCS-2000坐标系</p>
     * @param refPoint 参考点地理位置，在AR的应用场景中，通常采用场景相机启动时的位置
     * @param targetPoint 目标点在场景中的坐标位置
     * @param azimuth 在AR的应用场景中，通常采用场景相机启动时的方位角，单位：度
     * @return 目标点在场景中的地理位置
     */
    public static Location toGeoLocation(Location refPoint, Vector3 targetPoint, double azimuth){
        //注意：东北天坐标系 和 OpenGL右手系（X轴正方向指向右侧，y轴正方向指向上方）
        double[] xy = CoordinateUtilsNative.jni_ToGeoLocation(refPoint.getX(), refPoint.getY(),
                /*东西方向*/targetPoint.x, /*前后方向*/-targetPoint.z,
                /*方位角的弧度值*/Math.toRadians(azimuth));
        double z = /*上下方向*/targetPoint.y - refPoint.getZ();
        return new Location(xy[0], xy[1], z);
    }

    /**
     * 将目标的地理位置转换为场景坐标
     * <p>CGCS-2000坐标系</p>
     * @param refPoint
     * @param targetLocation
     * @param azimuth
     * @return
     */
    public static Vector3 toScenePosition(Location refPoint, Location targetLocation, double azimuth){
        //计算在东北天方向的xy值
        double[] xyInENU = CoordinateUtilsNative.jni_ToScenePosition(refPoint.getX(), refPoint.getY(),
                targetLocation.getX(), targetLocation.getY(),
                Math.toRadians(azimuth));
        Vector3 position = new Vector3((float) xyInENU[0],/*高度差*/(float) (targetLocation.getZ() - refPoint.getZ()), (float) -xyInENU[1]);
        return position;
    }

}
