plugins {
    id("com.android.library")
    id("kotlin-android")
    //apply plugin: 'com.github.dcendents.android-maven'
}

android {
    namespace = "com.ferfalk.simplesearchview"

    compileSdk = 36

    defaultConfig {
        minSdk = 21
        targetSdk = 36
        //versionCode = 20
        //versionName = "0.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.kotlin.stdlib.jdk7)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)

//    testImplementation 'junit:junit:4.13'
//    androidTestImplementation 'androidx.test:runner:1.3.0'
//    androidTestImplementation 'androidx.test.espresso:espreso-core:3.3.0'
}

//task sourcesJar(type: Jar) {
//    from android.sourceSets.main.java.srcDirs
//    getArchiveClassifier().value('sources')
//}
//
//task javadoc(type: Javadoc) {
//    failOnError  false
//    source = android.sourceSets.main.java.sourceFiles
//    classpath += project.files(android.getBootClasspath().join(File.pathSeparator))
//    classpath += configurations.compile
//}

//task javadocJar(type: Jar, dependsOn: javadoc) {
//    getArchiveClassifier().value('javadoc')
//    from javadoc.destinationDir
//}
//
//artifacts {
//    archives sourcesJar
//    archives javadocJar
//}
//repositories {
//    mavenCentral()
//}