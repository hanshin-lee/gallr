package com.gallr.shared.repository

import com.gallr.shared.data.model.Exhibition
import com.gallr.shared.data.model.FilterState
import kotlinx.datetime.LocalDate

/**
 * Hardcoded placeholder data for development/demo use before a real backend exists.
 */
class StubExhibitionRepository : ExhibitionRepository {

    private val exhibitions = listOf(
        Exhibition(
            id = "1",
            name = "Zen Master Eyeball",
            venueName = "Kukje Gallery K1",
            city = "Seoul",
            region = "Seoul",
            openingDate = LocalDate(2026, 3, 19),
            closingDate = LocalDate(2026, 5, 10),
            isFeatured = true,
            isEditorsPick = false,
            latitude = 37.5796,
            longitude = 126.9784,
            description = "",
            coverImageUrl = null,
        ),
        Exhibition(
            id = "2",
            name = "Chora",
            venueName = "Kukje Gallery K3, Hanok",
            city = "Seoul",
            region = "Seoul",
            openingDate = LocalDate(2026, 3, 19),
            closingDate = LocalDate(2026, 5, 10),
            isFeatured = true,
            isEditorsPick = true,
            latitude = 37.5796,
            longitude = 126.9784,
            description = "",
            coverImageUrl = null,
        ),
        Exhibition(
            id = "3",
            name = "Here Comes Spring",
            venueName = "A piece a peace",
            city = "Seoul",
            region = "Seoul",
            openingDate = LocalDate(2026, 3, 28),
            closingDate = LocalDate(2026, 4, 19),
            isFeatured = false,
            isEditorsPick = true,
            latitude = 37.5245,
            longitude = 127.0377,
            description = "",
            coverImageUrl = null,
        ),
    )

    override suspend fun getFeaturedExhibitions(): Result<List<Exhibition>> =
        Result.success(exhibitions.filter { it.isFeatured })

    override suspend fun getExhibitions(filter: FilterState): Result<List<Exhibition>> =
        Result.success(exhibitions.filter { filter.matches(it) })
}
