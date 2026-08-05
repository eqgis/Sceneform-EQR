package com.eqgis.eqr.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.annotation.SuppressLint;
import android.view.animation.LinearInterpolator;

import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * 节点曲线路径动画
 * <pre>
 *     使用 Android ObjectAnimator 驱动 Node.localPosition，
 *     起点、途经控制点与终点通过 Bézier 估值器生成连续平滑路径。
 * </pre>
 * @author tanyx
 */
public class ARAnimationPath extends ARAnimation {

    /**
     * 构造曲线路径动画
     * @param node 需要沿路径移动的场景节点
     */
    public ARAnimationPath(Node node) {
        super(node);
    }

    /**
     * 根据参数创建曲线路径动画，调用 {@link #play()} 后开始播放
     * @param parameter 动画周期、重复方式、起终点和途经控制点参数
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
        List<Vector3> controlPoints = new ArrayList<>();
        controlPoints.add(start);
        controlPoints.addAll(parameter.getWayPoints());
        controlPoints.add(end);

        ObjectAnimator animator = ObjectAnimator.ofObject(
                getNode(),
                "localPosition",
                new BezierVector3Evaluator(controlPoints),
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

    /**
     * 多控制点 Bézier 三维坐标估值器
     */
    private static class BezierVector3Evaluator implements TypeEvaluator<Vector3> {
        private final List<Vector3> controlPoints;

        BezierVector3Evaluator(List<Vector3> controlPoints) {
            this.controlPoints = controlPoints;
        }

        @Override
        public Vector3 evaluate(float fraction, Vector3 startValue, Vector3 endValue) {
            List<Vector3> points = new ArrayList<>(controlPoints.size());
            for (Vector3 point : controlPoints) {
                points.add(new Vector3(point.x, point.y, point.z));
            }
            //desc- 使用 De Casteljau 算法逐层插值，可支持任意数量的途经控制点。
            for (int level = points.size() - 1; level > 0; level--) {
                for (int index = 0; index < level; index++) {
                    points.set(index, Vector3.lerp(points.get(index), points.get(index + 1), fraction));
                }
            }
            return points.get(0);
        }
    }
}
