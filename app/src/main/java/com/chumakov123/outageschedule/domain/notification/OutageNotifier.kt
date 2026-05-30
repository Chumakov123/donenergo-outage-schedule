package com.chumakov123.outageschedule.domain.notification

import com.chumakov123.outageschedule.domain.model.Outage

interface OutageNotifier {
    fun show(outage: Outage, leadHours: Int)
}