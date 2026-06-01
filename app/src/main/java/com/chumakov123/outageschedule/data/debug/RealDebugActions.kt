package com.chumakov123.outageschedule.data.debug

import com.chumakov123.outageschedule.domain.debug.DebugActions
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.notification.OutageNotifier

class RealDebugActions(
    private val outageNotifier: OutageNotifier
) : DebugActions {
    override fun sendTestNotification() {
        val testOutage = Outage(
            city = "Тестовый город",
            address = "ул. Примерная, д. 1",
            startDate = "01.01.2025",
            endDate = "01.01.2025",
            startTime = "09:00",
            endTime = "17:00",
            reason = "Тестовое уведомление"
        )
        outageNotifier.show(
            outage = testOutage,
            leadHours = 24,
            placeTitles = listOf("Мой дом")
        )
    }
}