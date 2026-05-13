package com.eqgis.test.fragments.common;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.eqgis.test.R;
import com.eqgis.test.SampleLesson;
import com.eqgis.test.utils.AssetImageLoader;
import com.google.android.material.button.MaterialButton;

/**
 * 常用示例跳转说明页
 * <pre>
 *     常用示例保留原 Activity 作为真实运行入口。
 *     本 Fragment 只展示示例说明、代码位置和跳转按钮，避免归档页复制旧 Activity 的渲染生命周期。
 * </pre>
 * @author tanyx
 */
public class CommonSampleLaunchFragment extends Fragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_CODE_LOCATION = "codeLocation";
    private static final String ARG_PREVIEW_ASSET_PATH = "previewAssetPath";
    private static final String ARG_ACTIVITY_CLASS = "activityClass";

    /**
     * 创建常用示例跳转说明页
     * @param lesson {@link SampleLesson} 常用示例数据
     * @return 常用示例跳转说明页
     */
    public static CommonSampleLaunchFragment newInstance(SampleLesson lesson) {
        CommonSampleLaunchFragment fragment = new CommonSampleLaunchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, lesson.getTitle());
        args.putString(ARG_DESCRIPTION, lesson.getDescription());
        args.putString(ARG_CODE_LOCATION, lesson.getCodeLocation());
        args.putString(ARG_PREVIEW_ASSET_PATH, lesson.getPreviewAssetPath());
        if (lesson.getActivityClass() != null) {
            args.putString(ARG_ACTIVITY_CLASS, lesson.getActivityClass().getName());
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.setBackgroundColor(color(R.color.sample_background));

        TextView title = new TextView(requireContext());
        title.setText(readArg(ARG_TITLE, "常用示例"));
        title.setTextColor(color(R.color.sample_text_primary));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView description = new TextView(requireContext());
        description.setText(readArg(ARG_DESCRIPTION, ""));
        description.setTextColor(color(R.color.sample_text_secondary));
        description.setTextSize(16);
        description.setGravity(Gravity.CENTER);
        description.setLineSpacing(dp(2), 1.0f);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        descriptionParams.topMargin = dp(16);
        root.addView(description, descriptionParams);

        TextView codeLocation = new TextView(requireContext());
        codeLocation.setText("代码位置：" + readArg(ARG_CODE_LOCATION, ""));
        codeLocation.setTextColor(color(R.color.sample_text_secondary));
        codeLocation.setTextSize(14);
        codeLocation.setGravity(Gravity.CENTER);
        codeLocation.setPadding(dp(12), dp(10), dp(12), dp(10));
        codeLocation.setBackground(codePanelBackground());
        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        codeParams.topMargin = dp(18);
        root.addView(codeLocation, codeParams);

        TextView hint = new TextView(requireContext());
        hint.setText("该归档页仅展示说明和代码定位，实际演示逻辑保持原 Activity 不变。");
        hint.setTextColor(color(R.color.sample_text_secondary));
        hint.setTextSize(14);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        hintParams.topMargin = dp(14);
        root.addView(hint, hintParams);

        View spacer = new View(requireContext());
        root.addView(spacer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1));

        addPreviewImage(root);

        MaterialButton button = new MaterialButton(requireContext());
        button.setText("打开示例");
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setTextColor(0xffffffff);
        button.setMinHeight(dp(52));
        button.setCornerRadius(dp(14));
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundTintList(ColorStateList.valueOf(color(R.color.sample_accent)));
        button.setRippleColor(ColorStateList.valueOf(0x33ffffff));
        button.setOnClickListener(v -> launchOriginalActivity());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52));
        buttonParams.topMargin = dp(36);
        root.addView(button, buttonParams);

        return root;
    }

    /**
     * 添加当前示例预览图
     * @param root 页面根布局
     */
    private void addPreviewImage(LinearLayout root) {
        String previewAssetPath = readArg(ARG_PREVIEW_ASSET_PATH, "");
        if (previewAssetPath.isEmpty()) {
            return;
        }
        Bitmap bitmap = AssetImageLoader.loadBitmapFromAssets(requireContext(), previewAssetPath);
        if (bitmap == null) {
            return;
        }

        ImageView previewImage = new ImageView(requireContext());
        previewImage.setImageBitmap(bitmap);
        previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        previewImage.setAdjustViewBounds(false);
        previewImage.setBackground(previewBackground());
        previewImage.setClipToOutline(true);
        previewImage.setContentDescription("示例预览图");
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(180));
        previewParams.topMargin = dp(16);
        root.addView(previewImage, previewParams);
    }

    /**
     * 启动原有示例 Activity
     */
    private void launchOriginalActivity() {
        String className = readArg(ARG_ACTIVITY_CLASS, "");
        if (className.isEmpty()) {
            Toast.makeText(requireContext(), "未配置原示例 Activity", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Class<?> activityClass = Class.forName(className);
            startActivity(new Intent(requireContext(), activityClass));
        } catch (ClassNotFoundException e) {
            Toast.makeText(requireContext(), "找不到原示例 Activity：" + className, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 读取 Fragment 参数
     * @param key 参数 key
     * @param fallback 默认值
     * @return 参数值
     */
    private String readArg(String key, String fallback) {
        Bundle args = getArguments();
        if (args == null) {
            return fallback;
        }
        return args.getString(key, fallback);
    }

    /**
     * dp 转 px
     * @param value dp 值
     * @return px 值
     */
    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    /**
     * 读取当前主题下的颜色资源
     * @param colorRes 颜色资源 id
     * @return 解析后的颜色值
     */
    private int color(int colorRes) {
        return ContextCompat.getColor(requireContext(), colorRes);
    }

    /**
     * 创建代码位置提示面板背景
     * @return 轻量圆角描边背景
     */
    private GradientDrawable codePanelBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color(R.color.sample_surface));
        drawable.setCornerRadius(dp(14));
        drawable.setStroke(dp(1), color(R.color.sample_card_stroke));
        return drawable;
    }

    /**
     * 创建预览图背景
     * @return 圆角描边背景
     */
    private GradientDrawable previewBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color(R.color.sample_surface));
        drawable.setCornerRadius(dp(16));
        drawable.setStroke(dp(1), color(R.color.sample_card_stroke));
        return drawable;
    }
}
