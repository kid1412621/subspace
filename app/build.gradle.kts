plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // need migrate to ksp
    kotlin("kapt")
}

android {
    namespace = "me.nanova.subspace"
    compileSdk = 34

    defaultConfig {
        applicationId = "me.nanova.subspace"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        // https://developer.android.com/jetpack/androidx/releases/compose-kotlin#pre-release_kotlin_compatibility
        kotlinCompilerExtensionVersion = "1.5.7"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.window:window:1.2.0")

    // https://developer.android.com/jetpack/compose/bom/bom-mapping
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose")
    implementation("androidx.lifecycle:lifecycle-runtime-compose")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
    implementation("androidx.compose.runtime:runtime-livedata")
//    implementation("androidx.navigation:navigation-compose")
//    kapt("androidx.lifecycle:lifecycle-compiler")
    // Material Design 3 (TODO: specify the version to use PullToRefresh)
    implementation("androidx.compose.material3:material3:1.2.0-beta01")
    implementation("androidx.compose.material3:material3-window-size-class")
    // not ready yet
//    implementation("androidx.compose.material3:material3-adaptive:1.0.0-alpha03")
//    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.0.0-alpha02")
    // wait for MD3 implementation
    implementation("androidx.compose.material:material-icons-extended")

    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // TODO: use retrofit bom: https://github.com/square/retrofit/issues/3231
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")


    androidTestImplementation(composeBom)
    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
