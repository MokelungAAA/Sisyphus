pluginManagement {
    repositories {
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
