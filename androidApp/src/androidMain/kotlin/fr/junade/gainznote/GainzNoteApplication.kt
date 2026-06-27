package fr.junade.gainznote

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics

class GainzNoteApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialiser Firebase Analytics et logger le démarrage de l'app
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        AnalyticsHelper.init(firebaseAnalytics)
        AnalyticsHelper.logAppOpen()
    }
}
