pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("com.android.library") version "8.1.2"
        id("org.jetbrains.kotlin.android") version "1.9.10"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ApideSportes"
include(":app")
include(":demo")
