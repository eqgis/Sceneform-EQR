package com.eqgis.test.samples;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;


import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneViewType;
import com.eqgis.test.BaseActivity;
import com.eqgis.test.R;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.ExternalTexture;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.ModelRenderable;

import java.util.function.Function;


/**
 * VR 全景视频播放示例（VRScene360Activity）
 * <p>
 * 本示例演示如何在自定义 SceneForm 框架中构建一个可播放 360° 全景视频的 VR 场景，
 * 通过 {@link MediaPlayer} + {@link ExternalTexture} 实现视频纹理映射，
 * 并利用 {@link GeometryUtils} 绘制内球体作为全景视频的显示容器。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *     <li>初始化 VR 模式场景（{@link SceneViewType#VR}），设置摄像机参数与视野范围；</li>
 *     <li>使用 {@link MediaPlayer} 读取本地资源视频（R.raw.vr_video_city）并绑定 {@link ExternalTexture}；</li>
 *     <li>通过 {@link Material#builder()} 加载自定义视频材质（external_chroma_key_video_material）；</li>
 *     <li>将视频纹理映射到内球体，实现 360° 环绕播放效果；</li>
 *     <li>自动循环播放、实时帧更新、无缝贴合天空球体；</li>
 * </ul>
 *
 * <h3>渲染流程：</h3>
 * <ol>
 *     <li>创建 {@link ExternalTexture} 并关联 {@link android.view.Surface}；</li>
 *     <li>在 {@link MediaPlayer.OnPreparedListener} 中启动播放；</li>
 *     <li>在 {@link android.media.MediaPlayer.OnVideoSizeChangedListener} 中更新纹理缓冲区尺寸；</li>
 *     <li>通过 {@link Material#setExternalTexture(String, ExternalTexture)} 将视频流绑定到材质；</li>
 *     <li>调用 {@link GeometryUtils#makeInnerSphere(float, Vector3, Material)} 创建内贴图球体；</li>
 * </ol>
 *
 * <h3>技术要点：</h3>
 * <ul>
 *     <li>基于内球体反向法线渲染的全景视频显示方案；</li>
 *     <li>利用 {@link SurfaceTexture.OnFrameAvailableListener} 确保渲染对象在首帧到达后绑定；</li>
 *     <li>纹理采用 GPU 直连渲染，避免中间缓存，提高性能；</li>
 *     <li>关闭阴影计算（setShadowCaster / setShadowReceiver）以提升帧率；</li>
 * </ul>
 *
 * <h3>资源依赖：</h3>
 * <pre>
 * res/
 * ├── raw/
 * │   ├── vr_video_city.mp4                       // 360° 全景视频资源
 * │   └── external_chroma_key_video_material.sfb  // 视频纹理材质定义
 * └── layout/
 *     └── activity_base_scene.xml                 // 场景基础布局
 * </pre>
 *
 * <h3>生命周期管理：</h3>
 * <ul>
 *     <li>在 {@link #onDestroy()} 中销毁 Node、释放 MediaPlayer 与 Renderable 资源；</li>
 *     <li>防止纹理与播放器的内存泄漏；</li>
 * </ul>
 *
 * <h3>相关类：</h3>
 * <ul>
 *     <li>{@link GeometryUtils}：几何体工具类，用于生成内外球体、平面等结构；</li>
 *     <li>{@link ExternalTexture}：Sceneform 提供的视频外部纹理接口；</li>
 *     <li>{@link MediaPlayer}：系统级视频解码与播放引擎；</li>
 * </ul>
 *
 * <p>
 * 本示例适合作为 VR 全景视频播放器开发的基础模板，可拓展为互动视频、VR 观影厅、
 * 360° 现场回放等应用场景。
 * </p>
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
//        tempNode.destroy();
        //在 Filament 中，destroyMaterialInstance() 永远不能早于“Engine 中所有 Renderable Entity 的 destroy”，否则必崩。
        super.onDestroy();//，升级Filament至1.67.1时更新，现已在super.onDestroy()带有销毁entity

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