package com.google.sceneform;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import com.eqgis.eqr.listener.CompleteCallback;
import com.google.android.filament.IndirectLight;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Renderer;
import com.google.sceneform.rendering.ResourceManager;
import com.google.sceneform.rendering.ThreadPools;
import com.google.sceneform.utilities.MovingAverageMillisecondsTracker;
import com.google.sceneform.rendering.EngineInstance;
import com.google.sceneform.rendering.RenderableInternalFilamentAssetData;
import com.google.sceneform.utilities.AndroidPreconditions;
import com.google.sceneform.utilities.Preconditions;
import com.google.android.filament.ColorGrading;
import com.google.android.filament.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 场景视图
 * <p>这里继承于SurfaceView，若用TextureView，需相应修改Renderer</p>
 */
public class SceneView extends SurfaceView implements Choreographer.FrameCallback {
    private static final String TAG = SceneView.class.getSimpleName();

    @Nullable
    private Renderer renderer = null;
    private final FrameTime frameTime = new FrameTime();

    private Scene scene;
    private volatile boolean debugEnabled = false;

    private boolean isInitialized = false;

    @Nullable
    private Color backgroundColor;

    // 用于监测性能
    private final MovingAverageMillisecondsTracker frameTotalTracker =
            new MovingAverageMillisecondsTracker();
    private final MovingAverageMillisecondsTracker frameUpdateTracker =
            new MovingAverageMillisecondsTracker();
    private final MovingAverageMillisecondsTracker frameRenderTracker =
            new MovingAverageMillisecondsTracker();

    private List<OnTouchListener> mOnTouchListeners;
    protected int width;
    protected int height;

    /**
     * 构造函数
     *
     * @param context 安卓上下文
     * @see #SceneView(Context, AttributeSet)
     */
    @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
    public SceneView(Context context) {
        super(context);
        initialize();
    }

