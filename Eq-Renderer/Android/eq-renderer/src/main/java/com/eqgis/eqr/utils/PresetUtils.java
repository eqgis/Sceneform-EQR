package com.eqgis.eqr.utils;


import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * 预置的工具类
 *      主要方法：
 *          在两点间（或在一个点集中）按间距插入点，返回点集
 * @author Tanyunxiu
 * @date 2020/12/08
 */
public class PresetUtils {
    /**
     * 获取两点间距离
     * @param a
     * @param b
     * @return
     */
    public static float getDistance(Vector3 a, Vector3 b){
        return (float)Math.sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y) + (b.z - a.z) * (b.z - a.z));
    }
    /**
     * 判断src点是否在AB线段上
     * @param src 源点
     * @param a 线段端点A
     * @param b 线段端点B
     * @return boolean
     */
    public static boolean isInLineSegment(Vector3 src,Vector3 a,Vector3 b){
        float ab = getDistance(a, b);
        float oa = getDistance(src,a) + getDistance(src,b);
        return !(Math.abs(oa - ab) > 0.000001f);
    }

    /**
     * 判断平面上任意点，是否在空间某一平面上的多边形内
     * @param src
     * @param points 首尾相连的点集
     * @return
     */
    public static boolean isInPolygon(Point src, List<Point> points){
        if (points.size() < 4){
            return false;
        }
        return PolygonUtils.isInPolygon3(src,points);
    }

    /**
     * 判断空间中任意点，是否在空间某一平面上的多边形内
     * <p>已过时，使用{@link PolygonUtils#isInPolygon_XOY(Vector3, List)}替代</p>
     * @param src
     * @param points 首尾相连的点集
     * @param tolerance 容限
     * @return
     */
    @Deprecated
    public static boolean isInPolygon(Vector3 src, List<Vector3> points,float tolerance){
        if (points.size() < 4){
            return false;
        }
        if (tolerance <= 0){
            tolerance = 0.000001f;
        }
        return PolygonUtils.isInPolygon2(src,points,tolerance);
    }

    /**
     * 根据折线点集生成平行与XOY面的可构成平滑宽线多边形的点集
     * @param points
     * @param lineWidth
     * @param radius
     * @param edgeNum
     * @return
     */
    public static List<Vector3> genStripeLinePoints(List<Vector3> points,float lineWidth,float radius, int edgeNum){
//        if (radius == 0f){
//            //不使用圆角，备注，此方法会留有豁口
//            List<Point3D> resultList = new ArrayList<Point3D>();
//            List<Point3D> temp = genVerticalPoints(points.get(0), points.get(1), lineWidth);
//            resultList.add(temp.get(0));
//            resultList.add(temp.get(3));
//
//            for (int i = 0; i < points.size() - 1; i++) {
//                temp = genVerticalPoints(points.get(i), points.get(i + 1), lineWidth);
//                resultList.add(temp.get(1));
//                resultList.add(temp.get(2));
//            }
//            return resultList;
//        }

        if (radius < lineWidth){
            //圆角半径
            radius = lineWidth;
        }
        if (edgeNum < 2){
            //默认值
            edgeNum = 2;
        }
        return PolygonUtils.genStripe_TriPoints(points,lineWidth,radius,edgeNum);
    }


    /**
     * 在平行XOY的平面上，获取垂直AB，且过端点AB，且距离为width的点集（AB不重合，共4个点）
     *      3 --------- 2
     *      \a---------b\
     *      0 --------- 1
     * @param pointA
     * @param pointB
     * @param width
     * @return
     */
    public static List<Vector3> genVerticalPoints(Vector3 pointA, Vector3 pointB, float width) {
        return PolygonUtils.getVerticalPoints(pointA,pointB,width);
    }

    /**
     * 根据圆角的半径和圆弧段数，在拐点处将拐点替换为弧段上的连续点
     * @param points
     * @param radius
     * @param edgeNum
     * @return
     */
    public static List<Vector3> genArcPoints(List<Vector3> points,float radius, int edgeNum){
        if (points.size() < 3){
            return points;
        }

        if (edgeNum < 3){
            //默认值
            edgeNum = 36;
        }

        return PolygonUtils.genArcPoints(points,radius,edgeNum);
    }

    /**
     * 从点集中获取中心点的位置
     * @param sourceList
     * @return
     */
    public static Vector3 getCenterPoint(List<Vector3> sourceList){
        if (sourceList == null || sourceList.size() == 0){
            return null;
        }
        float sumX = 0f;
        float sumY = 0f;
        float sumZ = 0f;
        for (Vector3 p:sourceList) {
            sumX += p.x;
            sumY += p.y;
            sumZ += p.z;
        }
        return new Vector3(sumX / sourceList.size(),sumY / sourceList.size(),sumZ / sourceList.size());
    }

    /**
     * 获取单位向量V0旋转为单位向量V1的旋转四元数
     * @param v0
     * @param v1
     * @return
     */
    public static Quaternion getQuaternionByVector(Vector3 v0,Vector3 v1){
        try{
            return PolygonUtils.getQuaternionBy2Vector(v0,v1);
        }catch (Exception e){
            throw new RuntimeException("Parameter was wrong.");
        }
    }

    /**
     * 获取单位向量V0旋转为单位向量V1的旋转四元数
     * @param v0
     * @param v1
     * @return
     */
    public static Quaternion getQuaternionByVector3(Vector3 v0,Vector3 v1){
        try{
            return PolygonUtils.getQuaternionBy2Vector(v0,v1);
        }catch (Exception e){
            throw new RuntimeException("Parameter was wrong.");
        }
    }

    /**
     * 获取不共线三点构成的平面相对水平面的旋转四元数
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static Quaternion getQuaternionByPoint3Ds(Vector3 a,Vector3 b,Vector3 c){
        try{
            return PolygonUtils.getQuaternionByPoints(a,b,c);
        }catch (Exception e){
            throw new RuntimeException("Parameter was wrong.");
        }
    }

    /**
     * 纠正碰撞点位置
     * @param cameraPosition 相机位置
     * @param currentHitPoint 源碰撞点
     * @param list  碰撞对象的顶点（大于3个且不共线）
     * @return
     */
    public static Vector3 correctHitPoint(Vector3 cameraPosition, Vector3 currentHitPoint, List<Vector3> list){
        try {
            Vector3 intersectionPoint = PolygonUtils.getIntersectionPoint(cameraPosition,
                    currentHitPoint,
                    list.get(0),
                    list.get(1),
                    list.get(2));
            return intersectionPoint;
        }catch (NullPointerException e){
//            throw new NullPointerException("Parameter was null.");
            Log.e("PresetUtils", "correctHitPoint: Parameter was null.");
            return currentHitPoint;
        }catch (IndexOutOfBoundsException e){
//            throw new IndexOutOfBoundsException("The size of parameter[List<Point3D>] was wrong.");
            Log.e("PresetUtils", "correctHitPoint: The size of parameter[List<Point3D>] was wrong.");
            return currentHitPoint;
        }
    }

    /**
     * 获取通过屏幕坐标生成的射线与list点集构成的平面的交点，返回值可为null
     * 注意：当碰撞点，在朝向的后方也返回null
     * @param camera 场景相机
     * @param list  碰撞对象的顶点（大于3个且不共线）
     * @param x 屏幕x坐标值
     * @param y 屏幕y坐标值
     * @return
     */
    public static Vector3 getPointByScreenRayTest(Camera camera, List<Vector3> list, float x, float y){
        Vector3 cameraPosition = camera.getWorldPosition();

        //获取当前屏幕点发射的射线1米处的位置
        Vector3 tmpPoint = camera
                .screenPointToRay(x, y)
                .getPoint(1.0f);

        Vector3 hitPoint = PresetUtils.correctHitPoint(cameraPosition,tmpPoint,list);

        if (!checkDirection(cameraPosition,tmpPoint,hitPoint)){
            //判断朝向
            return null;
        }

        try {
            Vector3 intersectionPoint = PolygonUtils.getIntersectionPoint(cameraPosition,
                    hitPoint,
                    list.get(0),
                    list.get(1),
                    list.get(2));
            return intersectionPoint;
        }catch (NullPointerException e){
            Log.e("PresetUtils", "correctHitPoint: Parameter was null.");
            return null;
        }catch (IndexOutOfBoundsException e){
            Log.e("PresetUtils", "correctHitPoint: The size of parameter[List<Point3D>] was wrong.");
            return null;
        }
    }

    /**
     * 判断方向
     * @param cameraPosition
     * @param tmpPoint
     * @param hitPoint
     * @return
     */
    private static boolean checkDirection(Vector3 cameraPosition, Vector3 tmpPoint, Vector3 hitPoint) {
        Vector3 oT = new Vector3(tmpPoint.x - cameraPosition.x, tmpPoint.y - cameraPosition.y,tmpPoint.z - cameraPosition.z);
        Vector3 oH = new Vector3(hitPoint.x - cameraPosition.x, hitPoint.y - cameraPosition.y, hitPoint.z - cameraPosition.z);
        if (oT.x * oH.x + oT.y * oH.y + oT.z * oH.z > 0){
            //OT与OH同向
            return true;
        }else {
            return false;
        }
    }

    /**
     * 获取当前相机与相机启动时的夹角
     * <p>
     *     初始朝向为0值，顺时针为正
     * </p>
     * @param camera
     * @return
     */
    public static float getCurrentCameraAzimuth(Camera camera){
        //相机
        //check 使用实时相对相机的方位角 更新时间：2021年12月7日12:52:10
        Vector3 forward = camera.getForward();
        return (float) (Math.toDegrees(Math.atan2(forward.x, forward.z))) + 180.0f;
    }

    /**
     * 过滤前后相同的点
     * @param list
     * @return
     */
    public static List<Vector3> getNonRepeatPoints(List<Vector3> list){
        int size = list.size();
        if (size < 3){
            return null;
        }
        List<Vector3> resultList = new ArrayList<Vector3>();
        Vector3 currentPoint;
        Vector3 nextPoint;
        for (int i = 0; i < size - 1; i++) {
            currentPoint = list.get(i);
            nextPoint = list.get(i + 1);

            if (nextPoint.x == currentPoint.x
                    && nextPoint.y == currentPoint.y
                    && nextPoint.z == nextPoint.z){
                //..滤掉的内容
            }else {
                resultList.add(currentPoint);
            }
        }

        return resultList;
    }

