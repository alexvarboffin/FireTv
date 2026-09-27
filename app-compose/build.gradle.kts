import java.text.SimpleDateFormat
import java.util.Date

fun versionCodeDate(): Int {
    // yyMMddHH so same-day Play hotfixes (e.g. R8) can bump without waiting until tomorrow.
    return SimpleDateFormat("yyMMddHH").format(Date()).toInt()
}

plugins {
    id("com.android.application")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

base {
    archivesName.set("iptv")
}

android {
    // R / source package may stay compose.*; Play identity = applicationId (same as legacy).
    namespace = "tv.hdonlinetv.compose"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    val code = versionCodeDate()

    defaultConfig {
        // Play replacement: MUST match legacy :app
        applicationId = "tv.hdonlinetv.besttvchannels.movies.watchfree"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = code
        versionName = "1.4.$code"
    }

    signingConfigs {
        create("x") {
            keyAlias = "release"
            keyPassword = "release"
            storeFile = file("../app/keystore/keystore.jks")
            storePassword = "release"
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("x")
            versionNameSuffix = ".DEMO"
            resValue("string", "app_name", "1 APP")
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("x")
            versionNameSuffix = ".release"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            resValue("string", "app_name", "Ultimate.TV")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
}

dependencies {
    implementation(project(":common-resources"))
    implementation(project(":core:presentation"))
    implementation(project(":core:domain"))
    implementation(project(":core:data-bridge"))
    implementation(project(":features:ui"))

    val bom = platform(libs.androidx.compose.bom)
    implementation(bom)
    androidTestImplementation(bom)

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.material.icons.extended.android)

    // AdMob banner + interstitial scaffold (same app id as legacy :app)
    implementation(libs.play.services.ads)
    // Firebase + OneSignal (phone init only; skipped on TV in ComposeApp)
    implementation(libs.google.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.onesignal)

    // Legacy JiaoZi player stack (same as :app)
    implementation(libs.jiaozivideoplayer)
    implementation(libs.aliyun.player)
    implementation(libs.alivc.conan)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.extractor)
    implementation(libs.androidx.media3.cast)
    implementation(libs.androidx.media3.datasource.okhttp)
    implementation(libs.okhttp)
    implementation(libs.libvlc.all)
    implementation(libs.androidx.mediarouter)
    implementation(libs.play.services.cast.framework)
    implementation(fileTree(mapOf("dir" to "../app/libs", "include" to listOf("*.jar", "*.aar"))))
    compileOnly("com.google.errorprone:error_prone_annotations:2.36.0")

    debugImplementation(libs.androidx.compose.ui.tooling)
}

// OneSignal 5.1.x wants firebase-messaging in [21, 23.4.99]; app uses 25.x — skip strict check.
googleServices {
    disableVersionCheck = true
}
