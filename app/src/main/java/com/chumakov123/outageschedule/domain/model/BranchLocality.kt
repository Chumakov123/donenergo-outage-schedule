package com.chumakov123.outageschedule.domain.model

data class BranchLocality(
    val branchUrl: String,
    val branchName: String,
    val city: String,
    val street: String? = null
)