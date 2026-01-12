//
// Created by IKKYU on 2026/1/4.
//

#ifndef EQ_RENDERER_SAMPLE_PLYLOADER_H
#define EQ_RENDERER_SAMPLE_PLYLOADER_H

// PlyLoader.h
#pragma once

#include <vector>
#include <cstdint>

#include <filament/Box.h>
#include <filament/TextureSampler.h>

#include <utils/compiler.h>
#include <utils/Entity.h>
#include <filament/IndexBuffer.h>
#include "tinyply.h"

using namespace filament;
using namespace filament::math;
using namespace utils;

namespace EQR{
    struct PlyAsset
    {
        std::shared_ptr<tinyply::PlyData> vertices, normals, colors, texcoords, tripstrip,faces;


        math::float3 aabbMin;
        math::float3 aabbMax;

    };

    inline void setIntArray(JNIEnv* env, jobject obj, jfieldID fieldId, jint* data, jsize length) {
        jintArray array = env->NewIntArray(length);
        env->SetIntArrayRegion(array, 0, length, data);
        env->SetObjectField(obj, fieldId, array);
        env->DeleteLocalRef(array);
    }


    inline void setFloatArray(
            JNIEnv* env,
            jobject obj,
            jfieldID field,
            const float* data,
            size_t count)
    {
        if (!data || count == 0) return;

        jfloatArray arr = env->NewFloatArray(count);
        env->SetFloatArrayRegion(arr, 0, count, data);
        env->SetObjectField(obj, field, arr);
    }
}



#endif //EQ_RENDERER_SAMPLE_PLYLOADER_H
