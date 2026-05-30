@echo off
echo ========================================
echo Sisyphus App - 运行脚本
echo ========================================
echo.

echo [1/4] 检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境
    pause
    exit /b 1
)
echo.

echo [2/4] 清理Gradle缓存...
if exist ".gradle" (
    rmdir /s /q ".gradle"
    echo 已清理.gradle目录
)
echo.

echo [3/4] 测试网络连接...
echo 测试阿里云镜像连接...
curl -s --connect-timeout 10 "https://maven.aliyun.com/repository/google" >nul
if %errorlevel% equ 0 (
    echo ✓ 阿里云镜像连接正常
) else (
    echo ✗ 阿里云镜像连接失败
)

echo 测试Google Maven仓库...
curl -s --connect-timeout 10 "https://dl.google.com/dl/android/maven2/" >nul
if %errorlevel% equ 0 (
    echo ✓ Google Maven连接正常
) else (
    echo ✗ Google Maven连接失败（可能需要代理）
)
echo.

echo [4/4] 开始Gradle同步和构建...
echo 正在执行: gradlew.bat assembleDebug
echo.

call gradlew.bat assembleDebug --stacktrace --info

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo ✓ 构建成功！
    echo APK文件位置: app\build\outputs\apk\debug\app-debug.apk
    echo ========================================
    echo.
    echo 你可以将APK文件传输到手机上安装运行。
    echo 或者使用ADB命令安装: adb install app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo ========================================
    echo ✗ 构建失败
    echo 请查看上方的错误信息
    echo ========================================
)

echo.
pause
