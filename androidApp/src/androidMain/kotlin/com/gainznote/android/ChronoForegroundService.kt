package com.gainznote.android

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.*

/**
 * Service de premier plan qui affiche et met à jour le chronomètre de repos
 * dans la barre de notifications, même quand l'app est en arrière-plan.
 */
class ChronoForegroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var startTimeMs = 0L

    companion object {
        const val ACTION_START = "com.gainznote.CHRONO_START"
        const val ACTION_STOP  = "com.gainznote.CHRONO_STOP"
        const val EXTRA_START_TIME = "start_time_ms"
        const val CHANNEL_ID = "gainznote_chrono"
        const val NOTIF_ID = 2001
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                // Annuler toute coroutine de tick en cours avant d'en démarrer une nouvelle.
                // Évite les duplications si ACTION_START est reçu deux fois de suite.
                scope.coroutineContext.cancelChildren()
                startTimeMs = intent.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis())
                val notif = buildNotif("00:00")
                if (Build.VERSION.SDK_INT >= 34) {
                    ServiceCompat.startForeground(
                        this, NOTIF_ID, notif,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } else {
                    startForeground(NOTIF_ID, notif)
                }
                startTicking()
            }
            ACTION_STOP -> {
                scope.coroutineContext.cancelChildren()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTicking() {
        scope.launch {
            while (isActive) {
                val elapsed = (System.currentTimeMillis() - startTimeMs) / 1000L
                val m = elapsed / 60
                val s = elapsed % 60
                val display = "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIF_ID, buildNotif(display))
                delay(1000)
            }
        }
    }

    private fun buildNotif(display: String): Notification {
        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GainzNote · Temps de repos")
            .setContentText("⏱  $display")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .setContentIntent(tapIntent)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .build()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Chronomètre de repos",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Affiche le temps de repos en cours pendant l'entraînement"
            setShowBadge(false)
        }
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
