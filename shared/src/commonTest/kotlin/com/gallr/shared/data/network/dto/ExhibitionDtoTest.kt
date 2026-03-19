package com.gallr.shared.data.network.dto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Verifies that [ExhibitionDto] correctly deserializes Supabase PostgREST snake_case JSON.
 *
 * Per specs/007-gallery-data-sync/contracts/supabase-api.md, Supabase returns JSON keys
 * in snake_case (e.g. "venue_name", "opening_date", "is_featured"). The DTO must map
 * these to the correct Kotlin fields using @SerialName annotations.
 *
 * NOTE (TDD): This test MUST fail before ExhibitionDto.kt is updated (the current
 * @SerialName annotations use camelCase and will not match Supabase snake_case keys).
 */
class ExhibitionDtoTest {

    private val testJson = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val supabaseJson = """
        {
            "id": "a3f2b1c9d4e7f8a2",
            "name": "Zen Master Eyeball",
            "venue_name": "Kukje Gallery K1",
            "city": "Seoul",
            "region": "Seoul",
            "opening_date": "2026-03-19",
            "closing_date": "2026-05-10",
            "is_featured": true,
            "is_editors_pick": false,
            "latitude": 37.5796,
            "longitude": 126.9784,
            "description": "A solo exhibition",
            "cover_image_url": null,
            "updated_at": "2026-03-20T10:00:00Z"
        }
    """.trimIndent()

    @Test
    fun `ExhibitionDto deserializes venue_name from Supabase JSON`() {
        val dto = testJson.decodeFromString<ExhibitionDto>(supabaseJson)
        assertEquals("Kukje Gallery K1", dto.venueName)
    }

    @Test
    fun `ExhibitionDto deserializes opening_date from Supabase JSON`() {
        val dto = testJson.decodeFromString<ExhibitionDto>(supabaseJson)
        assertEquals("2026-03-19", dto.openingDate)
    }

    @Test
    fun `ExhibitionDto deserializes closing_date from Supabase JSON`() {
        val dto = testJson.decodeFromString<ExhibitionDto>(supabaseJson)
        assertEquals("2026-05-10", dto.closingDate)
    }

    @Test
    fun `ExhibitionDto deserializes is_featured from Supabase JSON`() {
        val dto = testJson.decodeFromString<ExhibitionDto>(supabaseJson)
        assertEquals(true, dto.isFeatured)
    }

    @Test
    fun `ExhibitionDto deserializes is_editors_pick from Supabase JSON`() {
        val dto = testJson.decodeFromString<ExhibitionDto>(supabaseJson)
        assertEquals(false, dto.isEditorsPick)
    }

    @Test
    fun `ExhibitionDto deserializes null cover_image_url from Supabase JSON`() {
        val dto = testJson.decodeFromString<ExhibitionDto>(supabaseJson)
        assertNull(dto.coverImageUrl)
    }

    @Test
    fun `ExhibitionDto toDomain maps all fields correctly`() {
        val dto = testJson.decodeFromString<ExhibitionDto>(supabaseJson)
        val exhibition = dto.toDomain()

        assertEquals("a3f2b1c9d4e7f8a2", exhibition.id)
        assertEquals("Zen Master Eyeball", exhibition.name)
        assertEquals("Kukje Gallery K1", exhibition.venueName)
        assertEquals("Seoul", exhibition.city)
        assertEquals("Seoul", exhibition.region)
        assertEquals(2026, exhibition.openingDate.year)
        assertEquals(3, exhibition.openingDate.monthNumber)
        assertEquals(19, exhibition.openingDate.dayOfMonth)
        assertEquals(true, exhibition.isFeatured)
        assertEquals(false, exhibition.isEditorsPick)
        assertEquals(37.5796, exhibition.latitude)
        assertEquals(126.9784, exhibition.longitude)
        assertNull(exhibition.coverImageUrl)
    }

    @Test
    fun `ExhibitionDto handles non-null cover_image_url`() {
        val jsonWithUrl = supabaseJson.replace(
            "\"cover_image_url\": null",
            "\"cover_image_url\": \"https://example.com/image.jpg\""
        )
        val dto = testJson.decodeFromString<ExhibitionDto>(jsonWithUrl)
        assertEquals("https://example.com/image.jpg", dto.coverImageUrl)
    }
}
