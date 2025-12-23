@echo off

REM 创建输出目录
if not exist out mkdir out

REM 遍历 Assets\mat 目录下的所有 .mat 文件
for %%G in (assets\mat\*.mat) do (
    matc --optimize-size --platform=mobile -o "out\%%~nG.filamat" "%%G"
)

echo success!
pause
