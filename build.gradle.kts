// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // https://developer.android.com/build/releases/gradle-plugin
    id("com.android.application") version "8.10.0" apply false
    // https://plugins.gradle.org/plugin/org.jetbrains.kotlin.android
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
    // https://plugins.gradle.org/plugin/org.jetbrains.kotlin.plugin.compose
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20" apply false
    // https://github.com/google/ksp/releases
    id("com.google.devtools.ksp") version "2.1.20-1.0.32" apply false
    // https://github.com/google/dagger/releases
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
    // https://mvnrepository.com/artifact/com.google.gms/google-services
    id("com.google.gms.google-services") version "4.4.2" apply false
    // https://mvnrepository.com/artifact/com.google.firebase/firebase-crashlytics-gradle
    id("com.google.firebase.crashlytics") version "3.0.3" apply false
}
