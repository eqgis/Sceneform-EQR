package com.eqgis.eqr.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.view.animation.LinearInterpolator;

import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.math.Vector3Evaluator;

/**
 * 节点位移动画
 * <pre>
 *     使用 Android ObjectAnimator 驱动 Node.localPosition，
 *     通过 Vector3Evaluator 在起点和终点之间进行三维坐标插值。
 * </pre>
 * @author tanyx
 */
public class ARAnimationTranslation extends ARAnimation {

    /**
     * 构造位移动画
     * @param node 需要移动的场景节点
     */
    public ARAnimationTranslation(Node node) {
        super(node);
    }

    /**
     * 根据参数创建位移动画，调用 {@link #play()} 后开始播放
     * @param parameter 动画周期、重复方式和起终点参数
     */
    public void createAnimation(ARAnimationParameter parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("parameter must not be null");
        }
        m_ObjectAnimator = createAnimator(parameter);
    }

    @SuppressLint("WrongConstant")
    private ObjectAnimator createAnimator(ARAnimationParameter parameter) {
        Vector3 start = parameter.getStartVector();
        Vector3 end = parameter.getEndVector();
        ObjectAnimator animator = ObjectAnimator.ofObject(
                getNode(),
                "localPosition",
                new Vector3Evaluator(),
                start,
                end);
        animator.setRepeatCount(parameter.getRepeatCount());
        animator.setRepeatMode(parameter.getRepeatMode().getValue());
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(parameter.getDuration());
        animator.setStartDelay(parameter.getStartDelay());
        animator.setAutoCancel(true);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mARAnimatorListener != null) {
                    mARAnimatorListener.onAnimationStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mARAnimatorListener != null) {
                    mARAnimatorListener.onAnimationEnd();
                }
            }
        });
        return animator;
    }
}
