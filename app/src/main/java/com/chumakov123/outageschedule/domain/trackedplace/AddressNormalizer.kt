package com.chumakov123.outageschedule.domain.trackedplace

import java.util.Locale

object AddressNormalizer {

    private val replacements = listOf(
        Regex("\\bм[- ]?р\\b") to "микрорайон",
        Regex("\\bмкрн\\.?\\b") to "микрорайон",
        Regex("\\bмкр\\.?\\b") to "микрорайон",
        Regex("\\bмикрорайон\\b") to "микрорайон",

        Regex("\\bг\\.?\\b") to "город",
        Regex("\\bгород\\b") to "город",

        Regex("\\bпос\\.?\\b") to "поселок",
        Regex("\\bп\\.?\\b") to "поселок",
        Regex("\\bпоселок\\b") to "поселок",

        Regex("\\bс\\.?\\b") to "село",
        Regex("\\bсело\\b") to "село",

        Regex("\\bст\\.?\\b") to "станица",
        Regex("\\bстаница\\b") to "станица",

        Regex("\\bсл\\.?\\b") to "слобода",
        Regex("\\bслобода\\b") to "слобода",

        Regex("\\bх\\.?\\b") to "хутор",
        Regex("\\bхутор\\b") to "хутор",

        Regex("\\bул\\.?\\b") to "улица",
        Regex("\\bулица\\b") to "улица",

        Regex("\\bпер\\.?\\b") to "переулок",
        Regex("\\bпереулок\\b") to "переулок",

        Regex("\\bпросп\\.?\\b") to "проспект",
        Regex("\\bпр-кт\\b") to "проспект",
        Regex("\\bпроспект\\b") to "проспект",

        Regex("\\bпл\\.?\\b") to "площадь",
        Regex("\\bплощадь\\b") to "площадь",

        Regex("\\bш\\.?\\b") to "шоссе",
        Regex("\\bшоссе\\b") to "шоссе",

        Regex("\\bр-он\\b") to "район",
        Regex("\\bрайон\\b") to "район",

        Regex("\\bснт\\b") to "снт",
        Regex("\\bдол\\b") to "дол",
        Regex("\\bдод\\b") to "дод"
    )

    fun normalizeComparable(text: String): String {
        var value = text.lowercase(Locale.getDefault()).replace('ё', 'е')

        replacements.forEach { (regex, replacement) ->
            value = regex.replace(value, " $replacement ")
        }

        value = value.replace(Regex("[^\\p{L}\\p{Nd}]+"), " ")
        return value.replace(Regex("\\s+"), " ").trim()
    }

    fun compact(text: String): String {
        return normalizeComparable(text).replace(" ", "")
    }
}