# Sceneform - EQR

> EQ-Renderer模块已开源，包含了sceneform中集成AREngine、ORB-SLAM，以及其它对sceneform的扩展。

## 介绍

Sceneform是一个3D框架，具有基于物理的渲染器，针对移动设备进行了优化，使您可以轻松构建增强现实应用程序，而无需OpenGL。

(Sceneform)EQ-Renderer是基于sceneform扩展的一个用于安卓端的三维AR渲染器。 当前接入了ARCore、AREngine、ORB-SLAM，可快速地进行AR开发。


## 运行

### 文件目录

- Eq-Renderer : 基于sceneform（filament）扩展的一个用于安卓端的渲染库
- SampleProj : 示例程序

```
├─Eq-Renderer
│  └─Android
│      └─eq-renderer
└─SampleProj
    └─app
```

### 运行示例

1. 启动AndroidStudio
2.  File > Open，选择`SampleProj`，点击"OK"
3. 点击"Run 'app' "，运行`app`模块

## 相关文档

### Sceneform

> sceneform相关接口使用可以参考以下文档。
>
> 需要注意的是：sceneform1.15以及之前的版本采用sfa、sfb的方式加载模型，sceneform1.16仅支持gltf2.0格式的模型（通过gltfio，具体使用可参考[filament](https://github.com/google/filament)）。

Google [sceneform1.16源码存档](https://github.com/google-ar/sceneform-android-sdk)

Google [sceneform1.15帮助文档](https://developers.google.cn/sceneform/develop/getting-started?hl=zh-cn)

### EQR使用文档

> sceneform-eqr是对sceneform的扩展，相关接口调用与sceneform大同小异。

[使用EQ-Renderer创建AR加载模型](https://www.eqgis.cn/2024/01/30/2024-01-30-%E4%BD%BF%E7%94%A8EQ-Renderer%E5%88%9B%E5%BB%BAAR%E5%8A%A0%E8%BD%BD%E6%A8%A1%E5%9E%8B)

[https://www.eqgis.cn/tags/EQ-R](https://www.eqgis.cn/tags/EQ-R)

[Android AR渲染引擎](https://blog.csdn.net/qq_41140324/category_12571725.html)

[安卓原生AR开发](https://www.cnblogs.com/eqgis/tag/%E5%AE%89%E5%8D%93%E5%8E%9F%E7%94%9FAR%E5%BC%80%E5%8F%91/)


## 仓库地址

>不用编译Eq-Renderer源码，直接使用EQ-R的aar

https://repo.eqgis.cn/com/eqgis/eq-renderer

```
maven {
    allowInsecureProtocol = true
    url "http://repo.eqgis.cn"
}
```
