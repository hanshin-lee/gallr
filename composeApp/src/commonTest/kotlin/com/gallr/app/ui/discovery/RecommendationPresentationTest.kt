package com.gallr.app.ui.discovery

import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.Exhibition
import com.gallr.shared.recommendation.ExhibitionRecommendation
import com.gallr.shared.recommendation.RecommendationReason
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecommendationPresentationTest {
    @Test
    fun `copy is bilingual and explicitly says computation stays on device`() {
        val korean = recommendationScreenCopy(AppLanguage.KO)
        val english = recommendationScreenCopy(AppLanguage.EN)

        assertEquals("내 취향 추천", korean.title)
        assertEquals("이 기기에서 계산됨", korean.deviceOnlyLabel)
        assertTrue("기기 밖으로 보내지 않고" in korean.explanation)
        assertEquals("FOR YOU", english.title)
        assertEquals("COMPUTED ON THIS DEVICE", english.deviceOnlyLabel)
        assertTrue("without sending that history off this device" in english.explanation)
    }

    @Test
    fun `loading empty error and retry copy never implies a hosted service`() {
        AppLanguage.entries.forEach { language ->
            val copy = recommendationScreenCopy(language)
            val allCopy =
                listOf(
                    copy.loading,
                    copy.emptyTitle,
                    copy.emptyBody,
                    copy.errorTitle,
                    copy.errorBody,
                    copy.retry,
                ).joinToString(" ").lowercase()

            assertFalse("server" in allCopy)
            assertFalse(Regex("\\bai\\b").containsMatchIn(allCopy))
            assertFalse("paid" in allCopy)
            assertFalse("upgrade" in allCopy)
        }
    }

    @Test
    fun `presentation caps organic cards and localized reasons without exposing scores`() {
        val recommendations =
            (0 until 8).map { index ->
                ExhibitionRecommendation(
                    exhibition = exhibition("exhibition-$index"),
                    scoreBasisPoints = 9_999 - index,
                    reasons =
                        listOf(
                            RecommendationReason.SIMILAR_TO_SAVED,
                            RecommendationReason.SIMILAR_TO_SAVED,
                            RecommendationReason.FOLLOWED_GALLERY,
                            RecommendationReason.CLOSING_SOON,
                        ),
                )
            }

        val presented = recommendationCardPresentations(recommendations, AppLanguage.EN)

        assertEquals(6, presented.size)
        assertEquals((0 until 6).map { "exhibition-$it" }, presented.map { it.exhibition.id })
        assertEquals(
            listOf("SIMILAR TO YOUR SAVES", "FROM A GALLERY YOU FOLLOW"),
            presented.first().reasonLabels,
        )
        assertEquals(
            RecommendationCardPresentation(
                exhibition = recommendations.first().exhibition,
                reasonLabels = listOf("SIMILAR TO YOUR SAVES", "FROM A GALLERY YOU FOLLOW"),
            ),
            presented.first(),
        )
    }

    @Test
    fun `existing reason order is preserved in both languages`() {
        val reasons =
            listOf(
                RecommendationReason.NEARBY,
                RecommendationReason.FEATURED,
                RecommendationReason.EDITOR_CURATED,
            )

        assertEquals(
            listOf("가까운 전시", "추천 전시"),
            localizedRecommendationReasons(reasons, AppLanguage.KO),
        )
        assertEquals(
            listOf("NEARBY", "FEATURED"),
            localizedRecommendationReasons(reasons, AppLanguage.EN),
        )
    }

    private fun exhibition(id: String) =
        Exhibition(
            id = id,
            nameKo = "전시 $id",
            nameEn = "Exhibition $id",
            venueNameKo = "갤러리",
            venueNameEn = "Gallery",
            cityKo = "서울",
            cityEn = "Seoul",
            regionKo = "종로구",
            regionEn = "Jongno-gu",
            openingDate = LocalDate(2026, 8, 1),
            closingDate = LocalDate(2026, 9, 30),
            isFeatured = false,
            latitude = 37.57,
            longitude = 126.98,
            descriptionKo = "",
            descriptionEn = "",
            addressKo = "",
            addressEn = "",
            coverImageUrl = null,
        )
}
