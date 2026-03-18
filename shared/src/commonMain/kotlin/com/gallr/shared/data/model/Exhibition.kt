package com.gallr.shared.data.model

import kotlinx.datetime.LocalDate

data class Exhibition(
    val id: String,
    val name: String,
    val venueName: String,
    val city: String,
    val region: String,
    val openingDate: LocalDate,
    val closingDate: LocalDate,
    val isFeatured: Boolean,
    val isEditorsPick: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val description: String,
    val coverImageUrl: String?,
)
