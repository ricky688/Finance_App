# ProGuard / R8 Rules for VibeFinance

# Keep WorkManager classes & reflection constructors
-keep class androidx.work.impl.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# Keep AndroidX Startup Initializers
-keep class androidx.startup.** { *; }

# Keep Room Generated Database Implementations (including WorkDatabase_Impl)
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class **.*_Impl {
    public <init>();
    *;
}

# Keep Jetpack Compose & Material 3 rules
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep Kotlin Serialization / Reflection Attributes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep VibeFinance Data Models & Database Entities
-keep class com.example.vibefinance.data.model.** { *; }
-keep class com.example.vibefinance.ui.** { *; }
