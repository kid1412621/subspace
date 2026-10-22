import com.android.build.api.dsl.ApkSigningConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

android {
    namespace = "me.nanova.subspace"
    compileSdk = 37

    defaultConfig {
        applicationId = "me.nanova.subspace"
        minSdk = 29
        targetSdk = 37
        versionCode = 16
        versionName = "0.8.0"
        base.archivesName.set("subspace-v${versionName}-${versionCode}")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        val keystoreDir = "keystore"
        fun buildSignConfig(keyStoreFile: String, apkSigningConfig: ApkSigningConfig) {
            val keystorePropertiesFile = rootProject.file(keyStoreFile)
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                keystoreProperties.let {
                    apkSigningConfig.storeFile =
                        rootProject.file("${keystoreDir}/${it["storeFile"] as String}")
                    apkSigningConfig.storePassword = it["storePassword"] as String
                    apkSigningConfig.keyAlias = it["keyAlias"] as String
                    apkSigningConfig.keyPassword = it["keyPassword"] as String
                }
            }
        }
        create("github") {
            buildSignConfig("${keystoreDir}/${name}.properties", this)
        }
        create("play") {
            buildSignConfig("${keystoreDir}/${name}.properties", this)
        }
    }

    flavorDimensions += listOf("distribution")
    productFlavors {
        create("github") {
            dimension = "distribution"
            signingConfig = signingConfigs.getByName(name)
        }
        create("play") {
            dimension = "distribution"
            signingConfig = signingConfigs.getByName(name)
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
}

androidComponents {
    val isCITest = System.getenv("CI_TEST")?.toBoolean() == true

    onVariants { variant ->
        // disable google service on non-prod build
        val googleTask =
            tasks.findByName("process${variant.name.replaceFirstChar(Char::uppercase)}GoogleServices")
        googleTask?.enabled = !isCITest && "release" == variant.buildType
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // https://developer.android.com/jetpack/compose/bom/bom-mapping
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
//    ksp("androidx.lifecycle:lifecycle-compiler")
    // Android Studio Preview support
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // paging
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.runtime.ktx)

    // nav
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)

    // Material Design 3
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.graphics)
    // https://developer.android.com/jetpack/androidx/releases/compose-material3-adaptive
//    implementation("androidx.compose.material3.adaptive:adaptive:1.0.0")
//    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.0.0")
//    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.0.0")
//    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.3.1")

    // datastore
    implementation(libs.androidx.datastore.preferences)

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    // retrofit
    implementation(platform(libs.retrofit.bom))
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp.logging.interceptor)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)


    androidTestImplementation(platform(libs.androidx.compose.bom))
    // UI Tests
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
