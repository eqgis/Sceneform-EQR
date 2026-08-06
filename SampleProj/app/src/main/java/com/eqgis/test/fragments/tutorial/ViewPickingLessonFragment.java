package com.eqgis.test.fragments.tutorial;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.eqgis.test.R;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;

/**
 * Android View 射线交互教程
 * <pre>
 *     将 Button、Switch、CheckBox 和可滚动 XML Layout 转换为 ViewRenderable，
 *     场景射线命中 ViewNode 后把触摸坐标继续分发给内部 Android View 与 ScrollView。
 * </pre>
 * @author tanyx
 */
public class ViewPickingLessonFragment extends BaseAndroidViewLessonFragment {
    private static final int SCROLL_LAYOUT_WIDTH_DP = 248;
    private static final int SCROLL_LAYOUT_HEIGHT_DP = 300;
    private TextView statusText;
    private Button button;
    private Switch switchView;
    private CheckBox checkBox;
    private ScrollView scrollLayout;
    private Button scrollLayoutButton;
    private int count = 0;

    @Override
    protected String getLessonTitle() {
        return "Android View 交互";
    }

    @Override
    protected String getLessonDescription() {
        return "Ray-picked ViewNode 会把触摸事件映射到 Android 2D View，可实现与常用控件的交互操作。";
    }

    /**
     * 创建可通过射线操作的 Android View 节点
     */
    @SuppressLint({"UseSwitchCompatOrMaterialCode", "DefaultLocale"})
    @Override
    protected void addViewExamples() {
        button = new Button(requireContext());
        button.setText(String.format("点击次数 %d", count));
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setPadding(dp(14), dp(6), dp(14), dp(6));
        button.setOnClickListener(view -> {
            updateStatus("Button 已点击");
            count ++ ;
            button.setText(String.format("点击次数 %d", count));
        });
        addViewRenderable(
                button,
                new Vector3(0.5f, 0.72f, -3.0f),
                Quaternion.identity(),
                0.78f);

        switchView = new Switch(requireContext());
        switchView.setText("开启效果");
        switchView.setTextColor(Color.rgb(29, 29, 31));
        switchView.setTextSize(15);
        switchView.setPadding(dp(12), dp(8), dp(12), dp(8));
        switchView.setBackground(createRoundedBackground(
                Color.WHITE,
                Color.rgb(210, 210, 215)));
        switchView.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateStatus("Switch：" + (isChecked ? "开启" : "关闭")));
        addViewRenderable(
                switchView,
                new Vector3(0.5f, -0.02f, -3.0f),
                Quaternion.identity(),
                0.78f);

        checkBox = new CheckBox(requireContext());
        checkBox.setText("显示标记");
        checkBox.setTextColor(Color.rgb(29, 29, 31));
        checkBox.setTextSize(15);
        checkBox.setPadding(dp(10), dp(8), dp(12), dp(8));
        checkBox.setBackground(createRoundedBackground(
                Color.WHITE,
                Color.rgb(210, 210, 215)));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateStatus("CheckBox：" + (isChecked ? "选中" : "未选中")));
        addViewRenderable(
                checkBox,
                new Vector3(0.5f, -0.72f, -3.0f),
                Quaternion.identity(),
                0.78f);

        addScrollableLayout();
    }

    /**
     * 加载内容超过固定高度的 XML Layout，并将 ScrollView 放入三维场景
     */
    private void addScrollableLayout() {
        scrollLayout = (ScrollView) LayoutInflater.from(requireContext()).inflate(
                R.layout.view_renderable_scroll_layout,
                null,
                false);
        //desc- 使用 null 父容器加载 XML 时根布局没有 LayoutParams，必须显式固定视口，否则 ViewRenderable 会按 WRAP_CONTENT 展示全部内容。
        scrollLayout.setLayoutParams(new LinearLayout.LayoutParams(
                dp(SCROLL_LAYOUT_WIDTH_DP),
                dp(SCROLL_LAYOUT_HEIGHT_DP)));
        scrollLayoutButton = scrollLayout.findViewById(R.id.button_scroll_layout_action);
        scrollLayoutButton.setOnClickListener(view -> updateStatus("Layout 内部 Button 已点击"));
        scrollLayout.setOnScrollChangeListener((view, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY != oldScrollY) {
                updateStatus("ScrollView 滚动位置：" + scrollY + " px");
            }
        });
        addViewRenderable(
                scrollLayout,
                new Vector3(-0.92f, -0.82f, -3.15f),
                new Quaternion(Vector3.up(), 8),
                0.70f);
    }

    /**
     * 创建 View 点击状态面板
     * @param actionContainer 悬浮面板容器
     */
    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        actionContainer.setOrientation(LinearLayout.VERTICAL);
        statusText = new TextView(requireContext());
        statusText.setText("请点击场景中的 Android View");
        statusText.setTextColor(0xff333333);
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        statusText.setPadding(0, dp(4), 0, dp(8));
        actionContainer.addView(statusText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void updateStatus(String text) {
        if (statusText != null && isSceneActive()) {
            statusText.setText(text);
        }
    }

    /**
     * 销毁 ViewRenderable 前解除 Android View 回调
     */
    @Override
    protected void onBeforeDestroyScene() {
        if (button != null) {
            button.setOnClickListener(null);
        }
        if (switchView != null) {
            switchView.setOnCheckedChangeListener(null);
        }
        if (checkBox != null) {
            checkBox.setOnCheckedChangeListener(null);
        }
        if (scrollLayoutButton != null) {
            scrollLayoutButton.setOnClickListener(null);
        }
        if (scrollLayout != null) {
            scrollLayout.setOnScrollChangeListener(null);
        }
        button = null;
        switchView = null;
        checkBox = null;
        scrollLayoutButton = null;
        scrollLayout = null;
        statusText = null;
        super.onBeforeDestroyScene();
    }
}
