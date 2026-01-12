//
// Created by IKKYU on 2025/12/26.
//
#include <jni.h>

#include <filament/Engine.h>
#include <filament/RenderableManager.h>

#include <gltfio/FilamentAsset.h>
#include <gltfio/AssetLoader.h>

#include <backend/BufferDescriptor.h>

#include <utils/Entity.h>
#include <utils/Log.h>

#include <cgltf.h>

#include "FFilamentAsset.h"


using namespace filament;
using namespace filament::gltfio;
using namespace utils;

static void rebuildFromMeshCache(
        RenderableManager& rm,
        RenderableManager::Instance instance,
        RenderableManager::PrimitiveType newType,
        FilamentAsset* asset) {

    auto* fAsset = downcast(asset);
    fAsset->getAssetInstances();

    // Renderable 上的 primitive 数
    const size_t primitiveCount = rm.getPrimitiveCount(instance);
    if (primitiveCount == 0) return;

    //
    auto& meshCache = fAsset->mMeshCache;

    for (size_t meshIndex = 0; meshIndex < meshCache.size(); ++meshIndex) {
        auto& prims = meshCache[meshIndex];

        for (size_t primIndex = 0; primIndex < prims.size(); ++primIndex) {
            auto& prim = prims[primIndex];

            if (!prim.vertices || !prim.indices) continue;

            rm.setGeometryAt(
                    instance,
                    primIndex,
                    newType,
                    prim.vertices,
                    prim.indices,
                    0,
                    prim.indices->getIndexCount()
            );
        }
    }
}


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_eqgis_eqr_core_FilamentPrimitiveUtilsNative_nRebuildPrimitiveGeometry(
        JNIEnv*, jclass,
        jlong nativeEngine,
        jlong nativeAsset,
        jint entityId,
        jint primitiveType) {

    Engine* engine = reinterpret_cast<Engine*>(nativeEngine);
    if (!engine) return JNI_FALSE;

    FilamentAsset* asset =
            reinterpret_cast<FilamentAsset*>(nativeAsset);
    if (!asset) return JNI_FALSE;

    auto* fAsset = downcast(asset);
    if (!fAsset) return JNI_FALSE;

    RenderableManager& rm = engine->getRenderableManager();

    Entity entity = Entity::import(entityId);
    auto instance = rm.getInstance(entity);
    if (!instance) return JNI_FALSE;

    rebuildFromMeshCache(
            rm,
            instance,
            static_cast<RenderableManager::PrimitiveType>(primitiveType),
            fAsset
    );

    return JNI_TRUE;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_eqgis_eqr_core_FilamentPrimitiveUtilsNative_nComputeVertexNormals(JNIEnv *env, jclass clazz,
                                                        jfloatArray jPositions, jintArray jIndices) {
    if (jPositions == nullptr || jIndices == nullptr)
        return nullptr;

    const jsize posLen = env->GetArrayLength(jPositions);
    const jsize idxLen = env->GetArrayLength(jIndices);

    if (posLen % 3 != 0 || idxLen % 3 != 0)
        return nullptr;

    const int vertexCount = posLen / 3;

    // 获取数组指针（不拷贝，性能最优）
    jfloat* positions = env->GetFloatArrayElements(jPositions, nullptr);
    jint*   indices   = env->GetIntArrayElements(jIndices, nullptr);

    std::vector<float> normals(posLen, 0.0f);

    // === accumulate normals ===
    for (int i = 0; i < idxLen; i += 3)
    {
        const int i0 = indices[i];
        const int i1 = indices[i + 1];
        const int i2 = indices[i + 2];

        const float x0 = positions[3 * i0];
        const float y0 = positions[3 * i0 + 1];
        const float z0 = positions[3 * i0 + 2];

        const float x1 = positions[3 * i1];
        const float y1 = positions[3 * i1 + 1];
        const float z1 = positions[3 * i1 + 2];

        const float x2 = positions[3 * i2];
        const float y2 = positions[3 * i2 + 1];
        const float z2 = positions[3 * i2 + 2];

        const float ex1 = x1 - x0;
        const float ey1 = y1 - y0;
        const float ez1 = z1 - z0;

        const float ex2 = x2 - x0;
        const float ey2 = y2 - y0;
        const float ez2 = z2 - z0;

        const float nx = ey1 * ez2 - ez1 * ey2;
        const float ny = ez1 * ex2 - ex1 * ez2;
        const float nz = ex1 * ey2 - ey1 * ex2;

        normals[3 * i0]     += nx;
        normals[3 * i0 + 1] += ny;
        normals[3 * i0 + 2] += nz;

        normals[3 * i1]     += nx;
        normals[3 * i1 + 1] += ny;
        normals[3 * i1 + 2] += nz;

        normals[3 * i2]     += nx;
        normals[3 * i2 + 1] += ny;
        normals[3 * i2 + 2] += nz;
    }

    // === normalize ===
    for (int i = 0; i < vertexCount; ++i)
    {
        float& nx = normals[3 * i];
        float& ny = normals[3 * i + 1];
        float& nz = normals[3 * i + 2];

        const float len = std::sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 1e-6f)
        {
            nx /= len;
            ny /= len;
            nz /= len;
        }
    }

    // 释放输入数组
    env->ReleaseFloatArrayElements(jPositions, positions, JNI_ABORT);
    env->ReleaseIntArrayElements(jIndices, indices, JNI_ABORT);

    // 构建返回数组
    jfloatArray jNormals = env->NewFloatArray(posLen);
    env->SetFloatArrayRegion(jNormals, 0, posLen, normals.data());

    return jNormals;
}

