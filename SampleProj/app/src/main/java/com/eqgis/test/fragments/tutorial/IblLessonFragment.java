package com.eqgis.test.fragments.tutorial;

import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.android.filament.IndirectLight;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.MaterialFactory;

/**
 * IBL 环境光教程
 * <pre>
 *     使用 KTX 环境光照亮不同粗糙度的 PBR 球体，并通过滑杆实时调整环境光强度。
 * </pre>
 * @author tanyx
 */
public class IblLessonFragment extends BaseMaterialCameraLessonFragment {
    private static final String TAG = IblLessonFragment.class.getSimpleName();
    private static final int DEFAULT_IBL_INTENSITY = 80;

    @Override
    protected String getLessonTitle() {
        return "IBL 环境光";
    }

    @Override
    protected String getLessonDescription() {
        return "加载 KTX 间接光并调整强度，观察环境反射对不同粗糙度 PBR 表面的影响。";
    }

    /**
     * 初始化 IBL 对比场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", DEFAULT_IBL_INTENSITY);
        createSphere(new Vector3(-0.9f, 0, -3.0f), 0.12f);
        createSphere(new Vector3(0, 0, -3.0f), 0.48f);
        createSphere(new Vector3(0.9f, 0, -3.0f), 0.9f);
    }

    /**
     * 初始化 IBL 强度滑杆
     * @param actionContainer 操作按钮容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        TextView label = new TextView(requireContext());
        label.setText("IBL：" + DEFAULT_IBL_INTENSITY);
        label.setTextColor(0xff333333);
        label.setTextSize(14);
        label.setGravity(Gravity.CENTER_VERTICAL);
        actionContainer.addView(label, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        SeekBar seekBar = new SeekBar(requireContext());
        seekBar.setMax(160);
        seekBar.setProgress(DEFAULT_IBL_INTENSITY);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int intensity = Math.max(1, progress);
                label.setText("IBL：" + intensity);
                if (!isSceneActive()) {
                    return;
                }
                IndirectLight indirectLight = sceneLayout.getIndirectLight();
                if (indirectLight != null) {
                    indirectLight.setIntensity(intensity);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        actionContainer.addView(seekBar, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1));
    }

    private void createSphere(Vector3 position, float roughness) {
        MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(0.82f, 0.86f, 0.92f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    material.setFloat(MaterialFactory.MATERIAL_METALLIC, 1.0f);
                    material.setFloat(MaterialFactory.MATERIAL_ROUGHNESS, roughness);
                    addRenderableNode(
                            GeometryUtils.makeSphere(0.42f, Vector3.zero(), material),
                            position);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建 IBL 测试球体失败", error);
                    return null;
                });
    }
}
