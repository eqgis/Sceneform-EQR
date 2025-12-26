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


