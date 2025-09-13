import java.text.SimpleDateFormat
import java.util.Date

fun versionCodeDate(): Int {
    return SimpleDateFormat("yyMMdd").format(Date()).toInt()
}
plugins {
    id("com.android.application")

    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}


//com.gdevs.firetvappbygdevelopers
//com.gdevs.firetv
//tv.hdonlinetv.bestchannels

android {
    namespace = "tv.hdonlinetv.besttvchannels.movies.watchfree"

    compileSdk = 36

    val code = versionCodeDate()

    defaultConfig {
        applicationId = "tv.hdonlinetv.besttvchannels.movies.watchfree"

        resConfigs("en", "es", "fr", "de", "it", "pt", "el", "ru", "ja", "zh-rCN", "zh-rTW", "ko", "ar", "uk", "vi", "uz", "az")
        //resConfigs "ru"

        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = code
        versionName = "1.4.$code"

        //testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        setProperty("archivesBaseName", "iptv")
    }

    signingConfigs {
        create("x") {
            keyAlias = "release"
            keyPassword = "release"
            storeFile = file("keystore/keystore.jks")
            storePassword = "release"
        }
    }

    buildTypes {
        getByName("debug") {
            //multiDexEnabled true
            //minifyEnabled true
            isShrinkResources = false
            //proguardFiles getDefaultProguardFile('proguard-android.txt'), "proguard-rules.pro"
            //proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            proguardFiles(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("x")
            versionNameSuffix = ".DEMO"
            resValue("string", "app_name", "1 APP")
        }

        getByName("release") {
            multiDexEnabled = true
            isMinifyEnabled = true
            isShrinkResources = true
            //proguardFiles getDefaultProguardFile('proguard-android.txt'), "proguard-rules.pro"
            //proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            proguardFiles(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
            )
            //debuggable false
            //jniDebuggable false
            signingConfig = signingConfigs.getByName("x")
            versionNameSuffix = ".release"
            resValue("string", "app_name", "Ultimate.TV")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

//============================================================
tasks.register<Copy>("copyAabToBuildFolder") {
    println("mmmmmmmmmmmmmmmmm ${layout.buildDirectory.get()}/outputs/bundle/release")
    println("mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm")
    val outputDirectory = file("C:/build")
    if (!outputDirectory.exists()) {
        outputDirectory.mkdirs()
    }

    from("${layout.buildDirectory.get()}/outputs/bundle/release") {
        include("*.aab")
    }
    into(outputDirectory)
}

apply(from = "C:\\scripts/copyReports.gradle")

//============================================================
dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.multidex:multidex:2.0.1")

    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.3")
    implementation("androidx.navigation:navigation-fragment:2.9.3")
    implementation("androidx.navigation:navigation-ui:2.9.3")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.8.0")
    implementation("androidx.media3:media3-cast:1.8.0")
    implementation(project(":xtream"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    //firebase
    implementation("com.google.firebase:firebase-analytics:23.0.0")
//    implementation 'com.google.firebase:firebase-storage:21.0.0'
//    implementation 'com.google.firebase:firebase-database:21.0.0'

    //images
    implementation("com.github.bumptech.glide:glide:5.0.5")
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.5")
    implementation("com.github.bumptech.glide:okhttp3-integration:5.0.5") {
        exclude(group = "glide-parent")
    }

    //player
    //implementation 'cn.jzvd:jiaozivideoplayer:6.2.9'
    //noinspection GradleDependency

    //implementation 'cn.jzvd:jiaozivideoplayer:7.0.5'

    implementation("cn.jzvd:jiaozivideoplayer:7.7.2.3300")

    //Ad network
//    implementation 'com.applovin:applovin-sdk:10.3.3'
//    implementation 'com.facebook.android:audience-network-sdk:6.8.0'

    //Ad network mediation
//    implementation 'com.google.ads.mediation:facebook:6.17.0.0'
//    implementation 'com.applovin.mediation:google-adapter:20.3.0.0'
//    implementation 'com.applovin.mediation:facebook-adapter:6.17.0.0'

//  DEPRECATED  implementation 'com.google.android.ads.consent:consent-library:1.0.8'

    implementation("com.google.android.gms:play-services-ads:24.5.0")
    implementation("com.google.firebase:firebase-ads:23.6.0")

    //implementation 'com.github.Ferfalk:SimpleSearchView:0.2.0'
    implementation(project(":simplesearchview"))
    //implementation("com.iheartradio.m3u8:open-m3u8:0.2.4")
    //@@@@implementation("com.github.bjoernpetersen:m3u-parser:1.4.0")

    //implementation 'com.android.volley:volley:1.2.1'

    implementation("com.google.firebase:firebase-messaging:25.0.0")
    implementation("com.onesignal:OneSignal:5.1.37")

    implementation(project(":features:ui"))
    implementation(project(":data"))

    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.1.0"))
    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    implementation("com.github.rahimlis:badgedtablayout:v1.2")
    implementation("com.google.code.gson:gson:2.13.2")

    implementation(libs.lottie)

    //ext
//    implementation 'com.aliyun.sdk.android:AliyunPlayer:4.5.0-full'
//    implementation 'com.alivc.conan:AlivcConan:0.9.5'

    //deprecated
    //implementation 'com.google.android.exoplayer:exoplayer:2.19.1'

    //migration
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.datasource.okhttp)  // Если вы используете OkHttp
    implementation(libs.androidx.media3.extractor)  // Для поддержки разных форматов

    //0.8.4'
    implementation("tv.danmaku.ijk.media:ijkplayer-java:0.8.8")
    implementation("tv.danmaku.ijk.media:ijkplayer-armv7a:0.8.8")

    // Other ABIs: optional
    implementation("tv.danmaku.ijk.media:ijkplayer-armv5:0.8.8")
    implementation("tv.danmaku.ijk.media:ijkplayer-arm64:0.8.8")
    implementation("tv.danmaku.ijk.media:ijkplayer-x86:0.8.8")
    implementation("tv.danmaku.ijk.media:ijkplayer-x86_64:0.8.8")

    implementation(libs.androidx.preference.ktx)

    //onboard
    implementation("com.merhold.extensiblepageindicator:extensiblepageindicator:1.0.1") {
        exclude(group = "com.google.android.gms")
        //exclude group: 'com.squareup.okhttp3'
        //exclude group: 'com.google.android.gms', module: 'play-services-basement'
    }

    //Splash Anim
    implementation("com.daimajia.easing:library:2.4@aar")
    implementation("com.daimajia.androidanimations:library:2.4@aar")

    //CHROMECAST или Smart TV
    implementation(libs.androidx.mediarouter)
    implementation(libs.play.services.cast.framework)
}


apply(plugin = "com.google.gms.google-services")