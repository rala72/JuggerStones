## https://github.com/krschultz/android-proguard-snippets/tree/master/libraries

## general
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
#-dontoptimize
#-dontpreverify
#-verbose

## support libs (to reduce build time)
-keep class android.support.** { *; }
-keep interface android.support.** { *; }
-dontwarn android.support.**

# play service
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }

## stream
-keep class com.annimon.** { *; }
-dontwarn java.lang.invoke.**

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
-keep class org.joda.** { *; }
-keep interface org.joda.** { *; }
-dontwarn org.joda.convert.**

## squareup (retrofit, picasso, ...)
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-dontnote retrofit2.Platform
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
-dontwarn retrofit2.Platform$Java8
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.Platform$Java8

## support-preference and materialdatetimepicker
-keep class net.xpece.android.** { *; }
-keep class com.wdullaer.** { *; }