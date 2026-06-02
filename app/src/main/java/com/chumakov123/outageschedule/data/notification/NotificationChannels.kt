package com.chumakov123.outageschedule.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.chumakov123.outageschedule.R

object NotificationChannels {

    const val OUTAGE_ALERTS_CHANNEL_ID = "outage_alerts"

    fun ensure(context: Context) {

        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            OUTAGE_ALERTS_CHANNEL_ID,
            context.getString(R.string.notification_channel_outages_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_outages_description)
        }

        manager.createNotificationChannel(channel)
    }
}