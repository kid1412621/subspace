import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "me.nanova.subspace"
    compileSdk = 34

    defaultConfig {
        applicationId = "me.nanova.subspace"
        minSdk = 29
        targetSdk = 34
        versionCode = 8
        versionName = "0.1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // skip for tes
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                signingConfigs {
                    create("releaseConfig") {
                        storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                        storePassword = keystoreProperties["storePassword"] as String
                        keyAlias = keystoreProperties["keyAlias"] as String
                        keyPassword = keystoreProperties["keyPassword"] as String
                    }
                }
                signingConfig = signingConfigs.getByName("releaseConfig")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
    composeOptions {
        // https://developer.android.com/jetpack/androidx/releases/compose-kotlin#pre-release_kotlin_compatibility
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.0")

    // https://developer.android.com/jetpack/compose/bom/bom-mapping
    val composeBom = platform("androidx.compose:compose-bom:2024.04.01")
    implementation(composeBom)
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.lifecycle:lifecycle-runtime-compose")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
//    ksp("androidx.lifecycle:lifecycle-compiler")
    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // paging
    implementation("androidx.paging:paging-compose:3.3.0-beta01")
    implementation("androidx.paging:paging-runtime-ktx:3.3.0-beta01")

    // nav
    implementation("androidx.navigation:navigation-compose")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.7")

    // Material Design 3
//    implementation("androidx.compose.material3:material3")
    // wait for https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#DismissibleDrawerSheet(androidx.compose.material3.DrawerState,androidx.compose.ui.Modifier,androidx.compose.ui.graphics.Shape,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.unit.Dp,androidx.compose.foundation.layout.WindowInsets,kotlin.Function1)
    // see: https://developer.android.com/jetpack/androidx/releases/compose-material3#1.3.0-alpha04
    implementation("androidx.compose.material3:material3:1.3.0-alpha05")
    // wait for MD3 implementation
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation:1.6.6")
    implementation("androidx.compose.animation:animation-graphics")
    // not ready yet
//    implementation("androidx.compose.material3:material3-adaptive:1.0.0-alpha08")
//    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.0.0-alpha05")

    // datastore
    implementation("androidx.datastore:datastore-preferences:1.1.0")

    // hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // retrofit
    implementation(platform("com.squareup.retrofit2:retrofit-bom:2.11.0"))
    implementation("com.squareup.retrofit2:retrofit")
    implementation("com.squareup.retrofit2:converter-scalars")
    implementation("com.squareup.retrofit2:converter-moshi")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // firebase
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")


    androidTestImplementation(composeBom)
    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

task("printVersion") {
    println("v" + android.defaultConfig.versionName + "(" + android.defaultConfig.versionCode + ")")
}
