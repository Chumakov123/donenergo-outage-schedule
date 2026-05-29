package com.chumakov123.outageschedule.domain.model

data class TrackedPlace(
    val id: Long = 0L,
    val title: String,
    val city: String,
    val street: String,
    val house: String = "",
    val isEnabled: Boolean = true
)