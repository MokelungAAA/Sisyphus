pluginManagement {
    repositories {
        // 使用阿里云镜像加速（国内）- 优先使用
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // 保留原始仓库作为备用（如果镜像失败）
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 使用阿里云镜像加速（国内）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        // 保留原始仓库作为备用
        google()
        mavenCentral()
    }
}

rootProject.name = "Sisyphus"
include(":app")
include(":core")
include(":feature:home")
include(":feature:subject")
include(":feature:pomodoro")
include(":feature:entry")
include(":feature:review")
include(":feature:exam")
include(":feature:reading")
include(":feature:search")
include(":feature:settings")
include(":feature:achievement")
include(":feature:stats")
include(":feature:skilltree")
include(":feature:nlp")
include(":feature:recommendation")
include(":feature:sync")