//    /**
//     * 在两点间根据间距生成对应点集
//     * @param a
//     * @param b
//     * @param spacingDistance
//     * @return
//     */
//    public static List<Vector3> genNewPointsBySpacingDistance(Vector3 a,Vector3 b, float spacingDistance) {
//        List<Vector3> points = new ArrayList<Vector3>();
//        points.add(a);
//        points.add(b);
//        return updatePointList(points, spacingDistance);
//    }
//    /**
//     * 根据固定的间隔距离spacingDistance和原有点集，生成新点集合
//     * @param points
//     * @param spacingDistance
//     * @return
//     */
//    public static List<Vector3> genNewPointsBySpacingDistance(List<Vector3> points, float spacingDistance) {
//        List<Vector3> resultList = new ArrayList<Vector3>();
//        int size_1 = points.size() - 1;
//        for (int i = 0; i < size_1; i++) {
//            //根据两点拆分，间距spacingDistance
//            resultList.addAll(genNewPointsBySpacingDistance(points.get(i), points.get(i + 1), spacingDistance));
//        }
//        resultList.add(points.get(size_1));
//        return resultList;
//    }

    /**
     * 判断点集是否绕顺时针旋转
     * @param point3DS 点集
     * @param status true时，表示首尾相连的点集
     * @return true时，为顺时针
     */
    public static boolean isClockwiseAroundZ(List<Vector3> point3DS,boolean status){
        //modified by tanyx 2022年10月11日，重新构造一个对象，防止原始对象的个数发生变化
        ArrayList<Vector3> pointList = new ArrayList<Vector3>();
        pointList.addAll(point3DS);
        //判断XOY平面上的点集，是否是顺时针构成 added by tanyx 2022年3月17日10:01:58
        if (status){
            if (pointList.size() < 4){
                throw new IllegalArgumentException("size less than 4.");
            }
            pointList.remove(pointList.size() -1);
        }else {
            if (pointList.size() < 3){
                throw new IllegalArgumentException("size less than 3.");
            }
        }

        //原理：找到凸点，通过凸点前一个点，凸点，凸点后一个点构成的三角形，判断有向面积的正负
        int index = 0;
        J:
        for (int i = 0; i < pointList.size(); i++) {
            //判断凸点
            Vector3 point3D = pointList.get(i);
            boolean concavePoint = PolygonUtils.isConcavePoint(point3D, pointList);
            if (/*不为凹点*/!concavePoint){
                index = i;
                break J;
            }
        }
        Vector3 a;
        Vector3 b;
        Vector3 c;
        if (index == 0){
            a = pointList.get(pointList.size() - 1);
            b = pointList.get(0);
            c = pointList.get(1);
        }else if (index == pointList.size() - 1){
            a = pointList.get(pointList.size() - 2);
            b = pointList.get(pointList.size() - 1);
            c = pointList.get(0);
        }else {
            a = pointList.get(index - 1);
            b = pointList.get(index);
            c = pointList.get(index + 1);
        }
        return PolygonUtils.isClockwise_Z(a, b, c);
    }

