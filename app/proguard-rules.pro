# Add project specific ProGuard rules here.
# Preserve line number information for debugging crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Room Database, DAOs, and Entities
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.data.local.dao.** { *; }
-keep interface com.example.data.local.dao.** { *; }
-dontwarn androidx.room.paging.**

# Keep Moshi models and adapters
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep class * {
    @com.squareup.moshi.JsonClass *;
}
-dontwarn com.squareup.moshi.**

# Keep ViewModels and Domain Use Cases
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class com.example.domain.usecase.** { *; }
-keep class com.example.domain.model.** { *; }
-keep class com.example.ui.viewmodel.** { *; }

# Keep FileProvider
-keep class androidx.core.content.FileProvider { *; }

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Suppress warnings for desktop JVM classes referenced by Ktor
-dontwarn java.lang.management.**
-dontwarn io.ktor.**


