package com.eqgis.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.eqgis.eqr.core.Eqr;

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
     * 转至使用EQ Slam的三维场景（AR）
     */
    public void toSlamSceneActivity(View view) {
        startActivity(new Intent(this, SlamSceneActivity.class));
    }
}