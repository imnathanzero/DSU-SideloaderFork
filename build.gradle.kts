buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath(Google.dagger.hilt.android.gradlePlugin)
        classpath("org.jetbrains.kotlin:kotlin-serialization:_")
    }
}

plugins {
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("com.google.devtools.ksp") apply false

    id("org.jetbrains.kotlin.multiplatform") apply false
    id("org.jetbrains.kotlin.plugin.serialization") apply false
    id("org.jetbrains.kotlin.plugin.compose") apply false

    id("com.mikepenz.aboutlibraries.plugin") apply false
    id("org.jmailen.kotlinter") apply false
}

val versionCode by extra { 8 }
val versionName by extra { "2.03" }
val packageName by extra { "vegabobo.dsusideloader" }

tasks.register("clean") {
    delete(layout.buildDirectory)
}
