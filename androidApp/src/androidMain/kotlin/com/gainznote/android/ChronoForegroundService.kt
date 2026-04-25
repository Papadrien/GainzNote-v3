package com.gainznote.android

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.gainznote.i18n.S
import kotlinx.coroutines.*

/**
 * Service de premier plan qui affiche et met à jour le chronomètre
 * (temps de repos en mode elapsed, ou compte à rebours en mode countdown)
 * dans la barre de notifications, même quand l'app est en arrière-plan.
 */
class ChronoForegroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var startTimeMs = 0L
    // Pour le mode countdown : timestamp (ms) auquel le compte à rebours atteint 0.
    // endTimeMs == 0 => mode elapsed (legacy). Sinon => countdown.
    private var endTimeMs = 0L

    companion object {
        const val ACTION_START     = "com.gainznote.CHRONO_START"
        const val ACTION_COUNTDOWN = "com.gainznote.CHRONO_COUNTDOWN"
        const val ACTION_STOP      = "com.gainznote.CHRONO_STOP"
        const val EXTRA_START_TIME = "start_time_ms"
        const val EXTRA_END_TIME   = "end_time_ms"
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
                scope.coroutineContext.cancelChildren()
                startTimeMs = intent.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis())
                endTimeMs = 0L
                val notif = buildNotif("00:00")
                startFg(notif)
                startTickingElapsed()
            }
            ACTION_COUNTDOWN -> {
                scope.coroutineContext.cancelChildren()
                endTimeMs = intent.getLongExtra(EXTRA_END_TIME, System.currentTimeMillis())
                startTimeMs = 0L
                val notif = buildNotif(formatRemaining(endTimeMs - System.currentTimeMillis()))
                startFg(notif)
                startTickingCountdown()
            }
            ACTION_STOP -> {
                scope.coroutineContext.cancelChildren()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startFg(notif: Notification) {
        if (Build.VERSION.SDK_INT >= 34) {
            ServiceCompat.startForeground(
                this, NOTIF_ID, notif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }

    private fun startTickingElapsed() {
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

    private fun startTickingCountdown() {
        scope.launch {
            while (isActive) {
                val remainingMs = endTimeMs - System.currentTimeMillis()
                val display = formatRemaining(remainingMs)
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIF_ID, buildNotif(display))
                if (remainingMs <= 0) {
                    // Fin du countdown : on stoppe le service automatiquement
                    delay(500)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    break
                }
                delay(1000)
            }
        }
    }

    private fun formatRemaining(remainingMs: Long): String {
        val sec = (remainingMs.coerceAtLeast(0) + 999) / 1000
        val m = sec / 60
        val s = sec % 60
        return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
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
            .setContentTitle(S.chronoNotifTitle)
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
            S.chronoChannelName,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = S.chronoChannelDesc
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
