package com.chumakov123.outageschedule.domain.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object OutageDateTimeParser {
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru"))

    fun parse(date: String?, time: String?): LocalDateTime? {
        val d = date?.takeIf { it.isNotBlank() } ?: return null
        val t = time?.takeIf { it.isNotBlank() } ?: return null
        return runCatching { LocalDateTime.parse("$d $t", formatter) }.getOrNull()
    }
}