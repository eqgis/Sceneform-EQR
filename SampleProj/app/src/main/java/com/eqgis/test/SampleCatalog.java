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
                new SampleTopic(TOPIC_ANDROID_VIEW, "Android View 渲染到 3D", "将 TextView、ImageView、WebView、常用控件和 XML Layout 渲染到三维空间。", androidViewLessons()),
                new SampleTopic(TOPIC_MATERIAL_CAMERA, "材质、光照与相机", "学习材质、IBL、天空盒、FOV、裁剪面和阴影。", materialCameraLessons()),
                new SampleTopic(TOPIC_ANIMATION, "动画篇", "节点旋转、位移、曲线路径和 GLTF 模型动画。", animationLessons()),
                new SampleTopic(TOPIC_INTERACTION, "交互篇", "学习相机与节点手势、碰撞点击、ViewNode 和三维节点射线拾取。", interactionLessons()),
                new SampleTopic(TOPIC_VIDEO_CAMERA, "视频、相机与外部纹理", "学习二维视频背景、三维视频贴图、全景视频与实时相机流。", videoCameraLessons()),
                new SampleTopic(TOPIC_XR, "XR 篇", "ARCore、AREngine、平面识别、3DoF AR/VR 与 ORB-SLAM3 集成说明。", xrLessons()),
                new SampleTopic(TOPIC_ADVANCED, "进阶专题", "深入生命周期、异步资源、性能、Filament 材质、动态 Mesh、截图和空间测量。", advancedLessons())
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
     * 获取 Android View 三维渲染教程功能
     * @return Android View 教程功能列表
     */
    private static List<SampleLesson> androidViewLessons() {
        return Arrays.asList(
                new SampleLesson("android_view_text", "TextView", "在场景中放置多个文字标签和状态信息。", "com.eqgis.test.fragments.tutorial.AndroidTextViewLessonFragment"),
                new SampleLesson("android_view_image", "ImageView", "将 Drawable 图标作为 ViewRenderable 展示。", "com.eqgis.test.fragments.tutorial.AndroidImageViewLessonFragment"),
                new SampleLesson("android_view_web", "WebView", "使用本地 HTML 创建三维网页信息卡。", "com.eqgis.test.fragments.tutorial.AndroidWebViewLessonFragment"),
                new SampleLesson("android_view_widgets", "常用 View", "展示 Button、Switch、CheckBox 和 ProgressBar。", "com.eqgis.test.fragments.tutorial.AndroidWidgetLessonFragment"),
                new SampleLesson("android_view_layout", "XML Layout", "把一组 Android View 作为完整布局渲染。", "com.eqgis.test.fragments.tutorial.AndroidLayoutLessonFragment")
        );
    }

    /**
     * 获取材质、光照与相机教程功能
     * @return 材质、光照与相机教程功能列表
     */
    private static List<SampleLesson> materialCameraLessons() {
        return Arrays.asList(
                new SampleLesson("material_camera_material", "材质与 PBR 参数", "比较颜色、透明度、金属度和粗糙度带来的表面差异。", "com.eqgis.test.fragments.tutorial.MaterialPropertiesLessonFragment"),
                new SampleLesson("material_camera_light", "灯光类型", "切换平行光、点光源与聚光灯，观察方向和衰减差异。", "com.eqgis.test.fragments.tutorial.LightTypesLessonFragment"),
                new SampleLesson("material_camera_ibl", "环境光 IBL", "调节间接光强度，并比较不同粗糙度金属材质的反射。", "com.eqgis.test.fragments.tutorial.IblLessonFragment"),
                new SampleLesson("material_camera_skybox", "天空盒", "加载 KTX 天空盒和匹配的 IBL，构建完整环境光照。", "com.eqgis.test.fragments.tutorial.SkyboxLessonFragment"),
                new SampleLesson("material_camera_fov", "相机 FOV", "实时修改垂直视场角，观察空间透视与可见范围变化。", "com.eqgis.test.fragments.tutorial.CameraFovLessonFragment"),
                new SampleLesson("material_camera_clip", "相机裁剪面", "切换近、远裁剪距离，观察不同深度物体的裁剪效果。", "com.eqgis.test.fragments.tutorial.CameraClipLessonFragment"),
                new SampleLesson("material_camera_shadow", "实时阴影", "控制主光源阴影，理解投射物与接收面的配置。", "com.eqgis.test.fragments.tutorial.ShadowLessonFragment")
        );
    }

    /**
     * 获取动画篇教程功能
     * @return 动画篇教程功能列表
     */
    private static List<SampleLesson> animationLessons() {
        return Arrays.asList(
                new SampleLesson("animation_overview", "动画总览", "在同一场景比较模型、旋转、位移与曲线路径动画。", "com.eqgis.test.fragments.tutorial.AnimationOverviewLessonFragment"),
                new SampleLesson("animation_model", "模型动画", "使用 ARAnimationModel 播放和切换 GLB 内置动画片段。", "com.eqgis.test.fragments.tutorial.ModelAnimationLessonFragment"),
                new SampleLesson("animation_rotation", "旋转动画", "使用 ARAnimationRotation 调整旋转轴、角度、方向和周期。", "com.eqgis.test.fragments.tutorial.RotationAnimationLessonFragment"),
                new SampleLesson("animation_translation", "位移动画", "使用 Android 属性动画在两个 Vector3 坐标之间移动节点。", "com.eqgis.test.fragments.tutorial.TranslationAnimationLessonFragment"),
                new SampleLesson("animation_path", "曲线路径动画", "使用 Bézier 估值器和 Android 属性动画沿曲线路径移动节点。", "com.eqgis.test.fragments.tutorial.PathAnimationLessonFragment")
        );
    }

    /**
     * 获取交互篇教程功能
     * @return 交互篇教程功能列表
     */
    private static List<SampleLesson> interactionLessons() {
        return Arrays.asList(
                new SampleLesson("interaction_camera_gesture", "相机手势控制", "单指旋转、双指平移，并通过双指缩放让相机前后移动。", "com.eqgis.test.fragments.tutorial.CameraGestureLessonFragment"),
                new SampleLesson("interaction_node_gesture", "节点手势控制", "点击选中模型后，使用单指旋转、双指平移和双指缩放控制节点。", "com.eqgis.test.fragments.tutorial.NodeGestureLessonFragment"),
                new SampleLesson("interaction_collision_click", "碰撞检测点击", "点击三维物体并读取射线与碰撞体相交的世界坐标。", "com.eqgis.test.fragments.tutorial.CollisionClickLessonFragment"),
                new SampleLesson("interaction_view_picking", "Android View 交互", "通过 Ray-picked ViewNode 操作 Button、Switch 与 CheckBox。", "com.eqgis.test.fragments.tutorial.ViewPickingLessonFragment"),
                new SampleLesson("interaction_node_picking", "3D Node 交互", "通过 Ray hit-test 拾取多个 Node（Renderable），点击后切换材质颜色。", "com.eqgis.test.fragments.tutorial.NodePickingLessonFragment")
        );
    }

    /**
     * 获取视频、相机与外部纹理教程功能
     * @return 视频与相机教程功能列表
     */
    private static List<SampleLesson> videoCameraLessons() {
        return Arrays.asList(
                new SampleLesson("video_camera_background", "2D 视频背景", "使用 ExSceneView 把视频作为二维背景，并通过时间轴控制播放进度。", "com.eqgis.test.fragments.tutorial.VideoBackgroundLessonFragment"),
                new SampleLesson("video_camera_texture", "视频纹理", "通过 ExternalTexture 将循环视频同时贴到 Cube 和 Quad。", "com.eqgis.test.fragments.tutorial.VideoTextureLessonFragment"),
                new SampleLesson("video_camera_panorama", "全景视频", "将 360° 视频贴到内球面，在球体内部观看全景画面。", "com.eqgis.test.fragments.tutorial.PanoramaVideoLessonFragment"),
                new SampleLesson("video_camera_stream", "实时相机流", "使用 CameraSceneView 把 Camera2 预览作为场景背景，并叠加三维物体。", "com.eqgis.test.fragments.tutorial.CameraStreamLessonFragment")
        );
    }

    /**
     * 获取 XR 教程功能
     * @return XR 教程功能列表
     */
    private static List<SampleLesson> xrLessons() {
        return Arrays.asList(
                new SampleLesson("xr_ar_engine", "ARCore / AREngine", "优先使用原生 6DoF AR；设备或服务不支持时明确说明原因并降级为 Camera 3DoF。", "com.eqgis.test.fragments.tutorial.ArEngineLessonFragment"),
                new SampleLesson("xr_plane_detection", "AR 平面识别", "识别水平平面并点击创建锚点；设备不支持时提示该能力无法使用 3DoF 降级。", "com.eqgis.test.fragments.tutorial.ArPlaneDetectionLessonFragment"),
                new SampleLesson("xr_ar_3dof", "AR 3DoF", "使用 Camera2 相机背景与方向传感器提供不依赖 AR 服务的旋转跟踪方案。", "com.eqgis.test.fragments.tutorial.ArThreeDofLessonFragment"),
                new SampleLesson("xr_vr_3dof", "VR 场景 3DoF", "使用 VrSceneView、方向传感器和天空盒构建纯虚拟沉浸场景。", "com.eqgis.test.fragments.tutorial.VrThreeDofLessonFragment"),
                new SampleLesson("xr_orb_slam3", "ORB-SLAM3 集成", "说明 GPL 许可边界，并提供 EqSlamSceneLayout、eq-slam AAR 与源码地址。", "com.eqgis.test.fragments.tutorial.OrbSlam3LessonFragment")
        );
    }

    /**
     * 获取进阶专题教程功能。
     * @return 进阶专题教程功能列表
     */
    private static List<SampleLesson> advancedLessons() {
        return Arrays.asList(
                new SampleLesson("advanced_lifecycle", "生命周期与资源释放", "明确 Node、Renderable、SceneLayout 与 Filament 全局资源的安全释放顺序。", "com.eqgis.test.fragments.tutorial.ResourceLifecycleLessonFragment"),
                new SampleLesson("advanced_async_cache", "异步加载与资源缓存", "比较同步/异步 GLB 加载与 registryId 缓存，并处理晚到回调。", "com.eqgis.test.fragments.tutorial.AsyncResourceCacheLessonFragment"),
                new SampleLesson("advanced_performance", "渲染性能诊断", "实时观察 FPS、实例数量、Java Heap、阴影与 SceneView 内部耗时日志。", "com.eqgis.test.fragments.tutorial.RenderPerformanceLessonFragment"),
                new SampleLesson("advanced_custom_material", "自定义 Filament 材质", "加载编译后的 filamat，并实时调节 color、metallic、roughness 与 reflectance。", "com.eqgis.test.fragments.tutorial.CustomFilamentMaterialLessonFragment"),
                new SampleLesson("advanced_dynamic_mesh", "动态 Mesh 与 Line3D", "运行时生成顶点和索引，并原位刷新 Line3D 管线几何。", "com.eqgis.test.fragments.tutorial.DynamicMeshLineLessonFragment"),
                new SampleLesson("advanced_capture_export", "场景截图与导出", "通过 PixelCopy 截取 SceneView，并使用应用缓存和 FileProvider 导出 JPEG。", "com.eqgis.test.fragments.tutorial.SceneCaptureExportLessonFragment"),
                new SampleLesson("advanced_screen_ray_measure", "屏幕坐标、射线与空间测量", "完成屏幕像素、Camera Ray、碰撞世界坐标与两点距离的转换链。", "com.eqgis.test.fragments.tutorial.ScreenRayMeasureLessonFragment")
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
