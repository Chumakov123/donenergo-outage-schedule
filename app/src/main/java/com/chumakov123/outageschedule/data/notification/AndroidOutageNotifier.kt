package com.chumakov123.outageschedule.data.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.chumakov123.outageschedule.R
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.notification.OutageNotifier

class AndroidOutageNotifier(
    private val context: Context
) : OutageNotifier {

    override fun show(outage: Outage, leadHours: Int, placeTitles: List<String>) {
        val title = "Предстоящее отключение"

        val datePart = if (outage.startDate == outage.endDate) {
            outage.startDate
        } else {
            "${outage.startDate} — ${outage.endDate}"
        }

        val timePart = when {
            !outage.startTime.isNullOrBlank() && !outage.endTime.isNullOrBlank() ->
                "${outage.startTime} — ${outage.endTime}"
            !outage.startTime.isNullOrBlank() -> "с ${outage.startTime}"
            !outage.endTime.isNullOrBlank() -> "до ${outage.endTime}"
            else -> ""
        }

        val dateTimeHeader = if (timePart.isNotBlank()) "$datePart, $timePart" else datePart

        val addressesText = if (placeTitles.isNotEmpty()) {
            placeTitles.joinToString(", ")
        } else {
            val city = outage.city.trim()
            if (city.isNotBlank()) "$city, ${outage.address.trim()}" else outage.address.trim()
        }

        val text = "$dateTimeHeader\n$addressesText\nЧерез $leadHours ч."

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.OUTAGE_ALERTS_CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher_monochrome)
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
            outage.startDate,
            outage.endDate,
            outage.startTime.orEmpty(),
            outage.endTime.orEmpty(),
            leadHours.toString()
        ).joinToString("|").hashCode()
    }
}