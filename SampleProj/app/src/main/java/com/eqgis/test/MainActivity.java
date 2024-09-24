package com.eqgis.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.eqgis.ar.ARPlugin;
import com.eqgis.eqr.core.Eqr;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
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
                        Manifest.permission.READ_CONTACTS
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
//                ARPlatForm.setType(ARPlatForm.Type.AR_CORE);
//                Uri uri = Uri.parse("market://details?id=com.google.ar.core");
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
                try {
                    ArCoreApk.getInstance().requestInstall(this, true);
                } catch (UnavailableDeviceNotCompatibleException e) {
//                    throw new RuntimeException(e);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (UnavailableUserDeclinedInstallationException e) {
//                    throw new RuntimeException(e);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
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
}