package com.eqgis.sceneform.rendering;

import android.content.Context;
import android.media.MediaPlayer;

import androidx.annotation.Nullable;

import com.eqgis.sceneform.Node;
import com.eqgis.sceneform.math.Vector3;
import com.eqgis.sceneform.utilities.LoadHelper;

/**
 * Node that can show a video by passing a {@link MediaPlayer} instance. Note that
 * VideoNode does not manage video playback by itself (e.g. starting the video).
 * <p>
 * Filtering out a specific color in the video is also supported by
 * defining a chroma key color.
 * </p>
 * <p>
 * Optionally an {@link ExternalTexture} can be passed if multiple VideoNode instances
 * need to render the exact same instance of a video. This will also improve performance
 * dramatically instead of rendering each instance separately.
 * </p>
 */
public class VideoNode extends Node {
    private static final String MATERIAL_PARAMETER_VIDEO_TEXTURE = "videoTexture";
    private static final String MATERIAL_PARAMETER_CHROMA_KEY_COLOR = "keyColor";

    private final MediaPlayer player;
    private final ExternalTexture texture;
    private final Color chromaKeyColor;
    private final Listener listener;
    private Vector3 mCenter;
    private final VideoAlignment mVideoAlignment;


    /**
     * Create a new VideoNode for showing a video from a MediaPlayer instance inside a node on an
     * adjusted plane renderable
     *
     * @param context       Resources context
     * @param player        The video media player to render on the plane node
     * @param listener Loading listener
     */
    public VideoNode(Context context, MediaPlayer player, @Nullable Listener listener) {
        this(context, player, null, VideoAlignment.CENTER, listener);
    }

    /**
     * Create a new VideoNode for showing a video from a MediaPlayer instance inside a node on an
     * adjusted plane renderable with video transparency color set to chromaKeyColor.
     *
     * @param context        Resources context
     * @param player         The video media player to render on the plane node
     * @param chromaKeyColor Chroma Key color to made the video transparent from
     * @param listener  Loading listener
     */
    public VideoNode(Context context, MediaPlayer player, @Nullable Color chromaKeyColor,VideoAlignment videoAlignment,
                     @Nullable Listener listener) {
        this(context, player, chromaKeyColor, null,videoAlignment, listener);
    }

    /**
     * Create a new VideoNode for showing a video from a MediaPlayer instance inside a node on your
     * own material renderable and material with video transparency color set to chromaKeyColor
     *
     * @param context        Resources context
     * @param player         The video media player to render on the plane node
     * @param chromaKeyColor Chroma Key color to made the video transparent from
     * @param texture        Custom ExternalTexture for using your own renderable and material.
     *                       Null for default Plane shape renderable.
     * @param listener  Loading listener
     */
    public VideoNode(Context context, MediaPlayer player, @Nullable Color chromaKeyColor,
                     @Nullable ExternalTexture texture,VideoAlignment videoAlignment, @Nullable Listener listener) {
        this.player = player;
        this.texture = texture != null ? texture : new ExternalTexture();
        this.chromaKeyColor = chromaKeyColor;
        this.listener = listener;
        this.mVideoAlignment=videoAlignment;
        this.mCenter=new Vector3();
        init(context);
    }

    private void init(Context context) {
        player.setSurface(texture.getSurface());
        final int rawResId;
        if (chromaKeyColor != null) {
            rawResId = LoadHelper.rawResourceNameToIdentifier(context,"external_chroma_key_video_material");
        } else {
            rawResId = LoadHelper.rawResourceNameToIdentifier(context,"external_chroma_key_video_material");
        }
        Material.builder()
                .setSource(context, rawResId)
                .build()
                .thenAccept(material -> {
                    material.setExternalTexture(MATERIAL_PARAMETER_VIDEO_TEXTURE, texture);
                    if (chromaKeyColor != null) {
                        material.setFloat4(MATERIAL_PARAMETER_CHROMA_KEY_COLOR, chromaKeyColor);
                    }
                    final Renderable renderable = createModel(player, material);
                    setRenderable(renderable);
                    onCreated(this);
                })
                .exceptionally(throwable -> {
                    onError(throwable);
                    return null;
                });
    }

    /**
     * Create the renderable on which the video will be displayed.
     * Override this function for using a custom Model.
     * Default return is a centered plane
     *
     * @param player   the media player
     * @param material the material to apply to your custom model
     * @return the the renderable with the applied material.
     */
    public Renderable createModel(MediaPlayer player, Material material) {
        final int width = player.getVideoWidth();
        final int height = player.getVideoHeight();
        final float x;
        final float y;
        if (width >= height) {
            x = 1.0f;
            y = (float) height / (float) width;
        } else {
            x = (float) width / (float) height;
            y = 1.0f;
        }
        return makePlane(x, y, material);
    }

    public Renderable makePlane(float width, float height, Material material) {
        switch (mVideoAlignment){
            case LEFT_TOP:
                mCenter=new Vector3(width/2,-height/2,0);
                break;
            case TOP:
                mCenter=new Vector3(0,-height/2,0);
                break;
            case RIGHT_TOP:
                mCenter=new Vector3(-width/2,-height/2,0);
                break;
            case LEFT:
                mCenter=new Vector3(width/2,0,0);
                break;
            case CENTER:
                mCenter=new Vector3(0,0,0);
                break;
            case RIGHT:
                mCenter=new Vector3(-width/2,0,0);
                break;
            case LEFT_BOTTOM:
                mCenter=new Vector3(width/2,height/2,0);
                break;
            case BOTTOM:
                mCenter=new Vector3(0,height/2,0);
                break;
            case RIGHT_BOTTOM:
                mCenter=new Vector3(-width/2,height/2,0);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + mVideoAlignment);
        }
        return PlaneFactory.makePlane(
                new Vector3(width, height, 0.0f),
                mCenter,
                material
        );
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public ExternalTexture getTexture() {
        return texture;
    }

    public Color getChromaKeyColor() {
        return chromaKeyColor;
    }

    private void onCreated(VideoNode videoNode) {
        if (listener != null) {
            listener.onCreated(videoNode);
        }
    }

    private void onError(Throwable throwable) {
        if (listener != null) {
            listener.onError(throwable);
        }
    }

    /**
     * Listener for VideoNode loading
     */
    public interface OnCreateListener {
        /**
         * Something wrong happened during the VideoNode instantiation
         *
         * @param throwable corresponding error
         */
        void onError(Throwable throwable);
    }

    /**
     * Listener for VideoNode loading
     */
    public interface Listener {

        /**
         * Called when the renderable and material have been set on this node.
         *
         * @param videoNode the created instance of the model.
         */
        void onCreated(VideoNode videoNode);

        /**
         * Something wrong happened during the VideoNode instantiation
         *
         * @param throwable corresponding error
         */
        void onError(Throwable throwable);
    }

    public enum VideoAlignment{
        LEFT_TOP,	//左上
        TOP,		//上
        RIGHT_TOP,	//右上
        LEFT,		//左
        RIGHT, 		//右
        LEFT_BOTTOM,//左下
        BOTTOM,		// 下
        RIGHT_BOTTOM,// 右下
        CENTER;		//中心
    }
}
