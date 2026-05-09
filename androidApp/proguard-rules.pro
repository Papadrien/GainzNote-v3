# Règles ProGuard spécifiques à GainzNote

# ── Google AdMob ───────────────────────────────────────────────────────────────
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# ── Google Play Billing ────────────────────────────────────────────────────────
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ── Kotlin Coroutines ──────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── SQLDelight ─────────────────────────────────────────────────────────────────
-keep class fr.junade.gainznote.db.** { *; }
-keep class app.cash.sqldelight.** { *; }
-dontwarn app.cash.sqldelight.**

# ── GainzNote — classes métier et BillingManager ──────────────────────────────
-keep class fr.junade.gainznote.** { *; }
-keepclassmembers class fr.junade.gainznote.** { *; }

# ── Kotlin (sérialisation, réflexion) ─────────────────────────────────────────
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Lazy {
    <fields>;
}

# ── Firebase Crashlytics ───────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
