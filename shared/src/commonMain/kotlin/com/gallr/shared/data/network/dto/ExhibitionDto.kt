package com.gallr.shared.data.network.dto

import com.gallr.shared.data.model.Exhibition
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExhibitionDto(
    val id: String,
    val name: String,
    @SerialName("venueName") val venueName: String,
    val city: String,
    val region: String,
    @SerialName("openingDate") val openingDate: String,
    @SerialName("closingDate") val closingDate: String,
    val isFeatured: Boolean,
    val isEditorsPick: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String = "",
    val coverImageUrl: String? = null,
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
