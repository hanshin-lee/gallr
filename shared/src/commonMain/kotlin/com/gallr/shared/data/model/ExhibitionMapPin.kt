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
)

fun Exhibition.toMapPin(): ExhibitionMapPin? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    return ExhibitionMapPin(
        id = id,
        name = name,
        venueName = venueName,
        latitude = lat,
        longitude = lng,
        openingDate = openingDate,
        closingDate = closingDate,
    )
}
