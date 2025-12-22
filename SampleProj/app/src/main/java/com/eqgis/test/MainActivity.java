package com.eqgis.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.eqgis.ar.ARPlugin;
import com.eqgis.eqr.core.Eqr;
import com.eqgis.test.samples.ARSceneActivity;
import com.eqgis.test.samples.ARScenePlaneActivity;
import com.eqgis.test.samples.BaseSceneActivity;
import com.eqgis.test.samples.CameraActivity;
import com.eqgis.test.samples.EarthActivity;
import com.eqgis.test.samples.InteractiveActivity;
//import com.eqgis.test.samples.SlamSceneActivity;
import com.eqgis.test.samples.VRScene360Activity;
import com.eqgis.test.samples.VRSceneActivity;
import com.eqgis.test.samples.VideoActivity;
import com.google.sceneform.ARPlatForm;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //申请权限
        ActivityCompat.requestPermissions(this,
                new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.VIBRATE,
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.ACCESS_WIFI_STATE,
                        android.Manifest.permission.WAKE_LOCK,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_PHONE_STATE,
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.INSTALL_PACKAGES
                }, PackageManager.PERMISSION_GRANTED);

        setContentView(R.layout.activity_main);

        if (Eqr.getCoreStatus()){
            Toast.makeText(this, "当前版本："+Eqr.getCoreVersion(), Toast.LENGTH_SHORT).show();   
        }else {
            Toast.makeText(this, "渲染器不可用", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkAR(View view){
        boolean arApkReady = ARPlugin.isARApkReady(this);
        Toast.makeText(this, "AR服务安装状态："+arApkReady, Toast.LENGTH_SHORT).show();
        if (!arApkReady){
            if (ARPlugin.isHuawei()){
                ARPlatForm.setType(ARPlatForm.Type.AR_ENGINE);
                Intent intent = new Intent("com.huawei.appmarket.intent.action.AppDetail");
                intent.putExtra("APP_PACKAGENAME", "com.huawei.arengine.service");
                intent.setPackage("com.huawei.appmarket");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("需要安装谷歌AR服务")
                        .setMessage("请选择安装方式，安装“Google Play Services for AR”（或在应用商店输入“ARCore”关键词进行搜索）")
                        .setPositiveButton("通过浏览器下载", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 要打开的链接地址
                                String url = "http://file.eqgis.cn/img/Google_Play_Services_for_AR_1.45.0.apk";
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            }
                        }).setNegativeButton("通过应用商店安装", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ARPlatForm.setType(ARPlatForm.Type.AR_CORE);
                                Uri uri = Uri.parse("market://details?id=com.google.ar.core");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }

    }

    /**
     * 转至普通三维场景
     */
    public void toBaseSceneActivity(View view) {
        startActivity(new Intent(this, BaseSceneActivity.class));
    }

    /**
     * 转至AR三维场景
     */
    public void toArSceneActivity(View view) {
        checkAR(null);//跳转前需检查AR服务支持状态
        startActivity(new Intent(this, ARSceneActivity.class));
    }

    /**
     * 转至AR三维场景
     */
    public void toArScenePlaneActivity(View view) {
        checkAR(null);//跳转前需检查AR服务支持状态
        startActivity(new Intent(this, ARScenePlaneActivity.class));
    }

//    /**
//     * 转至使用EQ Slam的三维场景（AR）
//     */
//    public void toSlamSceneActivity(View view) {
//        startActivity(new Intent(this, SlamSceneActivity.class));
//    }

    /**
     * 转至手势交互示例
     */
    public void toInteractiveActivity(View view) {
        startActivity(new Intent(this, InteractiveActivity.class));
    }

    /**
     * 转至视频播放器示例
     */
    public void toVideoActivity(View view) {
        startActivity(new Intent(this, VideoActivity.class));
    }

    /**
     * 转至相机示例
     */
    public void toCameraActivity(View view) {
        startActivity(new Intent(this, CameraActivity.class));
    }

    /**
     * 转至VR示例
     * @param view
     */
    public void toVrActivity(View view) {
        startActivity(new Intent(this, VRSceneActivity.class));
    }
    /**
     * 转至VR示例
     * @param view
     */
    public void toVr360Activity(View view) {
        startActivity(new Intent(this, VRScene360Activity.class));
    }

    /**
     * 转至地球示例
     * @param view
     */
    public void toEarthActivity(View view) {
        startActivity(new Intent(this, EarthActivity.class));
    }
}