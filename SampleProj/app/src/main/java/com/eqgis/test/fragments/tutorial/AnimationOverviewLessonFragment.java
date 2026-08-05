package com.eqgis.test.fragments.tutorial;

import android.animation.ObjectAnimator;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.eqgis.eqr.animation.ARAnimationModel;
import com.eqgis.eqr.animation.ARAnimationParameter;
import com.eqgis.eqr.animation.ARAnimationPath;
import com.eqgis.eqr.animation.ARAnimationRepeatMode;
import com.eqgis.eqr.animation.ARAnimationRotation;
import com.eqgis.eqr.animation.ARAnimationTranslation;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * 动画篇总览教程
 * <pre>
 *     在同一场景并行展示 GLB 模型动画、节点旋转、直线位移和 Bézier 曲线路径动画。
 * </pre>
 * @author tanyx
 */
public class AnimationOverviewLessonFragment extends BaseAnimationLessonFragment {
    private static final int MIN_DURATION_SECONDS = 2;
    private static final int DEFAULT_DURATION_SECONDS = 4;
    private static final int MAX_DURATION_SECONDS = 8;
    private final List<ObjectAnimator> demoAnimators = new ArrayList<>();
    private boolean playing = true;
    private long duration = DEFAULT_DURATION_SECONDS * 1000L;

    @Override
    protected String getLessonTitle() {
        return "动画总览";
    }

    @Override
    protected String getLessonDescription() {
        return "同时观察模型内置动画、节点旋转、直线位移与曲线路径，拖动周期滑杆可统一改变播放速度。";
    }

    /**
     * 初始化四种动画的并行演示场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 90);
        createModelDemo();
        createRotationDemo();
        createTranslationDemo();
        createPathDemo();
    }

    /**
     * 初始化全局动画周期和播放状态控件
     * @param actionContainer 悬浮参数面板
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        SeekBar durationSeekBar = new SeekBar(requireContext());
        durationSeekBar.setMax(MAX_DURATION_SECONDS - MIN_DURATION_SECONDS);
        durationSeekBar.setProgress(DEFAULT_DURATION_SECONDS - MIN_DURATION_SECONDS);
        TextView durationLabel = addControlRow(
                actionContainer,
                "统一周期：" + DEFAULT_DURATION_SECONDS + "s",
                durationSeekBar);
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int seconds = MIN_DURATION_SECONDS + progress;
                duration = seconds * 1000L;
                durationLabel.setText("统一周期：" + seconds + "s");
                for (ObjectAnimator animator : demoAnimators) {
                    animator.setDuration(duration);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Switch playSwitch = new Switch(requireContext());
        playSwitch.setText("播放");
        playSwitch.setChecked(true);
        playSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            playing = isChecked;
            for (ObjectAnimator animator : demoAnimators) {
                updateAnimatorPlaying(animator);
            }
        });
        addControlRow(actionContainer, "播放状态", playSwitch);
    }

    private void createModelDemo() {
        loadAnimatedBee(new Vector3(-1.15f, -0.05f, -4.0f), 0.45f, node -> {
            if (node.getRenderableInstance() == null
                    || node.getRenderableInstance().getAnimationCount() == 0) {
                return;
            }
            ARAnimationParameter parameter = createLoopParameter();
            ARAnimationModel animation = new ARAnimationModel(node);
            animation.createAnimation(parameter);
            animation.setCurrentIndex(Math.min(1, node.getRenderableInstance().getAnimationCount() - 1));
            registerDemoAnimator(animation.getObjectAnimator());
        });
    }

    private void createRotationDemo() {
        createCube(
                new Vector3(-0.4f, -0.05f, -4.0f),
                0.48f,
                new Color(0.12f, 0.55f, 1.0f),
                node -> {
                    ARAnimationParameter parameter = createLoopParameter();
                    parameter.setRotationAxis(Vector3.up());
                    ARAnimationRotation animation = new ARAnimationRotation(node);
                    animation.createAnimation(parameter);
                    registerDemoAnimator(animation.getObjectAnimator());
                });
    }

    private void createTranslationDemo() {
        Vector3 start = new Vector3(0.15f, -0.28f, -4.0f);
        Vector3 end = new Vector3(0.72f, 0.3f, -4.0f);
        createSphere(start, 0.22f, new Color(0.2f, 0.82f, 0.42f), node -> {
            ARAnimationParameter parameter = createLoopParameter();
            parameter.setRepeatMode(ARAnimationRepeatMode.REVERSE);
            parameter.setStartVector(start);
            parameter.setEndVector(end);
            ARAnimationTranslation animation = new ARAnimationTranslation(node);
            animation.createAnimation(parameter);
            registerDemoAnimator(animation.getObjectAnimator());
        });
    }

    private void createPathDemo() {
        Vector3 start = new Vector3(0.86f, -0.3f, -4.0f);
        Vector3 end = new Vector3(1.42f, -0.3f, -4.0f);
        createSphere(start, 0.2f, new Color(1.0f, 0.48f, 0.08f), node -> {
            ARAnimationParameter parameter = createLoopParameter();
            parameter.setRepeatMode(ARAnimationRepeatMode.REVERSE);
            parameter.setStartVector(start);
            parameter.addWayPoint(new Vector3(1.14f, 0.58f, -3.65f));
            parameter.setEndVector(end);
            ARAnimationPath animation = new ARAnimationPath(node);
            animation.createAnimation(parameter);
            registerDemoAnimator(animation.getObjectAnimator());
        });
    }

    private ARAnimationParameter createLoopParameter() {
        ARAnimationParameter parameter = new ARAnimationParameter();
        parameter.setDuration(duration);
        parameter.setRepeatCount(ObjectAnimator.INFINITE);
        parameter.setRepeatMode(ARAnimationRepeatMode.RESTART);
        return parameter;
    }

    private void registerDemoAnimator(ObjectAnimator animator) {
        animator.setDuration(duration);
        trackAnimator(animator);
        demoAnimators.add(animator);
        updateAnimatorPlaying(animator);
    }

    private void updateAnimatorPlaying(ObjectAnimator animator) {
        if (playing) {
            if (animator.isPaused()) {
                animator.resume();
            } else if (!animator.isStarted()) {
                animator.start();
            }
        } else if (animator.isStarted() && !animator.isPaused()) {
            animator.pause();
        }
    }
}
