package com.eqgis.media.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.eqgis.eqr.R;
import com.google.sceneform.ExSceneView;

/**
 * 视频时间轴
 * <p>配合{@link ExSceneView}播放视频</p>
 * @author tanyx 2021/11/3
 */
@SuppressLint("AppCompatCustomView")
public class VideoTimeLine extends FrameLayout {
    private int max = -1;
    private boolean isSeekBarTouching;
    private SeekBar mSeekBar;
    private TextView startTime;
    private TextView endTime;

    private Context mContext;
    private OnUpdateListener onUpdateListener;

    //当前进度
    private int current;
    private boolean hasPause = false;
    private MediaPlayer mediaPlayer;

    /**
     * 更新监听事件
     */
    public interface OnUpdateListener {
        /**
         * 播放进度
         */
        void onUpdate();
    }

    /**
     * 设置更新监听
     * @param onUpdateListener 监听事件
     */
    public void setOnChangeLister(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
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
        return (float)current / max;
    }


    /**
     * 绑定视图
     * @param exSceneView 视图
     * @param mediaPlayer 播放器
     * @return this
     */
    public VideoTimeLine bindView(ExSceneView exSceneView, MediaPlayer mediaPlayer){
//        if (!(mediaPlayer.getDuration() > 0))
//            throw new IllegalArgumentException("The duration of media was error.");
        this.mediaPlayer = mediaPlayer;
        initSeekBarSetting(mediaPlayer);

        exSceneView.getScene().addOnUpdateListener(frameTime -> {
            if (isSeekBarTouching){
                //如果在拖拽进度条，这就不更新
                return;
            }
            if (max < 1){
                updateMax(mediaPlayer);
            }
            int position = mediaPlayer.getCurrentPosition();
//            Log.i("IKKYU ", "bindView: " + position);
            mSeekBar.setProgress(position);
        });

        //seekTo(0)，恢复为第一帧
        reset();
        return this;
    }

    public void reset(){
        current = 0;
//                Log.i("IKKYU ", "onProgressChanged: "+current);
        startTime.setText(format(current,0));
        endTime.setText(format(max - current,1));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer.seekTo(current, MediaPlayer.SEEK_NEXT_SYNC);
        }else {
            mediaPlayer.seekTo(current);
        }
    }

    /**
     * 初始化
     */
    private void initSeekBarSetting(MediaPlayer mediaPlayer) {
        mSeekBar.setProgress(0);
        updateMax(mediaPlayer);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                current = progress;
//                Log.i("IKKYU ", "onProgressChanged: "+current);
                startTime.setText(format(current,0));
                endTime.setText(format(max - current,1));
                if (onUpdateListener != null){
                    onUpdateListener.onUpdate();
                }
                if (!isSeekBarTouching){
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mediaPlayer.seekTo(current, MediaPlayer.SEEK_NEXT_SYNC);
                }else {
                    mediaPlayer.seekTo(current);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarTouching = true;
                if (mediaPlayer.isPlaying()){
                    //拖拽中，暂停播放
                    mediaPlayer.pause();
                    hasPause = true;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarTouching = false;
                if (hasPause){
                    //拖拽结束，恢复播放
                    mediaPlayer.start();
                    hasPause = false;
                }
            }
        });
    }

    /**
     * 初始化
     */
    @SuppressLint({"InflateParams", "UseCompatLoadingForDrawables"})
    private void init(Context context) {
        this.mContext = context;

        View view = LayoutInflater.from(mContext).inflate(R.layout.eq_video_timeline,null);
        mSeekBar = view.findViewById(R.id.timeline_seekbar);
        startTime = view.findViewById(R.id.start_time_str);
        endTime = view.findViewById(R.id.end_time_str);
        ViewGroup.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(view,params);
        isSeekBarTouching = false;
    }

    /**
     * 更新seekbar的最大值
     */
    private void updateMax(MediaPlayer mediaPlayer) {
        int duration = mediaPlayer.getDuration();
        if (duration > 0){
            max = duration;
            mSeekBar.setMax(max);
        }
    }

    /**
     * 时间格式转换
     * @param millis 毫秒
     * @return 字符串
     */
    @SuppressLint("DefaultLocale")
    private String format(int millis,int type){
        int hours = (int) (millis / 1000 / 3600); // 计算小时数
        int minutes = (int) ((millis / 1000) % 3600 / 60); // 计算分钟数
        int seconds = (int) ((millis / 1000) % 60); // 计算秒数
        // 格式化时间为hh:mm:ss格式
        if (hours != 0){
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }else {
            if (type == 0) {
                return String.format("      %02d:%02d", minutes, seconds);
            }
            return String.format("%02d:%02d      ", minutes, seconds);
        }
    }
}
