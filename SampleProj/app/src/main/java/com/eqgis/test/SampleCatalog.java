package com.eqgis.test;

import com.eqgis.test.samples.ARSceneActivity;
import com.eqgis.test.samples.ARScenePlaneActivity;
import com.eqgis.test.samples.BaseSceneActivity;
import com.eqgis.test.samples.CameraActivity;
import com.eqgis.test.samples.CoordinateConvertActivity;
import com.eqgis.test.samples.EarthActivity;
import com.eqgis.test.samples.InteractiveActivity;
import com.eqgis.test.samples.PlyDataSceneActivity;
import com.eqgis.test.samples.VideoActivity;
import com.eqgis.test.samples.VRScene360Activity;
import com.eqgis.test.samples.VRSceneActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 示例目录数据源
 * <pre>
 *     集中维护 MainActivity 一级入口、常用示例归档和教程主题结构。
 *     新增教程功能时优先在这里登记 SampleLesson，再在 SampleFragmentFactory 中映射 Fragment。
 * </pre>
 * @author tanyx
 */
public class SampleCatalog {
    public static final String EXTRA_TOPIC_ID = "topicId";

    private static final String COVER_MAIN_COMMON = "img/catalog/main/common_samples.png";
    private static final String COVER_MAIN_TUTORIAL = "img/catalog/main/developer_tutorial.png";
    private static final String COVER_TUTORIAL_GEOMETRY = "img/catalog/tutorial/geometry/geometry_mesh.png";
    private static final String COVER_TUTORIAL_ANDROID_VIEW = "img/catalog/tutorial/android_view/android_view_3d.png";
    private static final String COVER_TUTORIAL_MATERIAL_CAMERA = "img/catalog/tutorial/material_camera/material_camera.png";
    private static final String COVER_TUTORIAL_ANIMATION = "img/catalog/tutorial/animation/animation.png";
    private static final String COVER_TUTORIAL_INTERACTION = "img/catalog/tutorial/interaction/interaction.png";
    private static final String COVER_TUTORIAL_VIDEO_CAMERA = "img/catalog/tutorial/video_camera/video_camera.png";
    private static final String COVER_TUTORIAL_XR = "img/catalog/tutorial/xr/xr.png";
    private static final String COVER_TUTORIAL_ADVANCED = "img/catalog/tutorial/advanced/advanced.png";
    private static final String PREVIEW_COMMON_BASE_SCENE = "img/catalog/common/base_scene/preview_placeholder.png";
    private static final String PREVIEW_COMMON_PLY_SCENE = "img/catalog/common/ply_scene/preview_placeholder.png";
    private static final String PREVIEW_COMMON_INTERACTIVE = "img/catalog/common/interactive/preview_placeholder.png";
    private static final String PREVIEW_COMMON_VIDEO = "img/catalog/common/video/preview_placeholder.png";
    private static final String PREVIEW_COMMON_AR_SCENE = "img/catalog/common/ar_scene/preview_placeholder.png";
    private static final String PREVIEW_COMMON_AR_PLANE = "img/catalog/common/ar_plane/preview_placeholder.png";
    private static final String PREVIEW_COMMON_CAMERA = "img/catalog/common/camera/preview_placeholder.png";
    private static final String PREVIEW_COMMON_VR = "img/catalog/common/vr/preview_placeholder.png";
    private static final String PREVIEW_COMMON_VR_360 = "img/catalog/common/vr_360/preview_placeholder.png";
    private static final String PREVIEW_COMMON_EARTH = "img/catalog/common/earth/preview_placeholder.png";
    private static final String PREVIEW_COMMON_COORDINATE = "img/catalog/common/coordinate/preview_placeholder.png";

    public static final String TOPIC_GEOMETRY = "geometry";
    public static final String TOPIC_ANDROID_VIEW = "android_view";
    public static final String TOPIC_MATERIAL_CAMERA = "material_camera";
    public static final String TOPIC_ANIMATION = "animation";
    public static final String TOPIC_INTERACTION = "interaction";
    public static final String TOPIC_VIDEO_CAMERA = "video_camera";
    public static final String TOPIC_XR = "xr";
    public static final String TOPIC_ADVANCED = "advanced";

