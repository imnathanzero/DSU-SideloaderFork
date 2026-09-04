import java.util.Properties

fun getReleaseSigningConfig(): File {
    return File(".sign/dsu_sideloader.prop")
}

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dagger.hilt.android.plugin")
    id("com.mikepenz.aboutlibraries.plugin")
    id("kotlinx-serialization")
    id("org.jmailen.kotlinter")
}

android {
    namespace = "vegabobo.dsusideloader"
    compileSdk = 37

    defaultConfig {
        applicationId = "vegabobo.dsusideloader"
        minSdk = 29
        targetSdk = 33
        versionCode = project.extra["versionCode"] as Int
        versionName = project.extra["versionName"] as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val propertiesFile = getReleaseSigningConfig()
            if (propertiesFile.exists()) {
                val properties = Properties().apply {
                    propertiesFile.inputStream().use { load(it) }
                }
                signingConfigs.create("release") {
                    storeFile = file(properties.getProperty("storeFile"))
                    storePassword = properties.getProperty("storePassword")
                    keyAlias = properties.getProperty("keyAlias")
                    keyPassword = properties.getProperty("keyPassword")
                }
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    flavorDimensions += "variant"

    productFlavors {
        create("mini") {
            dimension = "variant"
        }
        create("full") {
            dimension = "variant"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.compose.ui:ui:1.12.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.12.0")
    implementation("androidx.compose.runtime:runtime:1.12.0")
    implementation("androidx.compose.material:material:1.9.0")
    implementation("androidx.compose.material3:material3:1.5.0-alpha27")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    implementation("com.google.dagger:hilt-android:_")
    implementation("androidx.hilt:hilt-navigation-compose:1.4.0")
    ksp("com.google.dagger:hilt-compiler:2.60.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.tukaani:xz:_")
    implementation("org.apache.commons:commons-compress:_")

    implementation("com.mikepenz:aboutlibraries-core:_")

    implementation("dev.rikka.shizuku:api:_")
    implementation("dev.rikka.shizuku:provider:_")

    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:_")

    compileOnly(project(":hidden-api-stub"))

    testImplementation("junit:junit:4.13.2")
}

tasks {
    "preBuild" {
        dependsOn("lintKotlin")
    }
    "lintKotlin" {
        dependsOn("formatKotlin")
    }
}
