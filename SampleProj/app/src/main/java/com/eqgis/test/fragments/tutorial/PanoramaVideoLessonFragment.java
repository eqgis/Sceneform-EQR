package com.eqgis.test.fragments.tutorial;

import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.media.component.VideoTimeLine;
import com.eqgis.test.R;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.ExternalTexture;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.ModelRenderable;

/**
 * 360 度全景视频教程
 * <pre>
 *     将全景视频绑定到 ExternalTexture，并把视频材质映射到内球面；相机位于球体内部，
 *     可通过教程通用相机手势环视场景。
 * </pre>
 * @author tanyx
 */
public class PanoramaVideoLessonFragment extends BaseVideoLessonFragment {
    private static final String TAG = PanoramaVideoLessonFragment.class.getSimpleName();

    private Material panoramaMaterial;
    private boolean videoPrepared;
    private boolean panoramaCreated;
    private TextView statusText;
    private Button playbackButton;
    private VideoTimeLine videoTimeLine;

    @Override
    protected String getLessonTitle() {
        return "全景视频";
    }

    @Override
    protected String getLessonDescription() {
        return "构建半径 30 米的内球面并贴入 360° 视频；相机位于球心，可单指旋转查看环绕画面。";
    }

    /**
     * 初始化全景视频与球体内部的 Cube、Plane 装饰
     * @param sceneLayout 当前教程场景
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureVideoScene(sceneLayout);
        sceneLayout.getCamera().setFarClipPlane(80);
        addDecorationPlane(
                new Vector3(0, -1.15f, -4.0f),
                6.0f,
                5.5f,
                new Color(0.05f, 0.10f, 0.18f));
        addDecorationCube(
                new Vector3(0, -0.52f, -3.2f),
                0.9f,
                new Color(1.0f, 0.55f, 0.10f));

        ExternalTexture texture = new ExternalTexture();
        //desc- 先登记当前 ExternalTexture，避免缓存材质同步返回时因纹理尚未登记而永久跳过球体创建。
        prepareRawVideo(
                R.raw.vr_video_city,
                texture,
                true,
                mediaPlayer -> {
                    videoPrepared = true;
                    if (videoTimeLine != null) {
                        videoTimeLine.bindView(sceneLayout.getSceneView(), mediaPlayer);
                    }
                    updateStatus("全景视频已准备，正在启动纹理渲染…");
                    tryCreatePanorama();
                });
        loadPanoramaMaterial(texture);
    }

    private void loadPanoramaMaterial(ExternalTexture texture) {
        Material.builder()
                .setSource(requireContext(), com.eqgis.eqr.R.raw.external_chroma_key_video_material)
                .build()
                .thenAccept(material -> {
                    if (!isSceneActive() || getExternalTexture() != texture) {
                        return;
                    }
                    panoramaMaterial = material;
                    material.setFloat4("keyColor", new Color(0, 0, 0, 1));
                    tryCreatePanorama();
                })
                .exceptionally(error -> {
                    Log.e(TAG, "加载全景视频材质失败", error);
                    updateStatus("全景视频材质加载失败");
                    return null;
                });
    }

    private void tryCreatePanorama() {
        if (panoramaCreated
                || !videoPrepared
                || panoramaMaterial == null
                || getExternalTexture() == null
                || !isSceneActive()) {
            return;
        }
        //desc- 仅在球体即将创建时绑定 ExternalTexture，避免 Tab 快速切换留下未挂载的 MaterialInstance。
        panoramaMaterial.setExternalTexture("videoTexture", getExternalTexture());
        ModelRenderable sphere = GeometryUtils.makeInnerSphere(
                30,
                Vector3.zero(),
                panoramaMaterial);
        sphere.setShadowCaster(false);
        sphere.setShadowReceiver(false);
        addVideoSceneNode(sphere, Vector3.zero());
        panoramaCreated = true;
        updateStatus("全景纹理已挂载，正在内球面循环播放");
    }

    /**
     * 首个全景视频画面已写入 ExternalTexture 后更新面板状态
     * @param mediaPlayer 当前页面播放器
     */
    @Override
    protected void onVideoRenderingStarted(MediaPlayer mediaPlayer) {
        updateStatus("全景纹理已挂载，正在内球面循环播放，可单指环视");
    }

    private void updateStatus(String text) {
        if (statusText != null) {
            statusText.setText(text);
        }
    }

    /**
     * 创建全景视频控制面板
     * @param actionContainer 悬浮面板容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        statusText = addVideoStatus(actionContainer, "正在初始化全景视频…");
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
        if (playbackButton != null) {
            playbackButton.setOnClickListener(null);
        }
        panoramaMaterial = null;
        videoPrepared = false;
        panoramaCreated = false;
        statusText = null;
        playbackButton = null;
        videoTimeLine = null;
    }
}
