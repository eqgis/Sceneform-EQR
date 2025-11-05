package com.eqgis.test.samples;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.eqgis.eqr.layout.SceneViewType;
import com.eqgis.eqr.listener.InitializeListener;
import com.eqgis.media.component.VideoTimeLine;
import com.eqgis.test.BaseActivity;
import com.eqgis.test.R;
import com.google.sceneform.ExSceneView;
import com.google.sceneform.rendering.ExternalTexture;

import java.io.IOException;
/**
 * 视频播放示例 Activity
 * <p>
 *     本示例展示了如何通过 {@link ExSceneView} 与 {@link MediaPlayer} 结合播放视频纹理。
 *     该示例通过 {@link ExternalTexture} 将视频流渲染到场景，
 *     并结合自定义控件 {@link VideoTimeLine} 实现播放进度可视化与交互控制。
 * </p>
 *
 * <h2>主要功能</h2>
 * <ul>
 *     <li>通过 {@link ExSceneView} 初始化视频纹理通道（ExternalTexture）</li>
 *     <li>从 <code>assets/video/eq_test_video.mp4</code> 加载视频并绑定到 MediaPlayer</li>
 *     <li>支持视频播放、暂停与循环播放</li>
 *     <li>支持时间轴 {@link VideoTimeLine} 控件显示播放进度</li>
 *     <li>在点击视频区域时自动显示播放控制按钮</li>
 * </ul>
 *
 * <h2>布局文件</h2>
 * <p>对应布局：{@code res/layout/activity_video_scene.xml}</p>
 *
 * <h2>资源依赖</h2>
 * <ul>
 *     <li>视频资源路径：{@code assets/video/eq_test_video.mp4}</li>
 *     <li>场景视图组件：{@link com.eqgis.eqr.layout.SceneLayout}</li>
 *     <li>扩展渲染视图：{@link ExSceneView}</li>
 * </ul>
 *
 * <h2>使用说明</h2>
 * <p>
 *     运行本 Activity 后，系统将自动加载默认视频资源；
 *     点击视频区域可显示播放按钮，支持暂停/继续播放。
 *     视频播放进度将与 {@link VideoTimeLine} 控件同步更新。
 * </p>
 */
public class VideoActivity extends BaseActivity {

    private ExternalTexture externalTexture;
    private MediaPlayer mediaPlayer;
    private VideoTimeLine videoTimeLine;
    private View videoComponent;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //普通三维场景(场景3选1)
        setContentView(R.layout.activity_video_scene);
        sceneLayout = findViewById(R.id.video_scene_layout);
        sceneLayout.setSceneViewType(SceneViewType.EXTENSION).init(this);
        videoTimeLine = findViewById(R.id.time_line);

        ((ExSceneView)sceneLayout.getSceneView()).setInitializeListener(new InitializeListener() {
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

        videoComponent = findViewById(R.id.video_play_component);
        videoComponent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                findViewById(R.id.video_play_btn).setVisibility(View.VISIBLE);
                return false;
            }
        });

        //播放按钮
        findViewById(R.id.video_play_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaPlayer.isPlaying()){
                    //播放视频
                    mediaPlayer.start();
                }else {
                    mediaPlayer.pause();
                }
                view.setSelected(!view.isSelected());
                view.getHandler().postDelayed(()->{
                    view.setVisibility(View.GONE);
                },1500);
            }
        });
    }

    /**
     * 加载默认视频
     * @throws IOException
     */
    private void loadDefaultVideo() throws IOException {
        //这里使用eq_test_video.mp4为例，实际上，你也可以通过其它方式创建MediaPlayer，并设置数据源
        mediaPlayer = new MediaPlayer();
        // 获取assets中的mp4文件
        AssetFileDescriptor afd = getAssets().openFd("video/eq_test_video.mp4");
        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        mediaPlayer.prepare();
        videoTimeLine.bindView(sceneLayout.getSceneView(),mediaPlayer);
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
    }
}
