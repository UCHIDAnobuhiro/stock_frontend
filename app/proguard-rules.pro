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

# ============================================
# Retrofit / OkHttp
# ============================================
# Keep Retrofit interfaces and method annotations
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit service methods
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore Retrofit warnings
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# ============================================
# Kotlinx Serialization
# ============================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep serializers and model classes
-keep,includedescriptorclasses class com.example.stock.**$$serializer { *; }
-keepclassmembers class com.example.stock.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.stock.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# ============================================
# Hilt / Dagger
# ============================================
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# ============================================
# Coroutines
# ============================================
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }

# ============================================
# MPAndroidChart
# ============================================
-keep class com.github.mikephil.charting.** { *; }

# ============================================
# DataStore
# ============================================
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { *; }
