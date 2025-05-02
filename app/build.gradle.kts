import com.android.build.api.dsl.ApkSigningConfig
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "me.nanova.subspace"
    compileSdk = 35

    defaultConfig {
        applicationId = "me.nanova.subspace"
        minSdk = 29
        targetSdk = 35
        versionCode = 13
        versionName = "0.6.0"
        setProperty("archivesBaseName", "subspace-v${versionName}-${versionCode}")

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
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
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
    implementation("androidx.core:core-ktx:1.16.0")

    // https://developer.android.com/jetpack/compose/bom/bom-mapping
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.lifecycle:lifecycle-runtime-compose")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
//    ksp("androidx.lifecycle:lifecycle-compiler")
    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // paging
    implementation("androidx.paging:paging-compose:3.3.6")
    implementation("androidx.paging:paging-runtime-ktx:3.3.6")

    // nav
    implementation("androidx.navigation:navigation-compose")
    implementation("androidx.navigation:navigation-runtime-ktx:2.8.9")

    // Material Design 3
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.animation:animation-graphics")
    // https://developer.android.com/jetpack/androidx/releases/compose-material3-adaptive
//    implementation("androidx.compose.material3.adaptive:adaptive:1.0.0")
//    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.0.0")
//    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.0.0")
//    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.3.1")

    // datastore
    implementation("androidx.datastore:datastore-preferences:1.1.5")

    // hilt
    implementation("com.google.dagger:hilt-android:2.56.2")
    ksp("com.google.dagger:hilt-android-compiler:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // room
    implementation("androidx.room:room-runtime:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")
    implementation("androidx.room:room-paging:2.7.1")
    ksp("androidx.room:room-compiler:2.7.1")

    // retrofit
    implementation(platform("com.squareup.retrofit2:retrofit-bom:2.11.0"))
    implementation("com.squareup.retrofit2:retrofit")
    implementation("com.squareup.retrofit2:converter-scalars")
    implementation("com.squareup.retrofit2:converter-moshi")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // firebase
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")


    androidTestImplementation(platform("androidx.compose:compose-bom:2025.04.01"))
    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
