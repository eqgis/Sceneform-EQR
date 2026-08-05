package com.eqgis.test.fragments.tutorial;

import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.eqgis.eqr.animation.ARAnimationParameter;
import com.eqgis.eqr.animation.ARAnimationRepeatMode;
import com.eqgis.eqr.animation.ARAnimationTranslation;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;

import java.util.Locale;

/**
 * 节点位移动画教程
 * <pre>
 *     使用 ARAnimationTranslation 驱动 Node.localPosition，实时调整轴向、距离、周期和往返模式。
 * </pre>
 * @author tanyx
 */
public class TranslationAnimationLessonFragment extends BaseAnimationLessonFragment {
    private static final int MIN_DURATION_SECONDS = 1;
    private static final int DEFAULT_DURATION_SECONDS = 3;
    private static final int MAX_DURATION_SECONDS = 8;
    private static final int MIN_DISTANCE_STEP = 2;
    private static final int DEFAULT_DISTANCE_STEP = 8;
    private static final int MAX_DISTANCE_STEP = 12;
    private Node animatedNode;
    private ObjectAnimator animator;
    private int axisIndex;
    private float distance = DEFAULT_DISTANCE_STEP * 0.25f;
    private long duration = DEFAULT_DURATION_SECONDS * 1000L;
    private boolean reverse = true;
    private boolean playing = true;

    @Override
    protected String getLessonTitle() {
        return "位移动画";
    }

    @Override
    protected String getLessonDescription() {
        return "使用 ARAnimationTranslation 在两个 Vector3 坐标之间插值，实时调整移动轴、距离和周期。";
    }

    /**
     * 初始化位移动画球体
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 90);
        createSphere(
                calculateStartPosition(),
                0.35f,
                new Color(0.15f, 0.8f, 0.42f),
                node -> {
                    animatedNode = node;
                    restartAnimation();
                });
    }

    /**
     * 初始化位移参数悬浮面板
     * @param actionContainer 悬浮参数面板
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        Spinner axisSpinner = new Spinner(requireContext());
        String[] axisNames = {"X 轴", "Y 轴", "Z 轴"};
        configureTutorialSpinner(axisSpinner);
        axisSpinner.setAdapter(createTutorialSpinnerAdapter(axisNames));
        axisSpinner.setSelection(0, false);
        axisSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                axisIndex = position;
                restartAnimation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        addControlRow(actionContainer, "移动轴", axisSpinner);

        SeekBar distanceSeekBar = new SeekBar(requireContext());
        distanceSeekBar.setMax(MAX_DISTANCE_STEP - MIN_DISTANCE_STEP);
        distanceSeekBar.setProgress(DEFAULT_DISTANCE_STEP - MIN_DISTANCE_STEP);
        TextView distanceLabel = addControlRow(
                actionContainer,
                formatDistance(distance),
                distanceSeekBar);
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance = (MIN_DISTANCE_STEP + progress) * 0.25f;
                distanceLabel.setText(formatDistance(distance));
                restartAnimation();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

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
                if (animator != null) {
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

        Switch reverseSwitch = new Switch(requireContext());
        reverseSwitch.setText("往返");
        reverseSwitch.setChecked(true);
        reverseSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reverse = isChecked;
            restartAnimation();
        });
        addControlRow(actionContainer, "循环方式", reverseSwitch);

        Switch playSwitch = new Switch(requireContext());
        playSwitch.setText("播放");
        playSwitch.setChecked(true);
        playSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            playing = isChecked;
            updatePlayingState();
        });
        addControlRow(actionContainer, "播放状态", playSwitch);
    }

    private void restartAnimation() {
        if (animatedNode == null) {
            return;
        }
        untrackAnimator(animator);
        Vector3 start = calculateStartPosition();
        Vector3 end = calculateEndPosition();
        animatedNode.setLocalPosition(start);
        ARAnimationParameter parameter = new ARAnimationParameter();
        parameter.setDuration(duration);
        parameter.setRepeatCount(ObjectAnimator.INFINITE);
        parameter.setRepeatMode(reverse
                ? ARAnimationRepeatMode.REVERSE
                : ARAnimationRepeatMode.RESTART);
        parameter.setStartVector(start);
        parameter.setEndVector(end);
        ARAnimationTranslation translationAnimation = new ARAnimationTranslation(animatedNode);
        translationAnimation.createAnimation(parameter);
        animator = translationAnimation.getObjectAnimator();
        trackAnimator(animator);
        updatePlayingState();
    }

    private Vector3 calculateStartPosition() {
        float halfDistance = distance * 0.5f;
        if (axisIndex == 1) {
            return new Vector3(0, -halfDistance, -3.5f);
        }
        if (axisIndex == 2) {
            return new Vector3(0, 0, -3.5f + halfDistance);
        }
        return new Vector3(-halfDistance, 0, -3.5f);
    }

    private Vector3 calculateEndPosition() {
        float halfDistance = distance * 0.5f;
        if (axisIndex == 1) {
            return new Vector3(0, halfDistance, -3.5f);
        }
        if (axisIndex == 2) {
            return new Vector3(0, 0, -3.5f - halfDistance);
        }
        return new Vector3(halfDistance, 0, -3.5f);
    }

    private String formatDistance(float value) {
        return String.format(Locale.getDefault(), "移动距离：%.2f m", value);
    }

    private void updatePlayingState() {
        if (animator == null) {
            return;
        }
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
