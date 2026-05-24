package com.eqgis.test.fragments.common;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.layout.SceneViewType;
import com.eqgis.eqr.listener.InitializeListener;
import com.eqgis.test.fragments.BaseSampleFragment;
import com.google.sceneform.ExSceneView;
import com.google.sceneform.rendering.ExternalTexture;

import java.io.IOException;

/**
 * 视频渲染常用示例 Fragment
 * <pre>
 *     使用 ExSceneView 和 ExternalTexture 将本地视频绑定到 Filament 外部纹理。
 *     当前常用示例默认跳转原 Activity，本类保留给后续 Fragment 化迁移。
 * </pre>
 * @author tanyx
 */
public class VideoCommonFragment extends BaseSampleFragment {
    private ExternalTexture externalTexture;
    private MediaPlayer mediaPlayer;

    @Override
    protected String getLessonTitle() {
        return "视频渲染";
    }

    @Override
    protected String getLessonDescription() {
        return "使用 ExSceneView 和 ExternalTexture 将本地视频渲染到三维场景，并提供播放/暂停控制。";
    }

    /**
     * 创建支持外部纹理的 SceneLayout
     * @return 使用 EXTENSION 类型 SceneView 的 {@link SceneLayout}
     */
    @Override
    protected SceneLayout createSceneLayout() {
        return new SceneLayout(requireContext()).setSceneViewType(SceneViewType.EXTENSION);
    }

    /**
     * 初始化视频纹理场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        ExSceneView exSceneView = (ExSceneView) sceneLayout.getSceneView();
        exSceneView.setInitializeListener(new InitializeListener() {
            @Override
            public void initializeTexture(ExternalTexture texture) {
                externalTexture = texture;
                try {
                    //desc- 外部纹理创建完成后才能把 MediaPlayer Surface 绑定到纹理上。
                    loadDefaultVideo();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        sceneLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
    }

    /**
     * 初始化播放控制按钮
     * @param actionContainer 操作按钮容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        Button playButton = new Button(requireContext());
        playButton.setText("播放 / 暂停");
        playButton.setOnClickListener(v -> {
            if (mediaPlayer == null) {
                return;
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        });
        actionContainer.addView(playButton);
    }

    @Override
    public void onDestroyView() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroyView();
    }

    /**
     * 加载默认视频资源
     * @throws IOException assets 视频读取失败时抛出
     */
    private void loadDefaultVideo() throws IOException {
        mediaPlayer = new MediaPlayer();
        AssetFileDescriptor afd = requireContext().getAssets().openFd("video/eq_test_video.mp4");
        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        mediaPlayer.prepare();
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnVideoSizeChangedListener((player, width, height) -> {
            if (externalTexture != null) {
                externalTexture.getSurfaceTexture().setDefaultBufferSize(width, height);
                player.setSurface(externalTexture.getSurface());
            }
        });
    }
}
