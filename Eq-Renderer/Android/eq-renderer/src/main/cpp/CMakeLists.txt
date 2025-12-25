cmake_minimum_required(VERSION 3.22.1)
project(eqr-core)

add_subdirectory(filament-utils)

add_library(eqr-core SHARED
        core/src/Core.cpp
        core/src/CoordinateUtils.cpp
)

target_include_directories(eqr-core PRIVATE
        core/include
)

find_library(log-lib log)

target_link_libraries(eqr-core
        -Wl,--whole-archive
        filament-jni
        filament-utils-jni
        gltfio-jni
        -Wl,--no-whole-archive
        ${log-lib}
)
