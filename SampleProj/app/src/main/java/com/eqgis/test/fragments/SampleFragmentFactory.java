package com.eqgis.test.fragments;

import androidx.fragment.app.Fragment;

import com.eqgis.test.SampleLesson;
import com.eqgis.test.fragments.common.CommonSampleLaunchFragment;
import com.eqgis.test.fragments.tutorial.AndroidImageViewLessonFragment;
import com.eqgis.test.fragments.tutorial.AndroidLayoutLessonFragment;
import com.eqgis.test.fragments.tutorial.AndroidTextViewLessonFragment;
import com.eqgis.test.fragments.tutorial.AndroidWebViewLessonFragment;
import com.eqgis.test.fragments.tutorial.AndroidWidgetLessonFragment;
import com.eqgis.test.fragments.tutorial.AnimationOverviewLessonFragment;
import com.eqgis.test.fragments.tutorial.CameraClipLessonFragment;
import com.eqgis.test.fragments.tutorial.CameraFovLessonFragment;
import com.eqgis.test.fragments.tutorial.CubeLessonFragment;
import com.eqgis.test.fragments.tutorial.GltfLessonFragment;
import com.eqgis.test.fragments.tutorial.IblLessonFragment;
import com.eqgis.test.fragments.tutorial.LightTypesLessonFragment;
import com.eqgis.test.fragments.tutorial.MaterialPropertiesLessonFragment;
import com.eqgis.test.fragments.tutorial.ModelAnimationLessonFragment;
import com.eqgis.test.fragments.tutorial.PathAnimationLessonFragment;
import com.eqgis.test.fragments.tutorial.PlaneLessonFragment;
import com.eqgis.test.fragments.tutorial.PlyLessonFragment;
import com.eqgis.test.fragments.tutorial.RotationAnimationLessonFragment;
import com.eqgis.test.fragments.tutorial.ShadowLessonFragment;
import com.eqgis.test.fragments.tutorial.SkyboxLessonFragment;
import com.eqgis.test.fragments.tutorial.TranslationAnimationLessonFragment;
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
            case "material_camera_material":
                return new MaterialPropertiesLessonFragment();
            case "material_camera_light":
                return new LightTypesLessonFragment();
            case "material_camera_ibl":
                return new IblLessonFragment();
            case "material_camera_skybox":
                return new SkyboxLessonFragment();
            case "material_camera_fov":
                return new CameraFovLessonFragment();
            case "material_camera_clip":
                return new CameraClipLessonFragment();
            case "material_camera_shadow":
                return new ShadowLessonFragment();
            case "animation_overview":
                return new AnimationOverviewLessonFragment();
            case "animation_model":
                return new ModelAnimationLessonFragment();
            case "animation_rotation":
                return new RotationAnimationLessonFragment();
            case "animation_translation":
                return new TranslationAnimationLessonFragment();
            case "animation_path":
                return new PathAnimationLessonFragment();
            default:
                return BaseInfoFragment.newInstance(lesson.getTitle(), lesson.getDescription(), lesson.getCodeLocation());
        }
    }
}
