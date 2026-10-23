// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // https://developer.android.com/build/releases/gradle-plugin
    alias(libs.plugins.android.application) apply false
    // https://plugins.gradle.org/plugin/org.jetbrains.kotlin.plugin.compose
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    // https://github.com/google/ksp/releases
    alias(libs.plugins.ksp) apply false
    // https://github.com/google/dagger/releases
    alias(libs.plugins.hilt.android) apply false
    // https://mvnrepository.com/artifact/com.google.gms/google-services
    alias(libs.plugins.google.services) apply false
    // https://mvnrepository.com/artifact/com.google.firebase/firebase-crashlytics-gradle
    alias(libs.plugins.firebase.crashlytics) apply false
}
