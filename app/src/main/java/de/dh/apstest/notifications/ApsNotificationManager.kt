package de.dh.apstest.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.app.NotificationCompat
import de.dh.apstest.R

class ApsNotificationManager(
    val context: Context
) {
    fun createNotificationChannels() {
        val name = context.getString(R.string.aps_service_notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun createForegroundServiceNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.aps_service_notification_title))
            .setContentText(context.getString(R.string.aps_service_notification_content))
            .setSmallIcon(R.mipmap.ic_launcher) // Use app icon for now
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "aps_service_channel"
    }
}