# Sisyphus App - 网络连接问题解决方案

## 问题描述
Gradle同步时无法连接到Google服务器，导致构建失败。

## 已应用的修复

### 1. 配置了国内镜像源
- ✅ 阿里云Maven镜像（Google、Central、Gradle Plugin）
- ✅ 腾讯云Gradle下载镜像

### 2. 网络优化配置
- ✅ 增加网络超时时间（30秒连接，60秒读取）
- ✅ 强制使用IPv4
- ✅ 配置HTTP/HTTPS超时

## 如果问题仍然存在

### 方案1: 配置代理（推荐）
如果你使用VPN或代理软件，编辑 `gradle.properties` 文件：

```properties
# 取消注释以下行并配置你的代理
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=7890
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=7890
```

常见代理端口：
- Clash: 7890
- V2Ray: 10809
- Shadowsocks: 1080

### 方案2: 使用Android Studio
1. 打开Android Studio
2. File → Settings → Appearance & Behavior → System Settings → HTTP Proxy
3. 配置你的代理设置
4. 重新同步项目

### 方案3: 离线模式
如果网络不稳定，可以尝试离线模式：
1. 在Android Studio中: File → Settings → Build → Gradle
2. 勾选 "Offline work"
3. 注意：需要先成功同步一次

### 方案4: 手动下载依赖
如果镜像源有问题，可以手动下载：
1. 访问 https://maven.aliyun.com
2. 搜索需要的依赖
3. 下载后放入本地Maven仓库

## 测试网络连接

运行测试脚本：
```bash
# Windows
run_app.bat

# 或手动测试
curl -v "https://maven.aliyun.com/repository/google"
curl -v "https://dl.google.com/dl/android/maven2/"
```

## 常见错误及解决

### 错误: "Could not resolve com.android.tools.build:gradle"
**解决**: 检查阿里云镜像是否可访问，或配置代理

### 错误: "Connection timed out"
**解决**:
1. 检查网络连接
2. 增加超时时间（已在gradle.properties中配置）
3. 使用代理

### 错误: "SSL handshake failed"
**解决**:
1. 更新Java版本
2. 检查系统时间是否正确
3. 临时禁用SSL验证（不推荐）

## 构建和运行

### 步骤1: 清理缓存
```bash
# 删除.gradle缓存目录
rm -rf .gradle

# 或Windows
rmdir /s /q .gradle
```

### 步骤2: 构建APK
```bash
# 调试版
./gradlew assembleDebug

# 或Windows
gradlew.bat assembleDebug
```

### 步骤3: 安装到手机
```bash
# 方法1: 使用ADB
adb install app/build/outputs/apk/debug/app-debug.apk

# 方法2: 直接传输APK到手机
# 文件位置: app/build/outputs/apk/debug/app-debug.apk
```

## 联系支持
如果以上方案都无法解决问题，请提供：
1. 完整的错误日志
2. 网络环境说明（是否使用代理）
3. 操作系统和Java版本
