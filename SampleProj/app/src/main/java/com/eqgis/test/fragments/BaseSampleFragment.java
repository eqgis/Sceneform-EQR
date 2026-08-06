package com.eqgis.test.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.test.R;
import com.eqgis.test.scene.ISampleScene;
import com.google.android.filament.RenderableManager;
import com.google.sceneform.Node;

import java.util.List;

/**
 * 普通渲染示例 Fragment 基类
 * <pre>
 *     统一创建教程页的标题区、说明区、代码定位区、SceneLayout 渲染区和操作区。
 *     子类只需要提供文案，并在 onSceneReady 中挂载自己的节点或示例场景。
 * </pre>
 * @author tanyx
 */
public abstract class BaseSampleFragment extends Fragment {
    protected SceneLayout sceneLayout;
    protected ISampleScene sampleScene;
    private FrameLayout contentContainer;
    private LinearLayout actionContainer;
    private FrameLayout floatingActionRoot;
    private boolean viewDestroyed = true;

    /**
     * 获取功能标题
     * @return 当前功能标题
     */
    protected abstract String getLessonTitle();

    /**
     * 获取功能说明
     * @return 当前功能说明
     */
    protected abstract String getLessonDescription();

    /**
     * 获取示例代码位置
     * @return Java 类全路径
     */
    protected String getCodeLocation() {
        return getClass().getName();
    }

    /**
     * SceneLayout 初始化完成回调
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    protected void onSceneReady(SceneLayout sceneLayout) {
    }

    /**
     * 操作区初始化完成回调
     * @param actionContainer 操作按钮容器
     */
    protected void onActionsReady(LinearLayout actionContainer) {
    }

    /**
     * 是否将操作区作为可折叠面板放置在页面标题栏右侧
     * @return true 表示操作区位于页面右上角，false 表示操作区位于场景下方
     */
    protected boolean shouldOverlayActions() {
        return false;
    }

