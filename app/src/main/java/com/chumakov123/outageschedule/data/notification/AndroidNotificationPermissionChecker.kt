package com.chumakov123.outageschedule.data.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.chumakov123.outageschedule.domain.util.NotificationPermissionChecker

class AndroidNotificationPermissionChecker(
    private val context: Context
) : NotificationPermissionChecker {
    override fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}
