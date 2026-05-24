package com.eqgis.test.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * 通用说明 Fragment
 * <pre>
 *     用于暂未实现的教程主题或纯说明页面，展示标题、说明和可选代码位置。
 *     该 Fragment 不创建 SceneLayout，因此不会持有渲染资源。
 * </pre>
 * @author tanyx
 */
public class BaseInfoFragment extends Fragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_CODE_LOCATION = "codeLocation";

    /**
     * 创建说明 Fragment
     * @param title 标题
     * @param description 说明
     * @return 说明 Fragment
     */
    public static BaseInfoFragment newInstance(String title, String description) {
        return newInstance(title, description, "");
    }

    /**
     * 创建带代码位置的说明 Fragment
     * @param title 标题
     * @param description 说明
     * @param codeLocation 代码位置
     * @return 说明 Fragment
     */
    public static BaseInfoFragment newInstance(String title, String description, String codeLocation) {
        BaseInfoFragment fragment = new BaseInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_CODE_LOCATION, codeLocation);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));

        TextView title = new TextView(requireContext());
        title.setText(getArguments() == null ? "功能说明" : getArguments().getString(ARG_TITLE));
        title.setTextSize(22);
        title.setTextColor(0xff212121);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView description = new TextView(requireContext());
        description.setText(getArguments() == null ? "" : getArguments().getString(ARG_DESCRIPTION));
        description.setTextSize(16);
        description.setTextColor(0xff555555);
        description.setGravity(Gravity.CENTER);
        description.setLineSpacing(dp(2), 1.0f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(16);
        root.addView(description, params);

        String codeLocation = getArguments() == null ? "" : getArguments().getString(ARG_CODE_LOCATION, "");
        if (codeLocation != null && !codeLocation.isEmpty()) {
            TextView code = new TextView(requireContext());
            code.setText("代码位置：" + codeLocation);
            code.setTextSize(14);
            code.setTextColor(0xff666666);
            code.setGravity(Gravity.CENTER);
            code.setPadding(dp(12), dp(8), dp(12), dp(8));
            code.setBackgroundColor(0xffffffff);
            LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            codeParams.topMargin = dp(16);
            root.addView(code, codeParams);
        }

        return root;
    }

    /**
     * dp 转 px
     * @param value dp 值
     * @return px 值
     */
    protected int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
