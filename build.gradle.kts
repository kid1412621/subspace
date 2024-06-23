// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // https://developer.android.com/build/releases/gradle-plugin
    id("com.android.application") version "8.5.0" apply false
    // https://plugins.gradle.org/plugin/org.jetbrains.kotlin.android
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    // https://developer.android.com/develop/ui/compose/compiler#set-gradle
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    // https://github.com/google/ksp/releases
    id("com.google.devtools.ksp") version "2.0.0-1.0.22" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}
