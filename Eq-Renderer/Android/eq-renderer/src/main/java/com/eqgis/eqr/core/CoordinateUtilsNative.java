package com.eqgis.eqr.core;

import androidx.annotation.Keep;

import com.eqgis.eqr.Location;

/**
 * 坐标转换工具类
 * @author tanyx 2023/6/19
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/
@Keep
class CoordinateUtilsNative {
    /**
     * 场景坐标转地理坐标
     * @param refX 参考location
     * @param refY 参考location
     * @param targetX 目标position
     * @param targetY 目标position
     * @param azimuthRad 方位角，弧度值
     * @return {lon,lat}
     */
    public static native double[] jni_ToGeoLocation(double refX, double refY,
                                                    double targetX, double targetY,
                                                    double azimuthRad);


    /**
     *
     * @param refX 参考点的地理坐标
     * @param refY 参考点的地理坐标
     * @param targetLocationX 目标点的地理坐标
     * @param targetLocationY 目标点的地理坐标
     * @param azimuthRad 方位角
     * @return {relativeX,relativeY}
     */
    public static native double[] jni_ToScenePosition(double refX, double refY,
                                                      double targetLocationX, double targetLocationY,
                                                      double azimuthRad);

    /**
     * 计算三个点依次序构成的两条线段间的顺时针角度
     * @param p1    第一个点
     * @param p2    第二个点，即角的顶点
     * @param p3    第三个点
     * @return   返回夹角大小，单位为度
     */
    public static double computeGeoAngle(Location p1, Location p2, Location p3){
        double angle = 0;
        if (p1 != null && p2 != null && p3 != null) {
//			angle = ToolkitNative.jni_MeasureAngle(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());

            double DTOR = 0.0174532925199432957692369077;

            double Ax = p2.getX() - p1.getX();
            double Ay = p2.getY() - p1.getY();
            double Bx = p3.getX() - p2.getX();
            double By = p3.getY() - p2.getY();

            double angeleCos = (Ax * Bx + Ay * By) / (Math.sqrt((Ax * Ax + Ay * Ay)) * Math.sqrt((Bx * Bx + By * By)));
            angle = Math.acos(angeleCos) * 1 / DTOR;

            // 判断两条线的位置关系
            double df = (p3.getX() - p1.getX()) * (p2.getY() - p1.getY())
                    - (p3.getY() - p1.getY()) * (p2.getX() - p1.getX());
            if (df > 0) // 右边
                angle = 180 + angle;
            else if (df < 0) // 左边
                angle = 180 - angle;
            else// 三个点共线
            {
                if (!((p2.getX() == p3.getX()) && (p1.getX() == p3.getX()))) {
                    if (p1.getX() > p2.getX())
                        angle = p3.getX() < p2.getX() ? 180 : 0;
                    else
                        angle = p3.getX() > p2.getX() ? 180 : 0;
                } else// 垂直情况
                {
                    if (p1.getY() > p2.getY())
                        angle = p3.getY() < p2.getY() ? 180 : 0;
                    else
                        angle = p3.getY() > p2.getY() ? 180 : 0;
                }
            }
        }
        return angle;
    }

}
