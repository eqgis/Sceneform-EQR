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
import com.eqgis.test.fragments.tutorial.AsyncResourceCacheLessonFragment;
import com.eqgis.test.fragments.tutorial.ArEngineLessonFragment;
import com.eqgis.test.fragments.tutorial.ArPlaneDetectionLessonFragment;
import com.eqgis.test.fragments.tutorial.ArThreeDofLessonFragment;
import com.eqgis.test.fragments.tutorial.CameraClipLessonFragment;
import com.eqgis.test.fragments.tutorial.CameraFovLessonFragment;
import com.eqgis.test.fragments.tutorial.CameraGestureLessonFragment;
import com.eqgis.test.fragments.tutorial.CameraStreamLessonFragment;
import com.eqgis.test.fragments.tutorial.CollisionClickLessonFragment;
import com.eqgis.test.fragments.tutorial.CubeLessonFragment;
import com.eqgis.test.fragments.tutorial.CustomFilamentMaterialLessonFragment;
import com.eqgis.test.fragments.tutorial.DynamicMeshLineLessonFragment;
import com.eqgis.test.fragments.tutorial.GltfLessonFragment;
import com.eqgis.test.fragments.tutorial.IblLessonFragment;
import com.eqgis.test.fragments.tutorial.LightTypesLessonFragment;
import com.eqgis.test.fragments.tutorial.MaterialPropertiesLessonFragment;
import com.eqgis.test.fragments.tutorial.ModelAnimationLessonFragment;
import com.eqgis.test.fragments.tutorial.NodeGestureLessonFragment;
import com.eqgis.test.fragments.tutorial.NodePickingLessonFragment;
import com.eqgis.test.fragments.tutorial.OrbSlam3LessonFragment;
import com.eqgis.test.fragments.tutorial.PanoramaVideoLessonFragment;
import com.eqgis.test.fragments.tutorial.PathAnimationLessonFragment;
import com.eqgis.test.fragments.tutorial.PlaneLessonFragment;
import com.eqgis.test.fragments.tutorial.PlyLessonFragment;
import com.eqgis.test.fragments.tutorial.PrimitiveLessonFragment;
import com.eqgis.test.fragments.tutorial.RotationAnimationLessonFragment;
import com.eqgis.test.fragments.tutorial.RenderPerformanceLessonFragment;
import com.eqgis.test.fragments.tutorial.ResourceLifecycleLessonFragment;
import com.eqgis.test.fragments.tutorial.SceneCaptureExportLessonFragment;
import com.eqgis.test.fragments.tutorial.ScreenRayMeasureLessonFragment;
import com.eqgis.test.fragments.tutorial.ShadowLessonFragment;
import com.eqgis.test.fragments.tutorial.SkyboxLessonFragment;
import com.eqgis.test.fragments.tutorial.TranslationAnimationLessonFragment;
import com.eqgis.test.fragments.tutorial.TriangleLessonFragment;
import com.eqgis.test.fragments.tutorial.ViewPickingLessonFragment;
import com.eqgis.test.fragments.tutorial.VideoBackgroundLessonFragment;
import com.eqgis.test.fragments.tutorial.VideoTextureLessonFragment;
import com.eqgis.test.fragments.tutorial.VrThreeDofLessonFragment;

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
            case "lesson_primitives":
                return new PrimitiveLessonFragment();
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
            case "interaction_camera_gesture":
                return new CameraGestureLessonFragment();
            case "interaction_node_gesture":
                return new NodeGestureLessonFragment();
            case "interaction_collision_click":
                return new CollisionClickLessonFragment();
            case "interaction_view_picking":
                return new ViewPickingLessonFragment();
            case "interaction_node_picking":
                return new NodePickingLessonFragment();
            case "video_camera_background":
                return new VideoBackgroundLessonFragment();
            case "video_camera_texture":
                return new VideoTextureLessonFragment();
            case "video_camera_panorama":
                return new PanoramaVideoLessonFragment();
            case "video_camera_stream":
                return new CameraStreamLessonFragment();
            case "xr_ar_engine":
                return new ArEngineLessonFragment();
            case "xr_plane_detection":
                return new ArPlaneDetectionLessonFragment();
            case "xr_ar_3dof":
                return new ArThreeDofLessonFragment();
            case "xr_vr_3dof":
                return new VrThreeDofLessonFragment();
            case "xr_orb_slam3":
                return new OrbSlam3LessonFragment();
            case "advanced_lifecycle":
                return new ResourceLifecycleLessonFragment();
            case "advanced_async_cache":
                return new AsyncResourceCacheLessonFragment();
            case "advanced_performance":
                return new RenderPerformanceLessonFragment();
            case "advanced_custom_material":
                return new CustomFilamentMaterialLessonFragment();
            case "advanced_dynamic_mesh":
                return new DynamicMeshLineLessonFragment();
            case "advanced_capture_export":
                return new SceneCaptureExportLessonFragment();
            case "advanced_screen_ray_measure":
                return new ScreenRayMeasureLessonFragment();
            default:
                return BaseInfoFragment.newInstance(lesson.getTitle(), lesson.getDescription(), lesson.getCodeLocation());
        }
    }
}