    /**
     * 创建教程悬浮面板统一使用的下拉框适配器
     * @param items 下拉选项
     * @return 已配置选中项和弹出项布局的适配器
     */
    protected final ArrayAdapter<String> createTutorialSpinnerAdapter(String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_tutorial_spinner,
                android.R.id.text1,
                items);
        adapter.setDropDownViewResource(R.layout.item_tutorial_spinner_dropdown);
        return adapter;
    }

    /**
     * 统一教程下拉框的行高和垂直对齐方式
     * @param spinner 需要配置的下拉框
     */
    protected final void configureTutorialSpinner(Spinner spinner) {
        spinner.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        spinner.setMinimumHeight(dp(44));
        spinner.setPadding(0, 0, 0, 0);
    }

    /**
     * 向操作区添加图元类型下拉框
     * @param actionContainer 操作按钮容器
     */
    protected void addPrimitiveTypeSpinner(LinearLayout actionContainer) {
        TextView label = new TextView(requireContext());
        label.setText("图元类型：");
        label.setTextColor(0xff333333);
        label.setTextSize(14);
        label.setGravity(Gravity.CENTER_VERTICAL);
        actionContainer.addView(label, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        String[] primitiveNames = {"点图元", "线图元", "三角图元"};
        RenderableManager.PrimitiveType[] primitiveTypes = {
                RenderableManager.PrimitiveType.POINTS,
                RenderableManager.PrimitiveType.LINES,
                RenderableManager.PrimitiveType.TRIANGLES
        };
        Spinner primitiveSpinner = new Spinner(requireContext());
        configureTutorialSpinner(primitiveSpinner);
        primitiveSpinner.setAdapter(createTutorialSpinnerAdapter(primitiveNames));
        primitiveSpinner.setSelection(primitiveNames.length - 1, false);
        primitiveSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changePrimitiveType(primitiveTypes[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        actionContainer.addView(primitiveSpinner, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1));
    }

    /**
     * 切换当前场景直属可渲染节点的图元类型
     * @param type {@link RenderableManager.PrimitiveType} 目标图元类型
     */
    protected void changePrimitiveType(RenderableManager.PrimitiveType type) {
        if (!isSceneActive()) {
            return;
        }
        List<Node> children = sceneLayout.getRootNode().getChildren();
        for (int i = 0; i < children.size(); i++) {
            Node node = children.get(i);
            if (node.getRenderableInstance() != null) {
                node.getRenderableInstance().changePrimitive(type);
            }
        }
    }

    /**
     * 销毁 SceneLayout 前的清理回调
     */
    protected void onBeforeDestroyScene() {
    }

    /**
     * 创建当前 Fragment 使用的 SceneLayout
     * @return 当前 Fragment 独立持有的 {@link SceneLayout}
     */
    protected SceneLayout createSceneLayout() {
        return new SceneLayout(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewDestroyed = false;
        boolean overlayActions = shouldOverlayActions();
        FrameLayout pageRoot = new FrameLayout(requireContext());
        pageRoot.setBackgroundColor(0xfff5f5f5);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xfff5f5f5);
        pageRoot.addView(root, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        FrameLayout titleBar = new FrameLayout(requireContext());
        TextView title = new TextView(requireContext());
        title.setText(getLessonTitle());
        title.setTextColor(0xff212121);
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        if (overlayActions) {
            title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            title.setSingleLine(true);
            title.setEllipsize(TextUtils.TruncateAt.END);
            title.setMinHeight(dp(64));
            title.setPadding(dp(16), 0, dp(76), 0);
        } else {
            title.setPadding(dp(16), dp(12), dp(16), dp(4));
        }
        titleBar.addView(title, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(titleBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView description = new TextView(requireContext());
        description.setText(getLessonDescription());
        description.setTextColor(0xff555555);
        description.setTextSize(14);
        description.setPadding(dp(16), 0, dp(16), dp(10));
        description.setLineSpacing(dp(2), 1.0f);
        root.addView(description, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView codeLocation = new TextView(requireContext());
        codeLocation.setText("代码位置：" + getCodeLocation());
        codeLocation.setTextColor(0xff666666);
        codeLocation.setTextSize(13);
        codeLocation.setPadding(dp(16), 0, dp(16), dp(10));
        root.addView(codeLocation, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        contentContainer = new FrameLayout(requireContext());
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1);
        root.addView(contentContainer, contentParams);

        actionContainer = new LinearLayout(requireContext());
        actionContainer.setOrientation(LinearLayout.HORIZONTAL);
        actionContainer.setPadding(dp(12), dp(8), dp(12), dp(8));

        sceneLayout = createSceneLayout();
        contentContainer.addView(sceneLayout, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        if (overlayActions) {
            addFloatingActionContainer(pageRoot, actionContainer);
        } else {
            root.addView(actionContainer, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        sceneLayout.init(requireContext());
        onSceneReady(sceneLayout);
        onActionsReady(actionContainer);
        if (actionContainer.getChildCount() == 0) {
            actionContainer.setVisibility(View.GONE);
            if (floatingActionRoot != null) {
                floatingActionRoot.setVisibility(View.GONE);
            }
        }
        return pageRoot;
    }

    /**
     * 将可折叠操作区添加到页面标题栏右上角
     * <p>面板作为页面根容器的顶层叠加层，不参与标题、说明和 SceneLayout 的尺寸测量。</p>
     * @param container 页面根叠加容器
     * @param actions 操作控件容器
     */
    private void addFloatingActionContainer(FrameLayout container, LinearLayout actions) {
        int availableWidth = getResources().getDisplayMetrics().widthPixels - dp(32);
        int panelWidth = Math.min(dp(320), availableWidth);
        int maxPanelHeight = Math.max(
                dp(240),
                Math.min(dp(360), getResources().getDisplayMetrics().heightPixels / 3));

        floatingActionRoot = new FrameLayout(requireContext());
        floatingActionRoot.setElevation(dp(10));
        ImageButton settingsButton = createSettingsButton();
        FrameLayout.LayoutParams settingsParams = new FrameLayout.LayoutParams(
                dp(48),
                dp(48),
                Gravity.TOP | Gravity.END);
        floatingActionRoot.addView(settingsButton, settingsParams);

        LinearLayout panel = new LinearLayout(requireContext());
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundResource(R.drawable.bg_tutorial_floating_panel);
        panel.setClickable(true);
        panel.setElevation(dp(6));
        panel.setVisibility(View.GONE);
        actions.setPadding(dp(12), dp(4), dp(12), dp(10));

        FrameLayout header = new FrameLayout(requireContext());

        ImageView headerIcon = new ImageView(requireContext());
        headerIcon.setImageResource(R.drawable.ic_tutorial_settings);
        headerIcon.setScaleType(ImageView.ScaleType.CENTER);
        headerIcon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        FrameLayout.LayoutParams headerIconParams = new FrameLayout.LayoutParams(
                dp(22),
                dp(22),
                Gravity.START | Gravity.CENTER_VERTICAL);
        headerIconParams.leftMargin = dp(14);
        header.addView(headerIcon, headerIconParams);

        TextView panelTitle = new TextView(requireContext());
        panelTitle.setText("参数设置");
        panelTitle.setTextColor(0xff1d1d1f);
        panelTitle.setTextSize(15);
        panelTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        panelTitle.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams panelTitleParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        panelTitleParams.setMargins(dp(46), 0, dp(54), 0);
        header.addView(panelTitle, panelTitleParams);

        ImageButton collapseButton = new ImageButton(requireContext());
        collapseButton.setImageResource(R.drawable.ic_tutorial_collapse);
        collapseButton.setContentDescription("收起参数面板");
        collapseButton.setScaleType(ImageView.ScaleType.CENTER);
        collapseButton.setPadding(dp(10), dp(10), dp(10), dp(10));
        collapseButton.setBackgroundResource(R.drawable.bg_tutorial_panel_icon);
        FrameLayout.LayoutParams collapseParams = new FrameLayout.LayoutParams(
                dp(44),
                dp(44),
                Gravity.TOP | Gravity.END);
        collapseParams.topMargin = dp(2);
        collapseParams.rightMargin = dp(4);
        header.addView(collapseButton, collapseParams);
        panel.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)));

        View divider = new View(requireContext());
        divider.setBackgroundColor(0x40d2d2d7);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1));
        dividerParams.setMargins(dp(14), 0, dp(14), 0);
        panel.addView(divider, dividerParams);

        MaxHeightScrollView actionScroller = new MaxHeightScrollView(
                requireContext(), maxPanelHeight - dp(49));
        actionScroller.setFillViewport(false);
        actionScroller.setClipToPadding(false);
        actionScroller.setVerticalScrollBarEnabled(true);
        actionScroller.addView(actions, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        panel.addView(actionScroller, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        floatingActionRoot.addView(panel, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.END));

        settingsButton.setOnClickListener(view -> {
            settingsButton.setVisibility(View.GONE);
            panel.setVisibility(View.VISIBLE);
            //desc- 面板由 GONE 首次变为可见后刷新 Spinner，避免初始选中视图沿用未测量状态而向下偏移。
            panel.post(() -> refreshSpinnerViews(actions));
        });
        collapseButton.setOnClickListener(view -> {
            panel.setVisibility(View.GONE);
            settingsButton.setVisibility(View.VISIBLE);
        });

        FrameLayout.LayoutParams actionParams = new FrameLayout.LayoutParams(
                panelWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.END);
        actionParams.setMargins(dp(12), dp(8), dp(12), dp(8));
        container.addView(floatingActionRoot, actionParams);
        floatingActionRoot.bringToFront();
    }

    /**
     * 刷新容器内所有下拉框的选中视图与测量状态
     * @param view 当前遍历的控件或控件容器
     */
    private void refreshSpinnerViews(View view) {
        if (view instanceof Spinner) {
            Spinner spinner = (Spinner) view;
            if (spinner.getAdapter() instanceof ArrayAdapter) {
                ((ArrayAdapter<?>) spinner.getAdapter()).notifyDataSetChanged();
            }
            spinner.requestLayout();
            spinner.invalidate();
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                refreshSpinnerViews(group.getChildAt(index));
            }
        }
    }

    /**
     * 创建悬浮设置图标按钮
     * @return 已完成样式和无障碍描述配置的按钮
     */
    private ImageButton createSettingsButton() {
        ImageButton button = new ImageButton(requireContext());
        button.setImageResource(R.drawable.ic_tutorial_settings);
        button.setContentDescription("打开参数设置");
        button.setScaleType(ImageView.ScaleType.CENTER);
        button.setPadding(dp(12), dp(12), dp(12), dp(12));
        button.setBackgroundResource(R.drawable.bg_tutorial_settings_button);
        button.setElevation(dp(6));
        return button;
    }

    /** 限制参数面板最大高度，内容超出后在面板内部滚动。 */
    private static final class MaxHeightScrollView extends ScrollView {
        private final int maxHeight;

        MaxHeightScrollView(Context context, int maxHeight) {
            super(context);
            this.maxHeight = maxHeight;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int parentMode = MeasureSpec.getMode(heightMeasureSpec);
            int measuredLimit = maxHeight;
            if (parentMode != MeasureSpec.UNSPECIFIED) {
                measuredLimit = Math.min(maxHeight, MeasureSpec.getSize(heightMeasureSpec));
            }
            super.onMeasure(
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(measuredLimit, MeasureSpec.AT_MOST));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sceneLayout != null) {
            sceneLayout.resume();
        }
    }

    @Override
    public void onPause() {
        if (sceneLayout != null) {
            sceneLayout.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        viewDestroyed = true;
        if (sceneLayout != null) {
            sceneLayout.pause();
        }
        if (contentContainer != null && sceneLayout != null) {
            contentContainer.removeView(sceneLayout);
        }
        //desc- 先让子类解除节点父子关系，再销毁示例场景和 SceneLayout，避免渲染线程访问已释放资源。
        onBeforeDestroyScene();
        if (sampleScene != null) {
            sampleScene.destroy(requireContext());
            sampleScene = null;
        }
        if (sceneLayout != null) {
            sceneLayout.destroy();
            sceneLayout = null;
        }
        floatingActionRoot = null;
        actionContainer = null;
        contentContainer = null;
        super.onDestroyView();
    }

    /**
     * 判断当前 SceneLayout 是否仍可安全访问
     * @return true 表示 Fragment 仍处于可用状态
     */
    protected boolean isSceneActive() {
        return !viewDestroyed && isAdded() && sceneLayout != null && sceneLayout.getRootNode() != null;
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
