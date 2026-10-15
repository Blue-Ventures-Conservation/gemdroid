# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

## Google Maps Compose Keep Rules
#########################################
## Jetpack Compose
#########################################
#-keep class androidx.compose.runtime.** { *; }
#-keep class androidx.compose.ui.** { *; }
#-keep class androidx.compose.material3.** { *; }
#-dontwarn androidx.compose.**
#
#########################################
## ViewModels
#########################################
#-keep class * extends androidx.lifecycle.ViewModel { *; }
#-keepclassmembers class * extends androidx.lifecycle.ViewModel { <init>(...); }
#
#########################################
## Hilt / Dagger (minimal)
#########################################
#-keep class dagger.hilt.** { *; }
#-dontwarn dagger.hilt.internal.**
#-dontwarn javax.inject.**
#
#########################################
## Retrofit & Moshi
#########################################
#-keep interface retrofit2.** { *; }
#-dontwarn retrofit2.**
#
#
#-dontwarn com.squareup.moshi.**
#-keep class com.squareup.moshi.** { *; }
#-keepclassmembers class com.squareup.moshi.** { *; }
## Keep all Kotlin data classes used by Moshi
#-keep class **JsonAdapter { *; }
#-keep class **JsonAdapter$* { *; }
#-keep class * implements com.squareup.moshi.JsonAdapter { *; }
## Auto-value & code gen
#-keep class **_JsonAdapter { *; }
#
## Gson
#-keep class com.google.gson.** { *; }
#-dontwarn com.google.gson.**
#
## Retrofit suspend helpers
#-keep,allowobfuscation,allowshrinking interface retrofit2.Call
#-keep,allowobfuscation,allowshrinking class retrofit2.Response
#-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
#-keepattributes Signature
#
#########################################
## OkHttp / Okio
#########################################
#-dontwarn okhttp3.**
#-dontwarn okio.**
#
#########################################
## Firebase
#########################################
#-keep class com.google.firebase.** { *; }
#-dontwarn com.google.firebase.**
#
## Firebase Database - Keep model fields for reflection
#-keepclassmembers class com.brickyard.nhl.data.models.** {
#    <fields>;
#}
#-keep class com.brickyard.nhl.data.models.** { *; }
#
#########################################
## Google Mobile Ads (AdMob)
#########################################
#-keep class com.google.android.gms.ads.** { *; }
#-keep class com.google.android.gms.internal.ads.** { *; }
#-dontwarn com.google.android.gms.ads.**
#-dontwarn com.google.android.gms.internal.ads.**
#
## Google Play Services (recommended)
#-keep class com.google.android.gms.common.** { *; }
#-keep class com.google.android.gms.dynamite.** { *; }
#
#########################################
## Coil (image loading)
#########################################
#-dontwarn coil.**
#
#########################################
## Media3 / ExoPlayer
#########################################
#-dontwarn androidx.media3.**
#
#########################################
## Timber
#########################################
#-dontwarn timber.log.**
#
#########################################
## Parcelable (@Parcelize)
#########################################
#-keepclassmembers class * implements android.os.Parcelable { static ** CREATOR; }
#
#########################################
## Kotlin metadata
#########################################
#-keepclassmembers class kotlin.Metadata { *; }
#
#########################################
## Enums used in serialization
#########################################
#-keepclassmembers enum * { *; }
#
#########################################
## Room (required by WorkManager)
#########################################
#-keep class androidx.room.** { *; }
#-keep class androidx.sqlite.db.** { *; }
#-dontwarn androidx.room.**
#-dontwarn androidx.sqlite.db.**
#
#########################################
## WorkManager
#########################################
#-keep class androidx.work.** { *; }
#-dontwarn androidx.work.**

-keepclassmembers class org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader {
    private java.io.BufferedInputStream is;
}

-keepclassmembers class org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences {
    void advanceOneRecordNumber();
}