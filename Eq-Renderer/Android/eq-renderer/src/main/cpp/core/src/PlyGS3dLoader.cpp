// Created by IKKYU on 2026/1/4.
//
// PlyLoader.cpp
#include "tinyply.h"

#include <istream>
#include <cstring>

#include <jni.h>
#include <memory>
#include <sstream>
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
#include "PlyLoader.h"

using namespace filament;

using namespace tinyply;

using namespace EQR;

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

struct PlyGS3dAsset
{
    // 元数据
    bool is3DGS = false;        // 标记是否为3DGS文件
    size_t pointCount = 0;      // 点数量
    int shDegree = 3;           // 球谐阶数（默认为3阶）

    // 包围盒
    math::float3 aabbMin;
    math::float3 aabbMax;

    // 属性映射表 - 这是最重要的存储结构
    std::unordered_map<std::string, std::shared_ptr<tinyply::PlyData>> propertyMap;

    // 根据属性名获取数据
    std::shared_ptr<tinyply::PlyData> getProperty(const std::string& name) const {
        auto it = propertyMap.find(name);
        if (it != propertyMap.end()) {
            return it->second;
        }
        return nullptr;
    }

    // 获取组合的属性数据
    template<typename T>
    std::vector<T> getCombinedProperty(const std::vector<std::string>& names) const {
        std::vector<T> result;
        if (names.empty()) return result;

        auto firstProp = getProperty(names[0]);
        if (!firstProp) return result;

        size_t count = firstProp->count;
        size_t numChannels = names.size();
        result.resize(count * numChannels);

        for (size_t i = 0; i < numChannels; i++) {
            auto prop = getProperty(names[i]);
            if (!prop || prop->count != count) {
                // 属性不一致，返回空
                result.clear();
                return result;
            }

            const T* src = reinterpret_cast<const T*>(prop->buffer.get());
            if (!src) continue;

            for (size_t j = 0; j < count; j++) {
                result[j * numChannels + i] = src[j];
            }
        }

        return result;
    }

    // 简化的获取方法，用于标准属性
    std::shared_ptr<tinyply::PlyData> getVertices() const {
        // 注意：对于3DGS，顶点需要从x,y,z组合
        return getProperty("x");
    }

    std::shared_ptr<tinyply::PlyData> getNormals() const { return getProperty("nx"); }
    std::shared_ptr<tinyply::PlyData> getColors() const {
        auto prop = getProperty("red");
        if (!prop) prop = getProperty("r");
        return prop;
    }
    std::shared_ptr<tinyply::PlyData> getTexCoords() const { return getProperty("u"); }
    std::shared_ptr<tinyply::PlyData> getFaces() const { return getProperty("vertex_indices"); }
    std::shared_ptr<tinyply::PlyData> getTripStrip() const { return getProperty("vertex_indices"); }
};



