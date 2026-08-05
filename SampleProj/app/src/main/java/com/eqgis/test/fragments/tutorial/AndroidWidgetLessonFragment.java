package com.eqgis.test.fragments.tutorial;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;

/**
 * Android 常用控件三维渲染教程
 * <pre>
 *     将Switch、CheckBox 分别转换为 ViewRenderable，
 *     便于比较不同 Android 控件在三维场景中的显示效果。
 * </pre>
 * @author tanyx
 */
public class AndroidWidgetLessonFragment extends BaseAndroidViewLessonFragment {
    @Override
    protected String getLessonTitle() {
        return "常用 View 渲染";
    }

    @Override
    protected String getLessonDescription() {
        return "同时展示 Switch、CheckBox，验证常用 Android 控件的三维渲染。";
    }

    /**
     * 创建多个常用 Android View 示例
     */
    @Override
    protected void addViewExamples() {
        Switch switchView = new Switch(requireContext());
        switchView.setText("实时更新");
        switchView.setTextColor(Color.rgb(29, 29, 31));
        switchView.setTextSize(15);
        switchView.setChecked(true);
        switchView.setPadding(dp(12), dp(8), dp(12), dp(8));
        switchView.setBackground(createRoundedBackground(Color.WHITE, Color.rgb(210, 210, 215)));
        addViewRenderable(
                switchView,
                new Vector3(0.72f, 0.48f, -2.8f),
                Quaternion.identity(),
                0.82f);

        CheckBox checkBox = new CheckBox(requireContext());
        checkBox.setText("显示标注");
        checkBox.setTextColor(Color.rgb(29, 29, 31));
        checkBox.setTextSize(15);
        checkBox.setChecked(true);
        checkBox.setPadding(dp(10), dp(8), dp(12), dp(8));
        checkBox.setBackground(createRoundedBackground(Color.WHITE, Color.rgb(210, 210, 215)));
        addViewRenderable(
                checkBox,
                new Vector3(-0.72f, -0.18f, -2.75f),
                Quaternion.identity(),
                0.82f);

    }
}
