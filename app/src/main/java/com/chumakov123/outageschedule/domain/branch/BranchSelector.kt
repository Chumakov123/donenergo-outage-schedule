package com.chumakov123.outageschedule.domain.branch

import com.chumakov123.outageschedule.domain.model.Branch

object BranchSelector {

    private val DEFAULT_KEYWORDS = listOf(
        "ростов",
        "ргэс"
    )

    fun findDefault(branches: List<Branch>): Branch {
        return branches.firstOrNull { branch ->
            matches(branch)
        } ?: branches.first()
    }

    private fun matches(branch: Branch): Boolean {
        val normalized = normalize(branch.name)

        return DEFAULT_KEYWORDS.any { key ->
            normalized.contains(key)
        }
    }

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-zа-я0-9]"), "")
    }
}