    /**
     * 获取 MainActivity 一级入口
     * @return 示例系列与教程主题入口列表
     */
    public static List<SampleItem> mainEntries() {
        List<SampleItem> items = new ArrayList<>();
        items.add(new SampleItem(
                "常用示例",
                "归档当前常用能力。每个条目展示说明和代码位置，并可跳转到原有 Activity。",
                "系列",
                0,
                COVER_MAIN_COMMON,
                CommonSamplesActivity.class,
                null
        ));
        items.add(new SampleItem(
                "开发教程",
                "按学习主题组织零基础 3D 渲染教程。进入后可选择基础几何、Android View、材质、动画、交互、XR 等主题。",
                "教程",
                0,
                COVER_MAIN_TUTORIAL,
                DeveloperTutorialActivity.class,
                null
        ));
        return items;
    }

    /**
     * 获取开发教程主题入口
     * @return 开发教程主题入口列表
     */
    public static List<SampleItem> tutorialEntries() {
        List<SampleItem> items = new ArrayList<>();
        for (SampleTopic topic : tutorialTopics()) {
            items.add(new SampleItem(
                    topic.getTitle(),
                    topic.getDescription(),
                    "教程",
                    0,
                    coverAssetPathForTopic(topic.getId()),
                    TutorialTopicActivity.class,
                    topic.getId()
            ));
        }
        return items;
    }

    /**
     * 获取常用示例归档列表
     * @return 常用示例说明与原 Activity 映射列表
     */
    public static List<SampleLesson> commonLessons() {
        return Arrays.asList(
                new SampleLesson("common_base_scene", "普通 3D 场景", "加载 GLTF 模型、环境光和基础相机。", "com.eqgis.test.samples.BaseSceneActivity", PREVIEW_COMMON_BASE_SCENE, BaseSceneActivity.class),
                new SampleLesson("common_ply_scene", "PLY/3DGS 数据场景", "加载 PLY 点云和 3DGS 数据，观察数据渲染效果。", "com.eqgis.test.samples.PlyDataSceneActivity", PREVIEW_COMMON_PLY_SCENE, PlyDataSceneActivity.class),
                new SampleLesson("common_interactive", "手势交互", "演示模型点击、ViewRenderable 标记和触摸交互。", "com.eqgis.test.samples.InteractiveActivity", PREVIEW_COMMON_INTERACTIVE, InteractiveActivity.class),
                new SampleLesson("common_video", "视频渲染", "演示视频作为背景纹理渲染到场景。", "com.eqgis.test.samples.VideoActivity", PREVIEW_COMMON_VIDEO, VideoActivity.class),
                new SampleLesson("common_ar_scene", "AR 三维场景", "在 AR 场景中加载和展示三维模型。", "com.eqgis.test.samples.ARSceneActivity", PREVIEW_COMMON_AR_SCENE, true, ARSceneActivity.class),
                new SampleLesson("common_ar_plane", "AR 平面检测", "识别真实平面并点击放置 GLTF 模型。", "com.eqgis.test.samples.ARScenePlaneActivity", PREVIEW_COMMON_AR_PLANE, true, ARScenePlaneActivity.class),
                new SampleLesson("common_camera", "相机示例", "相机流、拍照和图像处理相关能力。", "com.eqgis.test.samples.CameraActivity", PREVIEW_COMMON_CAMERA, CameraActivity.class),
                new SampleLesson("common_vr", "VR 场景", "基于 VrSceneView 的虚拟现实场景。", "com.eqgis.test.samples.VRSceneActivity", PREVIEW_COMMON_VR, VRSceneActivity.class),
                new SampleLesson("common_vr_360", "360 全景 VR", "360 全景视频和沉浸式观看。", "com.eqgis.test.samples.VRScene360Activity", PREVIEW_COMMON_VR_360, VRScene360Activity.class),
                new SampleLesson("common_earth", "地球示例", "球体纹理、相机控制和交互展示。", "com.eqgis.test.samples.EarthActivity", PREVIEW_COMMON_EARTH, EarthActivity.class),
                new SampleLesson("common_coordinate", "坐标转换", "屏幕坐标、世界坐标和模型坐标转换。", "com.eqgis.test.samples.CoordinateConvertActivity", PREVIEW_COMMON_COORDINATE, CoordinateConvertActivity.class)
        );
    }

