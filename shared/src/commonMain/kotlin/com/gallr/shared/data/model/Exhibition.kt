package com.gallr.shared.data.model

import kotlinx.datetime.LocalDate

data class Exhibition(
    val id: String,
    val nameKo: String,
    val nameEn: String,
    val venueNameKo: String,
    val venueNameEn: String,
    val cityKo: String,
    val cityEn: String,
    val regionKo: String,
    val regionEn: String,
    val openingDate: LocalDate,
    val closingDate: LocalDate,
    val isFeatured: Boolean,
    val isEditorsPick: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val descriptionKo: String,
    val descriptionEn: String,
    val addressKo: String,
    val addressEn: String,
    val coverImageUrl: String?,
) {
    fun localizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> nameEn.ifEmpty { nameKo }
        AppLanguage.KO -> nameKo
    }

    fun localizedVenueName(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> venueNameEn.ifEmpty { venueNameKo }
        AppLanguage.KO -> venueNameKo
    }

    fun localizedCity(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> cityEn.ifEmpty { cityKo }
        AppLanguage.KO -> cityKo
    }

    fun localizedRegion(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> regionEn.ifEmpty { regionKo }
        AppLanguage.KO -> regionKo
    }

    fun localizedDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> descriptionEn.ifEmpty { descriptionKo }
        AppLanguage.KO -> descriptionKo
    }

    fun localizedAddress(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> addressEn.ifEmpty { addressKo }
        AppLanguage.KO -> addressKo
    }
}
