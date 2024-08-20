package com.eqgis.eqr.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.eqgis.sceneform.Camera;
import com.eqgis.sceneform.collision.Ray;
import com.eqgis.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * 屏幕坐标转换工具
 *      AR坐标 => 屏幕坐标
 */

public class ScreenPointTool {
    static final float NEAR = 1.0F;//单位距离（米）
    static float SCALE_DP_METER = 250.0F;//ARCORE->ViewRenderable中描述：view中250dp，在AR中为1米
    static Float SCALE_DP_METER_SCALE = null;//参数尺度： = densityDpi / （（xdpi + ydpi）/ 2 ）
    //    static Camera VIRTUAL_CAMERA = null;//AR场景的虚拟相机
    static Float SCREEN_DENSITY = null;//屏幕密度
    static Float SCREEN_HEIGHT_HALF = null;//屏幕高度
    static Float SCREEN_WIDTH_HALF = null;//屏幕宽度
    private static int SCREEN_DENSITY_DPI;
    private static int SCREEN_HEIGHT_PIXEL;
    private static int SCREEN_WIDTH_PIXEL;
    private static ArrayList<int[]> prevList = null;



    /**
     * 将场景坐标集转为屏幕像素坐标
     * @param camera 场景相机
     * @param point3DList
     * @return 屏幕坐标集
     */
    public static ArrayList<int[]> convertToPixel(Camera camera, List<Vector3> point3DList){
        Vector3 forward = camera.getForward();
//        Log.e("IKKYUTEST", "convertToPixel: "+ (Math.toDegrees(Math.atan2(forward.y, forward.x))));
        Vector3 currentPosition = camera.getWorldPosition();
        //desc-updated by tanyx 2022年1月26日15:41:35
        ArrayList<int[]> resList = new ArrayList<int[]>();
        for (Vector3 p : point3DList) {
            Vector3 vecTarget = Vector3.subtract(p,currentPosition);
            if (p != null){
                Vector3 result = camera
                        .worldToScreenPoint(p);

                double v = Math.toDegrees(Math.atan2(vecTarget.x, -vecTarget.z) - Math.atan2(forward.x, -forward.z));
                if (Math.abs(v)<90){
                    resList.add(new int[]{(int) result.x, (int) result.y});
                }else {
                    if (forward.y > 0){
                        //仰视
                        resList.add(new int[]{(int) result.x, 0});
                    }
                    else {
                        //desc-俯视
                        resList.add(new int[]{(int) result.x, (int) result.y});
                    }
                }
                //else
            }
        }
        return resList;
    }

