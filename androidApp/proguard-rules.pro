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

# ── Firebase ──────────────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
# Empêche R8 de supprimer le ContentProvider d'auto-init Firebase
-keep class com.google.firebase.provider.FirebaseInitProvider { *; }
-keep class com.google.firebase.components.** { *; }
-keep class com.google.firebase.installations.** { *; }
