# Ultimate.TV Compose release rules (ported from legacy :app/proguard-rules.pro)

# Same R8 mode as legacy :app (proven in Play).
-dontoptimize

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

# LibVLC (JNI)
-keep class org.videolan.libvlc.** { *; }
-dontwarn org.videolan.libvlc.**

# Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Cast: OptionsProvider is looked up by name from manifest meta-data.
-keep class * implements com.google.android.gms.cast.framework.OptionsProvider { *; }

# Parcelable / Serializable models
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements java.io.Serializable { *; }
