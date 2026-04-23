package com.gallr.shared.data.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class EventTest {

    private val sample = Event(
        id = "loop-lab-busan-2025",
        nameKo = "루프랩 부산 2025",
        nameEn = "Loop Lab Busan 2025",
        descriptionKo = "한국어 설명",
        descriptionEn = "English description",
        locationLabelKo = "부산 전역",
        locationLabelEn = "Across Busan",
        startDate = LocalDate(2025, 4, 18),
        endDate = LocalDate(2025, 5, 10),
        brandColor = "#0099FF",
        accentColor = "#FF5C5C",
        ticketUrl = "https://example.com/tickets",
        isActive = true,
    )

    @Test
    fun `localizedName returns Korean for KO`() {
        assertEquals("루프랩 부산 2025", sample.localizedName(AppLanguage.KO))
    }

    @Test
    fun `localizedName returns English for EN`() {
        assertEquals("Loop Lab Busan 2025", sample.localizedName(AppLanguage.EN))
    }

    @Test
    fun `localizedName falls back to Korean when English is empty`() {
        val koOnly = sample.copy(nameEn = "")
        assertEquals("루프랩 부산 2025", koOnly.localizedName(AppLanguage.EN))
    }

    @Test
    fun `localizedLocationLabel falls back to Korean when English is empty`() {
        val koOnly = sample.copy(locationLabelEn = "")
        assertEquals("부산 전역", koOnly.localizedLocationLabel(AppLanguage.EN))
    }

    @Test
    fun `localizedDescription returns English when both present`() {
        assertEquals("English description", sample.localizedDescription(AppLanguage.EN))
    }

    @Test
    fun `isActiveOn returns true when today equals start date`() {
        assertEquals(true, sample.isActiveOn(LocalDate(2025, 4, 18)))
    }

    @Test
    fun `isActiveOn returns true when today equals end date`() {
        assertEquals(true, sample.isActiveOn(LocalDate(2025, 5, 10)))
    }

    @Test
    fun `isActiveOn returns false the day after end date`() {
        assertEquals(false, sample.isActiveOn(LocalDate(2025, 5, 11)))
    }

    @Test
    fun `isActiveOn returns false the day before start date`() {
        assertEquals(false, sample.isActiveOn(LocalDate(2025, 4, 17)))
    }

    @Test
    fun `isActiveOn returns false when isActive flag is false`() {
        val killed = sample.copy(isActive = false)
        assertEquals(false, killed.isActiveOn(LocalDate(2025, 4, 20)))
    }

    @Test
    fun `nameLastToken extracts trailing whitespace-separated word`() {
        assertEquals("BUSAN", Event.nameLastToken("LOOP LAB BUSAN"))
        assertEquals("2025", Event.nameLastToken("Loop Lab Busan 2025"))
        assertEquals("Solo", Event.nameLastToken("Solo"))
        assertEquals("", Event.nameLastToken(""))
    }
}
