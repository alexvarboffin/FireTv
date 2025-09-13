pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.aliyun.com/nexus/content/repositories/releases")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
    }
}

rootProject.name = "Fire Tv"

include(":app")
include(":features:ui")
include(":simplesearchview")
include(":data")
include(":xtream")


include(":shared")
project(":shared").projectDir = File("C:\\src\\Synced\\WalhallaUI\\shared")
include(":features:ui")
project(":features:ui").projectDir = file("C:\\src\\Synced\\WalhallaUI\\features\\ui")
