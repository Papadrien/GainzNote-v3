package fr.junade.gainznote.android

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import fr.junade.gainznote.BuildConfig

/**
 * Point d'entrée de l'application — initialisé avant MainActivity.
 * Crashlytics est activé ici pour capturer les crashs dès le démarrage,
 * y compris ceux qui surviennent dans onCreate() de MainActivity.
 */
class GainzNoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Crashlytics en premier — avant toute autre init
        FirebaseCrashlytics.getInstance().apply {
            isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
            // Log le démarrage pour tracer les crashs au boot
            log("GainzNoteApplication.onCreate")
        }
    }
}
