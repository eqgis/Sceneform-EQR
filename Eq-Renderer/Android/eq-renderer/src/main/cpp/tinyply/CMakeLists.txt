cmake_minimum_required(VERSION 3.22.1)

# tinyply 作为纯 C++ 静态库
add_library(tinyply STATIC
        tinyply.cpp
)

# 头文件就在当前目录
target_include_directories(tinyply PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}
)

## 使用和 Filament 一致的 C++ 标准
#target_compile_features(tinyply PUBLIC cxx_std_17)
#
## 关闭异常 / RTTI（与主工程保持一致）
#target_compile_options(tinyply PRIVATE
#        -fno-exceptions
#        -fno-rtti
#)
#
## 启用无异常模式（改 tinyply.h 用到的宏）
#target_compile_definitions(tinyply PUBLIC
#        TINYPly_NO_EXCEPTIONS
#)

## Android 平台标识
#target_compile_definitions(tinyply PUBLIC
#        ANDROID
#)
