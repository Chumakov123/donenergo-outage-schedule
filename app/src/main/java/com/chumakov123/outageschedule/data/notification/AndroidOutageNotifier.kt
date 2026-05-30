package com.chumakov123.outageschedule.data.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.notification.OutageNotifier

class AndroidOutageNotifier(
    private val context: Context
) : OutageNotifier {

    override fun show(outage: Outage, leadHours: Int) {
        val title = "Предстоящее отключение"
        val city = outage.city.trim()
        val address = outage.address.trim()
        val text = if (city.isNotBlank()) {
            "$city, $address. Через $leadHours ч."
        } else {
            "$address. Через $leadHours ч."
        }

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.OUTAGE_ALERTS_CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(buildNotificationId(outage, leadHours), notification)
        } catch (_: SecurityException) { }
    }

    private fun buildNotificationId(outage: Outage, leadHours: Int): Int {
        return listOf(
            outage.city,
            outage.address,
            outage.startDate.orEmpty(),
            outage.endDate.orEmpty(),
            outage.startTime.orEmpty(),
            outage.endTime.orEmpty(),
            leadHours.toString()
        ).joinToString("|").hashCode()
    }
}