package com.eqgis.test;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.TimedMetaData;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.media.component.VideoTimeLine;
import com.google.sceneform.ExSceneView;
import com.google.sceneform.FrameTime;
import com.google.sceneform.Scene;
import com.google.sceneform.rendering.ExternalTexture;

import java.io.IOException;

/**
 * 视频Activity
 * <p>
 *     本示例介绍如何通过ExSceneView播放视频
 * </p>
 * @author tanyx 2024/8/26
 **/
public class VideoActivity extends BaseActivity{

    private ExternalTexture externalTexture;
    private MediaPlayer mediaPlayer;
    private VideoTimeLine videoTimeLine;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //启用ExSceneView，以便实现背景的自定义
        SceneLayout.enableExSceneView(true);

        super.onCreate(savedInstanceState);

        //普通三维场景(场景3选1)
        setContentView(R.layout.activity_video_scene);
        sceneLayout = findViewById(R.id.video_scene_layout);
        videoTimeLine = findViewById(R.id.time_line);

        sceneLayout.getExSceneView().setInitializeListener(new ExSceneView.InitializeListener() {
            @Override
            public void initializeTexture(ExternalTexture texture) {
                //纹理初始化成功时，触发回调
                externalTexture = texture;
                try {
                    loadDefaultVideo();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * 加载默认视频
     * @throws IOException
     */
    private void loadDefaultVideo() throws IOException {
        //这里使用eq_test_video.mp4为例，实际上，你也可以通过其它方式创建MediaPlayer，并设置数据源
        mediaPlayer = MediaPlayer.create(this,R.raw.eq_test_video);
        videoTimeLine.bindView(sceneLayout.getExSceneView(),mediaPlayer);
        mediaPlayer.setLooping(true);//循环播放
        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mediaPlayer, int w, int h) {
                if (externalTexture != null){
                    externalTexture.getSurfaceTexture().setDefaultBufferSize(w, h);
                    mediaPlayer.setSurface(externalTexture.getSurface());
                }
            }
        });
        //就绪时，自动播放
        mediaPlayer.start();
    }
}
