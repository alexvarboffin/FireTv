# Ultimate.TV / FireTv release ProGuard rules

# From AGP missing_rules.txt (Aliyun / Ktor optional refs)
-dontwarn com.aliyun.aio.keep.API
-dontwarn com.aliyun.aio.keep.CalledByNative
-dontwarn io.ktor.util.CharsetKt
-dontwarn io.ktor.util.StringValues
-dontwarn io.ktor.util.reflect.TypeInfo
-dontwarn io.ktor.utils.io.JvmSerializable_jvmKt
-dontwarn io.ktor.utils.io.JvmSerializer
-dontwarn io.ktor.utils.io.KtorDsl
-dontwarn io.ktor.websocket.Frame
-dontwarn io.ktor.**
-dontwarn com.aliyun.**

-keep class com.aliyun.** { *; }
-keep interface com.aliyun.** { *; }

# JZVD / Glide / Media3
-keep public class cn.jzvd.** { *; }
-keep public class cn.jzvd.demo.CustomMedia.** { *; }
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Keep Parcelable / Serializable models
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements java.io.Serializable { *; }
