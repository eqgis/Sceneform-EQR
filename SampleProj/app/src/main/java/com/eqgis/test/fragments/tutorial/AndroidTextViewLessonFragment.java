package com.eqgis.test.fragments.tutorial;

import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;

/**
 * TextView 三维渲染教程
 * <pre>
 *     创建多个不同内容和样式的 TextView，并通过 ViewRenderable 放置到场景可见区域。
 * </pre>
 * @author tanyx
 */
public class AndroidTextViewLessonFragment extends BaseAndroidViewLessonFragment {
    @Override
    protected String getLessonTitle() {
        return "TextView 渲染";
    }

    @Override
    protected String getLessonDescription() {
        return "将多个 TextView 转换为 ViewRenderable，在三维空间中展示文字标签、状态和场景注释。";
    }

    /**
     * 创建多个 TextView 示例
     */
    @Override
    protected void addViewExamples() {
        String[] texts = {"文字标签", "温度 24.6°C", "Cube 标注"};
        int[] fillColors = {
                Color.rgb(0, 122, 255),
                Color.rgb(52, 199, 89),
                Color.rgb(255, 149, 0)
        };
        float[] xPositions = {-1.0f, 0, 1.0f};
        for (int i = 0; i < texts.length; i++) {
            TextView textView = new TextView(requireContext());
            textView.setText(texts[i]);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(16);
            textView.setGravity(Gravity.CENTER);
            textView.setMinWidth(dp(112));
            textView.setMinHeight(dp(52));
            textView.setPadding(dp(14), dp(10), dp(14), dp(10));
            textView.setBackground(createRoundedBackground(fillColors[i], Color.WHITE));
            addViewRenderable(
                    textView,
                    new Vector3(xPositions[i], 0.38f, -2.8f),
                    Quaternion.identity(),
                    0.9f);
        }
    }
}
