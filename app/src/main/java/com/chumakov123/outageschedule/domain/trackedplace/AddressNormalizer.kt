package com.chumakov123.outageschedule.domain.trackedplace

import java.util.Locale

object AddressNormalizer {

    private val replacements = listOf(
        Regex("\\bм[- ]?р\\.?\\s*") to "микрорайон ",
        Regex("\\bмкрн\\.?\\s*") to "микрорайон ",
        Regex("\\bмкр\\.?\\s*") to "микрорайон ",
        Regex("\\bмикрорайон\\b") to "микрорайон",

        Regex("\\bг\\.?\\s*") to "город ",
        Regex("\\bгород\\b") to "город",

        Regex("\\bпос\\.?\\s*") to "поселок ",
        Regex("\\bп\\.?\\s*") to "поселок ",
        Regex("\\bпоселок\\b") to "поселок",

        Regex("\\bс\\.?\\s*") to "село ",
        Regex("\\bсело\\b") to "село",

        Regex("\\bст\\.?\\s*") to "станица ",
        Regex("\\bстаница\\b") to "станица",

        Regex("\\bсл\\.?\\s*") to "слобода ",
        Regex("\\bслобода\\b") to "слобода",

        Regex("\\bх\\.?\\s*") to "хутор ",
        Regex("\\bхутор\\b") to "хутор",

        Regex("\\bул\\.?\\s*") to "улица ",
        Regex("\\bулица\\b") to "улица",

        Regex("\\bпер\\.?\\s*") to "переулок ",
        Regex("\\bпереулок\\b") to "переулок",

        Regex("\\bпросп\\.?\\s*") to "проспект ",
        Regex("\\bпр-кт\\.?\\s*") to "проспект ",
        Regex("\\bпроспект\\b") to "проспект",

        Regex("\\bпл\\.?\\s*") to "площадь ",
        Regex("\\bплощадь\\b") to "площадь",

        Regex("\\bш\\.?\\s*") to "шоссе ",
        Regex("\\bшоссе\\b") to "шоссе",

        Regex("\\bр[- ]?он\\.?\\s*") to "район ",
        Regex("\\bрайон\\b") to "район",

        Regex("\\bснт\\b") to "снт",
        Regex("\\bдол\\b") to "дол",
        Regex("\\bдод\\b") to "дод"
    )

    fun normalizeComparable(text: String): String {
        var value = text.lowercase(Locale.getDefault()).replace('ё', 'е')

        replacements.forEach { (regex, replacement) ->
            value = regex.replace(value, replacement)
        }

        value = value.replace(Regex("[^\\p{L}\\p{Nd}]+"), " ")
        return value.replace(Regex("\\s+"), " ").trim()
    }

    fun compact(text: String): String {
        return normalizeComparable(text).replace(" ", "")
    }
}