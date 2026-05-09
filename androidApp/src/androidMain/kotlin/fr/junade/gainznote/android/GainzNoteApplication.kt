package fr.junade.gainznote.android

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import fr.junade.gainznote.BuildConfig

class GainzNoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            FirebaseCrashlytics.getInstance().apply {
                isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
                log("GainzNoteApplication.onCreate")
            }
        } catch (e: Exception) {
            Log.e("GainzNote", "Firebase init failed: ${e.message}", e)
        }
    }
}
