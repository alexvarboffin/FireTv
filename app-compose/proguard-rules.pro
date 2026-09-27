# Ultimate.TV Compose release rules (ported from legacy :app/proguard-rules.pro)
# Do NOT add -dontoptimize: Play Vitals requires DEX optimization ≥25% (was 0% with that flag).

# Aliyun / Ktor optional refs (AGP missing_rules.txt)
-dontwarn com.aliyun.aio.keep.API
-dontwarn com.aliyun.aio.keep.CalledByNative
-dontwarn com.aliyun.**
-dontwarn io.ktor.**
-keep class com.aliyun.** { *; }
-keep interface com.aliyun.** { *; }

# JZVD: media engines are created via reflection (Class<JZMediaInterface> + ctor(Jzvd)),
# fullscreen clones the player via ctor(Context).
-keep public class cn.jzvd.** { *; }
-keep class cn.jzvd.demo.CustomMedia.** { *; }
-keep class * extends cn.jzvd.JZMediaInterface { <init>(...); }
-keep class * extends cn.jzvd.Jzvd { <init>(...); }

# IJK (JNI): native lib registers Java methods by exact name — R8 must not rename/strip them.
# Without this, channel play aborts: NoSuchMethodError IjkMediaPlayer._setDataSourceFd(I)V
-keep class tv.danmaku.ijk.media.player.** { *; }
-keep interface tv.danmaku.ijk.media.player.** { *; }
-dontwarn tv.danmaku.ijk.media.player.**
-keepclasseswithmembernames class * {
    native <methods>;
}

# LibVLC (JNI)
-keep class org.videolan.libvlc.** { *; }
-dontwarn org.videolan.libvlc.**

# WorkManager (R8 full mode drops no-arg ctors used via reflection)
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.InputMerger
-keepclassmembers class * extends androidx.work.InputMerger {
    <init>();
}

# Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Cast: OptionsProvider is looked up by name from manifest meta-data.
-keep class * implements com.google.android.gms.cast.framework.OptionsProvider { *; }

# OneSignal / Firebase Messaging
-keep class com.onesignal.** { *; }
-dontwarn com.onesignal.**
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Parcelable / Serializable models
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements java.io.Serializable { *; }
