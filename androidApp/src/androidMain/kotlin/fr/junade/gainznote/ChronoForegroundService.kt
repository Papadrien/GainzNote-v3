package fr.junade.gainznote

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import fr.junade.gainznote.i18n.S

/**
 * Service de premier plan qui affiche le chronomètre dans la barre de notifications,
 * même quand l'app est en arrière-plan.
 * L'affichage du temps est délégué au système via setUsesChronometer (aucune boucle).
 */
class ChronoForegroundService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null

    companion object {
        const val ACTION_START     = "fr.junade.gainznote.CHRONO_START"
        const val ACTION_COUNTDOWN = "fr.junade.gainznote.CHRONO_COUNTDOWN"
        const val ACTION_STOP      = "fr.junade.gainznote.CHRONO_STOP"
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
                cancelStop()
                val startTimeMs = intent.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis())
                startFg(buildNotif(startTimeMs, isCountdown = false))
            }
            ACTION_COUNTDOWN -> {
                cancelStop()
                val endTimeMs = intent.getLongExtra(EXTRA_END_TIME, System.currentTimeMillis())
                startFg(buildNotif(endTimeMs, isCountdown = true))
                // Auto-arrêt à l'expiration du minuteur
                val delay = (endTimeMs - System.currentTimeMillis()).coerceAtLeast(0)
                stopRunnable = Runnable {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }.also { handler.postDelayed(it, delay) }
            }
            ACTION_STOP -> {
                cancelStop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun cancelStop() {
        stopRunnable?.let { handler.removeCallbacks(it) }
        stopRunnable = null
    }

    private fun startFg(notif: Notification) {
        if (Build.VERSION.SDK_INT >= 34) {
            ServiceCompat.startForeground(
                this, NOTIF_ID, notif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }

    /**
     * @param whenMs  Pour un chronomètre : timestamp de départ.
     *                Pour un minuteur    : timestamp d'expiration.
     * @param isCountdown true = minuteur (compte à rebours), false = chronomètre (elapsed).
     */
    private fun buildNotif(whenMs: Long, isCountdown: Boolean): Notification {
        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(S.chronoNotifTitle)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .setContentIntent(tapIntent)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setWhen(whenMs)
            .setUsesChronometer(true)
            .setChronometerCountDown(isCountdown)
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
        cancelStop()
        super.onDestroy()
    }
}
