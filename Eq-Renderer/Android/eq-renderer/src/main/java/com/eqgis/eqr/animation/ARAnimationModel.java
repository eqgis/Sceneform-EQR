package com.eqgis.eqr.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Keep;

import com.google.sceneform.Node;
import com.google.sceneform.animation.ModelAnimation;
import com.google.sceneform.rendering.RenderableInstance;

/**
 * 模型动画
 * <p>支持GLTF的模型动画</p>
 * @author tanyx 2024/8/27
 * @version 1.0
 **/
public class ARAnimationModel extends ARAnimation {
    //当前需要执行的动画索引
    private int index = 0;

    /**
     * 构造函数
     * @param node 节点对象
     */
    public ARAnimationModel(Node node){
        super(node);
    }

    /**
     * 创建动画
     * @param parameter 动画参数
     */
    public void createAnimation(ARAnimationParameter parameter){
        m_ObjectAnimator = createAnimator(parameter);
    }


    @SuppressLint("WrongConstant")
    private ObjectAnimator createAnimator(ARAnimationParameter parameter) {
        ObjectAnimator modelAnimator =ObjectAnimator.ofFloat(this,"currentProgress",0.0f,1.0f);
        modelAnimator.setEvaluator(new FloatEvaluator());
        //  设置动画重复无限次播放。
        modelAnimator.setRepeatCount(parameter.getRepeatCount());
        modelAnimator.setRepeatMode(parameter.getRepeatMode().getValue());
        modelAnimator.setInterpolator(new LinearInterpolator());
        modelAnimator.setDuration(parameter.getDuration());
        modelAnimator.setStartDelay(parameter.getStartDelay());
        modelAnimator.setAutoCancel(true);
        modelAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if(mARAnimatorListener!=null)mARAnimatorListener.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(mARAnimatorListener!=null)mARAnimatorListener.onAnimationEnd();
            }
        });
        return modelAnimator;
    }

    /**
     * 设置动画进度
     * @param progress 进度值
     */
    @Keep
    public void setCurrentProgress(float progress){
        RenderableInstance instance = getNode().getRenderableInstance();
        if (instance == null)return;
        ModelAnimation animation = instance.getAnimation(index);
        if (animation != null){
            //若动画存在，则更新百分比进度
            animation.setFractionPosition(progress);
        }
    }

    /**
     * 设置当前播放动画的索引
     * @param index 动画索引
     */
    public void setCurrentIndex(int index){
        this.index = index;
    }
}
