# Règles ProGuard spécifiques à GainzNote

# ── Google AdMob ───────────────────────────────────────────────────────────────
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**
