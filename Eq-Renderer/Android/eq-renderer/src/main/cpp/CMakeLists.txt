cmake_minimum_required(VERSION 3.22.1)
project(eqr-core)
## 启用 C++ 异常支持
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fexceptions")
#set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -fexceptions")
#set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -fexceptions")


add_subdirectory(filament-utils)
add_subdirectory(tinyply)

add_library(eqr-core SHARED
        core/src/Core.cpp
        core/src/CoordinateUtils.cpp
        core/src/FilamentAssetsUtils.cpp
        core/src/PlyLoader.cpp
)

target_include_directories(eqr-core PRIVATE
        tinyply
        core/include
        filament-android-native/include
        libs/gltfio/include
        libs/gltfio/src
        third_party/cgltf
)

find_library(log-lib log)

target_link_libraries(eqr-core
        tinyply
        -Wl,--whole-archive
        filament-jni
        filament-utils-jni
        gltfio-jni
        -Wl,--no-whole-archive
        ${log-lib}
)


# 启用无异常模式
target_compile_definitions(eqr-core PUBLIC
        EQR_NO_EXCEPTIONS
)