package com.eqgis.test.fragments.tutorial;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Material;

/**
 * 自定义 Filament 材质进阶教程。
 * <p>加载由 matc 编译的 filamat，并实时修改材质声明中的 PBR 参数。</p>
 * @author tanyx
 */
public class CustomFilamentMaterialLessonFragment extends BaseAdvancedLessonFragment {
    private static final String TAG = CustomFilamentMaterialLessonFragment.class.getSimpleName();
    private final Color[] colors = {
            new Color(0.08f, 0.48f, 1.0f),
            new Color(0.95f, 0.62f, 0.12f),
            new Color(0.12f, 0.78f, 0.50f)
    };
    private Material customMaterial;
    private TextView statusView;
    private int colorIndex;
    private float metallic = 0.7f;
    private float roughness = 0.25f;
    private float reflectance = 0.5f;

    @Override
    protected String getLessonTitle() {
        return "自定义 Filament 材质";
    }

    @Override
    protected String getLessonDescription() {
        return "加载 sceneform_opaque_colored_material.filamat，并实时设置 color、metallic、roughness 与 reflectance。源码 .mat 需先用 Filament matc 编译后放入 res/raw。";
    }

    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureAdvancedScene(sceneLayout, true);
        Material.builder()
                .setSource(requireContext(), com.eqgis.eqr.R.raw.sceneform_opaque_colored_material)
                .build()
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    customMaterial = material;
                    applyParameters();
                    addRenderableNode(
                            GeometryUtils.makeSphere(0.55f, Vector3.zero(), customMaterial),
                            new Vector3(-0.65f, 0.0f, -3.1f));
                    addRenderableNode(
                            GeometryUtils.makeCube(new Vector3(0.85f, 0.85f, 0.85f), Vector3.zero(), customMaterial),
                            new Vector3(0.75f, 0.0f, -3.1f));
                    updateStatus();
                })
                .exceptionally(error -> {
                    Log.e(TAG, "加载自定义 Filament 材质失败", error);
                    if (statusView != null) {
                        statusView.setText("材质加载失败：" + error.getMessage());
                    }
                    return null;
                });
    }

    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        statusView = addPanelText(actionContainer, "正在加载 res/raw 中的编译材质…");
        Spinner colorSpinner = addPanelSpinner(actionContainer, "基础色", new String[]{"科技蓝", "金属金", "翡翠绿"});
        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                colorIndex = position;
                applyParameters();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        bindParameterBar(addPanelSeekBar(actionContainer, "金属度：0.70", 100, 70), 0);
        bindParameterBar(addPanelSeekBar(actionContainer, "粗糙度：0.25", 100, 25), 1);
        bindParameterBar(addPanelSeekBar(actionContainer, "反射率：0.50", 100, 50), 2);
        addPanelText(actionContainer,
                "材质工作流：Tool/assets/mat/*.mat → matc 编译 → *.filamat → Android res/raw → Material.Builder。参数名必须与 .mat 的 parameters 声明完全一致。"
        );
    }

    private void bindParameterBar(SeekBar seekBar, int type) {
        LinearLayout parent = (LinearLayout) seekBar.getParent();
        int labelIndex = parent.indexOfChild(seekBar) - 1;
        TextView label = (TextView) parent.getChildAt(labelIndex);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                float value = progress / 100f;
                if (type == 0) {
                    metallic = value;
                    label.setText(String.format(java.util.Locale.US, "金属度：%.2f", value));
                } else if (type == 1) {
                    roughness = Math.max(0.04f, value);
                    label.setText(String.format(java.util.Locale.US, "粗糙度：%.2f", roughness));
                } else {
                    reflectance = value;
                    label.setText(String.format(java.util.Locale.US, "反射率：%.2f", value));
                }
                applyParameters();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void applyParameters() {
        if (customMaterial == null) {
            return;
        }
        customMaterial.setFloat3("color", colors[colorIndex]);
        customMaterial.setFloat("metallic", metallic);
        customMaterial.setFloat("roughness", roughness);
        customMaterial.setFloat("reflectance", reflectance);
        updateStatus();
    }

    private void updateStatus() {
        if (statusView != null && customMaterial != null) {
            statusView.setText(String.format(java.util.Locale.US,
                    "材质已加载\nmetallic %.2f · roughness %.2f · reflectance %.2f",
                    metallic, roughness, reflectance));
        }
    }

    @Override
    protected void onReleaseAdvancedScene() {
        customMaterial = null;
        statusView = null;
    }
}