    /**
     * 将场景坐标集转为屏幕像素坐标
     * @param camera 场景相机
     * @param point3DList   待转换的3D点集
     * @param isModify      是否保持位置镜像
     * @return 屏幕坐标集
     */
    public static ArrayList<int[]> convertToPixel(Camera camera, List<Vector3> point3DList, boolean isModify){
        if (isModify){
            return convertToPixel(camera, point3DList);
        }
        if (prevList == null){
            prevList = new ArrayList<int[]>();
        }
        // update:2022/05/10
        Vector3 forward = camera.getForward();
        Vector3 currentPosition = camera.getWorldPosition();
        ArrayList<int[]> resList = new ArrayList<int[]>();


        Vector3 firstV = new Vector3(0, 0, 0);

        for (int i = 0; i < point3DList.size(); i++) {
            Vector3 vector3 = point3DList.get(i);
            Vector3 vecTarget = Vector3.subtract(vector3, currentPosition);
            if (vector3 != null) {
                double v = Math.toDegrees(Math.atan2(vecTarget.x, -vecTarget.z) - Math.atan2(forward.x, -forward.z));
                Vector3 result = camera.worldToScreenPoint(vector3);
                if (i == 0){
                    firstV = result;
                }
                if (Math.abs(v) <= 89.8){
                    resList.add(new int[]{(int) result.x, (int) result.y});
//                } else  (Math.abs(v) > halfFov && Math.abs(v) < 180-halfFov){
                }
                else if( Math.abs(v) > 89.8 && Math.abs(v) < 90.2){
                    return prevList;
                }
                else {
                    if(v > 0){
                        if (forward.y > 0){
                            //仰视
//                            resList.add(new int[]{(int) Math.abs(result.x), (int) Math.abs(result.y)});
                            resList.add(new int[]{(int) (firstV.x - result.x), (int) (firstV.y - result.y)});
                        } else {
                            //desc-俯视
//                            resList.add(new int[]{(int) Math.abs(result.x), (int) Math.abs(result.y)});
                            resList.add(new int[]{(int) (firstV.x - result.x), (int) (firstV.y - result.y)});
                        }
                    }else{
                        if (forward.y > 0){
                            //仰视
//                            resList.add(new int[]{(int) -Math.abs(result.x), (int) Math.abs(result.y)});
                            resList.add(new int[]{(int)- (firstV.x - result.x), (int) (firstV.y - result.y)});
                        }else{
                            //desc-俯视
//                            resList.add(new int[]{(int) -Math.abs(result.x), -(int) Math.abs(result.y)});
                            resList.add(new int[]{(int) -(firstV.x - result.x), -(int) (firstV.y - result.y)});
                        }
                    }
                }
//                else {
////                    resList.add(new int[]{(int) -result.x, (int) -result.y});
//                }
            }
        }
        prevList = resList;
        return resList;
    }

    /**
     * 将场景中的点转为Z值为0的屏幕坐标
     *      注：坐标原点为屏幕中心
     * @param vector3
     * @return
     */
    public static int[] convertToScreenPoint(Context context,Camera camera, Vector3 vector3){
//        vector3 = covertToScreenPoint_Center(arView, vector3);
        Vector3 forward = camera.getForward();
        Vector3 currentPosition = camera.getWorldPosition();
        Vector3 result = camera.worldToScreenPoint(vector3);

        Vector3 vecTarget = Vector3.subtract(vector3,currentPosition);
        //desc-X、Z
//        double v = Math.toDegrees(Math.atan2(vector3.x, vector3.z) - Math.atan2(forward.x, forward.z));
        double v = Math.toDegrees(Math.atan2(vecTarget.x, -vecTarget.z) - Math.atan2(forward.x, -forward.z));
        if (Math.abs(v) < 90 ){
            result.x = result.x / getScreenDensity(context);
            result.y = result.y / getScreenDensity(context);
        }else {
            result.x = -result.x / getScreenDensity(context);
            result.y = -result.y / getScreenDensity(context);
        }
        return new int[]{(int) result.x, (int) result.y};
    }

    /**
     *将AR中的顶点组转换为屏幕坐标中的最大矩形的顶点
     * (bounds[0][0],bounds[0][1])-----------------
     * |                                          |
     * |                                          |
     * -----------------(bounds[1][0],bounds[1][1])
     * @param context 上下文
     * @param camera 场景相机
     * @param arVertexGroup  场景中的顶点组
     * @return
     */
    public static int[][] generateBounds(Context context,Camera camera, List<Vector3> arVertexGroup){
        int[][] bounds = new int[2][2];

        int[] temps;
        for (int i = 0; i < arVertexGroup.size(); i++) {
            temps = convertToScreenPoint(context,camera,arVertexGroup.get(i));
            if (i == 0){
                bounds[0][0] = temps[0];//x-min
                bounds[0][1] = temps[0];//x-max
                bounds[1][0] = temps[1];//y-min
                bounds[1][1] = temps[1];//y-max
            }

            if (bounds[0][0] > temps[0]){
                bounds[0][0] = temps[0];
            }

            if (bounds[0][1] < temps[0]){
                bounds[0][1] = temps[0];
            }

            if (bounds[1][0] > temps[1]){
                bounds[1][0] = temps[1];
            }

            if (bounds[1][1] < temps[1]){
                bounds[1][1] = temps[1];
            }
        }

        return bounds;
    }


