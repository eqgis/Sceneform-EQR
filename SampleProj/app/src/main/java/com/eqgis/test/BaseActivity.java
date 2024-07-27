package com.eqgis.test;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.test.scene.ISampleScene;

/**
 * 基础场景
 * <p>在相关时刻调用SceneLayout的resume、pause、destroy</p>
 **/
public class BaseActivity extends AppCompatActivity {

    protected SceneLayout sceneLayout;

    /**
     * 示例场景
     */
    protected ISampleScene sampleScene;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏展示
        window.setFlags(flag,flag);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sceneLayout.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sceneLayout.pause();
    }

    @Override
    protected void onDestroy() {
        if (sampleScene != null)
            sampleScene.destroy(this);
        sceneLayout.destroy();
        super.onDestroy();
    }
}
