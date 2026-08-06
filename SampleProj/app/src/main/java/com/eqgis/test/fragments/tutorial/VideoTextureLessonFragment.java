package com.eqgis.test.fragments.tutorial;

import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.media.component.VideoTimeLine;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.ExternalTexture;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.ModelRenderable;

/**
 * 视频外部纹理教程
 * <pre>
 *     使用 MediaPlayer 向 ExternalTexture 持续写入视频帧，再通过自定义 Filament
 *     视频材质把同一段循环视频分别贴到 Cube 和 Quad 上。
 * </pre>
 * @author tanyx
 */
public class VideoTextureLessonFragment extends BaseVideoLessonFragment {
    private static final String TAG = VideoTextureLessonFragment.class.getSimpleName();

    private Material videoMaterial;
    private boolean videoPrepared;
    private boolean videoGeometryCreated;
    private TextView statusText;
    private Button playbackButton;
    private VideoTimeLine videoTimeLine;

    @Override
    protected String getLessonTitle() {
        return "视频纹理";
    }

    @Override
    protected String getLessonDescription() {
        return "将循环播放的视频绑定到 ExternalTexture，并用同一视频流驱动 Cube 与 Quad 的 Filament 材质。";
    }

    /**
     * 初始化视频纹理与地面装饰
     * @param sceneLayout 当前教程场景
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureVideoScene(sceneLayout);
        addDecorationPlane(
                new Vector3(0, -0.95f, -3.8f),
                5.6f,
                5.0f,
                new Color(0.07f, 0.13f, 0.22f));

        ExternalTexture texture = new ExternalTexture();
        //desc- 先登记当前 ExternalTexture。材质命中资源缓存时 build 回调可能同步执行，反向调用会误判为旧纹理并丢弃材质。
        prepareAssetVideo(
                "video/eq_test_video.mp4",
                texture,
                true,
                mediaPlayer -> {
                    videoPrepared = true;
                    if (videoTimeLine != null) {
                        videoTimeLine.bindView(sceneLayout.getSceneView(), mediaPlayer);
                    }
                    updateStatus("视频已准备，正在启动纹理渲染…");
                    tryCreateVideoGeometry();
                });
        loadVideoMaterial(texture);
    }

    private void loadVideoMaterial(ExternalTexture texture) {
        Material.builder()
                .setSource(requireContext(), com.eqgis.eqr.R.raw.external_chroma_key_video_material)
                .build()
                .thenAccept(material -> {
                    if (!isSceneActive() || getExternalTexture() != texture) {
                        return;
                    }
                    videoMaterial = material;
                    tryCreateVideoGeometry();
                })
                .exceptionally(error -> {
                    Log.e(TAG, "加载视频纹理材质失败", error);
                    updateStatus("视频材质加载失败");
                    return null;
                });
    }

    private void tryCreateVideoGeometry() {
        if (videoGeometryCreated
                || !videoPrepared
                || videoMaterial == null
                || getExternalTexture() == null
                || !isSceneActive()) {
            return;
        }
        ExternalTexture texture = getExternalTexture();

        //desc- 模板材质本身用于 Cube，副本用于 Quad，确保每个绑定外部纹理的 MaterialInstance 都挂载到 Node。
        Material cubeMaterial = videoMaterial;
        Material quadMaterial = videoMaterial.makeCopy();
        bindVideoTexture(cubeMaterial, texture);
        bindVideoTexture(quadMaterial, texture);
        ModelRenderable cube = GeometryUtils.makeCube(
                new Vector3(1.25f, 1.25f, 1.25f),
                Vector3.zero(),
                cubeMaterial);
        disableVideoShadows(cube);
        addVideoSceneNode(cube, new Vector3(-0.95f, -0.30f, -3.55f));

        ModelRenderable quad = GeometryUtils.makeQuad(
                new Vector3(2.0f, 1.15f, 0),
                Vector3.zero(),
                quadMaterial);
        disableVideoShadows(quad);
        addVideoSceneNode(quad, new Vector3(1.05f, -0.25f, -3.65f));
        videoGeometryCreated = true;
        updateStatus("视频纹理已挂载，正在 Cube 与 Quad 上循环播放");
    }

    /**
     * 首个视频画面已写入 ExternalTexture 后更新面板状态
     * @param mediaPlayer 当前页面播放器
     */
    @Override
    protected void onVideoRenderingStarted(MediaPlayer mediaPlayer) {
        updateStatus("视频纹理已挂载，正在 Cube 与 Quad 上循环播放");
    }

    private void bindVideoTexture(Material material, ExternalTexture texture) {
        material.setFloat4("keyColor", new Color(0.1843f, 1.0f, 0.098f, 1.0f));
        material.setExternalTexture("videoTexture", texture);
    }

    private void disableVideoShadows(ModelRenderable renderable) {
        renderable.setShadowCaster(false);
        renderable.setShadowReceiver(false);
    }

    private void updateStatus(String text) {
        if (statusText != null) {
            statusText.setText(text);
        }
    }

    /**
     * 创建视频播放状态、播放按钮与时间轴
     * @param actionContainer 悬浮面板容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        statusText = addVideoStatus(actionContainer, "正在初始化 ExternalTexture…");
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
        videoMaterial = null;
        videoPrepared = false;
        videoGeometryCreated = false;
        statusText = null;
        playbackButton = null;
        videoTimeLine = null;
    }
}
