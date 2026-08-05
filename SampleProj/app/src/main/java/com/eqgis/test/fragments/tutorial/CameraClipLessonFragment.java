package com.eqgis.test.fragments.tutorial;

import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.MaterialFactory;

import java.util.Locale;

/**
 * 相机裁剪面教程
 * <pre>
 *     在不同深度放置三个 Cube，通过两个滑杆实时调整相机近裁剪面和远裁剪面。
 * </pre>
 * @author tanyx
 */
public class CameraClipLessonFragment extends BaseMaterialCameraLessonFragment {
    private static final String TAG = CameraClipLessonFragment.class.getSimpleName();
    private static final float DEFAULT_NEAR = 0.1f;
    private static final float DEFAULT_FAR = 12.0f;
    private static final int NEAR_STEPS = 29;
    private static final int MIN_FAR = 4;
    private static final int MAX_FAR = 20;
    private float nearClipPlane = DEFAULT_NEAR;
    private float farClipPlane = DEFAULT_FAR;

    @Override
    protected String getLessonTitle() {
        return "相机裁剪面";
    }

    @Override
    protected String getLessonDescription() {
        return "拖动近、远裁剪面滑杆，观察距离相机过近或过远的物体如何从渲染画面中被裁掉。";
    }

    /**
     * 初始化不同深度的裁剪参照物
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 80);
        applyClipPlanes(DEFAULT_NEAR, DEFAULT_FAR);
        createCube(new Vector3(-0.7f, 0, -1.0f), new Color(1.0f, 0.28f, 0.18f));
        createCube(new Vector3(0, 0, -3.2f), new Color(0.18f, 0.68f, 1.0f));
        createCube(new Vector3(0.8f, 0, -8.0f), new Color(0.32f, 0.82f, 0.36f));
    }

    /**
     * 初始化近、远裁剪面调节滑杆
     * @param actionContainer 操作按钮容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        actionContainer.setOrientation(LinearLayout.VERTICAL);

        TextView nearLabel = createSliderLabel(formatNearClip(DEFAULT_NEAR));
        SeekBar nearSeekBar = new SeekBar(requireContext());
        nearSeekBar.setMax(NEAR_STEPS);
        nearSeekBar.setProgress(0);
        nearSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //desc- 近裁剪范围为 0.1m 到 3.0m，步进 0.1m，始终小于远裁剪最小值。
                nearClipPlane = 0.1f + progress * 0.1f;
                nearLabel.setText(formatNearClip(nearClipPlane));
                applyClipPlanes(nearClipPlane, farClipPlane);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        actionContainer.addView(createSliderRow(nearLabel, nearSeekBar),
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView farLabel = createSliderLabel(formatFarClip((int) DEFAULT_FAR));
        SeekBar farSeekBar = new SeekBar(requireContext());
        farSeekBar.setMax(MAX_FAR - MIN_FAR);
        farSeekBar.setProgress((int) DEFAULT_FAR - MIN_FAR);
        farSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                farClipPlane = MIN_FAR + progress;
                farLabel.setText(formatFarClip((int) farClipPlane));
                applyClipPlanes(nearClipPlane, farClipPlane);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        actionContainer.addView(createSliderRow(farLabel, farSeekBar),
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private TextView createSliderLabel(String text) {
        TextView label = new TextView(requireContext());
        label.setText(text);
        label.setTextColor(0xff333333);
        label.setTextSize(14);
        label.setGravity(Gravity.CENTER_VERTICAL);
        return label;
    }

    private LinearLayout createSliderRow(TextView label, SeekBar seekBar) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(44));
        row.addView(label, new LinearLayout.LayoutParams(
                dp(112),
                ViewGroup.LayoutParams.MATCH_PARENT));
        row.addView(seekBar, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1));
        return row;
    }

    private String formatNearClip(float value) {
        return String.format(Locale.getDefault(), "近裁剪：%.1f m", value);
    }

    private String formatFarClip(int value) {
        return "远裁剪：" + value + " m";
    }

    private void applyClipPlanes(float nearPlane, float farPlane) {
        if (!isSceneActive()) {
            return;
        }
        sceneLayout.getCamera().setNearClipPlane(nearPlane);
        sceneLayout.getCamera().setFarClipPlane(farPlane);
    }

    private void createCube(Vector3 position, Color color) {
        MaterialFactory.makeOpaqueWithColor(requireContext(), color)
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    addRenderableNode(
                            GeometryUtils.makeCube(new Vector3(0.65f, 0.65f, 0.65f), Vector3.zero(), material),
                            position);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建裁剪面参照 Cube 失败", error);
                    return null;
                });
    }
}