    /**
     * 将场景中的点转为Z值为0的屏幕坐标
     *      注：坐标原点为屏幕中心
     * @param context 上下文
     * @param camera 场景相机
     * @param vector3 场景卓彪
     * @return
     */
    private static Vector3 covertToScreenPointCenter(Context context,Camera camera, Vector3 vector3) {
        checkScreenPara(context);//检测屏幕参数是否为null
        //获取相机的视图矩阵，转换AR场景中的坐标 => 相机坐标系的新坐标
        vector3 = camera.getViewMatrix().transformPoint(vector3);
//        VIRTUAL_CAMERA = arView.getArSceneView().getScene().getCamera();
//        //获取相机的视图矩阵，转换AR场景中的坐标 => 相机坐标系的新坐标
//        vector3 = VIRTUAL_CAMERA.getViewMatrix().transformPoint(vector3);
        return getScreenPoint(vector3);
    }

    /**
     * 检查屏幕参数
     * @param context
     */
    private static void checkScreenPara(Context context) {
        if (SCREEN_DENSITY == null || SCALE_DP_METER_SCALE == null){
            synchronized (ScreenPointTool.class){
                if (SCREEN_DENSITY == null || SCALE_DP_METER_SCALE == null){
                    //获取屏幕密度
                    WindowManager wm = (WindowManager) context
                            .getSystemService(Context.WINDOW_SERVICE);
                    DisplayMetrics dm = new DisplayMetrics();
                    wm.getDefaultDisplay().getMetrics(dm);
                    SCREEN_DENSITY = dm.density;        //屏幕密度（MI 8 设备屏幕密度为2.75）
                    int width = dm.widthPixels;         // 屏幕宽度（像素）
                    int height = dm.heightPixels;       // 屏幕高度（像素）
                    SCREEN_DENSITY_DPI = dm.densityDpi;
                    float xdpi = dm.xdpi;
                    float ydpi = dm.ydpi;
                    SCALE_DP_METER_SCALE = 2 * SCREEN_DENSITY_DPI / (xdpi + ydpi);
                    //更新SCALE_DP_METER尺度,备注：常用设备的值（MI8/9 为228.8，华为Mate20为198.4）
                    SCALE_DP_METER = SCALE_DP_METER / SCALE_DP_METER_SCALE;

//            SCREEN_WIDTH_HALF= (int)Math.ceil((width / SCREEN_DENSITY) / 2);  // 屏幕宽度(dp)
//            SCREEN_HEIGHT_HALF = (int)Math.ceil((height / SCREEN_DENSITY) / 2);// 屏幕高度(dp)
                    SCREEN_WIDTH_HALF= (width / SCREEN_DENSITY) / 2; // 屏幕宽度(dp)
                    SCREEN_HEIGHT_HALF = (height / SCREEN_DENSITY) / 2;// 屏幕高度(dp)
                    SCREEN_WIDTH_PIXEL = width;
                    SCREEN_HEIGHT_PIXEL = height;
                }
            }
        }
    }

    /**
     * 获取屏幕坐标
     *      step1:相机坐标转单位点的坐标
     *      step2：单位点的坐标转屏幕坐标
     * @param arPoint
     * @return
     */
    private static Vector3 getScreenPoint(Vector3 arPoint){
        //将相机坐标系中的坐标转为单位立方体中的坐标
        arPoint = convertToNearPoint3D(arPoint);
        //再将坐标转为屏幕坐标（原点为屏幕中心），Z值置0；
        arPoint = convertToScreenPointByNearDP(arPoint);
        return arPoint;
    }

