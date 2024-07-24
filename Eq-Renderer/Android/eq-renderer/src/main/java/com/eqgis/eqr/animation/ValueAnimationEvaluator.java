package com.eqgis.eqr.animation;

import android.animation.TypeEvaluator;

/**
 * 动画估值器
 * @author tanyx
 * @version 1.0
 **/
public class ValueAnimationEvaluator implements TypeEvaluator<Float> {
    public enum Type{
        SIN,
        COS,
        LINER
    }
    private Type type;

    /**
     * 构造函数
     * @param type
     */
    public ValueAnimationEvaluator(Type type) {
        this.type = type;
    }

    @Override
    public Float evaluate(float fraction, Float start, Float end) {
        float factor = fraction;
        switch (type){
            case SIN:
                //实现加速度越来越大的减速运动
                factor = (float) Math.sin((fraction * Math.PI) / 2);
                break;
            case COS:
                factor = 1.0f - (float) Math.cos((fraction * Math.PI) / 2);
                break;
            case LINER:
            default:
                break;
        }
        return (end - start) * factor + start;
    }
}