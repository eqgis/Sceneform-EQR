package com.eqgis.test;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;


import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneViewType;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.ExternalTexture;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.ModelRenderable;

import java.util.function.Function;


/**
 * VR全景视频播放示例
 */
public class VRScene360Activity extends BaseActivity {

    private ModelRenderable modelRenderable = null;
    private Node tempNode = new Node();
    private ExternalTexture texture;
    private MediaPlayer mediaPlayer;
    //    private final Color CHROMA_KEY_COLOR = new Color(0.1843f, 1.0f, 0.098f);

    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_scene);

        sceneLayout = findViewById(R.id.base_scene_layout);
        //设置VR模式
        sceneLayout.setSceneViewType(SceneViewType.VR).init(this);
        sceneLayout.getCamera().setVerticalFovDegrees(45);
        sceneLayout.getCamera().setFarClipPlane(100);

        load(sceneLayout.getRootNode());
    }




    public void load(Node parentNode) {

        tempNode = new Node();
        tempNode.setParent(parentNode);

        texture = new ExternalTexture();
        mediaPlayer = MediaPlayer.create(this, R.raw.vr_video_city);
        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setSurface(texture.getSurface());
            mediaPlayer.setLooping(true);
            mediaPlayer.start();//自动播放
            Log.d("IKKYU-Media", "run: start: ");
        });

        mediaPlayer.setOnVideoSizeChangedListener((mediaPlayer, w, h) -> {
            if (texture != null) {
                texture.getSurfaceTexture().setDefaultBufferSize(w,h);
                Log.d("IKKYU-Media", "skybox: w: " + w + " h: "+h);
            }
        });
        //载入视频纹理材质
        Material.builder()
                .setSource(this, com.eqgis.eqr.R.raw.external_chroma_key_video_material)
                .build()
                .thenAccept(material -> {
                    material.setFloat4("keyColor",new Color(0,0,0,1));
                    setMaterial(material);
                })
                .exceptionally(new Function<Throwable, Void>() {
                    @Override
                    public Void apply(Throwable throwable) {
                        Log.e(VRScene360Activity.class.getSimpleName(), "Unable to load video renderable.  apply: ", throwable);
                        return null;
                    }
                });
    }


    @Override
    protected void onDestroy() {
        tempNode.destroy();
        super.onDestroy();

        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (modelRenderable != null){
            modelRenderable.tryDestroyData();
            modelRenderable = null;
        }
    }

    private void setMaterial(Material material) {
        modelRenderable = GeometryUtils.makeInnerSphere(30, Vector3.zero(), material);
        modelRenderable.getMaterial().setExternalTexture("videoTexture", texture);
        //关闭阴影
        modelRenderable.setShadowCaster(false);
        modelRenderable.setShadowReceiver(false);
        //desc-拓展纹理texture
        texture.getSurfaceTexture().setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                tempNode.setRenderable(modelRenderable);
                texture.getSurfaceTexture().setOnFrameAvailableListener(null);
            }
        });
    }
}