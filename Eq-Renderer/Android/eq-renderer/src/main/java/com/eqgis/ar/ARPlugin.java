package com.eqgis.ar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import com.eqgis.sceneform.ARPlatForm;
import com.huawei.hiar.AREnginesApk;

/**
 * AR引擎
 * <pre>
 * 注意事项：
 *      1、ARCore与AREngine的坐标系不一致。主要受{@link ARCamera#getDisplayOrientedPose()}方法影响
 *      2、为了是AREngine与ARCore使用的坐标系相同，
 *          a、添加了{@link ARCamera#getOffsetAngle()}方法，用于获取AREngine启动时的角度偏移量
 *          b、{ ArSceneView}的onBeginFrame与resume中添加了偏移量的设置
 *          c、ArHelpers中添加了针对AREngine的坐标旋转
 *      3、此外，需特别注意的是：两个引擎平台在图片识别的过程中，创建AnchorNode后获取的Rotation有所差异。
 *      Ikkyu 2022年10月26日
 * </pre>
 */
public class ARPlugin {

    private static boolean usingFixedCoordinate = false;
    private static Exception exception = null;
    private static String message = null;
    private static boolean isARApkInstall;
    private static boolean isEnforceARCore = false;

    public static boolean isUsingARCore(){
        return ARPlatForm.isArCore();
    }

    public static boolean isUsingAREngine(){
        return ARPlatForm.isArEngine();
    }

    public static boolean isUsingFixedCoordinate() {
        return usingFixedCoordinate;
    }

    /**
     * 是否使用坐标修复功能
     * <p>用以解决AREngine在使用{@link ARCamera#getDisplayOrientedPose()}方法，返回结果与ARCore不一致的问题</p>
     * @param usingFixedCoordinate
     */
    public static void setUsingFixedCoordinate(boolean usingFixedCoordinate) {
        ARPlugin.usingFixedCoordinate = usingFixedCoordinate;
    }

    /**
     * use ARCore
     * <pre>
     *     //@Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *              AREngine.enforceARCore();
     *              setContentView(R.layout.activity_main);
     *         }
     * </pre>
     */
    public static void enforceARCore(){
        isEnforceARCore = true;
    }
    public static void enforceARCore(boolean enforceARCore){
        isEnforceARCore = enforceARCore;
    }

    public static boolean isHuawei(){
        if (isEnforceARCore){
            return false;
        }
        String manufacturer = Build.MANUFACTURER;
        return "huawei".equalsIgnoreCase(manufacturer);
        //return false;
    }

    public static void installARApk(Context mContext){
        if (isHuawei()){
            ARPlatForm.setType(ARPlatForm.Type.AR_ENGINE);
//            Intent intent = new Intent(mContext, com.supermap.hiar.common.ConnectAppMarketActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startActivity(new Intent(this, com.huawei.arengine.demos.common.ConnectAppMarketActivity.class));
            Intent intent = new Intent("com.huawei.appmarket.intent.action.AppDetail");
            intent.putExtra("APP_PACKAGENAME", "com.huawei.arengine.service");
            intent.setPackage("com.huawei.appmarket");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }else{
            ARPlatForm.setType(ARPlatForm.Type.AR_CORE);
            Uri uri = Uri.parse("market://details?id=com.google.ar.core");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    /**
     * 判断AR服务的APK是否已就绪
     * @param mContext 上下文
     * @return
     */
    public static boolean isARApkReady( Context mContext ){

        if (isHuawei()){
            ARPlatForm.setType(ARPlatForm.Type.AR_ENGINE);
            return AREnginesApk.isAREngineApkReady(mContext);
        }else{
            ARPlatForm.setType(ARPlatForm.Type.AR_CORE);
            try {
                PackageManager packageManager = mContext.getPackageManager();
                PackageInfo info = packageManager.getPackageInfo("com.google.ar.core", 0);
                return (info.versionCode >= 200603036);
            }catch (PackageManager.NameNotFoundException e){
                return false;
            }
        }
    }

}
