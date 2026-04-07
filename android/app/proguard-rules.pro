-keep class io.flutter.** { *; }
-keep class io.flutter.plugins.** { *; }

# Play Core / SplitCompat (referenced by Flutter engine for deferred components)
-dontwarn com.google.android.play.core.**
-keep class com.google.android.play.core.splitcompat.** { *; }
-keep class com.google.android.play.core.splitinstall.** { *; }
-keep class com.google.android.play.core.tasks.** { *; }
