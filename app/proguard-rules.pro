## https://github.com/krschultz/android-proguard-snippets/tree/master/libraries

## general
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
#-dontoptimize
#-dontpreverify
#-verbose

## support libs (to reduce build time)
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**

# play service
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }

## proguard
-dontwarn java.lang.invoke.*

## stream
-keep class com.annimon.** { *; }

## butternife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

## joda
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

## squareup (retrofit, picasso, ...)
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}