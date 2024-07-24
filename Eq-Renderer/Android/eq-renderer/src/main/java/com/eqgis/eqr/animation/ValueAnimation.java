package com.eqgis.eqr.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;

import androidx.annotation.Keep;

/**
 * 属性值动画
 * @author tanyx
 * @version 1.0
 **/
public class ValueAnimation extends CustomAnimation{
    /**
     * 数值更新监听事件
     */
    public interface OnUpdateListener{
        /**
         * 值更新
         * @param current 当前值
         * @param running 状态标记
         */
        void onValueUpdate(float current,boolean running);
    }

    //起始数值
    private float start;

    //结束数值
    private float end;

    //实时数值
    private float current;

    //值更新监听事件
    public OnUpdateListener onUpdateListener;

    /**
     * 构造函数
     */
    public ValueAnimation(){
    }

    /**
     * 更新值
     * @param start
     * @param end
     */
    public void updateValue(float start, float end){
        this.start = start;
        this.end = end;
        if (animator == null)return;
        this.animator.setFloatValues(start,end);
    }

    /**
     * 设置当前值
     * @param current 当前值
     */
    @Keep
    public void setCurrent(float current) {
        this.current = current;
        if (this.onUpdateListener != null){
            this.onUpdateListener.onValueUpdate(current,true);
        }
    }

    /**
     * 设置更新监听事件
     * @param onUpdateListener 监听事件
     */
    public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }

    /**
     * 动画初始化
     */
    @Override
    public ValueAnimation init(TypeEvaluator evaluator) {
        if (animator == null) {
            animator = ObjectAnimator.ofObject(this, "current",
                    evaluator,start,end);
            animator.setStartDelay(0);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.setRepeatCount(0);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    if (onUpdateListener != null)
                        onUpdateListener.onValueUpdate(start,true);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (onUpdateListener != null)
                        onUpdateListener.onValueUpdate(end,false);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    //取消时，则直接结束
                    if (onUpdateListener != null)
                        onUpdateListener.onValueUpdate(end,false);
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }

        init = true;
        return this;
    }

    /**
     * 释放动画对象
     */
    public void releaseAnimator(){
        if (animator==null)return;
        animator.cancel();
        animator = null;
    }
}