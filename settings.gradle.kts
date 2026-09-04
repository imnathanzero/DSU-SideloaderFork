pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.legacy-kapt") {
                useModule(
                    "com.android.legacy-kapt:com.android.legacy-kapt.gradle.plugin:${requested.version}"
                )
            }
        }
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.60.6"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }
}
rootProject.name = "DSU Helper"
include(":app", ":hidden-api-stub", ":magisk-module")
