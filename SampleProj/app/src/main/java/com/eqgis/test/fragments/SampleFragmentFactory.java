package com.eqgis.test.fragments;

import androidx.fragment.app.Fragment;

import com.eqgis.test.SampleLesson;
import com.eqgis.test.fragments.common.CommonSampleLaunchFragment;
import com.eqgis.test.fragments.tutorial.AndroidImageViewLessonFragment;
import com.eqgis.test.fragments.tutorial.AndroidLayoutLessonFragment;
import com.eqgis.test.fragments.tutorial.AndroidTextViewLessonFragment;
import com.eqgis.test.fragments.tutorial.AndroidWebViewLessonFragment;
import com.eqgis.test.fragments.tutorial.AndroidWidgetLessonFragment;
import com.eqgis.test.fragments.tutorial.CubeLessonFragment;
import com.eqgis.test.fragments.tutorial.GltfLessonFragment;
import com.eqgis.test.fragments.tutorial.PlaneLessonFragment;
import com.eqgis.test.fragments.tutorial.PlyLessonFragment;
import com.eqgis.test.fragments.tutorial.TriangleLessonFragment;

/**
 * 示例 Fragment 工厂
 * <pre>
 *     根据 SampleLesson 的 id 创建具体 Fragment。
 *     常用示例统一使用跳转说明 Fragment，教程示例使用独立渲染 Fragment。
 * </pre>
 * @author tanyx
 */
public class SampleFragmentFactory {
    /**
     * 创建功能示例 Fragment
     * @param lesson {@link SampleLesson} 功能示例数据
     * @return 与功能示例匹配的 Fragment
     */
    public static Fragment create(SampleLesson lesson) {
        if (lesson.getId().startsWith("common_")) {
            return CommonSampleLaunchFragment.newInstance(lesson);
        }
        switch (lesson.getId()) {
            case "lesson_triangle":
                return new TriangleLessonFragment();
            case "lesson_plane":
                return new PlaneLessonFragment();
            case "lesson_cube":
                return new CubeLessonFragment();
            case "lesson_gltf":
                return new GltfLessonFragment();
            case "lesson_ply":
                return new PlyLessonFragment();
            case "android_view_text":
                return new AndroidTextViewLessonFragment();
            case "android_view_image":
                return new AndroidImageViewLessonFragment();
            case "android_view_web":
                return new AndroidWebViewLessonFragment();
            case "android_view_widgets":
                return new AndroidWidgetLessonFragment();
            case "android_view_layout":
                return new AndroidLayoutLessonFragment();
            default:
                return BaseInfoFragment.newInstance(lesson.getTitle(), lesson.getDescription(), lesson.getCodeLocation());
        }
    }
}
