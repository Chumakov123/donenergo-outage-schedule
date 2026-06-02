package com.chumakov123.outageschedule.data.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.chumakov123.outageschedule.R
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.notification.OutageNotifier
import com.chumakov123.outageschedule.presentation.MainActivity

class AndroidOutageNotifier(
    private val context: Context
) : OutageNotifier {

    override fun show(outage: Outage, leadHours: Int, placeTitles: List<String>) {
        val datePart = if (outage.startDate == outage.endDate) {
            outage.startDate
        } else {
            "${outage.startDate} — ${outage.endDate}"
        }

        val timePart = when {
            !outage.startTime.isNullOrBlank() && !outage.endTime.isNullOrBlank() ->
                context.getString(R.string.notification_time_range, outage.startTime, outage.endTime)
            !outage.startTime.isNullOrBlank() -> context.getString(R.string.notification_time_from, outage.startTime)
            !outage.endTime.isNullOrBlank() -> context.getString(R.string.notification_time_to, outage.endTime)
            else -> ""
        }

        val addressesText = if (placeTitles.isNotEmpty()) {
            placeTitles.joinToString(", ")
        } else {
            val city = outage.city.trim()
            if (city.isNotBlank()) {
                "$city, ${outage.address.trim()}"
            } else {
                outage.address.trim()
            }
        }

        val leadTimeText = context.resources.getQuantityString(R.plurals.hour, leadHours, leadHours)
        val title = context.getString(R.string.notification_title_upcoming, leadTimeText)

        val compactText = addressesText

        val expandedText = buildString {
            appendLine(addressesText)
            appendLine()
            appendLine(datePart)
            appendLine(timePart)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.OUTAGE_ALERTS_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification_outage)
            .setColor(ContextCompat.getColor(context, R.color.purple_500))
            .setContentTitle(title)
            .setContentText(compactText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(expandedText)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
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