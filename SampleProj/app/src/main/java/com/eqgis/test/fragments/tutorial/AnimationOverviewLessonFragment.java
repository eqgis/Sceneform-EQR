package com.eqgis.test.fragments.tutorial;

import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
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
import java.util.Locale;

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
    private static final int MODEL_ANIMATION_INDEX = 2;
    private final List<ObjectAnimator> demoAnimators = new ArrayList<>();
    private ARAnimationModel modelAnimation;
    private ObjectAnimator modelAnimator;
    private boolean useSourceModelDuration;
    private TextView modelDurationLabel;
    private TextView customDurationLabel;
    private boolean playing = true;
    private long duration = DEFAULT_DURATION_SECONDS * 1000L;

    @Override
    protected String getLessonTitle() {
        return "动画总览";
    }

    @Override
    protected String getLessonDescription() {
        return "比较四种动画，并让模型动画选择自定义周期或 GLB 源动画时长。";
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
        Spinner durationModeSpinner = new Spinner(requireContext());
        String[] durationModes = {"自定义周期", "源动画时长"};
        configureTutorialSpinner(durationModeSpinner);
        durationModeSpinner.setAdapter(createTutorialSpinnerAdapter(durationModes));
        durationModeSpinner.setSelection(0, false);
        durationModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                useSourceModelDuration = position == 1;
                applyModelAnimatorDuration();
                updateDurationLabels();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        modelDurationLabel = addControlRow(actionContainer, "模型周期：自定义", durationModeSpinner);

        SeekBar durationSeekBar = new SeekBar(requireContext());
        durationSeekBar.setMax(MAX_DURATION_SECONDS - MIN_DURATION_SECONDS);
        durationSeekBar.setProgress(DEFAULT_DURATION_SECONDS - MIN_DURATION_SECONDS);
        customDurationLabel = addControlRow(
                actionContainer,
                "统一自定义周期：" + DEFAULT_DURATION_SECONDS + "s",
                durationSeekBar);
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int seconds = MIN_DURATION_SECONDS + progress;
                duration = seconds * 1000L;
                for (ObjectAnimator animator : demoAnimators) {
                    if (animator != modelAnimator) {
                        animator.setDuration(duration);
                    }
                }
                if (!useSourceModelDuration) {
                    applyModelAnimatorDuration();
                }
                updateDurationLabels();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        updateDurationLabels();

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
        loadAnimatedGlb(new Vector3(-1.f, -0.1f, -2.0f), 1.0f, node -> {
            if (node.getRenderableInstance() == null
                    || node.getRenderableInstance().getAnimationCount() == 0) {
                return;
            }
            ARAnimationParameter parameter = createLoopParameter();
            ARAnimationModel animation = new ARAnimationModel(node);
            int animationIndex = Math.min(
                    MODEL_ANIMATION_INDEX,
                    node.getRenderableInstance().getAnimationCount() - 1);
            animation.setCurrentIndex(animationIndex);
            animation.createAnimation(parameter);
            //desc- Fox 的索引 2 动作幅度清晰，适合在总览缩小展示中观察。
            registerModelDemoAnimator(animation);
        });
    }

    /**
     * 注册并通过 ARAnimationModel 公开接口启动模型内置动画
     * @param animation Fox 模型动画
     */
    private void registerModelDemoAnimator(ARAnimationModel animation) {
        modelAnimation = animation;
        modelAnimator = animation.getObjectAnimator();
        applyModelAnimatorDuration();
        trackAnimator(modelAnimator);
        demoAnimators.add(modelAnimator);
        updateDurationLabels();
        if (playing) {
            animation.play();
        }
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

    /**
     * 刷新模型和其他动画当前使用的周期文案
     */
    private void updateDurationLabels() {
        if (customDurationLabel != null) {
            customDurationLabel.setText(!useSourceModelDuration
                    ? "统一自定义周期：" + formatDuration(duration)
                    : "其他动画周期：" + formatDuration(duration));
        }
        if (modelDurationLabel == null) {
            return;
        }
        if (!useSourceModelDuration) {
            modelDurationLabel.setText("模型周期：自定义 " + formatDuration(duration));
            return;
        }
        long sourceDuration = modelAnimation == null
                ? 0L
                : modelAnimation.getSourceDurationMillis();
        modelDurationLabel.setText(sourceDuration > 0L
                ? "模型周期：源动画 " + formatDuration(sourceDuration)
                : "模型周期：源动画（等待模型）");
    }

    /**
     * 在应用层按当前模式设置模型属性动画周期
     */
    private void applyModelAnimatorDuration() {
        if (modelAnimator == null) {
            return;
        }
        long appliedDuration = duration;
        if (useSourceModelDuration && modelAnimation != null) {
            long sourceDuration = modelAnimation.getSourceDurationMillis();
            if (sourceDuration > 0L) {
                appliedDuration = sourceDuration;
            }
        }
        modelAnimator.setDuration(appliedDuration);
    }

    /**
     * 将毫秒周期格式化为秒
     * @param durationMillis 周期，单位毫秒
     * @return 最多保留两位小数的秒数文案
     */
    private String formatDuration(long durationMillis) {
        float seconds = durationMillis / 1000.0f;
        if (durationMillis % 1000L == 0L) {
            return String.format(Locale.getDefault(), "%.0fs", seconds);
        }
        return String.format(Locale.getDefault(), "%.2fs", seconds);
    }
}