    /**
     * 构造函数
     *
     * @param context the Android Context to use
     * @param attrs   the Android AttributeSet to associate with
     */
    @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
    public SceneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // 这会确保视图的onTouchListener被调用。
        if (!super.onTouchEvent(motionEvent)) {
            scene.onTouchEvent(motionEvent);
            // 必须总是返回true来保证这个视图将接收所有的触摸事件。
            for (OnTouchListener listener:mOnTouchListeners) {
                listener.onTouch(null,motionEvent);
            }
            return true;
        }
        return true;
    }

    /**
     * 将背景设置为给定的{@link Drawable}，或者删除背景。
     * 如果背景是{@link ColorDrawable}，那么{@link Scene}的背景颜色
     * 被设置为{@link ColorDrawable#getColor()}(该颜色的alpha值被忽略)。
     * 否则，默认为{@link SurfaceView#setBackground(Drawable)}。
     */
    @Override
    public void setBackground(@Nullable Drawable background) {
        if (background instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable) background;
            backgroundColor = new Color(colorDrawable.getColor());
            if (renderer != null) {
                renderer.setClearColor(backgroundColor);
            }
        } else {
            backgroundColor = null;
            if (renderer != null) {
                renderer.setDefaultClearColor();
            }
            super.setBackground(background);
        }
    }

    /**
     * 设置背景透明
     */
    public void setTransparent(boolean transparent) {
        setBackgroundColor(android.graphics.Color.TRANSPARENT);//Add this line.Avoid this method being invalid.--IkkyuTed
        setZOrderOnTop(transparent);
        getHolder().setFormat(transparent ? PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE);
        renderer.getFilamentView().setBlendMode(transparent ? View.BlendMode.TRANSLUCENT : View.BlendMode.OPAQUE);
    }

    /**
     * @hide
     */
    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        width = right - left;
        height = bottom - top;

        Preconditions.checkNotNull(renderer).setDesiredSize(width, height);
    }

    /**
     * Resume操作
     * <p>
     *     在onResume()方法中调用本方法
     * </p>
     */
    public void resume(){
//        getSurfaceTexture().setDefaultBufferSize(width, height);//在SceneView继承TextureView时，不适用
        if (renderer != null) {
            renderer.onResume();
        }
        // Start the drawing when the renderer is resumed.  Remove and re-add the callback
        // to avoid getting called twice.
        Choreographer.getInstance().removeFrameCallback(this);
        Choreographer.getInstance().postFrameCallback(this);
    }

    /**
     * Pause操作
     * <p>
     *     在onPause()方法中调用
     * </p>
     */
    public void pause() {
        Choreographer.getInstance().removeFrameCallback(this);
        if (renderer != null) {
            renderer.onPause();
        }
    }

    /**
     * destroy操作
     *
     * <p>
     *     在onDestroy()方法中调用
     * </p>
     */
    public void destroy() {
        Choreographer.getInstance().removeFrameCallback(this);

        if (renderer != null) {

            //todo check_free memory_: add this method to release memory
            try {
//                reclaimReleasedResources();//'renderer.dispose()' has call this method.

                renderer.dispose2();//包含renderer和filamentView、indirectLight
            }catch (IllegalStateException e){
                Log.w(TAG, "destroy: ", e);
            }finally {
                renderer = null;
            }
        }

        ResourceManager.getInstance().destroyAllResources();
        RenderableInternalFilamentAssetData.destroy();
        EngineInstance.destroyEngine();
//        destroyAllResources();
    }

    /**
     * 立即销毁所有渲染资源
     * <p>包括使用中的资源</p>
     *
     * <p>
     *     如果在这个场景或任何其他场景中没有更多的渲染，请使用此选项，并且必须立即释放内存。
     * </p>
     */
    public static void destroyAllResources() {
        Renderer.destroyAllResources();
    }

    /**
     * 启用调试模式
     * <p>若启用，则logcat中可以在debug下查看到frame相关信息</p>
     */
    public void enableDebug(boolean enable) {
        debugEnabled = enable;
    }

    /**
     * 判断是否启用调试模式
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * 获取渲染器对象
     *
     * @hide 内部使用
     */
    @Nullable
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * 获取视图中的场景对象
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * 开始将画面映射到指定平面{@link Surface}
     * @param surface 渲染场景应该被镜像到的表面。
     * @param left    将视图镜像到表面上的矩形的左边缘。
     * @param bottom  视图应被镜像到的矩形的下边缘表面。
     * @param width   将场景视图镜像到表面上的矩形的宽度。
     * @param height  将场景视图镜像到表面上的矩形的高度。
     */
    public void startMirroringToSurface(
            Surface surface, int left, int bottom, int width, int height) {
        if (renderer != null) {
            renderer.startMirroring(surface, left, bottom, width, height);
        }
    }

    /**
     * 捕获完成后，调用此方法停止将SceneView镜像到指定的{@link Surface}。如果不调用该函数，则额外的性能成本将保持不变。
     *
     * <p>
     *     完成后，应用程序负责在Surface上调用{@link Surface#release()}。
     * </p>
     */
    public void stopMirroringToSurface(Surface surface) {
        if (renderer != null) {
            renderer.stopMirroring(surface);
        }
    }

    /**
     * 初始化
     * 创建渲染器和配置相机
     * @see #SceneView(Context, AttributeSet)
     */
    private void initialize() {
        if (isInitialized) {
            Log.w(TAG, "SceneView already initialized.");
            return;
        }

        if (!AndroidPreconditions.isMinAndroidApiLevel()) {
            Log.e(TAG, "Sceneform requires Android N or later");
            renderer = null;
        } else {
            renderer = new Renderer(this);

            //added by Ikkyu 2022/03/31 修改颜色映射方式
            renderer.getFilamentView().setColorGrading(
                    new ColorGrading.Builder().toneMapping(
                            ColorGrading.ToneMapping.FILMIC
                    ).build(EngineInstance.getEngine().getFilamentEngine()));

            if (backgroundColor != null) {
                renderer.setClearColor(backgroundColor);
            }
            scene = new Scene(this);
            renderer.setCameraProvider(scene.getCamera());
        }
        isInitialized = true;
        mOnTouchListeners=new ArrayList<>();
    }

    /**
     * 在每个显示帧之前更新特定于视图的逻辑。
     *
     * @return 若返回false，则本帧不会渲染
     * @hide
     */
    protected boolean onBeginFrame(long frameTimeNanos) {
        //desc-added by Ikkyu 2022年1月18日，@Testing 可能不生效
//        if (customDepthImage != null && ARPlatForm.OCCLUSION_MODE == ARPlatForm.OcclusionMode.OCCLUSION_ENABLED){
//            recalculateOcclusion(customDepthImage);
//        }
        return true;
    }

    /**
     * 执行渲染操作
     * <p>每帧调用</p>
     *
     * @hide
     */
    @SuppressWarnings("AndroidApiChecker")
    @Override
    public void doFrame(long frameTimeNanos) {
        // Always post the callback for the next frame.
        Choreographer.getInstance().postFrameCallback(this);
        doFrameNoRepost(frameTimeNanos);
    }

    /**
     * 每个显示帧发生的回调，更新场景
     * @hide
     */
    public void doFrameNoRepost(long frameTimeNanos) {
        // TODO: Display the tracked performance metrics in debug mode.
        if (debugEnabled) {
            frameTotalTracker.beginSample();
        }

        if (onBeginFrame(frameTimeNanos)) {
            doUpdate(frameTimeNanos);
            doRender(frameTimeNanos);
        }

        if (debugEnabled) {
            frameTotalTracker.endSample();
            if ((System.currentTimeMillis() / 1000) % 60 == 0) {
                Log.d(TAG, " PERF COUNTER: frameRender: " + frameRenderTracker.getAverage());
                Log.d(TAG, " PERF COUNTER: frameTotal: " + frameTotalTracker.getAverage());
                Log.d(TAG, " PERF COUNTER: frameUpdate: " + frameUpdateTracker.getAverage());
            }
        }
    }

    private void doUpdate(long frameTimeNanos) {
        if (debugEnabled) {
            frameUpdateTracker.beginSample();
        }

        frameTime.update(frameTimeNanos);

        scene.dispatchUpdate(frameTime);

        if (debugEnabled) {
            frameUpdateTracker.endSample();
        }
    }

    private void doRender(long frameTimeNanos) {
        Renderer renderer = this.renderer;
        if (renderer == null) {
            return;
        }

        if (debugEnabled) {
            frameRenderTracker.beginSample();
        }

        renderer.render(frameTimeNanos, debugEnabled);

        if (debugEnabled) {
            frameRenderTracker.endSample();
        }
    }

    /**
     * ADD BY IKKYU========================
     */

    /**
     * 自定义深度图数据
     * {@link SceneView#updateDepthImageData(byte[], int, int)}
     */
    protected CustomDepthImage customDepthImage;

    /**
     * 更新深度图数据
     * @param data 深度数据
     * @param width 宽度
     * @param height 高度
     */
    public void updateDepthImageData(byte[] data,int width,int height){
        if (data == null){
            this.customDepthImage = null;
            return;
        }
        if (this.customDepthImage == null){
            this.customDepthImage = new CustomDepthImage(data,width,height);
            return;
        }
        customDepthImage.setBytes(data);
        customDepthImage.setWidth(width);
        customDepthImage.setHeight(height);
    }

    /**
     * 添加触摸监听事件
     * @param listener 监听事件
     */
    public void addOnTouchListener(OnTouchListener listener){
        //用于外部手势事件处理
        mOnTouchListeners.add(listener);
    }

}