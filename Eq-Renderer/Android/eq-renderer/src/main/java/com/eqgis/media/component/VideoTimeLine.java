package com.eqgis.media.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.google.sceneform.ExSceneView;

/**
 * 视频时间轴
 * <p>配合{@link ExSceneView}播放视频</p>
 * @author tanyx 2021/11/3
 */
@SuppressLint("AppCompatCustomView")
public class VideoTimeLine extends FrameLayout {
    private boolean isSeekBarTouching;
    private SeekBar mSeekBar;

    private Context mContext;
    private OnChangeLister onChangeLister;

    //当前进度
    private float currentFraction;

    /**
     * 更新监听事件
     */
    public interface OnChangeLister{
        /**
         * 播放进度
         * <p>取值区间[0,1]</p>
         * @param progress 进度值
         */
        void onChanged(float progress);
    }

    /**
     * 设置更新监听
     * @param onChangeLister 监听事件
     */
    public void setOnChangeLister(OnChangeLister onChangeLister) {
        this.onChangeLister = onChangeLister;
    }

    //<editor-fold> 构造函数
    public VideoTimeLine(Context context) {
        super(context);
        init(context);
    }

    public VideoTimeLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public VideoTimeLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    public VideoTimeLine(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    //</editor-fold>

    /**
     * 获取播放的进度
     * @return 取值区间[0,1]
     */
    public float getProgress(){
        return currentFraction;
    }


    /**
     * 绑定视图
     * @param exSceneView 视图
     * @param mediaPlayer 播放器
     * @return this
     */
    public VideoTimeLine bindView(ExSceneView exSceneView, MediaPlayer mediaPlayer){
        if (!(mediaPlayer.getDuration() > 0))
            throw new IllegalArgumentException("The duration of media was error.");
        initSeekBarSetting(mediaPlayer.getDuration());

        exSceneView.getScene().addOnUpdateListener(frameTime -> {
            if (isSeekBarTouching){
                //如果在拖拽进度条，这就不更新
                return;
            }
            int progress = mediaPlayer.getCurrentPosition();
            mSeekBar.setProgress(progress);
        });
        return this;
    }

    /**
     * 初始化
     */
    private void initSeekBarSetting(int max) {
        mSeekBar.setProgress(0);
        mSeekBar.setMax(max);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!isSeekBarTouching){
                    return;
                }

                currentFraction = (float) progress / max;
                if (onChangeLister != null){
                    onChangeLister.onChanged(currentFraction);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarTouching = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarTouching = false;
            }
        });
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        this.mContext = context;

        mSeekBar = new SeekBar(mContext);
        ViewGroup.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mSeekBar,params);
        isSeekBarTouching = false;
    }

}
