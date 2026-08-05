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
import com.google.sceneform.math.Vector3;

/**
 * GLB 模型动画教程
 * <pre>
 *     使用 ARAnimationModel 播放蜜蜂模型内置动画，并实时切换动画片段、周期和播放状态。
 * </pre>
 * @author tanyx
 */
public class ModelAnimationLessonFragment extends BaseAnimationLessonFragment {
    private static final int MIN_DURATION_SECONDS = 2;
    private static final int DEFAULT_DURATION_SECONDS = 6;
    private static final int MAX_DURATION_SECONDS = 10;
    private ARAnimationModel modelAnimation;
    private ObjectAnimator modelAnimator;
    private int animationIndex = 1;
    private long duration = DEFAULT_DURATION_SECONDS * 1000L;
    private boolean playing = true;

    @Override
    protected String getLessonTitle() {
        return "模型动画";
    }

    @Override
    protected String getLessonDescription() {
        return "使用 ARAnimationModel 播放 GLB 内置动画，切换 Idle、Hover 与起飞/降落片段。";
    }

    /**
     * 加载包含三个内置动画片段的蜜蜂模型
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 90);
        loadAnimatedBee(new Vector3(0, -0.15f, -3.2f), 0.9f, node -> {
            if (node.getRenderableInstance() == null
                    || node.getRenderableInstance().getAnimationCount() == 0) {
                return;
            }
            ARAnimationParameter parameter = new ARAnimationParameter();
            parameter.setDuration(duration);
            parameter.setRepeatCount(ObjectAnimator.INFINITE);
            parameter.setRepeatMode(ARAnimationRepeatMode.RESTART);
            modelAnimation = new ARAnimationModel(node);
            modelAnimation.createAnimation(parameter);
            animationIndex = Math.min(animationIndex,
                    node.getRenderableInstance().getAnimationCount() - 1);
            modelAnimation.setCurrentIndex(animationIndex);
            modelAnimator = modelAnimation.getObjectAnimator();
            trackAnimator(modelAnimator);
            updatePlayingState();
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
        String[] clips = {"Idle", "Hover", "起飞/降落"};
        configureTutorialSpinner(clipSpinner);
        clipSpinner.setAdapter(createTutorialSpinnerAdapter(clips));
        clipSpinner.setSelection(animationIndex, false);
        clipSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                animationIndex = position;
                if (modelAnimation != null) {
                    modelAnimation.setCurrentIndex(animationIndex);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        addControlRow(actionContainer, "动画片段", clipSpinner);

        SeekBar durationSeekBar = new SeekBar(requireContext());
        durationSeekBar.setMax(MAX_DURATION_SECONDS - MIN_DURATION_SECONDS);
        durationSeekBar.setProgress(DEFAULT_DURATION_SECONDS - MIN_DURATION_SECONDS);
        TextView durationLabel = addControlRow(
                actionContainer,
                "播放周期：" + DEFAULT_DURATION_SECONDS + "s",
                durationSeekBar);
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int seconds = MIN_DURATION_SECONDS + progress;
                duration = seconds * 1000L;
                durationLabel.setText("播放周期：" + seconds + "s");
                if (modelAnimator != null) {
                    modelAnimator.setDuration(duration);
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
            updatePlayingState();
        });
        addControlRow(actionContainer, "播放状态", playSwitch);
    }

    private void updatePlayingState() {
        if (modelAnimator == null) {
            return;
        }
        if (playing) {
            if (modelAnimator.isPaused()) {
                modelAnimator.resume();
            } else if (!modelAnimator.isStarted()) {
                modelAnimator.start();
            }
        } else if (modelAnimator.isStarted() && !modelAnimator.isPaused()) {
            modelAnimator.pause();
        }
    }
}
