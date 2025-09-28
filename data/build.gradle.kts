plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.walhalla.data"
    compileSdk = 36

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

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
    kotlinOptions {
        jvmTarget = "17"
    }
}
//room {
//    schemaDirectory("$projectDir/schemas")
//}
dependencies {
    implementation(project(":features:ui"))
    //favorite
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.core.ktx)
    //annotationProcessor(libs.androidx.room.compiler)

    implementation(libs.androidx.room.runtime) // Библиотека "Room"
    kapt("androidx.room:room-compiler:2.8.0") // Кодогенератор
    implementation(libs.room.ktx) // Дополнительно для Kotlin Coroutines, Kotlin Flows

}