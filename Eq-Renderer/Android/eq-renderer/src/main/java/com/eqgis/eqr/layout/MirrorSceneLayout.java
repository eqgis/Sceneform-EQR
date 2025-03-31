package com.eqgis.eqr.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.sceneform.SceneView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 镜像场景布局
 * <p>左右双屏显示</p>
 * @author tanyx 2023/12/12
 * @version 1.0
 **/
public class MirrorSceneLayout extends SceneLayout{

    private SurfaceView rightView;

    private FrameLayout leftLayout;
    private FrameLayout rightLayout;

    private boolean mirrorStatus = false;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private Bitmap mirrorCache;

    private int width,height;
    private final Paint paint = new Paint();
    private float offsetPixel = 0f;

    //<editor-fold> - 构造函数
    public MirrorSceneLayout(Context context) {
        super(context);
    }

    public MirrorSceneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MirrorSceneLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MirrorSceneLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    //</editor-fold>

    @Override
    protected void addLayout() {
        sceneView = new SceneView(context);
        leftLayout = new FrameLayout(context);
        leftLayout.addView(sceneView);
        this.addView(leftLayout);

        rightView = new SurfaceView(getContext());
        rightLayout = new FrameLayout(context);
        rightLayout.addView(rightView);
        this.addView(rightLayout);
        //事件初始化
        initEvent();
    }

    @SuppressLint({"DrawAllocation", "RtlHardcoded"})
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec) / 2;
        height = MeasureSpec.getSize(heightMeasureSpec);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.LEFT;
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.RIGHT;
        leftLayout.setLayoutParams(layoutParams2);
        sceneView.setLayoutParams(layoutParams2);

        rightLayout.setLayoutParams(layoutParams);
        rightView.setLayoutParams(layoutParams);
    }

    /**
     * 相关事件初始化
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        sceneView.getScene().addOnUpdateListener(
                frameTime -> {
                    if (!mirrorStatus)return;
                    executor.execute(mirrorRunnable);
                }
        );

        //生命周期监听
        setLifecycleListener(new LifecycleListener() {
            @Override
            public void onResume() {
                mirrorStatus = true;
            }

            @Override
            public void onPause() {
                mirrorStatus = false;
            }

            @Override
            public void onDestroy() {
                mirrorStatus = false;
            }
        });

        //副屏解决手势问题
        rightView.setOnTouchListener((view, event) -> {
            //事件传递
            return sceneView.onTouchEvent(event);
        });
    }


    private final Runnable mirrorRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mirrorStatus)return;//仅在resume后执行画面同步
            if (!(width > 0 && height > 0)){
                return;
            }

            if (mirrorCache == null) {
                mirrorCache = Bitmap.createBitmap(width,
                        height, Bitmap.Config.ARGB_8888);
            }else {
                if (mirrorCache.getWidth() != width
                        ||mirrorCache.getHeight() != height){
                    mirrorCache = Bitmap.createBitmap(width,
                            height, Bitmap.Config.ARGB_8888);
                }

                try {
                    PixelCopy.request(sceneView, mirrorCache, i -> {
                        if (mirrorCache != null){
                            try {
                                Canvas canvas = rightView.getHolder().lockCanvas();
                                canvas.drawBitmap(mirrorCache,0,0,paint);
                                rightView.getHolder().unlockCanvasAndPost(canvas);
                            }catch (NullPointerException e){
                                Log.w(MirrorSceneLayout.class.getSimpleName(), "copy: ", e);
                            }
                        }
                    },getHandler());
                }catch (IllegalArgumentException e){
                    Log.w(MirrorSceneLayout.class.getSimpleName(), "copy: ", e);
                }
            }
        }
    };

    /**
     * 实现3D效果
     * <pre>
     *     基本原理是让左右布局中相同位置的View，
     *     左边布局的view向右偏移，
     *     右边布局的view向左偏移，
     *     从而产生视差，不同的偏移值会产生不同的景深感觉，合目后形成3D视觉效果。
     * </pre>
     * @param offsetPixel
     */
    public void make3DEffect(float offsetPixel){
        //左侧向右移动
        sceneView.setTranslationX(offsetPixel);
        rightView.setTranslationX(-offsetPixel);
        this.offsetPixel = offsetPixel * 2;
    }

    public FrameLayout getLeftLayout() {
        return leftLayout;
    }

    public FrameLayout getRightLayout() {
        return rightLayout;
    }

    /**
     * 获取两个视图的偏移差值
     * @return 偏移像素值
     */
    public float getViewOffset(){
        return this.offsetPixel;
    }
}