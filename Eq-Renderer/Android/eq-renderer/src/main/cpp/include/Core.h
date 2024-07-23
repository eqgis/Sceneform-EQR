#include <jni.h>
#include <string>

namespace EQR{
    //核心模块状态值
    static bool CORE_STATUS = false;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_eqgis_eqr_core_CoreNative_jni_1GetVersion(JNIEnv *env, jclass clazz);

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_eqgis_eqr_core_CoreNative_jni_1CheckCoreStatus(JNIEnv *env, jclass clazz);
