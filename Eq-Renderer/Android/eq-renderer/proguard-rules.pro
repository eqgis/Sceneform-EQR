# 设置混淆的压缩比率 0 ~ 7
-optimizationpasses 5
# 混淆时不使用大小写混合，混淆后的类名为小写
-dontusemixedcaseclassnames
# 指定不去忽略非公共库的类
#-dontskipnonpubliclibraryclasses
# 指定不去忽略非公共库的成员
#-dontskipnonpubliclibraryclassmembers
# 混淆时不做预校验
-dontpreverify
# 混淆时不记录日志
-verbose
# 忽略警告
#-ignorewarning
# 代码优化
-dontshrink
# 不优化输入的类文件
-dontoptimize
# 保留注解不混淆
-keepattributes *Annotation*,InnerClasses
# 避免混淆泛型
-keepattributes Signature
# 保留代码行号，方便异常信息的追踪
-keepattributes SourceFile,LineNumberTable
# 混淆采用的算法
-optimizations !code/simplification/cast,!field/*,!class/merging/*

## dump.txt文件列出apk包内所有class的内部结构
#-dump class_files.txt
## seeds.txt文件列出未混淆的类和成员
#-printseeds seeds.txt
## usage.txt文件列出从apk中删除的代码
#-printusage unused.txt
## mapping.txt文件列出混淆前后的映射
#-printmapping mapping.txt

#-keep class com.supermap.eqtool.tmp.*{
#    public void *;
#    public boolean *;
#    public static *;
#}
-keep class com.eqgis.eqr.**{
    public *;
    protected *;
}
-keep class com.eqgis.eqr.runtime.**{
    public *;
    protected *;
}

-keep public class org.**{
    public *;
    protected *;
}

-keep public class com.**{
    public *;
    protected *;
    public static *;
}

-keepclasseswithmembernames class * {  # 保持 native 方法不被混淆
    native <methods>;
}

#避免gltfio
-keep class com.google.android.filament.gltfio.*{
    public *;
    protected *;
}

#避免filament-utils
-keep class com.google.android.filament.textured.*{
    public *;
    protected *;
}
-keep class com.google.android.filament.utils.*{
    public *;
    protected *;
}

#避免filament-android
-keep class com.google.android.filament.android.*{
    public *;
    protected *;
}
-keep class com.google.android.filament.proguard.*{
    public *;
    protected *;
}
-keep class com.google.android.filament.*{
    public *;
    protected *;
}

#避免ARCore
-keep class com.google.ar.core.*{
    public *;
    protected *;
}
-keep class com.google.ar.core.annotations.*{
    public *;
    protected *;
}
-keep class com.google.ar.core.dependencies.*{
    public *;
    protected *;
}
-keep class com.google.ar.core.exceptions.*{
    public *;
    protected *;
}
-keep class com.google.vr.dynamite.client.*{
    public *;
    protected *;
}

#避免AREngine
-keep class com.huawei.arengine.remoteLoader.*{
    public *;
    protected *;
}
-keep class com.huawei.arengine.service.*{
    public *;
    protected *;
}
-keep class com.huawei.hiar.annotations.*{
    public *;
    protected *;
}
-keep class com.huawei.hiar.common.*{
    public *;
    protected *;
}
-keep class com.huawei.hiar.exceptions.*{
    public *;
    protected *;
}
-keep class com.huawei.hiar.listener.*{
    public *;
    protected *;
}
-keep class com.huawei.hiar.*{
    public *;
    protected *;
}
-keep class com.huawei.remoteLoader.client.*{
    public *;
    protected *;
}


###filament相关
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep the annotations that proguard needs to process.
-keep class com.google.android.filament.proguard.UsedBy*

# Just because native code accesses members of a class, does not mean that the
# class itself needs to be annotated - only annotate classes that are
# referenced themselves in native code.
-keep @com.google.android.filament.proguard.UsedBy* class * {
  <init>();
}
-keepclassmembers class * {
  @com.google.android.filament.proguard.UsedBy* *;
}


###AREngine相关
-keepparameternames
#-renamesourcefileattribute SourceFile

#public class
-keep public class com.huawei.hiar.* {
      *;
}
-keep public class com.huawei.hiar.exceptions.* {
      *;
}

#Inner classes and signature
-keepattributes InnerClasses,Signature

-keep public class com.huawei.hiar.annotations.* {
      *;
}
-keep public class com.huawei.remoteLoader.client.* {
      *;
}

-keep public class com.huawei.hiar.common.* {
      *;
}

-keep public class com.huawei.hiar.listener.* {
      *;
}

#ARServiceProxy is used by service, and will keep it
-keep class com.huawei.hiar.ARServiceProxy{*;}
-keep class com.huawei.hiar.ARQuaternion{*;}
#class with native method
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
#used by native
-keep class com.huawei.hiar.annotations.UsedByNative
-keep @com.huawei.hiar.annotations.UsedByNative class *
-keepclassmembers class *{
    @com.huawei.hiar.annotations.UsedByNative *;
}

#used by reflection
-keep class com.huawei.hiar.annotations.UsedByReflection
-keep @com.huawei.hiar.annotations.UsedByReflection class *
-keepclassmembers class * {
    @com.huawei.hiar.annotations.UsedByReflection *;
}

#.aidl files
-keep class com.huawei.arengine.service.IAREngine {*;}
-keep class com.huawei.arengine.remoteLoader.IDynamicLoader {*;}
-keep class com.huawei.arengine.remoteLoader.ILoaderProducer {*;}
-keep class com.huawei.arengine.remoteLoader.IObjectWrapper {*;}


###ARCore相关
# Keep ARCore public-facing classes
-keepparameternames

# These are part of the Java <-> native interfaces for ARCore.
-keepclasseswithmembernames,includedescriptorclasses class !com.google.ar.core.services.logging.**, com.google.ar.** {
    native <methods>;
}

-keep public class !com.google.ar.core**.R$*, !com.google.ar.core.services.logging.**, com.google.ar.core.** {*;}

# If you need to build a library on top of arcore_client, and use this library for your project
# Please un-comment this line below.
# -keepattributes *Annotation*

-keep class com.google.ar.core.annotations.UsedByNative
-keep @com.google.ar.core.annotations.UsedByNative class *
-keepclassmembers class * {
    @com.google.ar.core.annotations.UsedByNative *;
}

-keep class com.google.ar.core.annotations.UsedByReflection
-keep @com.google.ar.core.annotations.UsedByReflection class *
-keepclassmembers class * {
    @com.google.ar.core.annotations.UsedByReflection *;
}
# Keep Dynamite classes

# .aidl file will be proguarded, we should keep all Aidls.
-keep class com.google.vr.dynamite.client.IObjectWrapper { *; }
-keep class com.google.vr.dynamite.client.ILoadedInstanceCreator { *; }
-keep class com.google.vr.dynamite.client.INativeLibraryLoader { *; }

# Keep annotation files and the file got annotated.
-keep class com.google.vr.dynamite.client.UsedByNative
-keep @com.google.vr.dynamite.client.UsedByNative class *
-keepclassmembers class * {
    @com.google.vr.dynamite.client.UsedByNative *;
}

-keep class com.google.vr.dynamite.client.UsedByReflection
-keep @com.google.vr.dynamite.client.UsedByReflection class *
-keepclassmembers class * {
    @com.google.vr.dynamite.client.UsedByReflection *;
}

