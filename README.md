# Sisyphus 学习目标规划系统

一款帮助高中生自主规划学习目标的应用。

## 项目概述

- **平台**: Android 10+ (API 29)
- **语言**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **架构**: Clean Architecture (app/core/feature)
- **DI**: Koin
- **数据库**: Room

## 技术栈

- Kotlin 2.0+
- Compose BOM 2024.12+
- Koin 3.5+
- Room 2.6+
- Navigation Compose 2.8+

## 项目结构

```
Sisyphus/
├── app/                    # 主应用模块
│   ├── src/main/
│   │   ├── java/          # Kotlin源码
│   │   └── res/           # 资源文件
│   └── build.gradle.kts
├── core/                   # 核心模块
│   └── src/main/java/
│       └── com/mokelab/sisyphus/core/
│           ├── database/  # Room数据库
│           ├── di/        # Koin依赖注入
│           └── ui/        # 主题、组件
├── feature/                # 功能模块
│   ├── home/              # 首页
│   ├── subject/           # 学科管理
│   ├── pomodoro/          # 番茄钟
│   ├── entry/             # 数据录入
│   ├── review/            # 复习
│   ├── exam/              # 考试
│   ├── reading/           # 阅读
│   ├── search/            # 搜索
│   ├── settings/          # 设置
│   ├── achievement/       # 成就
│   ├── stats/             # 数据图表
│   └── skilltree/         # 技能树
├── docs/                   # 文档
├── gradle/
│   └── libs.versions.toml # 版本目录
└── build.gradle.kts        # 根构建脚本
```

## 构建

1. 克隆项目
2. 使用Android Studio打开
3. 同步Gradle
4. 运行app模块

## 版本

当前版本: v1.0.0 (正式发布)

## 许可证

私人项目，版权所有
