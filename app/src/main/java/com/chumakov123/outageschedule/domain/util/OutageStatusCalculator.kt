package com.chumakov123.outageschedule.domain.util

import com.chumakov123.outageschedule.domain.model.OutageStatus
import java.time.LocalDateTime

object OutageStatusCalculator {
    fun calculate(now: LocalDateTime, start: LocalDateTime?, end: LocalDateTime?): OutageStatus {
        return when {
            end != null && now.isAfter(end) -> OutageStatus.FINISHED
            start != null && now.isBefore(start) -> OutageStatus.UPCOMING
            start != null || end != null -> OutageStatus.ACTIVE
            else -> OutageStatus.UPCOMING
        }
    }
}