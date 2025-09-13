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
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Retrofit с поддержкой Kotlin Serialization
    api("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    api("com.squareup.retrofit2:retrofit:3.0.0")
    api("com.squareup.retrofit2:converter-gson:3.0.0")

    // Core dependency for Ktor HTTP client
    implementation(libs.ktor.client.core)

    // Dependency for HTTP engine (for example, OkHttp)
    implementation(libs.ktor.client.okhttp)

    // Ktor serialization dependency (optional, if you need serialization)
    implementation(libs.ktor.client.serialization)

    // Dependency for Ktor HTTP
    implementation(libs.ktor.http)
}