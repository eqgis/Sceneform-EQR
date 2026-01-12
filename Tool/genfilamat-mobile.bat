@echo off

REM 创建输出目录
if not exist out mkdir out

REM 遍历 assets\mat 目录下的所有 .mat 文件
for %%G in (assets\mat\*.mat) do (
    echo Processing %%G ...

    REM 支持Vulkan 和 OpenGL
    matc --optimize-size --platform=mobile --api=vulkan --api opengl -o "out\%%~nG.filamat" "%%G"

    echo Done: %%~nG.filamat
)

echo success!
pause
