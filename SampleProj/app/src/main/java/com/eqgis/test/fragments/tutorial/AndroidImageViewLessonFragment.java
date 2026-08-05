package com.eqgis.test.fragments.tutorial;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.eqgis.test.R;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;

/**
 * ImageView 三维渲染教程
 * <pre>
 *     使用项目中的 Drawable 创建多个 ImageView，并通过 ViewRenderable 显示在三维场景中。
 * </pre>
 * @author tanyx
 */
public class AndroidImageViewLessonFragment extends BaseAndroidViewLessonFragment {
    @Override
    protected String getLessonTitle() {
        return "ImageView 渲染";
    }

    @Override
    protected String getLessonDescription() {
        return "展示 Drawable、缩放模式、内边距和着色后的 ImageView 如何作为三维场景图标。";
    }

    /**
     * 创建多个 ImageView 示例
     */
    @Override
    protected void addViewExamples() {
        int[] drawableIds = {
                R.drawable.ic_home,
                R.drawable.ic_video_btn,
                R.drawable.ic_launcher_foreground
        };
        int[] tintColors = {
                Color.rgb(0, 122, 255),
                Color.rgb(175, 82, 222),
                Color.rgb(255, 149, 0)
        };
        float[] xPositions = {-0.9f, 0, 0.9f};
        for (int i = 0; i < drawableIds.length; i++) {
            ImageView imageView = new ImageView(requireContext());
            imageView.setImageResource(drawableIds[i]);
            imageView.setImageTintList(ColorStateList.valueOf(tintColors[i]));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setPadding(dp(18), dp(18), dp(18), dp(18));
            imageView.setBackground(createRoundedBackground(Color.WHITE, Color.rgb(210, 210, 215)));
            imageView.setContentDescription("三维场景图标 " + (i + 1));
            imageView.setLayoutParams(new ViewGroup.LayoutParams(dp(96), dp(96)));
            addViewRenderable(
                    imageView,
                    new Vector3(xPositions[i], 0.18f, -2.75f),
                    Quaternion.identity(),
                    0.95f);
        }
    }
}
