package com.eqgis.eqr.utils;

import android.graphics.Point;

import com.eqgis.sceneform.math.Quaternion;
import com.eqgis.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 多边形工具&数学计算
 * @create by tanyx 2021年3月1日20:48:07
 * @update by tanyx 2021年4月14日13:45:44
 */
public class PolygonUtils {

    /**
     * 根据圆角的半径和圆弧段数，在拐点处将拐点替换为弧段上的连续点
     * @update tanyx 2021年4月14日13:45:34
     * @param points
     * @param radius
     * @param edgeNum
     * @return
     */
    public static List<Vector3> genArcPoints(List<Vector3> points,float radius, int edgeNum){
        List<Vector3> resultList = new ArrayList<Vector3>();

        resultList.add(points.get(0));

        for (int i = 1; i < points.size() - 1; i++) {
            Vector3 p1 = points.get(i - 1);
            Vector3 p2 = points.get(i);
            Vector3 p3 = points.get(i + 1);

            //向量P1P2
            Vector3 v12 = new Vector3(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
            float dP12 = PresetUtils.getDistance(p1,p2);//向量P1P2的模长

            //向量P2P3
            Vector3 v23 = new Vector3(p3.x - p2.x, p3.y - p2.y, p3.z - p2.z);
            float dP23 = PresetUtils.getDistance(p2,p3);

            //限制圆角半径参数，防止圆角半径大于距离时，引起弧线段不相连
            if (radius > dP12 * 0.5f){
                radius = dP12 * 0.5f;
            }
            if (radius > dP23 * 0.5f){
                radius = dP23 * 0.5f;
            }

            //两向量的夹角alpha
            double alpha = Math.toDegrees(Math.acos((v12.x * v23.x + v12.y * v23.y + v12.z * v23.z) / (dP12 * dP23)));

            float tanValue = (float) Math.tan(Math.toRadians(alpha * 0.5f)) * radius;
            //切点Pa
            Vector3 pa = new Vector3(p2.x - v12.x / dP12 * tanValue,
                    p2.y - v12.y / dP12 * tanValue,p2.z);

            //切点Pb
            Vector3 pb = new Vector3(p2.x + v23.x / dP23 * tanValue,
                    p2.y + v23.y / dP23 * tanValue,p2.z);

            //PaO的法向量(i1,j1,0);
            float i1 = v12.x;
            float j1 = v12.y;

            //PbO的法向量(i2,j2,0);
            float i2 = v23.x;
            float j2 = v23.y;

            //PaO直线方程的常数K1
            float k1 = pa.x * i1 + pa.y * j1;

            //PbO的直线方程常数K2
            float k2 = pb.x * i2 + pb.y * j2;

            //圆心坐标
            /**
             * 圆心为两直线方程的交点，解二元一次方程组 i*X + j*Y = K
             * | i1 j1 k1 |
             * | i2 j2 k2 |
             * 得=>
             */
            float D = (i1 * j2 - i2 * j1);

            if (D == 0){
                //方程组无唯一解，则直接返回P2点
                resultList.add(p2);
                continue;
            }

            Vector3 circleCenter = new Vector3((k1 * j2 - k2 * j1) / D,
                    (i1 * k2 - i2 * k1) / D,p2.z);

            /***生成圆弧上的点***/
            float deltaAngle = 360.0f / edgeNum;
            int count = (int) Math.ceil(alpha / deltaAngle);

            double angle = Math.toDegrees(Math.atan2(pa.y - circleCenter.y, pa.x - circleCenter.x)) % 360 + 360;
            float deltaZ = (pb.z - pa.z)/deltaAngle;

            resultList.add(pa);

            //判断顺逆（s < 0 => P1,P2,P3顺时针构成三角形）
            float s = (p1.x - p3.x)*(p2.y - p3.y) - (p1.y - p3.y)*(p2.x - p3.x);
            if (s < 0){
                //顺时针
                for (int j = 1; j < count; j++) {
                    angle -= deltaAngle;
                    resultList.add(new Vector3(circleCenter.x + (float) Math.cos(Math.toRadians(angle)) * radius,
                            circleCenter.y + (float) Math.sin(Math.toRadians(angle)) * radius,
                            pa.z + deltaZ * j));
                }
            }else {
                //逆时针
                for (int j = 1; j < count; j++) {
                    angle += deltaAngle;
                    resultList.add(new Vector3(circleCenter.x + (float) Math.cos(Math.toRadians(angle)) * radius,
                            circleCenter.y + (float) Math.sin(Math.toRadians(angle)) * radius,
                            pa.z + deltaZ * j));
                }
            }

            resultList.add(pb);

        }

        resultList.add(points.get(points.size() - 1));
        return resultList;
    }

    /**
     * 将首尾相连的点集，生成可用Mode.TRIANGLES模式绘制多边形的顶点集合
     * @param sourceList
     * @return
     */
    public static List<Vector3> genTrianglePoints(List<Vector3> sourceList){
        if (sourceList.size() < 3){
            return null;
        }

        List<Vector3> resultList = new ArrayList<Vector3>();

        genTriangle(sourceList,resultList);
        return resultList;
    }

    /**
     * 将多边形（凹/凸）转化为一组三角形
     * @update tanyx 2021年3月3日18:34:33
     */
    private static void genTriangle(List<Vector3> sourceList, List<Vector3> triangleList){
        while (true){
            List<Float> angleList = new ArrayList<Float>();
            List<Vector3> tempList = new ArrayList<Vector3>();
            List<Integer> indexList = new ArrayList<Integer>();

            /********主要思路：获取特征角最大的三角形=>保存该三角形********/
            /*******--------step1-----------********/
            for (int i = 0; i < sourceList.size() - 3; i++) {
                /**
                 * 三角形PQR 三点sourceList.get(i), sourceList.get(i + 1), sourceList.get(i + 2)
                 */
                //判断三角形PQR是否包含其他顶点
                Vector3 p = sourceList.get(i);
                Vector3 q = sourceList.get(i + 1);
                Vector3 r = sourceList.get(i + 2);

                //凹点判断
                if (isConcavePoint(q,sourceList)){
                    continue;
                }

                //判断其他点是否在PQR三角形外
                boolean outTriangle_z = true;
                List<Vector3> targetList = new ArrayList<Vector3>();
                targetList.addAll(sourceList);
//            if (i == 0){//TODO 此处可所有都执行remove
//                targetList.remove(targetList.size() - 1);
//            }
                //由于首尾两点为位置相同的不同对象，首点判定，则可都清除尾点的判断
                targetList.remove(targetList.size() - 1);

                //移除i后面的3个点
                targetList.remove(targetList.get(i));
                targetList.remove(targetList.get(i));
                targetList.remove(targetList.get(i));
//            targetList.remove(i+1);
//            targetList.remove(i+2);
                for (Vector3 e:targetList) {
                    outTriangle_z = isOutTriangle_Z(p,q,r,e);
                    if (!outTriangle_z){
                        //若有一点在三角形内，则break
                        break;
                    }
                }

                //其他点在三角形外
                if (!outTriangle_z){
                    continue;
                }
                //由P、Q、R 所构成的三角形PQR不包含多边形上其他顶点,则计算△PQR 的特征角(三角形内最小的角)
                float v = getMinAngle(p,q,r);
                //angleList的索引与tempTriangleList保持一致  i2 = 3 * i1
                angleList.add(v);

                //此方法求出所有这样的三角形，记为特征角三角形集合，用tempList，按序存点
                tempList.add(p);
                tempList.add(q);
                tempList.add(r);

                //保存下q点的索引,angeleList的索引与indexList的索引对应
                indexList.add(i + 1);
            }

            /*******--------step2-----------********/
            //取出特征角三角形中角度最大的三角形的角度索引
            int angleIndex = 0;
            float maxValue = 0f;
            for (int i = 0; i < angleList.size(); i++) {
                if (maxValue < angleList.get(i)){
                    maxValue = angleList.get(i);
                    angleIndex = i;
                }
            }

            /**********------step3--------********/
            if (sourceList.size() < 4 || indexList.size() == 0 || angleList.size() == 0){
                //由于起始点和结束点重叠，点对象不同，故最后共计3+1个点
                return;
            }else {
                //获取特征角最大的三角形顶点 3*angleIndex、3*angleIndex+1、3*angleIndex+2
                triangleList.add(tempList.get(3 * angleIndex));
                triangleList.add(tempList.get(3 * angleIndex + 1));
                triangleList.add(tempList.get(3 * angleIndex + 2));
                //取出q点索引
                int sourceIndex = indexList.get(angleIndex);
                //移除q点
                sourceList.remove(sourceList.get(sourceIndex));
//                genTriangle(sourceList,triangleList);//用while(true)替换递归
            }
        }
        //end
    }

    /**
     * 判断P点在XOY面的投影P1,是否在三角形ABC在XOY面的投影三角形外
     * @param A
     * @param B
     * @param C
     * @param P
     * @return
     */
    private static boolean isOutTriangle_Z(Vector3 A, Vector3 B, Vector3 C, Vector3 P) {
        /*利用叉乘法进行判断,假设P点就是M点*/
        float a = 0, b = 0, c = 0;

        //对应在XOY面（OpenGL YOZ面）的投影坐标
        Vector3 MA = new Vector3(P.x - A.x,P.y - A.y,0);
        Vector3 MB = new Vector3(P.x - B.x,P.y - B.y,0);
        Vector3 MC = new Vector3(P.x - C.x,P.y - C.y,0);

        /*向量叉乘*/
        a = MA.x * MB.y - MA.y * MB.x;
        b = MB.x * MC.y - MB.y * MC.x;
        c = MC.x * MA.y - MC.y * MA.x;

        //以下判断 点在三角形上，也返回ture
        if((a <= 0 && b <= 0 && c <= 0)||
                (a >= 0 && b >= 0 && c >= 0))
            return false;
        return true;
    }

    /**
     * 判断三角形ABC在XOY平面的投影三点是否是顺时针
     * @param a
     * @param b
     * @param c
     * @return
     */
    static boolean isClockwise_Z(Vector3 a, Vector3 b, Vector3 c) {
        float x1 = b.x - a.x;
        float y1 = b.y - a.y;
        float x2 = c.x - b.x;
        float y2 = c.y - b.y;

        //通过有向面积的正负判断三角形三点的方向
        float s = (a.x - c.x)*(b.y - c.y) - (a.y - c.y)*(b.x - c.x);
        if (s < 0)return true;
        return false;
    }


    /**
     * 获取最小角度
     * @param a
     * @param b
     * @param c
     * @return
     */
    private static float getMinAngle(Vector3 a, Vector3 b, Vector3 c){
        float angeleA = getAngleValueBy(b,a,c);
        float angeleB = getAngleValueBy(a,b,c);
        float angeleC = getAngleValueBy(b,c,a);

        //返回最小角
        if (angeleA > angeleB){
            angeleA = angeleB;
        }
        if (angeleA > angeleC){
            angeleA = angeleC;
        }
        return (float) Math.toDegrees(angeleA);
    }

    /**
     * 通过余弦定理获取B的角度
     * @param a
     * @param b
     * @param c
     * @return
     */
    private static float getAngleValueBy(Vector3 a, Vector3 b, Vector3 c){
        float la2 = (b.x - c.x) * (b.x - c.x) + (b.y - c.y) * (b.y - c.y) + (b.z - c.z) * (b.z - c.z);//BC边长的平方
        float lb2 = (c.x - a.x) * (c.x - a.x) + (c.y - a.y) * (c.y - a.y) + (c.z - a.z) * (c.z - a.z);
        float lc2 = (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y) + (b.z - a.z) * (b.z - a.z);
        return (float) Math.acos((la2+ lc2 - lb2)/(2.0* Math.sqrt(la2) * Math.sqrt(lc2)));
    }

    /**
     * 判断是否为凹点
     * @param point3D
     * @param sourceList_const
     * @return
     */
    public static boolean isConcavePoint(Vector3 point3D, List<Vector3> sourceList_const) {
        if (sourceList_const.size() < 4){
            //总点个数小于4
            return false;
        }
        List<Vector3> temp = new ArrayList<Vector3>();
        temp.addAll(sourceList_const);
        for (int i = 0; i < temp.size(); i++) {
            Vector3 e = temp.get(i);
            if (e.x == point3D.x && e.y == point3D.y && e.z == point3D.z){
                temp.remove(e);
                i--;
            }
        }
        boolean b = isInPolygon_XOY(point3D, temp);
        //true:凹点
        return b;
    }

    /**
     * 点是否在多边形（在XOY面的投影点集构成的多边形）内
     * @param test
     * @param points
     * @update tanyx 2021年3月24日
     * @return
     */
    public static boolean isInPolygon_XOY(Vector3 test, List<Vector3> points) {
        boolean result = false;
        int i = 0;

        for (int j = points.size() - 1; i < points.size(); j = i++) {
            if (points.get(i).y > test.y != points.get(j).y > test.y
                    && test.x < (points.get(j).x - points.get(i).x) * (test.y - points.get(i).y) / (points.get(j).y - points.get(i).y) + points.get(i).x) {
                result = !result;
            }
        }

        return result;
    }
    //todo 待验证
    public static boolean isPointOnLine(Vector3 p1, Vector3 p2, Vector3 q ,float tolerance){
        boolean flag = false;     // true   点在线上    false   点不在线上

        if(((q.x-p1.x) * (p2.y-p1.y) - (q.y-p1.y) * (p2.x-p1.x)) <= tolerance){
            //System.out.println("点在线上这条直线上");
            if(p1.x >= p2.x && p1.y >= p2.y){
                //点p1在点p2右上方    p1x > p2x p1y> p2y
                if(q.x <= p1.x && q.x >= p2.x && q.y <= p1.y && q.y >= p2.y){
                    flag = true;
                }
            }else if(p1.x >= p2.x && p1.y <= p2.y){
                //点p1在点p2的右下方   p1x > p2x p1y < p2y
                if(q.x <= p1.x && q.x >= p2.x && q.y >= p1.y && q.y <= p2.y){
                    flag = true;
                }
            }else if(p1.x <= p2.x && p1.y >= p2.y){
                //点p1在p2左上方   p1x < p2x p1y > p2y
                if(q.x >= p1.x && q.x <= p2.x && q.y <= p1.y && q.y >= p2.y){
                    flag = true;
                }
            }else if(p1.x <= p2.x && p1.y <= p2.y){
                //点p1在p2左下方 p1x < p2x p1y < p2y
                if(q.x >= p1.x && q.x <= p2.x && q.y >= p1.y && q.y <= p2.y){
                    flag = true;
                }
            }
        }
        return flag;
    }

    public static boolean isInLine_XOY(Vector3 test, Vector3 p1,Vector3 p2,float tolerance){


        return isInLine_XOY(test.x,test.y, p1.x, p1.y, p2.x, p2.y,tolerance);
    }
    static boolean isInLine_XOY(float x0, float y0, float x1, float y1,float x2,float y2,float offset){
        double dx = (x2 - x1) / 2;
        double dy = (y2 - y1) / 2;
        double cx = (x2 + x1) / 2;
        double cy = (y2 + y1) / 2;
        double m = Math.hypot(dx, dy);
        double ux = dx / m;
        double uy = dy / m;
        double vx = -uy;
        double vy = ux;
        double wx = x0 - cx;
        double wy = y0 - cy;
        return (Math.abs(ux * wx + uy * wy) <= m && Math.abs(vx * wx + vy * wy) <= offset);
    }



    /**
     * 判断空间中任意点，是否在空间某一平面上的多边形内
     * @param v0
     * @param points 首尾相连的点集
     * @create by tanyx 2021年3月24日
     * @return
     */
    static boolean isInPolygon(Vector3 v0, List<Vector3> points){
        //计算空间中多边形的面积
        float polygonArea = getAreaBy3Point3Ds(points) * 1.00001f;//容限

        Float sumArea = 0f;
        //原理：空间中一点不在空间中某平面上的多边形内，则该点与多边形个点连线组成的三角形集合的总面积必大于多边形面积
        for (int i = 0; i < points.size() - 1; i++) {
            Float area= getAreaBy3Point(v0, points.get(i),
                    points.get(i + 1));
            if (!area.isNaN()){
                //点与顶点重合时，向量计算时会出现分母为0的情况，这时会获得NaN
                sumArea += area;
            }
        }
        if (sumArea < polygonArea){
            //点在平面外
            return true;
        }else {
            return false;
        }
    }

    /**
     * 判断空间中任意点，是否在空间某一平面上的多边形内
     * @param v0
     * @param points 首尾相连的点集
     * @param tolerance 容限
     * @create by tanyx 2021年5月06日
     * @return
     */
    static boolean isInPolygon2(Vector3 v0, List<Vector3> points,float tolerance){
        //计算空间中多边形的面积
        float polygonArea = getAreaBy3Point3Ds(points) * (1.0f + tolerance);//容限

        Float sumArea = 0f;
        //原理：空间中一点不在空间中某平面上的多边形内，则该点与多边形个点连线组成的三角形集合的总面积必大于多边形面积
        for (int i = 0; i < points.size() - 1; i++) {
            Float area= getAreaBy3Point(v0, points.get(i),
                    points.get(i + 1));
            if (!area.isNaN()){
                //点与顶点重合时，向量计算时会出现分母为0的情况，这时会获得NaN
                sumArea += area;
            }
        }
        if (sumArea < polygonArea){
            //点在平面外
            return true;
        }else {
            return false;
        }
    }

    /**
     * 判断空间中任意点，是否在空间某一平面上的多边形内
     * @param test
     * @param points 首尾相连的点集
     * @create by tanyx 2021年5月06日
     * @return
     */
    static boolean isInPolygon3(Point test, List<Point> points){
        //计算空间中多边形的面积
        double sumPointsArea = 0;
        double comparePointsArea = 0;
        for (int i = 0; i < points.size()-2; i++) {
            Point a = points.get(i);
            Point b = points.get(i + 1);
            Point c = points.get(i + 2);
            if (i == 0){
                comparePointsArea += getAreaBy3Point(test,a,b);
            }

            comparePointsArea += getAreaBy3Point(test,b,c);

            sumPointsArea += getAreaBy3Point(a, b, c);
        }

        return !(comparePointsArea > sumPointsArea);
    }

    /**
     * 获取三角形面积
     * @param a
     * @param b
     * @param c
     * @return
     */
    private static double getAreaBy3Point(Point a, Point b, Point c) {
        Point ab = new Point(b.x - a.x, b.y - a.y);
        Point ac = new Point(c.x - a.x, b.y - a.y);


        double ab_x_ac = Math.sqrt(ab.x * ab.x + ab.y * ab.y)
                * Math.sqrt(ac.x * ac.x + ac.y * ac.y);
        if (ab_x_ac == 0){
            return 0;
        }
        double ab_o_ac = ab.x * ac.x + ab.y * ac.y;
        double cosa = ab_o_ac / ab_x_ac;
        double sina = Math.sqrt(1 - Math.pow(cosa,2));
        return 0.5f*ab_x_ac*sina;
    }

    /**
     * 获取三点构成的三角形面积(凸)
     * @param v0
     * @param v1
     * @param v2
     * @return
     */
    private static float getAreaBy3Point(Vector3 v0, Vector3 v1, Vector3 v2){
        Vector3 AB = Vector3.subtract(v0,v1);
        Vector3 AC = Vector3.subtract(v0,v2);

        float ABxAC = AB.length()*AC.length();
        float cosa = Vector3.dot(AB,AC)/ABxAC;
        float sina = (float) Math.sqrt(1 - Math.pow(cosa,2));
        return 0.5f*ABxAC*sina;
    }

    /**
     * 计算首尾相连的点集围成的多边形的面积
     * @param pointList
     * @return 面积
     * @updated by tanyx 2021年8月19日15:22:17 修改为可计算凹多边形
     */
    private static float getAreaBy3Point3Ds(List<Vector3> pointList){
        if (pointList.size() == 3){
            return 0;
        }else if (pointList.size() < 4){
            throw new RuntimeException("Less than 3 points");
        }

        Vector3 v0 = pointList.get(0);
        float sumArea = 0;
        int size = pointList.size();
        for (int i = 1; i  < size - 2; i++) {
            sumArea += getAreaBy3Point(
                    v0,
                    pointList.get(i),
                    pointList.get(i +1)
            );
        }
        return sumArea;
    }

    /**
     * 获取线段与平面的交点坐标
     * @param v0 线段起点
     * @param v1 线段终点
     * @param a 平面内A点(ABC不共线)
     * @param b 平面内B点(ABC不共线)
     * @param c 平面内C点(ABC不共线)
     * @return
     * @create by tanyx 2021年3月25日15:44:16
     */
    static Vector3 getIntersectionPoint(Vector3 v0,Vector3 v1,Vector3 a,Vector3 b,Vector3 c){
        try {
            //向量AB
            Vector3 vectorAB = Vector3.subtract(b, a);
            //向量AC
            Vector3 vectorAC = Vector3.subtract(c, a);
            //直线方向向量i
            Vector3 i = Vector3.subtract(v1, v0);
            //平面法向量n
            Vector3 n = Vector3.cross(vectorAB,vectorAC);

            //点法式平面方程常数K
            float constK = n.x * a.x + n.y * a.y + n.z * a.z;
            float v = i.x * n.x + i.y * n.y + i.z * n.z;
            if (Math.abs(v) < 0.0001f)v = 0.0001f;
            //点向式直线方程常数M
            float constM = (constK - n.x * v0.x - n.y * v0.y - n.z * v0.z)/v;

            return new Vector3(v0.x + i.x * constM,v0.y + i.y * constM,v0.z + i.z * constM);
        }catch (NullPointerException e){
            return null;
        }
    }

    /**
     * temp
     * @param sceneLayout
     * @param a
     * @param b
     * @param c
     * @return
     */
//    static Vector3 getForwardPoint(SceneLayout sceneLayout, Vector3 a, Vector3 b, Vector3 c){
//        try{
//            //向量AB
//            Vector3 vectorAB = Vector3.subtract(b, a);
//            //向量AC
//            Vector3 vectorAC = Vector3.subtract(c, a);
//            //平面法向量n
//            Vector3 n = Vector3.cross(vectorAB,vectorAC);
//
//            //相机位置v0
//            Vector3 v0 = sceneLayout.getCamera().getWorldPosition();
//            //直线方向向量i
//            Vector3 i = sceneLayout.getCamera().getForward();
//
//            //点法式平面方程常数K
//            float constK = n.x * a.x + n.y * a.y + n.z * a.z;
//            //点向式直线方程常数M
//            float constM = (constK - n.x * v0.x - n.y * v0.y - n.z * v0.z)/(i.x * n.x + i.y * n.y + i.z * n.z);
//            return new Vector3(v0.x + i.x * constM,v0.y + i.y * constM,v0.z + i.z * constM);
//        }catch (NullPointerException e){
//            return null;
//        }
//    }

    /**
     * 获取不共线三点构成的平面相对水平面的旋转四元数
     * @param a
     * @param b
     * @param c
     * @return
     */
    static Quaternion getQuaternionByPoints(Vector3 a,Vector3 b, Vector3 c){


        /**step1.计算ABC三点构成平面的法向量n*/
        //向量AB
        Vector3 vectorAB = Vector3.subtract(b, a);
        //向量AC
        Vector3 vectorAC = Vector3.subtract(c, a);
        //单位法向量n
        Vector3 n = Vector3.cross(vectorAB,vectorAC).normalized();

        return getQuaternionBy2Vector(new Vector3(0,1,0),n);
    }

    /**
     * 获取单位向量V0旋转为单位向量V1的旋转四元数
     * @param v0
     * @param v1
     * @return
     */
    static Quaternion getQuaternionBy2Vector(Vector3 v0, Vector3 v1){
        float cosA = Vector3.dot(v0,v1);//点乘/模长
        double angle = Math.acos(cosA);//旋转角度

        //step1.求旋转平面的法向量k(旋转轴的单位向量（备注：逆时针旋转为正）)
        Vector3 k = Vector3.cross(v1,v0).normalized();

        float sinA = (float) Math.sin(angle);
        //step2.计算旋转矩阵R
        float[][] R = new float[3][3];

        R[0][0] = cosA + (1 - cosA) * k.x * k.x;
        R[0][1] = (1 - cosA) * k.x * k.y - sinA * k.z;
        R[0][2] = (1 - cosA) * k.x * k.z + sinA * k.y;

        R[1][0] = (1 - cosA) * k.y * k.x + sinA * k.z;
        R[1][1] = cosA + (1 - cosA) * k.y * k.y;
        R[1][2] = (1 - cosA) * k.y * k.z - sinA * k.x;

        R[2][0] = (1 - cosA) * k.z * k.x - sinA * k.y;
        R[2][1] = (1 - cosA) * k.z * k.y + sinA * k.x;
        R[2][2] = cosA + (1 - cosA) * k.z * k.z;

        //step3.旋转矩阵R转四元数Q(x,y,z,w)
        Quaternion quaternion = new Quaternion();
        quaternion.w = (float) (0.5f * Math.sqrt(1 + R[0][0] + R[1][1] + R[2][2]));
        float w4 = 4 * quaternion.w;
        quaternion.x = (R[2][1] - R[1][2]) / w4;
        quaternion.y = (R[0][2] - R[2][0]) / w4;
        quaternion.z = (R[1][0] - R[0][1]) / w4;

        return quaternion;
    }

    /**
     * 在XOY平面上，获取垂直AB，且过端点AB，且距离为width的点集（AB不重合，共4个点）
     * @param pointA
     * @param pointB
     * @param width
     * @return
     */
    static List<Vector3> getVerticalPoints(Vector3 pointA, Vector3 pointB, float width) {
        List<Vector3> ps = new ArrayList<Vector3>();
        Vector3 p1 = new Vector3(pointA.x,pointA.y,pointA.z);
        Vector3 p2 = new Vector3(pointB.x,pointB.y,pointB.z);
        Vector3 p3 = new Vector3(pointB.x,pointB.y,pointB.z);
        Vector3 p4 = new Vector3(pointA.x,pointA.y,pointA.z);
        if (pointA.x == pointB.x){
            //x值相等,线段AB//Y轴
            p1.x += width;
            p2.x += width;
            p3.x -= width;
            p4.x -= width;
        }else {
            if (pointA.y == pointB.y){
                //y值相等，线段AB//X轴
                p1.y += width;
                p2.y += width;
                p3.y -= width;
                p4.y -= width;
            }else {
                /**x、y都不相等，线段AB以及AB垂线的斜率k都存在*/
                //AB垂线的斜率 k2 = -（1/k）
//                float k2 = (pointA.x - pointB.x) / (pointB.y - pointA.y);
                double arcTan_k2 = Math.atan2(pointA.x - pointB.x, pointB.y - pointA.y);
                float delta_x = (float) Math.cos(arcTan_k2) * width;
                float delta_y = (float) Math.sin(arcTan_k2) * width;
                //根据增量计算新坐标
                calculatePoint(p1, true, delta_x, delta_y);
                calculatePoint(p2, true, delta_x, delta_y);
                calculatePoint(p3, false, delta_x, delta_y);
                calculatePoint(p4, false, delta_x, delta_y);
            }
        }
        ps.add(p1);
        ps.add(p2);
        ps.add(p3);
        ps.add(p4);
        //判断顺逆（s < 0 => P1,P2,P3顺时针构成三角形）0AB
        float s = (p1.x - p3.x)*(p4.y - p3.y) - (p1.y - p3.y)*(p4.x - p3.x);
        if (s > 0){
            return Arrays.asList(p1,p2,p3,p4);
        }else {
            return Arrays.asList(p4,p3,p2,p1);
        }
//        return ps;
    }

    /**
     * 根据增量计算新坐标
     * @param p
     * @param tf
     * @param delta_x
     * @param delta_y
     * @return
     */
    private static Vector3 calculatePoint(Vector3 p, boolean tf, float delta_x, float delta_y) {
        if (tf){
            p.x += delta_x;
            p.y += delta_y;
        }else {
            p.x -= delta_x;
            p.y -= delta_y;
        }
        return p;
    }

    /**
     * 根据折线点集生成可构成平滑宽线多边形的点集
     * @update tanyx 2021年4月15日
     * @param points
     * @param lineWidth
     * @param radius
     * @param edgeNum
     * @return
     */
    static List<Vector3> genStripe_TriPoints(List<Vector3> points, float lineWidth, float radius, int edgeNum) {

        List<Vector3> resultPoints = new ArrayList<Vector3>();

        //startPoint
        List<Vector3> tempList = getVerticalPoints(points.get(0), points.get(1), lineWidth);
        resultPoints.add(tempList.get(3));//左
        resultPoints.add(tempList.get(0));//右

        if (points.size() > 2){
            for (int i = 1; i < points.size() - 1; i++) {
                Vector3 p1 = points.get(i - 1);
                Vector3 p2 = points.get(i);
                Vector3 p3 = points.get(i + 1);

                //向量P1P2
                Vector3 v12 = new Vector3(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
                float dP12 = PresetUtils.getDistance(p1,p2);//向量P1P2的模长

                //向量P2P3
                Vector3 v23 = new Vector3(p3.x - p2.x, p3.y - p2.y, p3.z - p2.z);
                float dP23 = PresetUtils.getDistance(p2,p3);

                //限制圆角半径参数，防止圆角半径大于距离时，引起弧线段不相连
                if (radius > dP12 * 0.5f){
                    radius = dP12 * 0.5f;
                }
                if (radius > dP23 * 0.5f){
                    radius = dP23 * 0.5f;
                }

                //两向量的夹角alpha
                double alpha = Math.toDegrees(Math.acos((v12.x * v23.x + v12.y * v23.y + v12.z * v23.z) / (dP12 * dP23)));

                float tanValue = (float) Math.tan(Math.toRadians(alpha * 0.5f)) * radius;
                //切点Pa
                Vector3 pa = new Vector3(p2.x - v12.x / dP12 * tanValue,
                        p2.y - v12.y / dP12 * tanValue,p2.z);

                //切点Pb
                Vector3 pb = new Vector3(p2.x + v23.x / dP23 * tanValue,
                        p2.y + v23.y / dP23 * tanValue,p2.z);

                //PaO的法向量(i1,j1,0);
                float i1 = v12.x;
                float j1 = v12.y;

                //PbO的法向量(i2,j2,0);
                float i2 = v23.x;
                float j2 = v23.y;

                //PaO直线方程的常数K1
                float k1 = pa.x * i1 + pa.y * j1;

                //PbO的直线方程常数K2
                float k2 = pb.x * i2 + pb.y * j2;

                //圆心坐标
                /**
                 * 圆心为两直线方程的交点，解二元一次方程组 i*X + j*Y = K
                 * | i1 j1 k1 |
                 * | i2 j2 k2 |
                 * 得=>
                 */
                float D = (i1 * j2 - i2 * j1);

                if (D == 0){
                    //方程组无唯一解，则直接返回P2点
//                resultList.add(p2);
                    //此处过滤掉该点P2的两侧点
                    continue;
                }

                Vector3 circleCenter = new Vector3((k1 * j2 - k2 * j1) / D,
                        (i1 * k2 - i2 * k1) / D,p2.z);

                /***生成圆弧上的点***/
                float deltaAngle = 360.0f / edgeNum;
                int count = (int) Math.ceil(alpha / deltaAngle);

                double angle = Math.toDegrees(Math.atan2(pa.y - circleCenter.y, pa.x - circleCenter.x)) % 360 + 360;
                double theta = angle;
                float deltaZ = (pb.z - pa.z)/deltaAngle;

//            resultList.add(pa);

                //p1 -> pa ,pa处两侧的垂直点

                //判断顺逆（s < 0 => P1,P2,P3顺时针构成三角形）
                float s = (p1.x - p3.x)*(p2.y - p3.y) - (p1.y - p3.y)*(p2.x - p3.x);

                if (s < 0){
                    //顺时针
                    for (int j = 0; j < count; j++) {

                        //右//内侧
                        resultPoints.add(new Vector3(circleCenter.x + (float) Math.cos(Math.toRadians(angle)) * (radius - lineWidth),
                                circleCenter.y + (float) Math.sin(Math.toRadians(angle)) * (radius - lineWidth),
                                pa.z + deltaZ * j));
                        //左//外侧
                        resultPoints.add(new Vector3(circleCenter.x + (float) Math.cos(Math.toRadians(angle)) * (radius + lineWidth),
                                circleCenter.y + (float) Math.sin(Math.toRadians(angle)) * (radius + lineWidth),
                                pa.z + deltaZ * j));

                        angle -= deltaAngle;

                    }
                    //pb处
                    //右//内侧
                    double cos = Math.cos(Math.toRadians(theta - alpha));
                    double sin = Math.sin(Math.toRadians(theta - alpha));
                    resultPoints.add(new Vector3(circleCenter.x + (float) cos * (radius - lineWidth),
                            circleCenter.y + (float) sin * (radius - lineWidth),
                            pb.z));

                    //左//外侧
                    resultPoints.add(new Vector3(circleCenter.x + (float) cos * (radius + lineWidth),
                            circleCenter.y + (float) sin * (radius + lineWidth),
                            pb.z));

                }
                else {
                    //逆时针
                    for (int j = 0; j < count; j++) {
                        resultPoints.add(new Vector3(circleCenter.x + (float) Math.cos(Math.toRadians(angle)) * (radius + lineWidth),
                                circleCenter.y + (float) Math.sin(Math.toRadians(angle)) * (radius + lineWidth),
                                pa.z + deltaZ * j));
                        resultPoints.add(new Vector3(circleCenter.x + (float) Math.cos(Math.toRadians(angle)) * (radius - lineWidth),
                                circleCenter.y + (float) Math.sin(Math.toRadians(angle)) * (radius - lineWidth),
                                pa.z + deltaZ * j));

                        angle += deltaAngle;
                    }
                    //pb处
                    //右//外侧
                    double cos = Math.cos(Math.toRadians(theta + alpha));
                    double sin = Math.sin(Math.toRadians(theta + alpha));
                    //左//内侧
                    //RL
                    resultPoints.add(new Vector3(circleCenter.x + (float) cos * (radius + lineWidth),
                            circleCenter.y + (float) sin * (radius + lineWidth),
                            pb.z));
                    resultPoints.add(new Vector3(circleCenter.x + (float) cos * (radius - lineWidth),
                            circleCenter.y + (float) sin * (radius - lineWidth),
                            pb.z));

                }

                //end
            }

        }
        //endPoint
        tempList = getVerticalPoints(points.get(points.size() - 2), points.get(points.size() - 1), lineWidth);

        resultPoints.add(tempList.get(2));
        resultPoints.add(tempList.get(1));

        return resultPoints;
    }

    /**
     *      3 --------- 2
     *      \a---------b\
     *      0 --------- 1
     */
//    static List<Point3D> getVerticalPoints2(Point3D pointA, Point3D pointB, float width) {
//        Point3D v_ab = new Point3D(pointB.x - pointA.x, pointB.y - pointA.y,pointB.z - pointA.z);
//        //XOY平面上与直线AB的垂直的方向向量
//        Point3D n = new Point3D(v_ab.y , - v_ab.x, 0);
//
//        float nD = (float) Math.sqrt(n.x * n.x + n.y * n.y);
//
//        Point3D p3 = new Point3D(pointA.x + n.x * width / nD,pointB.y + n.y * width / nD, pointA.z);
//        Point3D p0 = new Point3D(pointA.x - n.x * width / nD,pointB.y - n.y * width / nD, pointA.z);
//        Point3D p2 = new Point3D(pointB.x + n.x * width / nD,pointB.y + n.y * width / nD, pointB.z);
//        Point3D p1 = new Point3D(pointB.x - n.x * width / nD,pointB.y - n.y * width / nD, pointB.z);
//
//        //判断顺逆（s < 0 => P1,P2,P3顺时针构成三角形）0AB
//        float s = (p0.x - pointB.x)*(pointA.y - pointB.y) - (p0.y - pointB.y)*(pointA.x - pointB.x);
//        if (s > 0){
//            return Arrays.asList(p0,p1,p2,p3);
//        }else {
//            return Arrays.asList(p3,p2,p1,p0);
//        }
//    }
}