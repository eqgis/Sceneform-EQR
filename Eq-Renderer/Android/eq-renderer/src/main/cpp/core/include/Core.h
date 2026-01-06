#include <jni.h>
#include <string>
#include <android/log.h>
#include <string>
#include <jni.h>

// 定义宏简化使用
#define LOG_TAG "EQR-NativeModule"
#define LOGE(fmt, ...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##__VA_ARGS__)
#define LOGI(fmt, ...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##__VA_ARGS__)

#define MIN(a,b) ((a) < (b) ? (a) : (b))
#define MAX(a,b) ((a) > (b) ? (a) : (b))


#if defined(EQR_NO_EXCEPTIONS)

#define Handle_ERROR(msg) \
        do {                                           \
            const std::string _msg = (msg);            \
            assert(false && _msg.c_str());             \
        } while (0)

#else

#include <stdexcept>
#define Handle_ERROR(msg)                         \
        throw std::runtime_error(msg)

#endif

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

extern "C"
JNIEXPORT jstring JNICALL
Java_com_eqgis_eqr_core_CoreNative_jni_1GetFilamentVersion(JNIEnv *env, jclass clazz);