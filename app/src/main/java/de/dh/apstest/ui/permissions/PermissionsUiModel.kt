package de.dh.apstest.ui.permissions

import android.content.Context
import android.content.res.Resources
import de.dh.apstest.R
import de.dh.apstest.ui.common.DisplayTextUtils

sealed class PermissionStatus {
    /**
     * The permission is needed and granted for this app.
     */
    object Granted : PermissionStatus()

    /**
     * The permission is currently not necessary for this app (e.g. calendar sync not configured).
     */
    object NotNeeded : PermissionStatus()

    /**
     * The permission is necessary but not granted for this app.
     */
    object Denied : PermissionStatus()

    /**
     * Returns if the state of this permission is ok, i.e. granted or not needed.
     */
    fun isSatisfied(): Boolean = this is Granted || this is NotNeeded

    companion object {
        fun create(
            isGranted: Boolean,
            isNeeded: Boolean
        ): PermissionStatus = when {
            isGranted -> Granted
            isNeeded -> NotNeeded
            else -> Denied
        }
    }
}

data class PermissionsUiModel(
    val isLoading: Boolean,
    val alarmPermissionStatus: PermissionStatus,
    val notificationPermissionStatus: PermissionStatus,
    val fullscreenPermissionStatus: PermissionStatus,
    val ignoreBatteryOptimizationPermissionStatus: PermissionStatus,
    val autoRevokePermissionsPermissionStatus: PermissionStatus,
    val readWriteCalendarPermissionStatus: PermissionStatus,
    val numPermissionsMissing: Int,
    val permissionsMissingText: String
) {
    val isPermissionsConfigComplete: Boolean = numPermissionsMissing == 0

    companion object {
        fun create(
            alarmPermissionStatus: PermissionStatus,
            notificationPermissionStatus: PermissionStatus,
            fullscreenPermissionStatus: PermissionStatus,
            ignoreBatteryOptimizationPermissionStatus: PermissionStatus,
            readWriteCalendarPermissionStatus: PermissionStatus,
            autoRevokePermissionsPermissionStatus: PermissionStatus,
            context: Context
        ): PermissionsUiModel {
            val numMissing = getNumPermissionsMissing(
                alarmPermissionStatus,
                notificationPermissionStatus,
                fullscreenPermissionStatus,
                ignoreBatteryOptimizationPermissionStatus,
                readWriteCalendarPermissionStatus,
                autoRevokePermissionsPermissionStatus
            )

            val res: Resources = context.resources
            val permissionsText = DisplayTextUtils.getQuantityStringZero(
                res,
                R.plurals.permissions_activity_start_permissions_missing,
                R.string.permissions_activity_start_all_permissions_granted,
                numMissing,
                numMissing
            )

            return PermissionsUiModel(
                isLoading = false,
                alarmPermissionStatus = alarmPermissionStatus,
                notificationPermissionStatus = notificationPermissionStatus,
                fullscreenPermissionStatus = fullscreenPermissionStatus,
                ignoreBatteryOptimizationPermissionStatus = ignoreBatteryOptimizationPermissionStatus,
                autoRevokePermissionsPermissionStatus = autoRevokePermissionsPermissionStatus,
                readWriteCalendarPermissionStatus = readWriteCalendarPermissionStatus,
                numPermissionsMissing = numMissing,
                permissionsMissingText = permissionsText
            )
        }

        fun loading(): PermissionsUiModel {
            return PermissionsUiModel(
                isLoading = true,
                alarmPermissionStatus = PermissionStatus.NotNeeded,
                notificationPermissionStatus = PermissionStatus.NotNeeded,
                fullscreenPermissionStatus = PermissionStatus.NotNeeded,
                ignoreBatteryOptimizationPermissionStatus = PermissionStatus.NotNeeded,
                readWriteCalendarPermissionStatus = PermissionStatus.NotNeeded,
                autoRevokePermissionsPermissionStatus = PermissionStatus.NotNeeded,
                numPermissionsMissing = 0,
                permissionsMissingText = ""
            )
        }

        fun allMissing(context: Context): PermissionsUiModel {
            return create(
                alarmPermissionStatus = PermissionStatus.Denied,
                notificationPermissionStatus = PermissionStatus.Denied,
                fullscreenPermissionStatus = PermissionStatus.Denied,
                ignoreBatteryOptimizationPermissionStatus = PermissionStatus.Denied,
                readWriteCalendarPermissionStatus = PermissionStatus.Denied,
                autoRevokePermissionsPermissionStatus = PermissionStatus.Denied,
                context = context
            )
        }

        fun allGranted(context: Context): PermissionsUiModel {
            return create(
                alarmPermissionStatus = PermissionStatus.Granted,
                notificationPermissionStatus = PermissionStatus.Granted,
                fullscreenPermissionStatus = PermissionStatus.Granted,
                ignoreBatteryOptimizationPermissionStatus = PermissionStatus.Granted,
                readWriteCalendarPermissionStatus = PermissionStatus.Granted,
                autoRevokePermissionsPermissionStatus = PermissionStatus.Granted,
                context = context
            )
        }

        private fun getNumPermissionsMissing(
            alarmPermissionStatus: PermissionStatus,
            notificationPermissionStatus: PermissionStatus,
            fullscreenPermissionStatus: PermissionStatus,
            ignoreBatteryOptimizationPermissionStatus: PermissionStatus,
            readWriteCalendarPermissionStatus: PermissionStatus,
            autoRevokePermissionsPermissionStatus: PermissionStatus
        ): Int {
            val permissions = listOf(
                alarmPermissionStatus,
                notificationPermissionStatus,
                fullscreenPermissionStatus,
                ignoreBatteryOptimizationPermissionStatus,
                autoRevokePermissionsPermissionStatus,
                readWriteCalendarPermissionStatus
            )
            return permissions.count { !it.isSatisfied() }
        }
    }
}