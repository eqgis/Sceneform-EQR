package com.eqgis.eqr.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.animation.LinearInterpolator;

import com.google.sceneform.Node;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.QuaternionEvaluator;


/**
 * 旋转动画
 */
public class ARAnimationRotation extends ARAnimation {
    public float mStartAngle=0;//旋转起始角度
    public float mRotationAngle=360;//旋转角度

    /**
     * 构造方法
     * @param node 节点对象
     * */
    public ARAnimationRotation(Node node) {
        super(node);
    }

    /**
     * 创建旋转动画
     * */
    private ObjectAnimator createAnimator(ARAnimationParameter parameter)  {
        // 节点的位置和角度信息设置通过Quaternion来设置
        Quaternion orientation1;
        Quaternion orientation2;
        Quaternion orientation3;
        Quaternion orientation4;

        //获取旋转轴参数getRotationAxis(),创建4个Quaternion 来设置四个关键位置
        if (parameter.getClockwise()){
            //顺时针旋转
            orientation1 = Quaternion.axisAngle(parameter.getRotation(),mStartAngle);
            orientation2 = Quaternion.axisAngle(parameter.getRotation(),mStartAngle-mRotationAngle/2);
//            orientation3 = Quaternion.axisAngle(parameter.getRotation(),-240f);
            orientation4 = Quaternion.axisAngle(parameter.getRotation(),mStartAngle-mRotationAngle);
        }else {
            orientation1 = Quaternion.axisAngle(parameter.getRotation(),mStartAngle);
            orientation2 = Quaternion.axisAngle(parameter.getRotation(),mStartAngle+mRotationAngle/2);
//            orientation3 = Quaternion.axisAngle(parameter.getRotation(),240f);
            orientation4 = Quaternion.axisAngle(parameter.getRotation(),mStartAngle+mRotationAngle);
        }
        ObjectAnimator rotationAnimation =ObjectAnimator.ofObject(getNode(),"localRotation",new QuaternionEvaluator(),orientation1,orientation2,orientation4);

        // 设置属性动画修改的属性为localRotation
        rotationAnimation.setPropertyName( "localRotation");

        // 使用Sceneform 框架提供的估值器 QuaternionEvaluator 作为属性动画估值器
        rotationAnimation.setEvaluator(new QuaternionEvaluator());
        //  设置动画重复无限次播放。
        rotationAnimation.setRepeatCount(parameter.getRepeatCount());
        rotationAnimation.setRepeatMode(parameter.getRepeatMode().getValue());
        rotationAnimation.setInterpolator(new LinearInterpolator());
        rotationAnimation.setDuration(parameter.getDuration());
        rotationAnimation.setStartDelay(parameter.getStartDelay());
        rotationAnimation.setAutoCancel(true);
        rotationAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if(mARAnimatorListener!=null)mARAnimatorListener.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(mARAnimatorListener!=null)mARAnimatorListener.onAnimationEnd();
            }
        });
        return rotationAnimation;
    }

    /**
     * 创建动画
     * @param parameter 动画参数
     */
    public void creatAnimation(ARAnimationParameter parameter){
        m_ObjectAnimator=createAnimator(parameter);
    }

    public void setStartAngle(float startAngle) {
        this.mStartAngle = startAngle;
    }

    public void setRotationAngle(float rotationAngle) {
        this.mRotationAngle = rotationAngle;
    }
}
