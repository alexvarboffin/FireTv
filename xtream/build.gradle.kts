plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    //id("kotlinx-serialization")
}

android {
    namespace = "com.walhalla.xtream"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Room Database
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)  // Для генерации кода

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)

    // Retrofit с поддержкой Kotlin Serialization
    api(libs.retrofit2.kotlinx.serialization.converter)
    api(libs.retrofit)
    api(libs.converter.gson)

    // Core dependency for Ktor HTTP client
    implementation(libs.ktor.client.core)

    // Dependency for HTTP engine (for example, OkHttp)
    implementation(libs.ktor.client.okhttp)

    // Ktor serialization dependency (optional, if you need serialization)
    implementation(libs.ktor.client.serialization)

    // Dependency for Ktor HTTP
    implementation(libs.ktor.http)
}