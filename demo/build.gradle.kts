plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.diegodev.apidesportes.demo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.diegodev.apidesportes.demo"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":app"))
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
}
