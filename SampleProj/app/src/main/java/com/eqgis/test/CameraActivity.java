package com.eqgis.test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.sceneform.CameraSceneView;

import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

    private Camera camera;
    private CameraSceneView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

//        textureView = findViewById(R.id.texture_view);
//        textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        textureView.resume();
        openCamera();
    }

    private void openCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            return;
        }

        camera = Camera.open(); // 打开相机

        try {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setRotation(270);
//            camera.setDisplayOrientation(90); // 旋转显示角度
            camera.setParameters(parameters);

            SurfaceTexture surfaceTexture = textureView.getExternalTexture().getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
            Log.i("IKKYU ", "SurfaceTexture: w:"+ textureView.getWidth() + "   h:" +textureView.getHeight());

            camera.setPreviewTexture(surfaceTexture); // 设置预览纹理
            camera.startPreview(); // 开始预览
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

