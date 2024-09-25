package com.eqgis.test;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.eqgis.ar.ARPlugin;
import com.eqgis.eqr.ARCoreApkInstaller;
import com.eqgis.eqr.core.Eqr;
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
        System.out.println();
        boolean arApkReady = ARPlugin.isARApkReady(this);
        Toast.makeText(this, "AR服务支持状态："+arApkReady, Toast.LENGTH_SHORT).show();
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
                        .setPositiveButton("本地安装", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                installAPK();
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
//                try {
//                    ArCoreApk.getInstance().requestInstall(this, true);
//                } catch (UnavailableDeviceNotCompatibleException e) {
////                    throw new RuntimeException(e);
//                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                } catch (UnavailableUserDeclinedInstallationException e) {
////                    throw new RuntimeException(e);
//                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                }

            }
        }

    }
    private void installAPK() {
        ARCoreApkInstaller installer = new ARCoreApkInstaller(this, "core145.eqr");
        if(!ARPlugin.isARApkReady(this)){
            setInstallPermission(installer);
        }else {
            Toast.makeText(this, "已经安装过ARCore了", Toast.LENGTH_SHORT).show();
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
        startActivity(new Intent(this, ARSceneActivity.class));
    }

    /**
     * 转至AR三维场景
     */
    public void toArScenePlaneActivity(View view) {
        startActivity(new Intent(this, ARScenePlaneActivity.class));
    }

    /**
     * 转至使用EQ Slam的三维场景（AR）
     */
    public void toSlamSceneActivity(View view) {
        startActivity(new Intent(this, SlamSceneActivity.class));
    }

    /**
     * 转至手势交互示例
     */
    public void toInteractiveActivity(View view) {
        startActivity(new Intent(this,InteractiveActivity.class));
    }

    /**
     * 转至视频播放器示例
     */
    public void toVideoActivity(View view) {
        startActivity(new Intent(this,VideoActivity.class));
    }



    public void setInstallPermission(ARCoreApkInstaller installer){
        boolean haveInstallPermission;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //先判断是否有安装未知来源应用的权限
            haveInstallPermission = getPackageManager().canRequestPackageInstalls();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if(!haveInstallPermission){
                builder.setTitle("提示")
                        .setMessage("请您开启安装应用的权限，允许当前程序为您安装“Google Play Services for AR”");
                //弹框提示用户手动打开
                builder.setPositiveButton("开启权限", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            //此方法需要API>=26才能使用
                            toInstallPermissionSettingIntent();
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).show();
                // toInstallPermissionSettingIntent();
            }else{
                installer.install();
            }
        }else {
            //低于安卓8.0的版本，允许默认安装
            installer.install();
        }
    }


    /**
     * 开启安装未知来源权限
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void toInstallPermissionSettingIntent() {
        Uri packageURI = Uri.parse("package:"+this.getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 0x13147) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (getPackageManager().canRequestPackageInstalls()) {
                    // 权限已被授予，可以安装应用
                    installAPK();
                } else {
                    // 权限被拒绝
                    Toast.makeText(this, "Permission denied to install apps.", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}