//    /**
//     * 更新点集
//     * @param points
//     * @param spacingDistance
//     * @return
//     */
//    private static List<Vector3> updatePointList(List<Vector3> points, float spacingDistance) {
//        int size_1 = points.size() - 1;
//        for (int i = 0; i < size_1; i++) {
//            //根据两点拆分，间距spacingDistance
//            points = insertPointsByTwoPoints(points, points.get(i), points.get(i + 1), spacingDistance);
//        }
//        return points;
//    }

    /**
     * 在两点间按间距插入点
     */
    private static List<Vector3> insertPointsByTwoPoints(List<Vector3> list, Vector3 a, Vector3 b, float distance) {
        List<Vector3> retList = new ArrayList<Vector3>();
        float scale = getDistance(a, b) / distance;//空间中AB向量的模 / 间隔距离
        int count = (int)Math.round(scale);//个数 = 总距离 / 间距 （四舍五入）
        float dealtX = (b.x - a.x) / scale; //单位间隔距离在X轴的增量
        float dealtY = (b.y - a.y) / scale; //单位间隔距离在Y轴的增量
        float dealtZ = (b.z - a.z) / scale; //单位间隔距离在Z轴的增量
        //i = 0时，为起点A，因此index从1开始
        for (int i = 0; i < count; i++) {
            //按索引顺序插入新点
            retList.add(i,new Vector3(
                    a.x + dealtX * i,
                    a.y + dealtY * i,
                    a.z + dealtZ * i));
        }
        return retList;
    }

    /**
     * 获取一个点与一组点串，距离最近的点
     */
    public static int getTouchNearPointIndex(Vector3 p, List<Vector3> list){
        float minDistance=Float.MAX_VALUE;
        int temp_index=-1;
        for (int i = 0; i < list.size(); i++) {
            float distance = getDistance(p, list.get(i));
            if (distance<minDistance){
                temp_index=i;
                minDistance=distance;
            }
        }
        return temp_index;
    }

    /**
     * 增加节点时，通过点击位置，判断是否增加节点，以及加在哪儿
     */
    public static InsertInfo getTouchNearPoint(android.graphics.Point p, List<android.graphics.Point> list){
        double min_distance=Double.MAX_VALUE;
        int index=-1;
        android.graphics.Point point3D=null;
        for (int i = 0; i < list.size()-1; i++) {
            android.graphics.Point p1 = list.get(i);
            android.graphics.Point p2 = list.get(i + 1);
            float x0=p.x;
            float y0=p.y;
            float x1=p1.x;
            float y1=p1.y;
            float x2=p2.x;
            float y2=p2.y;

            float A = y2 - y1;
            float B = x1 - x2;
            float C = x2*y1 - x1*y2;

            //求垂足点
            float foot_x = (  B*B*x0  -  A*B*y0  -  A*C  ) / ( A*A + B*B );
            float foot_y  =  ( -A*B*x0 + A*A*y0 - B*C  ) / ( A*A + B*B );
            boolean flag = false;
            Rect rectangle2D;
            if (p1.x<= p2.x){
                if (p1.y<= p2.y){
                    rectangle2D=new Rect((int) x1, (int) y1, (int) x2, (int) y2);
                }else {
                    rectangle2D=new Rect((int) x1, (int) y2, (int) x2, (int) y1);
                }
            }else {
                if (p1.y<= p2.y){
                    rectangle2D=new Rect((int) x2, (int) y1, (int) x1, (int) y2);
                }else {
                    rectangle2D=new Rect((int) x2, (int) y2, (int) x1, (int) y1);
                }
            }
            if (rectangle2D.contains((int) foot_x, (int) foot_y)){
                double distance=Math.abs( ( A*x0 + B*y0 + C ) / Math.sqrt ( A*A + B*B ));
                if (distance<=min_distance){
                    min_distance=distance;
                    index=i+1;
                    point3D=new android.graphics.Point((int)foot_x,(int)foot_y);
                }
            }
        }
        return new InsertInfo(point3D,index);
    }
    public static class InsertInfo{
        public android.graphics.Point mPoint3D;
        public int index;

        public InsertInfo(android.graphics.Point point3D, int index) {
            mPoint3D = point3D;
            this.index = index;
        }

    }


    /**
     * 根据固定的间隔距离spacingDistance和原有点集，生成新点集合
     * @param points
     * @param spacingDistance
     * @return
     */
    public static ArrayList<Vector3> genNewPointsBySpacingDistance(ArrayList<Vector3> points, float spacingDistance) {
        ArrayList<Vector3> resultList = new ArrayList<Vector3>();
        int size_1 = points.size() - 1;
        for (int i = 0; i < size_1; i++) {
            //根据两点拆分，间距spacingDistance
            Vector3 a = points.get(i);
            Vector3 b = points.get(i + 1);
            resultList.add(a);
            if (Vector3.subtract(b,a).length() > spacingDistance){
                resultList.addAll(insertPointsByTwoPoints(a,b, spacingDistance));
            }
        }
        resultList.add(points.get(size_1));
        return resultList;
    }

    /**
     * 在两点间按间距插入点
     */
    private static ArrayList<Vector3> insertPointsByTwoPoints(Vector3 a, Vector3 b, float distance) {
        ArrayList<Vector3> list = new ArrayList<>();
        float scale = Vector3.subtract(a,b).length() / distance;//空间中AB向量的模 / 间隔距离
        int count = (int)Math.floor(scale);//个数 = 总距离 / 间距 （向下取整）
        float dealtX = (b.x - a.x) / scale; //单位间隔距离在X轴的增量
        float dealtY = (b.y - a.y) / scale; //单位间隔距离在Y轴的增量
        float dealtZ = (b.z - a.z) / scale; //单位间隔距离在Z轴的增量
        //i = 0时，为起点A，因此index从1开始
        for (int i = 1; i < count; i++) {
            //按索引顺序插入新点
            list.add(new Vector3(
                    a.x + dealtX * i,
                    a.y + dealtY * i,
                    a.z + dealtZ * i));
        }
        return list;
    }
}
