package com.eqgis.eqr.animation;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;

import java.util.Objects;

/**
 * 自定义动画
 * @author tanyx
 * @version 1.0
 **/
public abstract class CustomAnimation<S extends CustomAnimation<S>>  {
    private long duration;
    protected ObjectAnimator animator;
    protected boolean init = false;

    public abstract S init(TypeEvaluator evaluator);

    /**
     * 设置动画周期
     * @param duration 周期，单位ms
     */
    public void setDuration(long duration) {
        this.duration = duration;
        if (animator==null)return;
        this.animator.setDuration(duration);
    }

    /**
     * 获取动画周期
     * @return 周期，单位ms
     */
    public long getDuration() {
        return duration;
    }

    /**
     * 获取动画对象
     * @return Animator
     */
    public ObjectAnimator getAnimator() {
        return animator;
    }

    /**
     * 开始
     */
    public void start() {
        Objects.requireNonNull(animator).start();
    }

    /**
     * 退出
     */
    public void cancel() {
        Objects.requireNonNull(animator).cancel();
    }

    /**
     * 暂停正在播放的动画
     */
    public void pause(){
        if (Objects.requireNonNull(animator).isStarted())
            animator.pause();
    }

    /**
     * 恢复已暂停的动画
     */
    public void resume(){
        if (Objects.requireNonNull(animator).isPaused())
            animator.resume();
    }
}
