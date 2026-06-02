# Project specific ProGuard rules

# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>(...);
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Koin
-keep class org.koin.** { *; }

# Jsoup
-keep class org.jsoup.** { *; }

# Keep Compose internal members
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
}

# Keep DataStore/Protobuf if used (you use Preferences, but good to have)
-keepclassmembers class * extends androidx.datastore.preferences.core.MutablePreferences {
    *;
}

# General optimizations
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
