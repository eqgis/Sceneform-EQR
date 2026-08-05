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
import com.eqgis.eqr.animation.ARAnimationRotation;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.Node;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;

/**
 * 节点旋转动画教程
 * <pre>
 *     使用 ARAnimationRotation 驱动 Node.localRotation，实时调整旋转轴、角度、周期与方向。
 * </pre>
 * @author tanyx
 */
public class RotationAnimationLessonFragment extends BaseAnimationLessonFragment {
    private static final int MIN_DURATION_SECONDS = 1;
    private static final int DEFAULT_DURATION_SECONDS = 4;
    private static final int MAX_DURATION_SECONDS = 8;
    private static final int MIN_ANGLE = 90;
    private static final int ANGLE_STEP = 30;
    private static final int MAX_ANGLE = 720;
    private Node animatedNode;
    private ObjectAnimator animator;
    private Vector3 rotationAxis = Vector3.up();
    private float rotationAngle = 360;
    private long duration = DEFAULT_DURATION_SECONDS * 1000L;
    private boolean clockwise;
    private boolean playing = true;

    @Override
    protected String getLessonTitle() {
        return "旋转动画";
    }

    @Override
    protected String getLessonDescription() {
        return "使用 ARAnimationRotation 创建四元数属性动画，并实时调节旋转轴、角度、周期和方向。";
    }

    /**
     * 初始化旋转动画 Cube
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 90);
        createCube(
                new Vector3(0, 0, -3.2f),
                0.9f,
                new Color(0.08f, 0.5f, 1.0f),
                node -> {
                    animatedNode = node;
                    restartAnimation();
                });
    }

    /**
     * 初始化旋转参数悬浮面板
     * @param actionContainer 悬浮参数面板
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        Spinner axisSpinner = new Spinner(requireContext());
        String[] axisNames = {"X 轴", "Y 轴", "Z 轴"};
        Vector3[] axes = {Vector3.right(), Vector3.up(), Vector3.forward()};
        configureTutorialSpinner(axisSpinner);
        axisSpinner.setAdapter(createTutorialSpinnerAdapter(axisNames));
        axisSpinner.setSelection(1, false);
        axisSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rotationAxis = axes[position];
                restartAnimation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        addControlRow(actionContainer, "旋转轴", axisSpinner);

        SeekBar angleSeekBar = new SeekBar(requireContext());
        angleSeekBar.setMax((MAX_ANGLE - MIN_ANGLE) / ANGLE_STEP);
        angleSeekBar.setProgress((360 - MIN_ANGLE) / ANGLE_STEP);
        TextView angleLabel = addControlRow(actionContainer, "旋转角度：360°", angleSeekBar);
        angleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rotationAngle = MIN_ANGLE + progress * ANGLE_STEP;
                angleLabel.setText("旋转角度：" + (int) rotationAngle + "°");
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

        Switch clockwiseSwitch = new Switch(requireContext());
        clockwiseSwitch.setText("顺时针");
        clockwiseSwitch.setChecked(false);
        clockwiseSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            clockwise = isChecked;
            restartAnimation();
        });
        addControlRow(actionContainer, "旋转方向", clockwiseSwitch);

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
        animatedNode.setLocalRotation(Quaternion.identity());
        ARAnimationParameter parameter = new ARAnimationParameter();
        parameter.setDuration(duration);
        parameter.setRepeatCount(ObjectAnimator.INFINITE);
        parameter.setRepeatMode(ARAnimationRepeatMode.RESTART);
        parameter.setRotationAxis(rotationAxis);
        parameter.setClockwise(clockwise);
        ARAnimationRotation rotationAnimation = new ARAnimationRotation(animatedNode);
        rotationAnimation.setRotationAngle(rotationAngle);
        rotationAnimation.createAnimation(parameter);
        animator = rotationAnimation.getObjectAnimator();
        trackAnimator(animator);
        updatePlayingState();
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
