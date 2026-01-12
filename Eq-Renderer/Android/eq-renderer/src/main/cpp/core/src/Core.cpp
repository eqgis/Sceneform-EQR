using namespace std;
#include "../include/Core.h"
#include <iostream>
#include "tinyply.h"
tinyply::PlyFile file;


//版本信息
string EQR_CORE_VERSION = "EQ-Renderer_v1.2.0";
string FILAMENT_VERSION = "v1.67.1";

//更新核心模块状态值
void UpdateStatus();

#ifdef __cplusplus
extern "C"{
#endif

/**
 * 获取版本信息
 * @param env
 * @param clazz
 * @return
 */
JNIEXPORT jstring JNICALL
Java_com_eqgis_eqr_core_CoreNative_jni_1GetVersion(JNIEnv *env, jclass clazz) {
    UpdateStatus();
    if (!EQR::CORE_STATUS)return NULL;

    return env->NewStringUTF(EQR_CORE_VERSION.c_str());
}

/**
 * 检查核心库状态
 * @param env
 * @param clazz
 * @return
 */
JNIEXPORT jboolean JNICALL
Java_com_eqgis_eqr_core_CoreNative_jni_1CheckCoreStatus(JNIEnv *env, jclass clazz) {
    UpdateStatus();
    return EQR::CORE_STATUS;
}

#ifdef __cplusplus
}

#endif

//更新核心模块状态值
void UpdateStatus() {
    //开源后不设置权限校验，默认为true
    EQR::CORE_STATUS = true;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_eqgis_eqr_core_CoreNative_jni_1GetFilamentVersion(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(FILAMENT_VERSION.c_str());
}

extern "C" jint registerFilament(JavaVM* vm, void* reserved);
extern "C" jint registerUtils(JavaVM* vm, void* reserved);

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
    JNIEnv* env = nullptr;

    registerFilament(vm,nullptr);
    registerUtils(vm,nullptr);

    return JNI_VERSION_1_6;
}
