package com.eqgis.test.fragments.tutorial;

import android.media.MediaPlayer;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.layout.SceneViewType;
import com.eqgis.media.component.VideoTimeLine;
import com.google.sceneform.ExSceneView;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;

/**
 * 二维视频背景播放教程
 * <pre>
 *     使用 ExSceneView 的 ExternalTexture 通道把 assets 视频作为屏幕背景，
 *     同时在三维场景中叠加 Cube、Plane，并通过 VideoTimeLine 控制播放进度。
 * </pre>
 * @author tanyx
 */
public class VideoBackgroundLessonFragment extends BaseVideoLessonFragment {
    private TextView statusText;
    private Button playbackButton;
    private VideoTimeLine videoTimeLine;

    @Override
    protected String getLessonTitle() {
        return "2D 视频背景";
    }

    @Override
    protected String getLessonDescription() {
        return "使用 ExSceneView 将本地视频作为二维背景循环播放，并通过进度条定位视频；Cube 和 Plane 仍由三维场景渲染。";
    }

    /**
     * 创建支持背景外部纹理的 SceneLayout
     * @return EXTENSION 类型场景布局
     */
    @Override
    protected SceneLayout createSceneLayout() {
        return new SceneLayout(requireContext()).setSceneViewType(SceneViewType.EXTENSION);
    }

    /**
     * 初始化二维视频背景及三维装饰物
     * @param sceneLayout 当前教程场景
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureVideoScene(sceneLayout);
        addDecorationPlane(
                new Vector3(0, -0.9f, -3.8f),
                5.2f,
                4.8f,
                new Color(0.12f, 0.30f, 0.48f));
        addDecorationCube(
                new Vector3(-1.0f, -0.45f, -3.2f),
                0.8f,
                new Color(1.0f, 0.52f, 0.08f));
        addDecorationCube(
                new Vector3(1.05f, -0.58f, -4.2f),
                0.62f,
                new Color(0.10f, 0.62f, 1.0f));

        ExSceneView sceneView = (ExSceneView) sceneLayout.getSceneView();
        sceneView.setInitializeListener(texture -> {
            if (!isSceneActive()) {
                return;
            }
            prepareAssetVideo(
                    "video/eq_test_video.mp4",
                    texture,
                    true,
                    mediaPlayer -> {
                        if (videoTimeLine != null) {
                            videoTimeLine.bindView(sceneLayout.getSceneView(), mediaPlayer);
                        }
                        if (statusText != null) {
                            statusText.setText("视频已准备，正在启动背景播放…");
                        }
                    });
        });
    }

    /**
     * 首个背景视频画面开始渲染后更新面板状态
     * @param mediaPlayer 当前页面播放器
     */
    @Override
    protected void onVideoRenderingStarted(MediaPlayer mediaPlayer) {
        if (statusText != null) {
            statusText.setText("背景视频正在循环播放");
        }
    }

    /**
     * 创建播放按钮和视频进度条
     * @param actionContainer 悬浮面板容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        statusText = addVideoStatus(actionContainer, "等待 ExternalTexture 初始化…");
        playbackButton = addPlaybackButton(actionContainer);
        videoTimeLine = new VideoTimeLine(requireContext());
        actionContainer.addView(videoTimeLine, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(28)));
    }

    @Override
    protected void onBeforeReleaseVideo() {
        if (videoTimeLine != null) {
            videoTimeLine.unbindView();
        }
        if (sceneLayout != null && sceneLayout.getSceneView() instanceof ExSceneView) {
            ((ExSceneView) sceneLayout.getSceneView()).setInitializeListener(null);
        }
        if (playbackButton != null) {
            playbackButton.setOnClickListener(null);
        }
        playbackButton = null;
        videoTimeLine = null;
        statusText = null;
    }
}
