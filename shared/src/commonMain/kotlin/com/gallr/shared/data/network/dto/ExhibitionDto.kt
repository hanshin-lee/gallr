package com.gallr.shared.data.network.dto

import com.gallr.shared.data.model.Exhibition
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExhibitionDto(
    val id: String,
    val name: String,
    @SerialName("venue_name") val venueName: String,
    val city: String,
    val region: String,
    @SerialName("opening_date") val openingDate: String,
    @SerialName("closing_date") val closingDate: String,
    @SerialName("is_featured") val isFeatured: Boolean,
    @SerialName("is_editors_pick") val isEditorsPick: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String = "",
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
) {
    fun toDomain(): Exhibition = Exhibition(
        id = id,
        name = name,
        venueName = venueName,
        city = city,
        region = region,
        openingDate = LocalDate.parse(openingDate),
        closingDate = LocalDate.parse(closingDate),
        isFeatured = isFeatured,
        isEditorsPick = isEditorsPick,
        latitude = latitude,
        longitude = longitude,
        description = description,
        coverImageUrl = coverImageUrl,
    )
}
