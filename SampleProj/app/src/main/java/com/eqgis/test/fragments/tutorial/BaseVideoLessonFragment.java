package com.eqgis.test.fragments.tutorial;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RawRes;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.ExternalTexture;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.Renderable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 视频、相机与外部纹理教程公共基类
 * <pre>
 *     统一管理 MediaPlayer、ExternalTexture、视频播放状态、三维装饰节点和悬浮控制面板。
 *     页面暂停时暂停解码，销毁时先解绑 Surface，再解除节点与 Renderable 引用。
 * </pre>
 * @author tanyx
 */
public abstract class BaseVideoLessonFragment extends BaseTutorialFragment {
    private static final String TAG = BaseVideoLessonFragment.class.getSimpleName();

    private final List<Node> sceneNodes = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private ExternalTexture externalTexture;
    private boolean playerPrepared;
    private boolean playbackRequested;
    private boolean lifecycleResumed;

    /**
     * 配置普通视频教程场景参数
     * @param sceneLayout 当前教程场景
     */
    protected final void configureVideoScene(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 70);
        sceneLayout.getCamera().setVerticalFovDegrees(62);
        sceneLayout.getCamera().setFarClipPlane(100);
    }

    /**
     * 从 assets 加载视频并绑定外部纹理
     * @param assetPath assets 下的视频路径
     * @param texture 已创建的 {@link ExternalTexture}
     * @param autoPlay 准备完成且页面可见时是否自动播放
     * @param preparedConsumer 播放器准备完成回调
     */
    protected final void prepareAssetVideo(String assetPath, ExternalTexture texture,
                                           boolean autoPlay,
                                           Consumer<MediaPlayer> preparedConsumer) {
        try (AssetFileDescriptor descriptor = requireContext().getAssets().openFd(assetPath)) {
            prepareVideo(descriptor, texture, autoPlay, preparedConsumer);
        } catch (IOException error) {
            Log.e(TAG, "读取 assets 视频失败: " + assetPath, error);
        }
    }

    /**
     * 从 raw 资源加载视频并绑定外部纹理
     * @param rawResId raw 视频资源 id
     * @param texture 已创建的 {@link ExternalTexture}
     * @param autoPlay 准备完成且页面可见时是否自动播放
     * @param preparedConsumer 播放器准备完成回调
     */
    protected final void prepareRawVideo(@RawRes int rawResId, ExternalTexture texture,
                                         boolean autoPlay,
                                         Consumer<MediaPlayer> preparedConsumer) {
        try (AssetFileDescriptor descriptor = getResources().openRawResourceFd(rawResId)) {
            if (descriptor == null) {
                Log.e(TAG, "无法打开 raw 视频资源: " + rawResId);
                return;
            }
            prepareVideo(descriptor, texture, autoPlay, preparedConsumer);
        } catch (IOException error) {
            Log.e(TAG, "读取 raw 视频失败: " + rawResId, error);
        }
    }

    private void prepareVideo(AssetFileDescriptor descriptor, ExternalTexture texture,
                              boolean autoPlay, Consumer<MediaPlayer> preparedConsumer)
            throws IOException {
        releasePlayer();
        externalTexture = texture;
        playbackRequested = autoPlay;
        MediaPlayer player = new MediaPlayer();
        mediaPlayer = player;
        player.setDataSource(
                descriptor.getFileDescriptor(),
                descriptor.getStartOffset(),
                descriptor.getLength());
        player.setLooping(true);
        //desc- ExternalTexture 已创建后再绑定 Surface，视频帧由解码器直接写入 Filament 外部纹理。
        player.setSurface(texture.getSurface());
        player.setOnVideoSizeChangedListener((mediaPlayer, width, height) -> {
            if (externalTexture == texture && width > 0 && height > 0) {
                texture.getSurfaceTexture().setDefaultBufferSize(width, height);
            }
        });
        player.setOnPreparedListener(mediaPlayer -> {
            //desc- 仅使用播放器实例判断回调代际；页面暂停但 View 尚存时仍需记录准备状态，避免恢复后永久停留在未准备状态。
            if (player != this.mediaPlayer) {
                return;
            }
            playerPrepared = true;
            int width = mediaPlayer.getVideoWidth();
            int height = mediaPlayer.getVideoHeight();
            if (width > 0 && height > 0) {
                texture.getSurfaceTexture().setDefaultBufferSize(width, height);
            }
            if (preparedConsumer != null) {
                preparedConsumer.accept(mediaPlayer);
            }
            if (playbackRequested && lifecycleResumed) {
                mediaPlayer.start();
            }
        });
        player.setOnInfoListener((mediaPlayer, what, extra) -> {
            if (player != this.mediaPlayer || !isSceneActive()) {
                return false;
            }
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                onVideoRenderingStarted(mediaPlayer);
            }
            return false;
        });
        player.setOnErrorListener((mediaPlayer, what, extra) -> {
            Log.e(TAG, "MediaPlayer 播放失败, what=" + what + ", extra=" + extra);
            return true;
        });
        player.prepareAsync();
    }

    /**
     * 切换当前视频播放状态
     * @return true 表示切换后请求播放，false 表示切换后暂停
     */
    protected final boolean togglePlayback() {
        playbackRequested = !playbackRequested;
        if (!playerPrepared || mediaPlayer == null) {
            return playbackRequested;
        }
        if (playbackRequested && lifecycleResumed) {
            mediaPlayer.start();
        } else if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        return playbackRequested;
    }

    /**
     * 获取当前播放器
     * @return 已创建的 MediaPlayer，尚未加载时返回 null
     */
    protected final MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    /**
     * 获取当前外部纹理
     * @return 已绑定播放器的外部纹理
     */
    protected final ExternalTexture getExternalTexture() {
        return externalTexture;
    }

    /**
     * 将 Renderable 节点挂载到当前场景
     * @param renderable 渲染对象
     * @param position 节点局部位置
     * @return 已创建节点
     */
    protected final Node addVideoSceneNode(Renderable renderable, Vector3 position) {
        Node node = new Node();
        node.setRenderable(renderable);
        node.setLocalPosition(position);
        node.setParent(sceneLayout.getRootNode());
        sceneNodes.add(node);
        return node;
    }

    /**
     * 创建视频场景装饰 Cube
     * @param position 节点位置
     * @param size Cube 边长
     * @param color 材质颜色
     */
    protected final void addDecorationCube(Vector3 position, float size, Color color) {
        MaterialFactory.makeOpaqueWithColor(requireContext(), color)
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    addVideoSceneNode(
                            GeometryUtils.makeCube(Vector3.one().scaled(size), Vector3.zero(), material),
                            position);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建视频场景装饰 Cube 失败", error);
                    return null;
                });
    }

    /**
     * 创建视频场景装饰平面
     * @param position 平面中心位置
     * @param width 平面宽度
     * @param depth 平面深度
     * @param color 材质颜色
     */
    protected final void addDecorationPlane(Vector3 position, float width, float depth, Color color) {
        MaterialFactory.makeOpaqueWithColor(requireContext(), color)
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    addVideoSceneNode(
                            GeometryUtils.makePlane(
                                    new Vector3(width, 0, depth),
                                    Vector3.zero(),
                                    material),
                            position);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建视频场景装饰平面失败", error);
                    return null;
                });
    }

    /**
     * 在悬浮面板添加状态文本
     * @param container 面板容器
     * @param text 初始文案
     * @return 状态 TextView
     */
    protected final TextView addVideoStatus(LinearLayout container, String text) {
        container.setOrientation(LinearLayout.VERTICAL);
        TextView status = new TextView(requireContext());
        status.setText(text);
        status.setTextColor(0xff333333);
        status.setTextSize(14);
        status.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        status.setPadding(0, dp(4), 0, dp(8));
        container.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return status;
    }

    /**
     * 在悬浮面板添加播放按钮
     * @param container 面板容器
     * @return 播放控制按钮
     */
    protected final Button addPlaybackButton(LinearLayout container) {
        container.setOrientation(LinearLayout.VERTICAL);
        Button button = new Button(requireContext());
        button.setText("暂停视频");
        button.setAllCaps(false);
        button.setOnClickListener(view ->
                button.setText(togglePlayback() ? "暂停视频" : "播放视频"));
        container.addView(button, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return button;
    }

    /**
     * 子类在播放器和节点释放前执行的清理回调
     */
    protected void onBeforeReleaseVideo() {
    }

    /**
     * MediaPlayer 已向视频 Surface 输出首个可渲染画面
     * @param mediaPlayer 当前页面持有的播放器
     */
    protected void onVideoRenderingStarted(MediaPlayer mediaPlayer) {
    }

    @Override
    public void onResume() {
        super.onResume();
        lifecycleResumed = true;
        if (playerPrepared && playbackRequested && mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    public void onPause() {
        lifecycleResumed = false;
        if (playerPrepared && mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        super.onPause();
    }

    /**
     * SceneLayout 销毁前停止解码、解绑 Surface 并解除所有节点引用
     */
    @Override
    protected final void onBeforeDestroyScene() {
        //desc- 先解除时间轴、初始化监听等外部持有，再 release MediaPlayer。
        onBeforeReleaseVideo();
        releasePlayer();
        for (Node node : sceneNodes) {
            node.setRenderable(null);
            node.setParent(null);
        }
        sceneNodes.clear();
        externalTexture = null;
    }

    private void releasePlayer() {
        playerPrepared = false;
        MediaPlayer player = mediaPlayer;
        mediaPlayer = null;
        if (player == null) {
            return;
        }
        player.setOnPreparedListener(null);
        player.setOnVideoSizeChangedListener(null);
        player.setOnInfoListener(null);
        player.setOnErrorListener(null);
        try {
            player.setSurface(null);
        } catch (RuntimeException error) {
            Log.w(TAG, "解绑视频 Surface 失败", error);
        }
        player.release();
    }
}
