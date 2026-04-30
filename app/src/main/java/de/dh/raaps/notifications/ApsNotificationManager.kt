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
        val bgValueStr = data.getBgValueAsString();
        val contentText = if (bgValueStr == null)
            context.getString(R.string.aps_service_notification_content_no_value_yet)
        else
            context.getString(R.string.aps_service_notification_content_value, bgValueStr)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.aps_service_notification_title))
            .setContentText(contentText)
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
        if (!canPostNotifications(context)) {
            Log.w(TAG, "Missing permissions to show notification")
            return
        }
        try {
            manager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Fallback to log message below
        }
    }

    companion object {
        val TAG = ApsNotificationManager::class.simpleName
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "aps_service_channel"
    }
}