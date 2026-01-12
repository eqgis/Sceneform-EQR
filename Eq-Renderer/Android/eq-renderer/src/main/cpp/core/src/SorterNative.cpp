#include <jni.h>
#include <vector>
#include <algorithm>

struct Node {
    float depth;
    int index;
};

extern "C"
JNIEXPORT void JNICALL
Java_com_eqgis_eqr_core_SorterNative_nSortByModelAndCameraMatrix(
        JNIEnv *env,
        jclass /*clazz*/,
        jfloatArray jCenters,
        jfloatArray jModelMat,
        jfloatArray jCameraMat,
        jintArray jIndices) {

    // ===== 获取数组（零拷贝）=====
    jfloat* centers = env->GetFloatArrayElements(jCenters, nullptr);
    jfloat* model   = env->GetFloatArrayElements(jModelMat, nullptr);
    jfloat* camera  = env->GetFloatArrayElements(jCameraMat, nullptr);
    jint*   indices = env->GetIntArrayElements(jIndices, nullptr);

    const int gaussianCount = env->GetArrayLength(jCenters) / 3;

    // ===== 1. 计算 view = inverse(cameraModelMat)（刚体）=====
    float view[16];

    // rotation^T
    view[0]  = camera[0];  view[1]  = camera[4];  view[2]  = camera[8];
    view[4]  = camera[1];  view[5]  = camera[5];  view[6]  = camera[9];
    view[8]  = camera[2];  view[9]  = camera[6];  view[10] = camera[10];

    // translation
    float tx = camera[12];
    float ty = camera[13];
    float tz = camera[14];

    view[12] = -(view[0] * tx + view[4] * ty + view[8]  * tz);
    view[13] = -(view[1] * tx + view[5] * ty + view[9]  * tz);
    view[14] = -(view[2] * tx + view[6] * ty + view[10] * tz);

    view[3] = view[7] = view[11] = 0.0f;
    view[15] = 1.0f;

    // ===== 2. modelView = view * model =====
    float mv[16];
    for (int c = 0; c < 4; ++c) {
        int ci = c * 4;
        mv[ci]     = view[0] * model[ci]     + view[4] * model[ci + 1] +
                     view[8] * model[ci + 2] + view[12] * model[ci + 3];
        mv[ci + 1] = view[1] * model[ci]     + view[5] * model[ci + 1] +
                     view[9] * model[ci + 2] + view[13] * model[ci + 3];
        mv[ci + 2] = view[2] * model[ci]     + view[6] * model[ci + 1] +
                     view[10] * model[ci + 2]+ view[14] * model[ci + 3];
        mv[ci + 3] = 0.0f;
    }
    mv[15] = 1.0f;

    // ===== 3. 计算 depth（camera space -Z）=====
    std::vector<Node> nodes;
    nodes.resize(gaussianCount);

    for (int i = 0; i < gaussianCount; ++i) {
        int b = i * 3;

        float cz =
                mv[2]  * centers[b] +
                mv[6]  * centers[b + 1] +
                mv[10] * centers[b + 2] +
                mv[14];

        nodes[i].depth = -cz;
        nodes[i].index = i;
    }

    // ===== 4. 排序（远 → 近）=====
    std::sort(nodes.begin(), nodes.end(),
              [](const Node& a, const Node& b) {
                  return a.depth > b.depth;
              });

    // ===== 5. 填充 indicesCache（6 indices / quad）=====
    int out = 0;
    for (int i = 0; i < gaussianCount; ++i) {
        int base = nodes[i].index * 4;

        indices[out++] = base;
        indices[out++] = base + 1;
        indices[out++] = base + 2;

        indices[out++] = base;
        indices[out++] = base + 2;
        indices[out++] = base + 3;
    }

    // ===== 6. 释放 JNI 数组 =====
    env->ReleaseFloatArrayElements(jCenters, centers, JNI_ABORT);
    env->ReleaseFloatArrayElements(jModelMat, model, JNI_ABORT);
    env->ReleaseFloatArrayElements(jCameraMat, camera, JNI_ABORT);
    env->ReleaseIntArrayElements(jIndices, indices, 0);
}
