# Sceneform-EQR

[English](./README.md)

Sceneform-EQR 是基于 Google Sceneform 扩展、由 Filament 驱动的 Android 原生 3D/XR 渲染库。仓库提供可复用的场景布局，以及模型和几何渲染、动画、交互、Android View 三维渲染、视频/外部纹理、ARCore/AREngine、VR、PLY 点云与 3D Gaussian Splatting 等能力。

采用 Apache-2.0 协议的 `main` 分支支持 ARCore 和 AREngine，但不包含 GPL 协议的 eq-slam 实现。ORB-SLAM3 集成代码与依赖库使用说明位于 [GPLv3 分支](https://github.com/eqgis/Sceneform-EQR/tree/main-GPLv3)。

[![GitHub stars](https://img.shields.io/github/stars/eqgis/Sceneform-EQR?style=flat-square)](https://github.com/eqgis/Sceneform-EQR)
[![Filament](https://img.shields.io/badge/Filament-v1.75.0-8bb903)](https://github.com/google/filament)
[![ARCore](https://img.shields.io/badge/ARCore-v1.45.0-8bb903)](https://github.com/google-ar/arcore-android-sdk)
[![AREngine](https://img.shields.io/badge/AREngine-v4.0.0.5-8bb903)](https://developer.huawei.com/consumer/cn/doc/graphics-References/ar-engine-java-api-0000001064060313)
[![License](https://img.shields.io/badge/main-Apache--2.0-blue)](./LICENSE)

镜像地址：[GitHub](https://github.com/eqgis/Sceneform-EQR) · [GitCode](https://gitcode.com/EQXR/Sceneform-EQR)

## 功能概览

| 分类 | 已包含能力 |
| --- | --- |
| 场景容器 | 普通 3D `SceneLayout`、AR `ARSceneLayout`、镜像显示 `MirrorSceneLayout`，以及相机背景、扩展背景和 VR SceneView |
| 模型与几何 | GLTF/GLB、PLY 点云/Mesh、动态 Mesh、`Line3D`、点/线/三角形基本图元 |
| 材质与光照 | PBR 参数、自定义 Filament `.filamat`、IBL、天空盒、阴影、外部纹理、点尺寸材质、屏幕空间宽线材质 |
| 动画 | GLTF 模型动画片段、节点旋转、位移、路径动画、循环参数，以及模型动画源时长读取 |
| 交互 | Node 点击/触摸、射线拾取、碰撞坐标、Android View 拾取、节点手势、相机旋转/平移/缩放手势 |
| Android UI 三维渲染 | 通过 `ViewRenderable` 渲染 `TextView`、`ImageView`、`WebView`、常用控件、XML Layout 与可滚动布局 |
| 视频与 XR | 二维视频背景、Cube/Quad 视频纹理、360° 视频、Camera2 实时画面、ARCore、华为 AREngine、AR 平面识别、AR/VR 3DoF 降级方案 |
| 工具能力 | 屏幕/世界坐标转换、相机射线、空间测量、场景截图与导出、异步资源缓存、渲染性能诊断 |

## 仓库结构

```text
.
├─ Eq-Renderer/
│  └─ Android/eq-renderer/   # Android 渲染库及 Filament Native 集成
├─ SampleProj/               # 可运行的 Android 示例与教程应用
├─ Tool/                     # Filament 工具、材质源文件、IBL 资源和构建脚本
└─ doc/
   ├─ javadoc/               # 已生成的 API 文档
   └─ img/                   # README 截图与动图
```

渲染库能力放在 `Eq-Renderer`，集成演示放在 `SampleProj`，材质和渲染资产的编译输入放在 `Tool`。

## 环境要求

| 项目 | 当前仓库配置 |
| --- | --- |
| Android | min SDK 24；compile/target SDK 34 |
| 构建工具链 | JDK 17、Gradle 8.5、Android Gradle Plugin 8.1.0 |
| Native 工具链 | 从源码构建渲染库时需要 NDK 27.3.x、CMake 3.22.1 |
| Java/Kotlin 字节码 | Java 8 / JVM 1.8 target |
| ABI | 当前渲染库和示例配置仅启用 `arm64-v8a` |
| 可选 AR 服务 | ARCore 1.45.0 和/或华为 AREngine 4.0.0.5 |

AR 功能仍取决于设备能力、相机权限和对应 AR 服务是否安装。XR 教程会展示检测到的不支持原因，并在允许降级的页面使用 Camera2 3DoF 方案。

## 快速开始

### 从源码运行示例

1. 克隆本仓库。
2. 使用 Android Studio 打开 `SampleProj` 目录。
3. 等待 Gradle 同步本地 `:eq-renderer` 模块。
4. 连接 Android 7.0 以上的 `arm64-v8a` 设备，运行 `app` 配置。

Windows 命令行构建：

```powershell
cd SampleProj
.\gradlew.bat assembleDebug
```

### 使用已发布 AAR

当前源码声明的 Maven 坐标为 `com.eqgis:eq-renderer:1.2.1`。在 `settings.gradle` 中添加 EQGIS 仓库：

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            allowInsecureProtocol = true
            url "http://repo.eqgis.cn"
        }
    }
}
```

然后添加依赖：

```groovy
dependencies {
    implementation "com.eqgis:eq-renderer:1.2.1"

    // 只添加应用实际使用的 XR 运行库。
    implementation "com.google.ar:core:1.45.0"
    // implementation "com.huawei.hms:arenginesdk:4.0.0.5"
}
```

仓库浏览地址：[repo.eqgis.cn/com/eqgis/eq-renderer](https://repo.eqgis.cn/com/eqgis/eq-renderer)

### 创建普通 3D 场景

在 XML 布局中添加 `SceneLayout`：

```xml
<com.eqgis.eqr.layout.SceneLayout
    android:id="@+id/scene_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

初始化并转发宿主生命周期：

```java
private SceneLayout sceneLayout;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.your_scene);
    sceneLayout = findViewById(R.id.scene_layout);
    sceneLayout.init(this)
            .addIndirectLight("enviroments/light/lightroom_ibl.ktx", 50);
}

@Override
protected void onResume() {
    super.onResume();
    sceneLayout.resume();
}

@Override
protected void onPause() {
    sceneLayout.pause();
    super.onPause();
}

@Override
protected void onDestroy() {
    sceneLayout.destroy();
    super.onDestroy();
}
```

模型加载、环境光、相机配置和节点交互可参考 [`BaseSceneActivity.java`](./SampleProj/app/src/main/java/com/eqgis/test/samples/BaseSceneActivity.java)。

### 相机手势

```java
CameraGestureController cameraController =
        new CameraGestureController(sceneLayout.getCamera())
                .attachTo(sceneLayout.getSceneView());

// 宿主 View 销毁前解除绑定。
cameraController.detach();
```

### 创建立方体

```java
MaterialFactory.makeOpaqueWithColor(requireContext(), new Color(1.0f, 0.55f, 0.1f))
    .thenAccept(material -> {
        cubeNode = new Node();
        cubeNode.setRenderable(GeometryUtils.makeCube(
            new Vector3(0.8f, 0.8f, 0.8f),
            Vector3.zero(),
            material));
        cubeNode.setWorldPosition(new Vector3(0, 0, -2.8f));
        cubeNode.setParent(sceneLayout.getRootNode());
    });
```

## 教程目录

示例工程保持“一个功能一个 Fragment”，切换 Tab 时销毁旧 Fragment，使场景、模型、播放器和 AR Session 能正常完成生命周期释放。

| 主题 | 教程内容 |
| --- | --- |
| 基础几何与 Mesh | 五种基本图元、三角形、平面、Cube、GLTF/GLB、PLY |
| Android View 渲染到 3D | TextView、ImageView、WebView、常用控件、XML Layout/ScrollView |
| 材质、光照与相机 | PBR 参数、灯光类型、IBL、天空盒、FOV、裁剪面、阴影 |
| 动画篇 | 动画总览、GLTF 模型动画、旋转、位移、Bézier 曲线路径动画 |
| 交互篇 | 相机手势、节点手势、碰撞坐标、ViewNode 拾取、Node 射线拾取 |
| 视频、相机与外部纹理 | 二维视频背景、Cube/Quad 视频纹理、全景视频、Camera2 实时流 |
| XR 篇 | ARCore/AREngine 检测、AR 平面识别、AR 3DoF、VR 3DoF、ORB-SLAM3 集成说明 |
| 进阶专题 | 生命周期、异步/缓存、性能、自定义 Filament 材质、动态 Mesh/Line3D、截图导出、射线测量 |

常用示例还包含普通 3D 场景、PLY/3DGS、交互、视频、AR、VR、360° VR、纹理地球和坐标转换。

## API 与工具链

- [已生成的 Javadoc](./doc/javadoc/index.html)
- [Sceneform 1.16 源码存档](https://github.com/google-ar/sceneform-android-sdk)
- [Sceneform 1.15 开发文档](https://developers.google.cn/sceneform/develop/getting-started?hl=zh-cn)
- [Filament 文档](https://google.github.io/filament/)

Sceneform 1.15 及以前使用 `.sfa`/`.sfb`；本仓库通过 Filament `gltfio` 加载 GLTF 2.0（`.gltf`/`.glb`）模型。

`Tool` 目录包含 Windows 版本的 `matc`、`cmgen`、`gltf_viewer`、`resgen`，以及渲染库使用的 `.mat` 材质源文件。修改内置材质后，运行 `Tool/genfilamat-mobile.bat`，并同步更新对应的 Android raw 资源。

## 生命周期约束

- 所有 `SceneLayout`/`ARSceneLayout` 宿主都要转发 `resume()`、`pause()` 和 `destroy()`。
- 释放 Renderable 或 Material 前，先移除当前场景持有的节点并停止动画。
- 异步模型/材质回调返回时，先确认 Fragment View 和 Scene 仍然有效，再挂载资源。
- 使用 `ExternalTexture` 时，先从全部 Material/Renderable 解绑纹理，再释放播放器、Surface 或 Texture。
- Tab 切换时不要销毁引擎级 GLTF/材质共享资源；`SceneLayout` 与 `SceneView` 会在最后一个活动场景退出后协调全局回收。
- 在对应宿主生命周期中释放 `MediaPlayer`/Media3、相机 Session、手势控制器和监听器。

生命周期、异步缓存和视频教程 Fragment 中包含了上述处理示例。

## 效果预览

| 普通 3D 场景加载 GLTF | AR 场景加载 GLTF |
| --- | --- |
| <img src="./doc/img/a3.png" width="320" alt="普通三维场景中的 GLTF 模型" /> | <img src="./doc/img/a4.png" width="320" alt="AR 场景中的 GLTF 模型" /> |

| 模型动画 | 手势交互 |
| --- | --- |
| <img src="./doc/img/g3.gif" width="320" alt="GLTF 模型动画" /> | <img src="./doc/img/g4.gif" width="320" alt="模型手势交互" /> |

更多示例包括 [Android View 三维渲染](./doc/img/g1.gif)、[纹理球体](./doc/img/earth.gif) 和 [视频纹理](./doc/img/g6.gif)。

## 博客专栏

- CSDN：[Sceneform-EQR（安卓原生 3D 渲染引擎）](https://blog.csdn.net/qq_41140324/category_12571725.html)
- 博客园：[安卓原生 AR 开发](https://www.cnblogs.com/eqgis/tag/%E5%AE%89%E5%8D%93%E5%8E%9F%E7%94%9FAR%E5%BC%80%E5%8F%91/)
- 自建站（自 2025 年 1 月 1 日起不再更新）：[eqgis.cn/tags/EQ-R](https://www.eqgis.cn/tags/EQ-R)

## 许可证

`main` 分支采用 [Apache License 2.0](./LICENSE)，且不包含 eq-slam。

ORB-SLAM3 采用 GPLv3。任何包含、修改或直接链接 eq-slam/ORB-SLAM3 的分支都必须继续采用 GPLv3。GPL 实现隔离在 [main-GPLv3 分支](https://github.com/eqgis/Sceneform-EQR/tree/main-GPLv3)，相关 AAR 和源码地址可在示例工程的 ORB-SLAM3 教程中查看。

第三方组件和仓库内附带的资产可能保留各自许可证，分发前请检查对应源码目录中的许可文件。
