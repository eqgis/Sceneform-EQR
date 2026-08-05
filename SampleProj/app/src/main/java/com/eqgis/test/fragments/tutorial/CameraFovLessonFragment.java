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

/**
 * 相机视场角教程
 * <pre>
 *     使用固定位置的多个 Cube 作为参照，通过滑杆实时修改垂直 FOV 并观察透视变化。
 * </pre>
 * @author tanyx
 */
public class CameraFovLessonFragment extends BaseMaterialCameraLessonFragment {
    private static final String TAG = CameraFovLessonFragment.class.getSimpleName();
    private static final int MIN_FOV = 30;
    private static final int DEFAULT_FOV = 60;
    private static final int MAX_FOV = 100;

    @Override
    protected String getLessonTitle() {
        return "相机 FOV";
    }

    @Override
    protected String getLessonDescription() {
        return "调整相机垂直视场角，观察广角与窄视角对空间透视、物体大小和可见范围的影响。";
    }

    /**
     * 初始化 FOV 参照场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 80);
        sceneLayout.getCamera().setVerticalFovDegrees(DEFAULT_FOV);
        sceneLayout.getCamera().setFarClipPlane(100);
        createCube(new Vector3(-1.25f, 0, -3.0f), new Color(0.05f, 0.48f, 1.0f));
        createCube(new Vector3(0, 0, -3.6f), new Color(0.22f, 0.78f, 0.48f));
        createCube(new Vector3(1.25f, 0, -4.2f), new Color(1.0f, 0.52f, 0.08f));
    }

    /**
     * 初始化 FOV 调节滑杆
     * @param actionContainer 操作按钮容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        TextView label = new TextView(requireContext());
        label.setText("FOV：" + DEFAULT_FOV + "°");
        label.setTextColor(0xff333333);
        label.setTextSize(14);
        label.setGravity(Gravity.CENTER_VERTICAL);
        actionContainer.addView(label, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        SeekBar seekBar = new SeekBar(requireContext());
        seekBar.setMax(MAX_FOV - MIN_FOV);
        seekBar.setProgress(DEFAULT_FOV - MIN_FOV);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int fov = MIN_FOV + progress;
                label.setText("FOV：" + fov + "°");
                if (isSceneActive()) {
                    sceneLayout.getCamera().setVerticalFovDegrees(fov);
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

    private void createCube(Vector3 position, Color color) {
        MaterialFactory.makeOpaqueWithColor(requireContext(), color)
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    addRenderableNode(
                            GeometryUtils.makeCube(new Vector3(0.72f, 0.72f, 0.72f), Vector3.zero(), material),
                            position);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建 FOV 参照 Cube 失败", error);
                    return null;
                });
    }
}