    /**
     * 单位球上的点转为屏幕上的点
     * @param arPoint
     * @return
     */
    private static Vector3 convertToScreenPointByNearDP(Vector3 arPoint) {
        arPoint.x = arPoint.x * SCALE_DP_METER * SCREEN_DENSITY + SCREEN_WIDTH_HALF;
        arPoint.y = SCREEN_HEIGHT_HALF - arPoint.y * SCALE_DP_METER * SCREEN_DENSITY;
        arPoint.z = 0;
        //检查点
        checkPoint(arPoint);
        return arPoint;
    }

    /**
     * 转换为近平面的坐标
     * @param arPoint
     * @return
     */
    private static Vector3 convertToNearPoint3D(Vector3 arPoint) {
        //获取原点到目标点的距离
        float distance = (float) Math.sqrt(arPoint.x * arPoint.x + arPoint.y * arPoint.y + arPoint.z * arPoint.z);
        float scale = NEAR / distance;//比例
        arPoint.x = arPoint.x * scale;//X轴的单位分量
        arPoint.y = arPoint.y * scale;
        arPoint.z = arPoint.z * scale;
        return arPoint;
//        return arPoint.normalized();
    }

    /**
     * 检测点
     * @param arPoint
     */
    private static void checkPoint(Vector3 arPoint) {
        if (arPoint.x < 0){
            arPoint.x = 0;
        }
        if (arPoint.x > 2 * SCREEN_WIDTH_HALF){
            arPoint.x = 2 * SCREEN_WIDTH_HALF;
        }
        if (arPoint.y < 0){
            arPoint.y = 0;
        }
        if (arPoint.y > 2 * SCREEN_HEIGHT_HALF){
            arPoint.y = 2 * SCREEN_HEIGHT_HALF;
        }
    }

    /**
     * 获取屏幕密度
     * @param context
     * @return
     */
    public static float getScreenDensity(Context context){
        checkScreenPara(context);
        return SCREEN_DENSITY;
    }

    /**
     * 获取屏幕dpi
     * @param context
     * @return
     */
    public static int getScreenDensityDPI(Context context){
        checkScreenPara(context);
        return SCREEN_DENSITY_DPI;
    }

    /**
     * 获取dp值与真实尺度的比例
     * @param context
     * @return AR场景中1米在布局文件中对应多少dp值
     */
    public static float getDpScale(Context context) {
        checkScreenPara(context);
        return SCALE_DP_METER;
    }

    /**
     * 获取dp值与真实尺度的比例
     * @return AR场景中1米在布局文件中对应多少dp值
     */
    static float getDpScale() {
        return SCALE_DP_METER;
    }

    /**
     * 获取屏幕宽度
     *      单位：dp
     * @param context
     * @return
     */
    public static float getScreenWidth_DP(Context context){
        checkScreenPara(context);
        return 2 * SCREEN_WIDTH_HALF;
    }

    /**
     * 获取屏幕高度
     *      单位：dp
     * @param context
     * @return
     */
    public static float getScreenHeight_DP(Context context){
        checkScreenPara(context);
        return 2 * SCREEN_HEIGHT_HALF;
    }

    /**
     * 获取手机屏幕高度
     * @param context 上下文
     * @return 高度，单位：像素
     */
    public static int getScreenHeightPixel(Context context) {
        checkScreenPara(context);
        return SCREEN_HEIGHT_PIXEL;
    }

    /**
     * 获取手机屏幕宽度
     * @param context 上下文
     * @return 宽度，单位：像素
     */
    public static int getScreenWidthPixel(Context context) {
        checkScreenPara(context);
        return SCREEN_WIDTH_PIXEL;
    }

    /**
     * 屏幕坐标转射线
     * @param context 上下文
     * @param camera 场景相机
     * @param x 点坐标的X值
     * @param y 点坐标的Y值
     * @return

     */
    public static Ray screenPointToRay(Context context,Camera camera, int x, int y){
        checkScreenPara(context);

        // @update by tanyx 2021年5月10日
        Ray ray = camera.screenPointToRay(x,y);
        return ray;
    }

}
