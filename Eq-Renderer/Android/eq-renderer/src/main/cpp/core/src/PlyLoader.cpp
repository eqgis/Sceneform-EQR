// Created by IKKYU on 2026/1/4.
//
// PlyLoader.cpp
#include "tinyply.h"

#include <istream>
#include <cstring>

#include <jni.h>
#include <memory>
#include <sstream>
#include <PlyLoader.h>
#include "Core.h"

#include <thread>
#include <chrono>
#include <vector>
#include <fstream>
#include <iostream>
#include <iterator>

#include <filament/VertexBuffer.h>
#include <filament/IndexBuffer.h>
#include <filament/Engine.h>

using namespace filament;

using namespace tinyply;
using namespace EQR;

inline std::vector<uint8_t> read_file_binary(const std::string & pathToFile)
{
    std::ifstream file(pathToFile, std::ios::binary);
    std::vector<uint8_t> fileBufferBytes;

    if (file.is_open())
    {
        file.seekg(0, std::ios::end);
        size_t sizeBytes = file.tellg();
        file.seekg(0, std::ios::beg);
        fileBufferBytes.resize(sizeBytes);
        if (file.read((char*)fileBufferBytes.data(), sizeBytes)) return fileBufferBytes;
    }
    else throw std::runtime_error("could not open binary ifstream to path " + pathToFile);
    return fileBufferBytes;
}

struct memory_buffer : public std::streambuf
{
    char * p_start {nullptr};
    char * p_end {nullptr};
    size_t size;

    memory_buffer(char const * first_elem, size_t size)
            : p_start(const_cast<char*>(first_elem)), p_end(p_start + size), size(size)
    {
        setg(p_start, p_start, p_end);
    }

    pos_type seekoff(off_type off, std::ios_base::seekdir dir, std::ios_base::openmode which) override
    {
        if (dir == std::ios_base::cur) gbump(static_cast<int>(off));
        else setg(p_start, (dir == std::ios_base::beg ? p_start : p_end) + off, p_end);
        return gptr() - p_start;
    }

    pos_type seekpos(pos_type pos, std::ios_base::openmode which) override
    {
        return seekoff(pos, std::ios_base::beg, which);
    }
};

struct memory_stream : virtual memory_buffer, public std::istream
{
    memory_stream(char const * first_elem, size_t size)
            : memory_buffer(first_elem, size), std::istream(static_cast<std::streambuf*>(this)) {}
};

class manual_timer
{
    std::chrono::high_resolution_clock::time_point t0;
    double timestamp{ 0.0 };
public:
    void start() { t0 = std::chrono::high_resolution_clock::now(); }
    void stop() { timestamp = std::chrono::duration<double>(std::chrono::high_resolution_clock::now() - t0).count() * 1000.0; }
    const double & get() { return timestamp; }
};

