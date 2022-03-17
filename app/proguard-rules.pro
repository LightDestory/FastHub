-keepattributes SourceFile,LineNumberTable
-dontobfuscate
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-keepattributes *Annotation*,Signature

-keepclassmembers class com.fastaccess.** { *; }
-keep class com.fastaccess.** { *; }

-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE