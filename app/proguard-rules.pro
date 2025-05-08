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

# Gson과 관련된 클래스 보호
-keep class com.sumi.pockon.data.model.** { *; }
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-keepattributes *Annotation*

# Gson 역직렬화/직렬화 클래스 보호
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Room 엔티티 클래스 보호 / Gson 관련 직렬화/역직렬화 작업에서 사용되는 클래스 보호
-keep class com.sumi.pockon.data.local.gift.** { *; }
-keep class com.sumi.pockon.data.local.brand.** { *; }

# Room의 타입 변환기 보호
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <methods>;
    @com.google.gson.annotations.SerializedName <fields>;
}

# Serializable 인터페이스를 구현한 클래스 보호
-keep class * implements java.io.Serializable { *; }

# Firebase Firestore와 관련된 필드 보호
-keepattributes *Annotation*

# Retrofit 인터페이스와 모델 클래스 보호
-keep interface com.sumi.pockon.data.remote.brand.KaKaoSearchAPI { *; }
-keep class com.sumi.pockon.data.remote.brand.** { *; }

# Retrofit 자체 유지
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature

# OkHttp 보호 (Retrofit 내부에서 사용)
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# 콜백 관련 보호
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# 익명 클래스 보호 (Retrofit enqueue 등)
-keepclassmembers class * {
    void onResponse(...);
    void onFailure(...);
}

# Firebase Firestore / Realtime Database
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}
-keepclassmembers class * {
    @com.google.firebase.firestore.Exclude <fields>;
}

# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
-keepclassmembers class * extends androidx.room.Entity {
    *;
}
-keepclassmembers class * extends androidx.room.Dao {
    *;
}
-keepclassmembers class * {
    @androidx.room.* <methods>;
}