// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // https://developer.android.com/build/releases/gradle-plugin
    id("com.android.application") version "9.4.1" apply false
    // https://plugins.gradle.org/plugin/org.jetbrains.kotlin.plugin.compose
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
    // https://github.com/google/ksp/releases
    id("com.google.devtools.ksp") version "2.3.12" apply false
    // https://github.com/google/dagger/releases
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
    // https://mvnrepository.com/artifact/com.google.gms/google-services
    id("com.google.gms.google-services") version "4.5.0" apply false
    // https://mvnrepository.com/artifact/com.google.firebase/firebase-crashlytics-gradle
    id("com.google.firebase.crashlytics") version "3.0.8" apply false
}
