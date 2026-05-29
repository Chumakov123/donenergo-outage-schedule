package com.chumakov123.outageschedule.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {

    const val OUTAGE_ALERTS_CHANNEL_ID = "outage_alerts"

    fun ensure(context: Context) {

        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            OUTAGE_ALERTS_CHANNEL_ID,
            "Отключения электроэнергии",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о предстоящих отключениях"
        }

        manager.createNotificationChannel(channel)
    }
}