void read_ply_from_memory(PlyGS3dAsset& out, const uint8_t* data, size_t size)
{
    try
    {
        if (!data || size == 0)
            throw std::runtime_error("PLY data is empty");

        memory_stream file_stream(
                reinterpret_cast<const char*>(data),
                size);

        const float size_mb = size * float(1e-6);

        PlyFile file;
        file.parse_header(file_stream);

        LOGI("Parsing PLY file...");

        // ===== 1. 检测是否为3DGS文件 =====
        out.is3DGS = false;
        const auto& elements = file.get_elements();

        for (const auto& elem : elements) {
            if (elem.name == "vertex") {
                LOGI("Vertex element with %zu properties", elem.properties.size());
                for (const auto& prop : elem.properties) {
                    if (prop.name.find("f_dc_") != std::string::npos) {
                        out.is3DGS = true;
                        LOGI("Detected 3DGS property: %s", prop.name.c_str());
                        break;
                    }
                }
                break;
            }
        }

        LOGI("File format: %s", out.is3DGS ? "3D Gaussian Splatting" : "Standard PLY");

        // ===== 2. 请求属性 =====
        std::vector<std::shared_ptr<tinyply::PlyData>> requestedProperties;

        if (out.is3DGS) {
            LOGI("Requesting 3DGS properties...");

            // 基础属性
            std::vector<std::string> baseProps = {"x", "y", "z", "opacity"};

            // f_dc属性
            for (int i = 0; i < 3; i++) {
                baseProps.push_back("f_dc_" + std::to_string(i));
            }

            // scale属性
            for (int i = 0; i < 3; i++) {
                baseProps.push_back("scale_" + std::to_string(i));
            }

            // rot属性
            for (int i = 0; i < 4; i++) {
                baseProps.push_back("rot_" + std::to_string(i));
            }

            // f_rest属性
            for (int i = 0; i < 45; i++) {
                baseProps.push_back("f_rest_" + std::to_string(i));
            }

            // 请求所有属性
            for (const auto& propName : baseProps) {
                try {
                    auto propData = file.request_properties_from_element("vertex", {propName});
                    if (propData) {
                        requestedProperties.push_back(propData);
                        out.propertyMap[propName] = propData;
                        LOGI("Requested: %s (count: %zu)", propName.c_str(), propData->count);
                    }
                } catch (const std::exception& e) {
                    LOGE("Failed to request %s: %s", propName.c_str(), e.what());
                }
            }

            // 设置pointCount
            auto xProp = out.getProperty("x");
            if (xProp) {
                out.pointCount = xProp->count;
                LOGI("Point count: %zu", out.pointCount);
            }

        } else {
            // 标准PLY文件
            LOGI("Requesting standard PLY properties...");

            std::vector<std::string> vertexProps = {"x", "y", "z", "nx", "ny", "nz"};

            for (const auto& propName : vertexProps) {
                try {
                    auto propData = file.request_properties_from_element("vertex", {propName});
                    if (propData) {
                        requestedProperties.push_back(propData);
                        out.propertyMap[propName] = propData;
                    }
                } catch (...) {}
            }
        }

        // ===== 3. 读取数据 =====
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

        // ===== 4. 计算球谐阶数（如果是3DGS） =====
        if (out.is3DGS && out.pointCount > 0) {
            // 检查f_rest属性数量来计算球谐阶数
            int f_rest_count = 0;
            for (int i = 0; i < 45; i++) {
                if (out.getProperty("f_rest_" + std::to_string(i))) {
                    f_rest_count++;
                }
            }

            if (f_rest_count > 0) {
                size_t coeffPerColor = f_rest_count / 3;
                out.shDegree = static_cast<int>(std::sqrt(coeffPerColor + 1)) - 1;
                LOGI("SH Degree: %d (f_rest properties: %d)", out.shDegree, f_rest_count);
            }
        }

        LOGI("Completed parsing %.2f MB in %.3f sec (%.2f MB/s)",
             size_mb, parsing_time,
             parsing_time > 0 ? size_mb / parsing_time : 0);
    }
    catch (const std::exception& e)
    {
        LOGE("read_ply_from_memory error: %s", e.what());
        throw;
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_eqgis_eqr_core_PlyGS3dLoader_nDestroyPlyAsset(
        JNIEnv*, jclass, jlong native_object)
{
    if (!native_object)
        return;

    auto* pAsset = reinterpret_cast<PlyGS3dAsset*>(native_object);
    delete pAsset;
}


extern "C" JNIEXPORT void JNICALL
Java_com_eqgis_eqr_core_PlyGS3dLoader_nFill(
        JNIEnv *env,
        jclass,
        jlong nativeObject,
        jobject j_ply_asset)
{
    if (!nativeObject || !j_ply_asset) {
        LOGE("nFill: Invalid parameters");
        return;
    }

    auto* asset = reinterpret_cast<PlyGS3dAsset*>(nativeObject);
    jclass cls = env->GetObjectClass(j_ply_asset);
    if (!cls) {
        LOGE("Failed to get Java class");
        return;
    }

    LOGI("nFill: Processing asset - is3DGS=%d, pointCount=%zu",
         asset->is3DGS, asset->pointCount);

    // ===== 1. 获取所有字段ID =====
    // 标准字段
    jfieldID verticesField = env->GetFieldID(cls, "vertices", "[F");
    jfieldID normalsField = env->GetFieldID(cls, "normals", "[F");
    jfieldID aabbField = env->GetFieldID(cls, "aabb", "[F");

    // 3DGS特有字段
    jfieldID f_dcField = env->GetFieldID(cls, "f_dc", "[F");
    jfieldID f_restField = env->GetFieldID(cls, "f_rest", "[F");
    jfieldID opacityField = env->GetFieldID(cls, "opacity", "[F");
    jfieldID scaleField = env->GetFieldID(cls, "scale", "[F");
    jfieldID rotationField = env->GetFieldID(cls, "rot", "[F"); // 注意：Java中是rot

    // 元数据字段
    jfieldID is3DGSField = env->GetFieldID(cls, "is3DGS", "Z");
    jfieldID pointCountField = env->GetFieldID(cls, "pointCount", "I");
    jfieldID shDegreeField = env->GetFieldID(cls, "shDegree", "I");

    // 设置元数据
    if (is3DGSField) env->SetBooleanField(j_ply_asset, is3DGSField, asset->is3DGS);
    if (pointCountField) env->SetIntField(j_ply_asset, pointCountField, static_cast<jint>(asset->pointCount));
    if (shDegreeField) env->SetIntField(j_ply_asset, shDegreeField, asset->shDegree);

    // ===== 2. 填充顶点数据和计算包围盒 =====
    if (verticesField) {
        auto propX = asset->getProperty("x");
        auto propY = asset->getProperty("y");
        auto propZ = asset->getProperty("z");

        if (propX && propY && propZ &&
            propX->buffer.get() && propY->buffer.get() && propZ->buffer.get()) {

            size_t count = propX->count;
            if (count > 0 && count == propY->count && count == propZ->count) {
                LOGI("Filling vertices: %zu points", count);

                std::vector<float> vertices(count * 3);
                const float* xPtr = reinterpret_cast<const float*>(propX->buffer.get());
                const float* yPtr = reinterpret_cast<const float*>(propY->buffer.get());
                const float* zPtr = reinterpret_cast<const float*>(propZ->buffer.get());

                // 初始化和计算包围盒
                asset->aabbMin = { xPtr[0], yPtr[0], zPtr[0] };
                asset->aabbMax = asset->aabbMin;

                for (size_t i = 0; i < count; i++) {
                    size_t idx = i * 3;
                    vertices[idx] = xPtr[i];
                    vertices[idx + 1] = yPtr[i];
                    vertices[idx + 2] = zPtr[i];

                    // 更新包围盒
                    asset->aabbMin.x = MIN(asset->aabbMin.x, xPtr[i]);
                    asset->aabbMin.y = MIN(asset->aabbMin.y, yPtr[i]);
                    asset->aabbMin.z = MIN(asset->aabbMin.z, zPtr[i]);

                    asset->aabbMax.x = MAX(asset->aabbMax.x, xPtr[i]);
                    asset->aabbMax.y = MAX(asset->aabbMax.y, yPtr[i]);
                    asset->aabbMax.z = MAX(asset->aabbMax.z, zPtr[i]);
                }

                // 设置顶点数组
                setFloatArray(env, j_ply_asset, verticesField, vertices.data(), vertices.size());

                // 设置包围盒
                if (aabbField) {
                    float aabb[6] = {
                            asset->aabbMin.x, asset->aabbMin.y, asset->aabbMin.z,
                            asset->aabbMax.x, asset->aabbMax.y, asset->aabbMax.z
                    };
                    setFloatArray(env, j_ply_asset, aabbField, aabb, 6);
                }

                LOGI("Vertices filled: %zu elements", vertices.size());
            }
        }
    }

    // ===== 3. 如果是3DGS，填充特有数据 =====
    if (asset->is3DGS) {
        LOGI("Filling 3DGS specific data...");

        // 填充f_dc
        if (f_dcField) {
            std::vector<std::string> f_dc_names = {"f_dc_0", "f_dc_1", "f_dc_2"};
            auto f_dc_data = asset->getCombinedProperty<float>(f_dc_names);
            if (!f_dc_data.empty()) {
                setFloatArray(env, j_ply_asset, f_dcField, f_dc_data.data(), f_dc_data.size());
                LOGI("f_dc filled: %zu elements", f_dc_data.size());
            }
        }

        // 填充f_rest
        if (f_restField) {
            std::vector<std::string> f_rest_names;
            for (int i = 0; i < 45; i++) {
                f_rest_names.push_back("f_rest_" + std::to_string(i));
            }
            auto f_rest_data = asset->getCombinedProperty<float>(f_rest_names);
            if (!f_rest_data.empty()) {
                setFloatArray(env, j_ply_asset, f_restField, f_rest_data.data(), f_rest_data.size());
                LOGI("f_rest filled: %zu elements", f_rest_data.size());
            }
        }

        // 填充opacity
        if (opacityField) {
            auto opacityProp = asset->getProperty("opacity");
            if (opacityProp && opacityProp->buffer.get() && opacityProp->count > 0) {
                const float* opacityPtr = reinterpret_cast<const float*>(opacityProp->buffer.get());
                setFloatArray(env, j_ply_asset, opacityField, opacityPtr, opacityProp->count);
                LOGI("opacity filled: %zu elements", opacityProp->count);
            }
        }

        // 填充scale
        if (scaleField) {
            std::vector<std::string> scale_names = {"scale_0", "scale_1", "scale_2"};
            auto scale_data = asset->getCombinedProperty<float>(scale_names);
            if (!scale_data.empty()) {
                setFloatArray(env, j_ply_asset, scaleField, scale_data.data(), scale_data.size());
                LOGI("scale filled: %zu elements", scale_data.size());
            }
        }

        // 填充rotation (注意：Java中是rot字段)
        if (rotationField) {
            std::vector<std::string> rot_names = {"rot_0", "rot_1", "rot_2", "rot_3"};
            auto rot_data = asset->getCombinedProperty<float>(rot_names);
            if (!rot_data.empty()) {
                setFloatArray(env, j_ply_asset, rotationField, rot_data.data(), rot_data.size());
                LOGI("rotation (rot) filled: %zu elements", rot_data.size());
            }
        }

        LOGI("3DGS data filling complete");
    }

    // ===== 4. 填充其他标准属性 =====
    // 这些可以保持你原有的填充逻辑，但需要适配新的数据结构

    LOGI("nFill completed successfully");
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_eqgis_eqr_core_PlyGS3dLoader_nLoadFromBytes(
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

        PlyGS3dAsset* plyAsset = new PlyGS3dAsset();
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
