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
import de.dh.raaps.core.api.ToDo
import de.dh.raaps.core.api.data.BgValue
import de.dh.raaps.core.api.data.SmoothedBgSample
import de.dh.raaps.ui.screens.permissions.canPostNotifications

class ApsNotificationManager(
    val context: Context
) {
    private val manager = context.getSystemService<NotificationManager>()!!

    fun createNotificationChannels() {
        val name = context.getString(R.string.aps_service_notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.setShowBadge(false)
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun getBgValueSmoothedString(sample: SmoothedBgSample?): String? {
        if (sample == null) return null
        return "${sample.origValue.mgdl} -> ${sample.smoothedValue.mgdl} mg/dl"
    }

    fun getBgValueString(sample: BgValue?): String? {
        if (sample == null) return null
        return "${sample.mgdl} mg/dl"
    }

    fun createForegroundServiceNotification(data: ApsNotificationData): Notification {
        Log.d(TAG, "Build notification for ${data.lastBgSample}")
        ToDo.toBeImplemented("Take glucose unit from preferences")
        val bgValueStr = getBgValueString(data.lastBgSample?.origValue);
        val title = if (bgValueStr == null) {
            context.getString(R.string.aps_service_notification_content_no_value_yet)
        } else {
            var ret = bgValueStr
            val bgDeltaStr = getBgValueString(data.getBgDelta())
            if (bgDeltaStr != null) {
                ret = "$ret, Δ$bgDeltaStr"
            }
            ret
        }
        val details = getBgValueSmoothedString(data.lastBgSample)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(details)
            .setSmallIcon(R.mipmap.ic_launcher) // Use app icon for now
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAllowSystemGeneratedContextualActions(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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
            manager.cancel(notificationId) // To force the update of the notification
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