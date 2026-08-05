package com.eqgis.test.fragments.tutorial;

import com.eqgis.test.R;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;

/**
 * XML Layout 三维渲染教程
 * <pre>
 *     通过 ViewRenderable.Builder.setView(Context, layoutResId) 加载完整 XML 布局，
 *     同一个布局以不同位置和姿态多次放入三维场景。
 * </pre>
 * @author tanyx
 */
public class AndroidLayoutLessonFragment extends BaseAndroidViewLessonFragment {
    @Override
    protected String getLessonTitle() {
        return "XML Layout 渲染";
    }

    @Override
    protected String getLessonDescription() {
        return "将包含 ImageView、TextView 和 Button 的完整 XML Layout 作为一个 ViewRenderable 子项渲染。";
    }

    /**
     * 创建多个 XML Layout 示例
     */
    @Override
    protected void addViewExamples() {
        addLayoutRenderable(
                R.layout.view_renderable_info_card,
                new Vector3(-0.72f, -0.02f, -2.9f),
                new Quaternion(Vector3.up(), 10),
                0.72f);
        addLayoutRenderable(
                R.layout.view_renderable_info_card,
                new Vector3(0.72f, -0.02f, -2.9f),
                new Quaternion(Vector3.up(), -10),
                0.72f);
    }
}
