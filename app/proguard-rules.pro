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

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
#  -keep class hilt_aggregated_deps.** { *; }


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keep class com.google.gson.** { *; }
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class me.nanova.subspace.domain.model.** { *; }
##---------------End: proguard configuration for Gson  ----------

##---------------Begin: proguard configuration for OkHttp  ----------
# Don't warn on unused classes.
# See: https://github.com/square/okhttp/issues/6258
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.*
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
##---------------End: proguard configuration for OkHttp  ----------

# ---------------Begin: proguard configuration for Jetpack DataStore Preferences ----------
# Keep the classes and members that DataStore Preferences relies on for Protocol Buffer serialization.
# This is crucial for preventing crashes like "Field preferences_ not found".

# Keep the main DataStore Preferences classes
-keep class androidx.datastore.preferences.core.** { *; }
-keep class androidx.datastore.preferences.** { *; }

# Keep Protocol Buffer Lite runtime classes, which DataStore Preferences uses internally.
-keep class androidx.datastore.preferences.protobuf.** { *; }

# Specifically, keep the generated PreferencesProto classes and their members.
# The error often relates to PreferencesProto$PreferenceMap and its 'preferences_' field.
-keep class androidx.datastore.preferences.PreferencesProto* {
    <fields>;
    <methods>;
}

# Keep all classes that extend GeneratedMessageLite, as these are part of the protobuf mechanism.
-keep public class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
    <methods>;
}

# If using custom serializers or types with DataStore,
# ensure those are kept as well. For example:
# -keep class your.package.YourCustomPreferenceClass { *; }
##---------------End: proguard configuration for Jetpack DataStore Preferences ----------