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

# Preserve the generic signature of Retrofit Call for R8 full mode.
-keep,allowobfuscation,allowshrinking interface retrofit2.Call

-keepattributes Signature
-keepattributes *Annotation*

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

# qBittorrent response models are decoded by Moshi reflection.
-keep class me.nanova.subspace.domain.model.QBCategory { *; }
-keep class me.nanova.subspace.data.api.dto.TorrentDto { *; }
-keep class me.nanova.subspace.data.api.dto.TorrentPropertiesDto { *; }
-keep class me.nanova.subspace.data.api.dto.TorrentFileDto { *; }
-keep class me.nanova.subspace.data.api.dto.TorrentTrackerDto { *; }
-keep class me.nanova.subspace.data.api.dto.TorrentPeersResponseDto { *; }
-keep class me.nanova.subspace.data.api.dto.TorrentPeerDto { *; }
-keep class me.nanova.subspace.data.api.dto.TransferInfoDto { *; }
