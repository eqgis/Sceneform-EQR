package com.eqgis.test.fragments.tutorial;

import android.animation.ObjectAnimator;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.eqgis.eqr.animation.ARAnimationParameter;
import com.eqgis.eqr.animation.ARAnimationPath;
import com.eqgis.eqr.animation.ARAnimationRepeatMode;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;

import java.util.Locale;

/**
 * 节点曲线路径动画教程
 * <pre>
 *     使用 ARAnimationPath 和 Bézier 控制点驱动 Node.localPosition，
 *     实时调整路径宽度、弧高、周期和往返模式。
 * </pre>
 * @author tanyx
 */
public class PathAnimationLessonFragment extends BaseAnimationLessonFragment {
    private static final int MIN_DURATION_SECONDS = 1;
    private static final int DEFAULT_DURATION_SECONDS = 4;
    private static final int MAX_DURATION_SECONDS = 8;
    private Node animatedNode;
    private ObjectAnimator animator;
    private float pathWidth = 3.0f;
    private float pathHeight = 1.0f;
    private long duration = DEFAULT_DURATION_SECONDS * 1000L;
    private boolean reverse = true;
    private boolean playing = true;

    @Override
    protected String getLessonTitle() {
        return "曲线路径动画";
    }

    @Override
    protected String getLessonDescription() {
        return "使用 ARAnimationPath 沿 Bézier 曲线移动节点，并实时调整路径宽度、弧高和播放周期。";
    }

    /**
     * 初始化曲线路径动画球体
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 90);
        createSphere(
                calculateStartPosition(),
                0.32f,
                new Color(1.0f, 0.42f, 0.08f),
                node -> {
                    animatedNode = node;
                    restartAnimation();
                });
    }

    /**
     * 初始化曲线路径参数悬浮面板
     * @param actionContainer 悬浮参数面板
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        SeekBar widthSeekBar = new SeekBar(requireContext());
        widthSeekBar.setMax(12);
        widthSeekBar.setProgress(8);
        TextView widthLabel = addControlRow(actionContainer, formatWidth(pathWidth), widthSeekBar);
        widthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pathWidth = 1.0f + progress * 0.25f;
                widthLabel.setText(formatWidth(pathWidth));
                restartAnimation();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar heightSeekBar = new SeekBar(requireContext());
        heightSeekBar.setMax(18);
        heightSeekBar.setProgress(8);
        TextView heightLabel = addControlRow(actionContainer, formatHeight(pathHeight), heightSeekBar);
        heightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pathHeight = 0.2f + progress * 0.1f;
                heightLabel.setText(formatHeight(pathHeight));
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
        parameter.addWayPoint(new Vector3(0, pathHeight, -2.8f));
        parameter.setEndVector(end);
        ARAnimationPath pathAnimation = new ARAnimationPath(animatedNode);
        pathAnimation.createAnimation(parameter);
        animator = pathAnimation.getObjectAnimator();
        trackAnimator(animator);
        updatePlayingState();
    }

    private Vector3 calculateStartPosition() {
        return new Vector3(-pathWidth * 0.5f, -0.45f, -3.5f);
    }

    private Vector3 calculateEndPosition() {
        return new Vector3(pathWidth * 0.5f, -0.45f, -3.5f);
    }

    private String formatWidth(float value) {
        return String.format(Locale.getDefault(), "路径宽度：%.2f m", value);
    }

    private String formatHeight(float value) {
        return String.format(Locale.getDefault(), "路径弧高：%.1f m", value);
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
