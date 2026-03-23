package com.gallr.shared.data.network.dto

import com.gallr.shared.data.model.Exhibition
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExhibitionDto(
    val id: String,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("name_en") val nameEn: String = "",
    @SerialName("venue_name_ko") val venueNameKo: String,
    @SerialName("venue_name_en") val venueNameEn: String = "",
    @SerialName("city_ko") val cityKo: String,
    @SerialName("city_en") val cityEn: String = "",
    @SerialName("region_ko") val regionKo: String,
    @SerialName("region_en") val regionEn: String = "",
    @SerialName("opening_date") val openingDate: String,
    @SerialName("closing_date") val closingDate: String,
    @SerialName("is_featured") val isFeatured: Boolean,
    @SerialName("is_editors_pick") val isEditorsPick: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("description_ko") val descriptionKo: String = "",
    @SerialName("description_en") val descriptionEn: String = "",
    @SerialName("address_ko") val addressKo: String = "",
    @SerialName("address_en") val addressEn: String = "",
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
) {
    fun toDomain(): Exhibition = Exhibition(
        id = id,
        nameKo = nameKo,
        nameEn = nameEn,
        venueNameKo = venueNameKo,
        venueNameEn = venueNameEn,
        cityKo = cityKo,
        cityEn = cityEn,
        regionKo = regionKo,
        regionEn = regionEn,
        openingDate = LocalDate.parse(openingDate),
        closingDate = LocalDate.parse(closingDate),
        isFeatured = isFeatured,
        isEditorsPick = isEditorsPick,
        latitude = latitude,
        longitude = longitude,
        descriptionKo = descriptionKo,
        descriptionEn = descriptionEn,
        addressKo = addressKo,
        addressEn = addressEn,
        coverImageUrl = coverImageUrl,
    )
}
