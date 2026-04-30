package de.dh.raaps.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import de.dh.raaps.R
import de.dh.raaps.ui.screens.permissions.canPostNotifications

class ApsNotificationManager(
    val context: Context
) {
    private val manager = context.getSystemService<NotificationManager>()!!

    fun createNotificationChannels() {
        val name = context.getString(R.string.aps_service_notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun createForegroundServiceNotification(data: ApsNotificationData): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.aps_service_notification_title))
            .setContentText(context.getString(R.string.aps_service_notification_content, data.getBgValueAsString()))
            .setSmallIcon(R.mipmap.ic_launcher) // Use app icon for now
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAllowSystemGeneratedContextualActions(false)
            .build()
    }

    fun updateNotification(data: ApsNotificationData) {
        val notification: Notification = createForegroundServiceNotification(data)
        notify(NOTIFICATION_ID, notification)
    }

    private fun notify(notificationId: Int, notification: Notification) {
        if (canPostNotifications(context)) {
            try {
                manager.notify(notificationId, notification)
                return
            } catch (e: SecurityException) {
                // Fallback to log message below
            }
        }
        Log.w(TAG, "Missing permissions to show due reminders notification")
    }

    companion object {
        val TAG = ApsNotificationManager::class.simpleName
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "aps_service_channel"
    }
}

