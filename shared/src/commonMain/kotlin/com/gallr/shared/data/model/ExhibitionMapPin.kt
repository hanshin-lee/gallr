package com.gallr.shared.data.model

import kotlinx.datetime.LocalDate

/**
 * Lightweight projection of [Exhibition] for map rendering.
 * Only constructed from exhibitions where latitude and longitude are non-null.
 */
data class ExhibitionMapPin(
    val id: String,
    val name: String,
    val venueName: String,
    val latitude: Double,
    val longitude: Double,
    val openingDate: LocalDate,
    val closingDate: LocalDate,
) {
    fun localizedDateRange(lang: AppLanguage): String = when (lang) {
        AppLanguage.KO -> "${formatKo(openingDate)} – ${formatKo(closingDate)}"
        AppLanguage.EN -> formatEnRange(openingDate, closingDate)
    }
}

private val EN_MONTHS = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

private fun formatKo(d: LocalDate): String =
    "${d.year}.${d.monthNumber.toString().padStart(2, '0')}.${d.dayOfMonth.toString().padStart(2, '0')}"

private fun formatEnRange(from: LocalDate, to: LocalDate): String {
    val fm = EN_MONTHS[from.monthNumber - 1]
    val tm = EN_MONTHS[to.monthNumber - 1]
    return if (from.year == to.year) "$fm ${from.dayOfMonth} – $tm ${to.dayOfMonth}, ${to.year}"
    else "$fm ${from.dayOfMonth}, ${from.year} – $tm ${to.dayOfMonth}, ${to.year}"
}

fun Exhibition.toMapPin(lang: AppLanguage): ExhibitionMapPin? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    return ExhibitionMapPin(
        id = id,
        name = localizedName(lang),
        venueName = localizedVenueName(lang),
        latitude = lat,
        longitude = lng,
        openingDate = openingDate,
        closingDate = closingDate,
    )
}