void read_ply_from_memory(PlyAsset& out,const uint8_t* data,size_t size)
{
    try
    {
        if (!data || size == 0)
            throw std::runtime_error("PLY data is empty");

        // tinyply 官方 memory_stream
        memory_stream file_stream(
                reinterpret_cast<const char*>(data),
                size);

        const float size_mb = size * float(1e-6);

        PlyFile file;
        file.parse_header(file_stream);

        // ===== vertex =====
        try { out.vertices = file.request_properties_from_element(
                    "vertex", { "x", "y", "z" }); }
        catch (...) {}

        try { out.normals = file.request_properties_from_element(
                    "vertex", { "nx", "ny", "nz" }); }
        catch (...) {}

        try { out.colors = file.request_properties_from_element(
                    "vertex", { "red", "green", "blue", "alpha" }); }
        catch (...) {}

        try { out.colors = file.request_properties_from_element(
                    "vertex", { "r", "g", "b", "a" }); }
        catch (...) {}

        try { out.texcoords = file.request_properties_from_element(
                    "vertex", { "u", "v" }); }
        catch (...) {}

        // ===== face =====
        try { out.faces = file.request_properties_from_element(
                    "face", { "vertex_indices" }, 3); }
        catch (...) {}

        // ===== tristrips =====
        try { out.tripstrip = file.request_properties_from_element(
                    "tristrips", { "vertex_indices" }, 0); }
        catch (...) {}

        manual_timer timer;
        timer.start();
        file.read(file_stream);
        timer.stop();

        const float parsing_time =
                static_cast<float>(timer.get()) / 1000.f;

        LOGI("Parsed %.2f MB in %.3f sec (%.2f MB/s)",
             size_mb,
             parsing_time,
             parsing_time > 0 ? size_mb / parsing_time : 0);

//        if (out.vertices)
//        {
//            LOGI("Vertices: %zu", out.vertices->count);
//        }
//
//        if (out.normals)
//            LOGI("Normals: %zu", out.normals->count);
//
        if (out.colors)
            LOGI("Colors: %zu", out.colors->count);
//
//        if (out.texcoords)
//            LOGI("Texcoords: %zu", out.texcoords->count);
//
//        if (out.faces)
//        {
//            LOGI("Faces: %zu", out.faces->count);
//            LOGI("Face index type = %d", (int)out.faces->t);
//        }
//
//        if (out.tripstrip)
//        {
//            LOGI("Tristrip indices: %zu",
//                 out.tripstrip->buffer.size_bytes() /
//                 tinyply::PropertyTable[out.tripstrip->t].stride);
//        }
    }
    catch (const std::exception& e)
    {
        LOGE("read_ply_from_memory error: %s", e.what());
        throw;
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_eqgis_eqr_core_PlyLoader_nDestroyPlyAsset(
        JNIEnv*, jclass, jlong native_object)
{
    if (!native_object)
        return;

    auto* pAsset = reinterpret_cast<PlyAsset*>(native_object);
    delete pAsset;
}


extern "C" JNIEXPORT void JNICALL
Java_com_eqgis_eqr_core_PlyLoader_nFill(
        JNIEnv *env,
        jclass,
        jlong nativeObject,
        jobject j_ply_asset)
{
    if (!nativeObject || !j_ply_asset)
        return;

    auto* asset = reinterpret_cast<PlyAsset*>(nativeObject);

    // 4. 填充 Java 对象
    jclass cls = env->GetObjectClass(j_ply_asset);


    if (asset->vertices){
        if (asset->vertices->count > 0){
            const float* v =
                    reinterpret_cast<const float*>(asset->vertices->buffer.get());

            const size_t count = asset->vertices->count;

            // 初始化
            asset->aabbMin = { v[0], v[1], v[2] };
            asset->aabbMax = asset->aabbMin;

            const float* p = v;

            for (size_t i = 1; i < count; ++i)
            {
                p += 3;

                asset->aabbMin.x = MIN(asset->aabbMin.x, p[0]);
                asset->aabbMin.y = MIN(asset->aabbMin.y, p[1]);
                asset->aabbMin.z = MIN(asset->aabbMin.y, p[1]);

                asset->aabbMax.x = MAX(asset->aabbMax.x, p[0]);
                asset->aabbMax.y = MAX(asset->aabbMax.y, p[1]);
                asset->aabbMax.z = MAX(asset->aabbMax.z, p[2]);
            }
            float aabb[6] = {
                    asset->aabbMin.x,
                    asset->aabbMin.y,
                    asset->aabbMin.z,
                    asset->aabbMax.x,
                    asset->aabbMax.y,
                    asset->aabbMax.z
            };

            setFloatArray(env, j_ply_asset,
                          env->GetFieldID(cls, "aabb", "[F"),
                          aabb,
                          6);
        }
        setFloatArray(env, j_ply_asset,
                      env->GetFieldID(cls, "vertices", "[F"),
                      reinterpret_cast<float*>(asset->vertices->buffer.get()),
                      asset->vertices->count * 3);
    }


    if (asset->normals){
        setFloatArray(env, j_ply_asset,
                      env->GetFieldID(cls, "normals", "[F"),
                      reinterpret_cast<float*>(asset->normals->buffer.get()),
                      asset->normals->count * 3);
    }


    if (asset->texcoords){
        setFloatArray(env, j_ply_asset,
                      env->GetFieldID(cls, "texcoords", "[F"),
                      reinterpret_cast<float*>(asset->texcoords->buffer.get()),
                      asset->texcoords->count * 2);
    }

    // 修复：正确处理 faces 数据
    if (asset->faces) {
//        LOGI("Debug faces info:");
//        LOGI("  faces->count = %zu", asset->faces->count);
//        LOGI("  faces->t = %d (type)", (int)asset->faces->t);
//        LOGI("  faces->buffer.size_bytes() = %zu", asset->faces->buffer.size_bytes());

        // 根据 faces 的数据类型进行处理
        size_t indexCount = asset->faces->count * 3;  // 每个三角形面有3个顶点索引

        // 创建临时数组来存储转换后的索引
        std::vector<jint> indices(indexCount);

        // 根据数据类型转换
        switch (asset->faces->t) {
            case tinyply::Type::UINT8: {
                uint8_t* data = reinterpret_cast<uint8_t*>(asset->faces->buffer.get());
                for (size_t i = 0; i < indexCount; i++) {
                    indices[i] = static_cast<jint>(data[i]);
                }
                break;
            }
            case tinyply::Type::INT8: {
                int8_t* data = reinterpret_cast<int8_t*>(asset->faces->buffer.get());
                for (size_t i = 0; i < indexCount; i++) {
                    indices[i] = static_cast<jint>(data[i]);
                }
                break;
            }
            case tinyply::Type::UINT16: {
                uint16_t* data = reinterpret_cast<uint16_t*>(asset->faces->buffer.get());
                for (size_t i = 0; i < indexCount; i++) {
                    indices[i] = static_cast<jint>(data[i]);
                }
                break;
            }
            case tinyply::Type::INT16: {
                int16_t* data = reinterpret_cast<int16_t*>(asset->faces->buffer.get());
                for (size_t i = 0; i < indexCount; i++) {
                    indices[i] = static_cast<jint>(data[i]);
                }
                break;
            }
            case tinyply::Type::UINT32: {
                uint32_t* data = reinterpret_cast<uint32_t*>(asset->faces->buffer.get());
                for (size_t i = 0; i < indexCount; i++) {
                    indices[i] = static_cast<jint>(data[i]);
                }
                break;
            }
            case tinyply::Type::INT32: {
                int32_t* data = reinterpret_cast<int32_t*>(asset->faces->buffer.get());
                for (size_t i = 0; i < indexCount; i++) {
                    indices[i] = static_cast<jint>(data[i]);
                }
                break;
            }
            default:
                LOGI("Unsupported face index type: %d", (int)asset->faces->t);
                return;
        }

        // 设置 int 数组到 Java 对象
        setIntArray(env, j_ply_asset,
                    env->GetFieldID(cls, "faces", "[I"),
                    indices.data(),
                    indices.size());
    }

    if (asset->tripstrip)
    {
        const uint8_t* raw = asset->tripstrip->buffer.get();
        const size_t bufferSize = asset->tripstrip->buffer.size_bytes();
        const size_t indexStride =
                tinyply::PropertyTable[asset->tripstrip->t].stride;

        size_t offset = 0;
        size_t totalInts = 0;

        // ---------- 第一遍：统计 & 边界检查 ----------
        for (size_t s = 0; s < asset->tripstrip->count; ++s)
        {
            if (offset + 1 > bufferSize)
            {
                LOGE("Tristrip buffer overflow (len)");
                return;
            }

            uint8_t len = raw[offset++];
            totalInts += 1 + len;

            size_t advance = len * indexStride;
            if (offset + advance > bufferSize)
            {
                LOGE("Tristrip buffer overflow (indices)");
                return;
            }

            offset += advance;
        }

        std::vector<jint> out;
        out.reserve(totalInts);

        offset = 0;

        // ---------- 第二遍：填充 ----------
        switch (asset->tripstrip->t)
        {
            case tinyply::Type::UINT16:
            {
                for (size_t s = 0; s < asset->tripstrip->count; ++s)
                {
                    uint8_t len = raw[offset++];
                    out.push_back(len);

                    const uint16_t* idx =
                            reinterpret_cast<const uint16_t*>(raw + offset);

                    for (uint8_t i = 0; i < len; ++i)
                        out.push_back(idx[i]);

                    offset += len * sizeof(uint16_t);
                }
                break;
            }

            case tinyply::Type::UINT32:
            {
                for (size_t s = 0; s < asset->tripstrip->count; ++s)
                {
                    uint8_t len = raw[offset++];
                    out.push_back(len);

                    const uint32_t* idx =
                            reinterpret_cast<const uint32_t*>(raw + offset);

                    for (uint8_t i = 0; i < len; ++i)
                        out.push_back(idx[i]);

                    offset += len * sizeof(uint32_t);
                }
                break;
            }

            case tinyply::Type::INT32:
            {
                for (size_t s = 0; s < asset->tripstrip->count; ++s)
                {
                    uint8_t len = raw[offset++];
                    out.push_back(len);

                    const int32_t* idx =
                            reinterpret_cast<const int32_t*>(raw + offset);

                    for (uint8_t i = 0; i < len; ++i)
                        out.push_back(idx[i]);

                    offset += len * sizeof(int32_t);
                }
                break;
            }

            default:
                LOGE("Unsupported tristrip index type");
                return;
        }

        setIntArray(env, j_ply_asset,
                    env->GetFieldID(cls, "tripstrip", "[I"),
                    out.data(),
                    out.size());
    }

    // 新增：处理颜色数据 - 自动补齐至4通道
    if (asset->colors) {
        const size_t vertexCount = asset->colors->count;
//        LOGI("Colors count: %zu", asset->colors->count);
//        LOGI("Colors type: %d", (int)asset->colors->t);
//        LOGI("Colors buffer size: %zu bytes", asset->colors->buffer.size_bytes());

        // 计算实际通道数
        size_t elementSize = tinyply::PropertyTable[asset->colors->t].stride;
        size_t totalElements = asset->colors->buffer.size_bytes() / elementSize;
        size_t srcChannels = totalElements / vertexCount;
        LOGI("Colors Channels per vertex: %zu", srcChannels);

        std::vector<float> floatColors;
        floatColors.resize(vertexCount * 4); // 总是输出4通道RGBA

        // 根据不同数据类型和通道数处理
        switch (asset->colors->t) {
            case tinyply::Type::UINT8: {
                const uint8_t* srcData =
                        reinterpret_cast<const uint8_t*>(asset->colors->buffer.get());

                // 根据源通道数处理
                if (srcChannels == 1) {
                    // 单通道 -> RGBA (R,G,B = 单通道值, A=255)
                    for (size_t i = 0; i < vertexCount; i++) {
                        const size_t srcIdx = i;
                        const size_t dstIdx = i * 4;
                        float value = static_cast<float>(srcData[srcIdx]) / 255.0f;
                        floatColors[dstIdx] = value;      // R
                        floatColors[dstIdx + 1] = value;  // G
                        floatColors[dstIdx + 2] = value;  // B
                        floatColors[dstIdx + 3] = 1.0f;   // A
                    }
                }
                else if (srcChannels == 2) {
                    // 双通道 -> RGBA (R=通道1, G=通道2, B=0, A=255)
                    for (size_t i = 0; i < vertexCount; i++) {
                        const size_t srcIdx = i * 2;
                        const size_t dstIdx = i * 4;
                        floatColors[dstIdx] = static_cast<float>(srcData[srcIdx]) / 255.0f;      // R
                        floatColors[dstIdx + 1] = static_cast<float>(srcData[srcIdx + 1]) / 255.0f; // G
                        floatColors[dstIdx + 2] = 0.0f;   // B
                        floatColors[dstIdx + 3] = 1.0f;   // A
                    }
                }
                else if (srcChannels == 3) {
                    // 三通道RGB -> RGBA (A=255)
                    for (size_t i = 0; i < vertexCount; i++) {
                        const size_t srcIdx = i * 3;
                        const size_t dstIdx = i * 4;
                        floatColors[dstIdx] = static_cast<float>(srcData[srcIdx]) / 255.0f;      // R
                        floatColors[dstIdx + 1] = static_cast<float>(srcData[srcIdx + 1]) / 255.0f; // G
                        floatColors[dstIdx + 2] = static_cast<float>(srcData[srcIdx + 2]) / 255.0f; // B
                        floatColors[dstIdx + 3] = 1.0f;   // A
                    }
                }
                else if (srcChannels == 4) {
                    // 四通道RGBA -> 直接转换
                    for (size_t i = 0; i < vertexCount; i++) {
                        const size_t srcIdx = i * 4;
                        const size_t dstIdx = i * 4;
                        floatColors[dstIdx] = static_cast<float>(srcData[srcIdx]) / 255.0f;      // R
                        floatColors[dstIdx + 1] = static_cast<float>(srcData[srcIdx + 1]) / 255.0f; // G
                        floatColors[dstIdx + 2] = static_cast<float>(srcData[srcIdx + 2]) / 255.0f; // B
                        floatColors[dstIdx + 3] = static_cast<float>(srcData[srcIdx + 3]) / 255.0f; // A
                    }
                }
                else {
                    LOGE("Unsupported channel count for UINT8: %zu", srcChannels);
                    return;
                }
                break;
            }
            case tinyply::Type::FLOAT32: {
                const float* srcData =
                        reinterpret_cast<const float*>(asset->colors->buffer.get());

                if (srcChannels == 1) {
                    // 单通道float -> RGBA
                    for (size_t i = 0; i < vertexCount; i++) {
                        const size_t srcIdx = i;
                        const size_t dstIdx = i * 4;
                        float value = std::max(0.0f, std::min(1.0f, srcData[srcIdx]));
                        floatColors[dstIdx] = value;      // R
                        floatColors[dstIdx + 1] = value;  // G
                        floatColors[dstIdx + 2] = value;  // B
                        floatColors[dstIdx + 3] = 1.0f;   // A
                    }
                }
                else if (srcChannels == 2) {
                    // 双通道float -> RGBA
                    for (size_t i = 0; i < vertexCount; i++) {
                        const size_t srcIdx = i * 2;
                        const size_t dstIdx = i * 4;
                        floatColors[dstIdx] = std::max(0.0f, std::min(1.0f, srcData[srcIdx]));      // R
                        floatColors[dstIdx + 1] = std::max(0.0f, std::min(1.0f, srcData[srcIdx + 1])); // G
                        floatColors[dstIdx + 2] = 0.0f;   // B
                        floatColors[dstIdx + 3] = 1.0f;   // A
                    }
                }
                else if (srcChannels == 3) {
                    // 三通道float -> RGBA
                    for (size_t i = 0; i < vertexCount; i++) {
                        const size_t srcIdx = i * 3;
                        const size_t dstIdx = i * 4;
                        floatColors[dstIdx] = std::max(0.0f, std::min(1.0f, srcData[srcIdx]));      // R
                        floatColors[dstIdx + 1] = std::max(0.0f, std::min(1.0f, srcData[srcIdx + 1])); // G
                        floatColors[dstIdx + 2] = std::max(0.0f, std::min(1.0f, srcData[srcIdx + 2])); // B
                        floatColors[dstIdx + 3] = 1.0f;   // A
                    }
                }
                else if (srcChannels == 4) {
                    // 四通道float -> 直接使用
                    // 注意：这里不能直接用setFloatArray，因为count是顶点数，不是元素数
                    for (size_t i = 0; i < vertexCount; i++) {
                        const size_t srcIdx = i * 4;
                        const size_t dstIdx = i * 4;
                        floatColors[dstIdx] = std::max(0.0f, std::min(1.0f, srcData[srcIdx]));      // R
                        floatColors[dstIdx + 1] = std::max(0.0f, std::min(1.0f, srcData[srcIdx + 1])); // G
                        floatColors[dstIdx + 2] = std::max(0.0f, std::min(1.0f, srcData[srcIdx + 2])); // B
                        floatColors[dstIdx + 3] = std::max(0.0f, std::min(1.0f, srcData[srcIdx + 3])); // A
                    }
                }
                else {
                    LOGE("Unsupported channel count for FLOAT32: %zu", srcChannels);
                    return;
                }
                break;
            }
            case tinyply::Type::UINT16: {
                // 支持uint16类型
                const uint16_t* srcData =
                        reinterpret_cast<const uint16_t*>(asset->colors->buffer.get());

                // 根据通道数处理（与UINT8类似）
                if (srcChannels == 1) {
                    for (size_t i = 0; i < vertexCount; i++) {
                        const size_t srcIdx = i;
                        const size_t dstIdx = i * 4;
                        float value = static_cast<float>(srcData[srcIdx]) / 65535.0f;
                        floatColors[dstIdx] = value;
                        floatColors[dstIdx + 1] = value;
                        floatColors[dstIdx + 2] = value;
                        floatColors[dstIdx + 3] = 1.0f;
                    }
                }
                    // ... 可以添加其他通道数的处理
                else {
                    LOGE("UINT16 color with %zu channels not implemented", srcChannels);
                    return;
                }
                break;
            }
            default:
                LOGE("Unsupported color data type: %d", (int)asset->colors->t);
                return;
        }

        // 设置到Java对象
        setFloatArray(env, j_ply_asset,
                      env->GetFieldID(cls, "colors", "[F"),
                      floatColors.data(),
                      floatColors.size());
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_eqgis_eqr_core_PlyLoader_nLoadFromBytes(
        JNIEnv* env,
        jclass,
        jbyteArray jdata)
{
    try
    {
        if (!jdata)
            throw std::runtime_error("PLY byte array is null");

        jsize length = env->GetArrayLength(jdata);
        if (length <= 0)
            throw std::runtime_error("PLY byte array is empty");

        // 直接 pin 内存（避免拷贝）
        jbyte* bytes = env->GetByteArrayElements(jdata, nullptr);

        PlyAsset* plyAsset = new PlyAsset();
        read_ply_from_memory(
                *plyAsset,
                reinterpret_cast<uint8_t*>(bytes),
                static_cast<size_t>(length));

        // 释放 Java 内存
        env->ReleaseByteArrayElements(jdata, bytes, JNI_ABORT);

        return reinterpret_cast<jlong>(plyAsset);
    }
    catch (const std::exception& e)
    {
        LOGE("nLoadFromBytes error: %s", e.what());
        return 0;
    }
}
