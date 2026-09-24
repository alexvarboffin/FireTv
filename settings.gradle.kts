enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {

        flatDir {
            dirs("C:/libs")
        }

        mavenLocal()
        mavenCentral()  // Primary repository for dependencies
        google()        // Required for Android-specific dependencies
        gradlePluginPortal()  // Access to Gradle plugins

//        google {
//            mavenContent {
//                includeGroupAndSubgroups("androidx")
//                includeGroupAndSubgroups("com.android")
//                includeGroupAndSubgroups("com.google")
//            }
//        }
        maven("https://maven.google.com")
        // Google Maven blocked/404 locally — Aliyun Google mirror works
        maven("https://maven.aliyun.com/repository/google")
        maven("https://dl.bintray.com/videolan/Android")
        // aliyun releases: 502 Bad Gateway breaks plugin resolution (google-services)
        // maven {
        //     url = uri("https://maven.aliyun.com/nexus/content/repositories/releases")
        // }
    }
}

dependencyResolutionManagement {
    //repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        flatDir {
            dirs("C:/libs")
        }
        // Prefer Aliyun Google: dl.google.com returns 404 for androidx from this network
        maven("https://maven.aliyun.com/repository/google")
        google()
//        google {
//            mavenContent {
//                includeGroupAndSubgroups("androidx")
//                includeGroupAndSubgroups("com.android")
//                includeGroupAndSubgroups("com.google")
//            }
//        }
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo1.maven.org/maven2/")
        maven("https://androidx.dev/storage/compose-compiler/repository/")

        maven("https://maven.google.com")
        maven("https://dl.bintray.com/videolan/Android")
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        // ApsaraVideo / AliyunPlayer SDK (official docs)
        maven("https://maven.aliyun.com/repository/releases")
    }
}

rootProject.name = "FireTv"
include(":app")
include(":features:ui")
include(":simplesearchview")
include(":data")
include(":xtream")
include(":shared")
project(":shared").projectDir = File("C:\\Synced\\WalhallaUI\\shared")
include(":features:ui")
project(":features:ui").projectDir = file("C:\\Synced\\WalhallaUI\\features\\ui")
include(":mylibrary")
include(":app-compose")
include(":common-resources")
include(":core:domain")
include(":core:presentation")
include(":core:data-bridge")
