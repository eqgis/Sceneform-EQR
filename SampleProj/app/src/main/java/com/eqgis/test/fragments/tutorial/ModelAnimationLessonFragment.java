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
import com.eqgis.eqr.animation.ARAnimationRepeatMode;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;

import java.util.Locale;

/**
 * GLB 模型动画教程
 * <pre>
 *     使用 ARAnimationModel 播放 Fox 模型内置动画，并按动画索引实时切换动画、周期模式和播放状态。
 * </pre>
 * @author tanyx
 */
public class ModelAnimationLessonFragment extends BaseAnimationLessonFragment {
    private static final int MIN_DURATION_SECONDS = 2;
    private static final int DEFAULT_DURATION_SECONDS = 6;
    private static final int MAX_DURATION_SECONDS = 10;
    private ARAnimationModel modelAnimation;
    private ObjectAnimator modelAnimator;
    private Node modelNode;
    private int animationIndex = 0;
    private int animationCount;
    private long customDuration = DEFAULT_DURATION_SECONDS * 1000L;
    private boolean useSourceDuration;
    private SeekBar durationSeekBar;
    private TextView durationLabel;
    private boolean playing = true;

    @Override
    protected String getLessonTitle() {
        return "模型动画";
    }

    @Override
    protected String getLessonDescription() {
        return "播放 GLB 内置动画，并在自定义周期与模型源动画时长之间切换。";
    }

    /**
     * 加载包含三个内置动画片段的 Fox 模型
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 90);
        loadAnimatedGlb(new Vector3(0, -0.15f, -1.2f), 1.0f, node -> {
            if (node.getRenderableInstance() == null
                    || node.getRenderableInstance().getAnimationCount() == 0) {
                return;
            }
            modelNode = node;
            animationCount = node.getRenderableInstance().getAnimationCount();
            switchAnimationByIndex(animationIndex);
        });
    }

    /**
     * 初始化模型片段、播放周期和播放状态控件
     * @param actionContainer 悬浮参数面板
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        Spinner clipSpinner = new Spinner(requireContext());
        String[] animationIndexes = {"索引 0", "索引 1", "索引 2"};
        configureTutorialSpinner(clipSpinner);
        clipSpinner.setAdapter(createTutorialSpinnerAdapter(animationIndexes));
        clipSpinner.setSelection(animationIndex, false);
        clipSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switchAnimationByIndex(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        addControlRow(actionContainer, "动画索引", clipSpinner);

        Spinner durationModeSpinner = new Spinner(requireContext());
        String[] durationModes = {"自定义周期", "源动画时长"};
        configureTutorialSpinner(durationModeSpinner);
        durationModeSpinner.setAdapter(createTutorialSpinnerAdapter(durationModes));
        durationModeSpinner.setSelection(0, false);
        durationModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                useSourceDuration = position == 1;
                applyModelAnimatorDuration();
                updateDurationControls();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        addControlRow(actionContainer, "周期模式", durationModeSpinner);

        durationSeekBar = new SeekBar(requireContext());
        durationSeekBar.setMax(MAX_DURATION_SECONDS - MIN_DURATION_SECONDS);
        durationSeekBar.setProgress(DEFAULT_DURATION_SECONDS - MIN_DURATION_SECONDS);
        durationLabel = addControlRow(
                actionContainer,
                "自定义周期：" + DEFAULT_DURATION_SECONDS + "s",
                durationSeekBar);
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int seconds = MIN_DURATION_SECONDS + progress;
                customDuration = seconds * 1000L;
                if (!useSourceDuration) {
                    applyModelAnimatorDuration();
                }
                updateDurationControls();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        updateDurationControls();

        Switch playSwitch = new Switch(requireContext());
        playSwitch.setText("播放");
        playSwitch.setChecked(true);
        playSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            playing = isChecked;
            updatePlayingState();
        });
        addControlRow(actionContainer, "播放状态", playSwitch);
    }

    /**
     * 按索引切换 GLB 模型内置动画
     * @param index 从 0 开始的动画索引
     */
    private void switchAnimationByIndex(int index) {
        animationIndex = index;
        if (modelNode == null || modelNode.getRenderableInstance() == null
                || animationCount == 0) {
            return;
        }
        animationIndex = Math.max(0, Math.min(animationIndex, animationCount - 1));

        //desc- 每个索引创建独立属性动画，避免复用 ObjectAnimator 时保留上一个索引的运行状态。
        untrackAnimator(modelAnimator);
        ARAnimationParameter parameter = new ARAnimationParameter();
        parameter.setDuration(customDuration);
        parameter.setRepeatCount(ObjectAnimator.INFINITE);
        parameter.setRepeatMode(ARAnimationRepeatMode.RESTART);
        modelAnimation = new ARAnimationModel(modelNode);
        modelAnimation.setCurrentIndex(animationIndex);
        modelAnimation.createAnimation(parameter);
        modelAnimator = modelAnimation.getObjectAnimator();
        applyModelAnimatorDuration();
        trackAnimator(modelAnimator);
        modelAnimation.setCurrentProgress(0.0f);
        updateDurationControls();
        if (playing) {
            modelAnimation.play();
        }
    }

    /**
     * 刷新周期滑杆状态和当前生效周期文案
     */
    private void updateDurationControls() {
        if (durationSeekBar == null || durationLabel == null) {
            return;
        }
        boolean customMode = !useSourceDuration;
        durationSeekBar.setEnabled(customMode);
        durationSeekBar.setAlpha(customMode ? 1.0f : 0.45f);
        if (customMode) {
            durationLabel.setText("自定义周期：" + formatDuration(customDuration));
            return;
        }
        long sourceDuration = modelAnimation == null
                ? 0L
                : modelAnimation.getSourceDurationMillis();
        durationLabel.setText(sourceDuration > 0L
                ? "源动画周期：" + formatDuration(sourceDuration)
                : "源动画周期：等待模型");
    }

    /**
     * 在应用层按当前模式设置模型属性动画周期
     */
    private void applyModelAnimatorDuration() {
        if (modelAnimator == null) {
            return;
        }
        long appliedDuration = customDuration;
        if (useSourceDuration && modelAnimation != null) {
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

    private void updatePlayingState() {
        if (modelAnimator == null) {
            return;
        }
        if (playing) {
            if (modelAnimator.isPaused()) {
                modelAnimation.resume();
            } else if (!modelAnimator.isStarted()) {
                //desc- 与 GltfSampleScene 保持一致，通过 ARAnimationModel 公开接口启动模型动画。
                modelAnimation.play();
            }
        } else if (modelAnimator.isStarted() && !modelAnimator.isPaused()) {
            modelAnimation.pause();
        }
    }
}
