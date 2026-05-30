@echo off
echo ========================================
echo 网络连接测试
echo ========================================
echo.

echo [1/3] 测试阿里云镜像...
echo 测试 Google Maven 镜像...
curl -s --connect-timeout 10 -I "https://maven.aliyun.com/repository/google" | findstr "HTTP/"
if %errorlevel% equ 0 (
    echo ✓ 阿里云 Google Maven 镜像连接正常
) else (
    echo ✗ 阿里云 Google Maven 镜像连接失败
)

echo.
echo 测试 Central Maven 镜像...
curl -s --connect-timeout 10 -I "https://maven.aliyun.com/repository/central" | findstr "HTTP/"
if %errorlevel% equ 0 (
    echo ✓ 阿里云 Central Maven 镜像连接正常
) else (
    echo ✗ 阿里云 Central Maven 镜像连接失败
)

echo.
echo [2/3] 测试原始仓库...
echo 测试 Google Maven 原始仓库...
curl -s --connect-timeout 10 -I "https://dl.google.com/dl/android/maven2/" | findstr "HTTP/"
if %errorlevel% equ 0 (
    echo ✓ Google Maven 原始仓库连接正常
) else (
    echo ✗ Google Maven 原始仓库连接失败（可能需要代理）
)

echo.
echo 测试 Maven Central...
curl -s --connect-timeout 10 -I "https://repo1.maven.org/maven2/" | findstr "HTTP/"
if %errorlevel% equ 0 (
    echo ✓ Maven Central 连接正常
) else (
    echo ✗ Maven Central 连接失败
)

echo.
echo [3/3] 测试Gradle下载...
echo 测试腾讯云Gradle镜像...
curl -s --connect-timeout 10 -I "https://mirrors.cloud.tencent.com/gradle/" | findstr "HTTP/"
if %errorlevel% equ 0 (
    echo ✓ 腾讯云 Gradle 镜像连接正常
) else (
    echo ✗ 腾讯云 Gradle 镜像连接失败
)

echo.
echo ========================================
echo 测试完成
echo ========================================
echo.
echo 如果所有测试都失败，请检查：
echo 1. 网络连接是否正常
echo 2. 是否需要配置代理
echo 3. 防火墙设置
echo.
echo 如果只有原始仓库失败但镜像正常，
echo 项目应该可以正常构建。
echo.
pause
