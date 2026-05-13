package com.eqgis.test.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.test.scene.ISampleScene;

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
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xfff5f5f5);

        TextView title = new TextView(requireContext());
        title.setText(getLessonTitle());
        title.setTextColor(0xff212121);
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(dp(16), dp(12), dp(16), dp(4));
        root.addView(title, new LinearLayout.LayoutParams(
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
        root.addView(actionContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        sceneLayout = createSceneLayout();
        contentContainer.addView(sceneLayout, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        sceneLayout.init(requireContext());
        onSceneReady(sceneLayout);
        onActionsReady(actionContainer);
        return root;
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
