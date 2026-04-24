package com.gallr.shared.data.model

import kotlinx.datetime.LocalDate

data class Event(
    val id: String,
    val nameKo: String,
    val nameEn: String,
    val descriptionKo: String,
    val descriptionEn: String,
    val locationLabelKo: String,
    val locationLabelEn: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val brandColor: String,
    val accentColor: String?,
    val ticketUrl: String?,
    val isActive: Boolean,
    val coverImageUrl: String? = null,
) {
    fun localizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> nameEn.ifEmpty { nameKo }
        AppLanguage.KO -> nameKo
    }

    fun localizedDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> descriptionEn.ifEmpty { descriptionKo }
        AppLanguage.KO -> descriptionKo
    }

    fun localizedLocationLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> locationLabelEn.ifEmpty { locationLabelKo }
        AppLanguage.KO -> locationLabelKo
    }

    fun isActiveOn(today: LocalDate): Boolean =
        isActive && today >= startDate && today <= endDate

    companion object {
        /**
         * Returns the last whitespace-separated token of a string.
         * Used to render the trailing word of an event name in the accent color
         * (e.g., "Loop Lab BUSAN" — "BUSAN" gets accent color treatment).
         */
        fun nameLastToken(name: String): String {
            if (name.isEmpty()) return ""
            return name.trim().split(Regex("\\s+")).lastOrNull() ?: ""
        }
    }
}