    /**
     * 获取零基础教程主题列表
     * @return 教程主题列表
     */
    public static List<SampleTopic> tutorialTopics() {
        return Arrays.asList(
                new SampleTopic(TOPIC_GEOMETRY, "基础几何与 Mesh", "从三角形、平面、立方体 到 GLTF/PLY格式 模型加载。", geometryLessons()),
                new SampleTopic(TOPIC_ANDROID_VIEW, "Android View 渲染到 3D", "将 TextView、ImageView 和自定义布局渲染到三维空间。", placeholderLessons("android_view", "Android View 渲染到 3D")),
                new SampleTopic(TOPIC_MATERIAL_CAMERA, "材质、光照与相机", "学习材质、IBL、天空盒、FOV、裁剪面和阴影。", placeholderLessons("material_camera", "材质、光照与相机")),
                new SampleTopic(TOPIC_ANIMATION, "动画篇", "节点旋转、位移、曲线路径和 GLTF 模型动画。", placeholderLessons("animation", "动画篇")),
                new SampleTopic(TOPIC_INTERACTION, "交互篇", "点击、触摸、AABB、碰撞体和射线拾取。", placeholderLessons("interaction", "交互篇")),
                new SampleTopic(TOPIC_VIDEO_CAMERA, "视频、相机与外部纹理", "视频贴图、相机流、截图和 PixelCopy。", placeholderLessons("video_camera", "视频、相机与外部纹理")),
                new SampleTopic(TOPIC_XR, "XR 篇", "ARCore、AREngine、3DoF AR、VR 和跟踪状态。", placeholderLessons("xr", "XR 篇")),
                new SampleTopic(TOPIC_ADVANCED, "进阶专题", "生命周期、性能优化、Native 加载和 Filament 材质工作流。", placeholderLessons("advanced", "进阶专题"))
        );
    }

    /**
     * 查找教程主题
     * @param topicId 主题 id
     * @return 匹配到的主题，未匹配时返回基础几何主题
     */
    public static SampleTopic findTopic(String topicId) {
        for (SampleTopic topic : tutorialTopics()) {
            if (topic.getId().equals(topicId)) {
                return topic;
            }
        }
        return tutorialTopics().get(0);
    }

    /**
     * 获取基础几何与 Mesh 的首批教程功能
     * @return 基础几何教程功能列表
     */
    private static List<SampleLesson> geometryLessons() {
        return Arrays.asList(
                new SampleLesson("lesson_triangle", "绘制三角形", "手动构建顶点、索引和 RenderableDefinition。", "com.eqgis.test.fragments.tutorial.TriangleLessonFragment"),
                new SampleLesson("lesson_plane", "创建平面", "理解平面顶点、UV、法线和材质。", "com.eqgis.test.fragments.tutorial.PlaneLessonFragment"),
                new SampleLesson("lesson_cube", "绘制 Cube", "使用几何工具生成立方体并调整位置。", "com.eqgis.test.fragments.tutorial.CubeLessonFragment"),
                new SampleLesson("lesson_gltf", "加载 GLTF/GLB 模型", "使用 ModelRenderable 加载 Filament GLTF 模型。", "com.eqgis.test.fragments.tutorial.GltfLessonFragment"),
                new SampleLesson("lesson_ply", "加载 PLY 模型", "加载 PLY 数据并观察点云/Mesh 渲染。", "com.eqgis.test.fragments.tutorial.PlyLessonFragment")
        );
    }

    /**
     * 创建暂未实现主题的占位功能
     * @param prefix 主题 id 前缀
     * @param title 主题标题
     * @return 占位功能列表
     */
    private static List<SampleLesson> placeholderLessons(String prefix, String title) {
        return Arrays.asList(
                new SampleLesson(prefix + "_overview", "主题导览", "本主题教程已规划，后续按一个 Fragment 一个功能逐步补齐。", title + "：待新增 Fragment")
        );
    }

    /**
     * 获取教程主题封面 assets 路径
     * @param topicId 主题 id
     * @return assets 下的封面相对路径
     */
    private static String coverAssetPathForTopic(String topicId) {
        if (TOPIC_GEOMETRY.equals(topicId)) {
            return COVER_TUTORIAL_GEOMETRY;
        } else if (TOPIC_ANDROID_VIEW.equals(topicId)) {
            return COVER_TUTORIAL_ANDROID_VIEW;
        } else if (TOPIC_MATERIAL_CAMERA.equals(topicId)) {
            return COVER_TUTORIAL_MATERIAL_CAMERA;
        } else if (TOPIC_ANIMATION.equals(topicId)) {
            return COVER_TUTORIAL_ANIMATION;
        } else if (TOPIC_INTERACTION.equals(topicId)) {
            return COVER_TUTORIAL_INTERACTION;
        } else if (TOPIC_VIDEO_CAMERA.equals(topicId)) {
            return COVER_TUTORIAL_VIDEO_CAMERA;
        } else if (TOPIC_XR.equals(topicId)) {
            return COVER_TUTORIAL_XR;
        } else if (TOPIC_ADVANCED.equals(topicId)) {
            return COVER_TUTORIAL_ADVANCED;
        }
        return COVER_MAIN_TUTORIAL;
    }
}
