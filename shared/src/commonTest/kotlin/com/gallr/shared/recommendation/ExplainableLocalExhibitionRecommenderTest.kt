package com.gallr.shared.recommendation

import com.gallr.shared.data.model.ArtTerm
import com.gallr.shared.data.model.ArtTermCategory
import com.gallr.shared.data.model.Exhibition
import com.gallr.shared.data.model.ExhibitionArtist
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class ExplainableLocalExhibitionRecommenderTest {
    private val today = LocalDate(2026, 8, 30)
    private val recommender = LocalExhibitionRecommender()

    @Test
    fun `exact canonical artist leads free text and carries a saved anchor`() {
        val artist = ExhibitionArtist("artist-kimsooja", "김수자", "Kimsooja")
        val saved = exhibition("saved", descriptionEn = "ceramic vessels", artists = listOf(artist))
        val sameArtist = exhibition("same-artist", descriptionEn = "unrelated light", artists = listOf(artist))
        val sameText = exhibition("same-text", descriptionEn = "ceramic vessels")

        val result = recommend(listOf(sameText, sameArtist, saved), bookmarks = setOf(saved.id))

        assertEquals(sameArtist.id, result.first().exhibition.id)
        val evidence = assertIs<RecommendationEvidence.ArtistMatch>(result.first().evidence.first())
        assertEquals(RecommendationSignalSource.SAVED, evidence.source)
        assertEquals(saved.id, evidence.anchor.exhibitionId)
        assertEquals(artist, evidence.artist)
    }

    @Test
    fun `controlled term identity leads text fallback and preserves category`() {
        val term = ArtTerm("mood:quiet", ArtTermCategory.MOOD, "고요함", "Quiet / meditative")
        val saved = exhibition("saved", descriptionEn = "bright painting", artTerms = listOf(term))
        val termMatch = exhibition("term-match", descriptionEn = "video", artTerms = listOf(term))
        val textMatch = exhibition("text-match", descriptionEn = "bright painting")

        val result = recommend(listOf(textMatch, termMatch, saved), bookmarks = setOf(saved.id))

        assertEquals(termMatch.id, result.first().exhibition.id)
        val evidence = assertIs<RecommendationEvidence.ArtTermMatch>(result.first().evidence.first())
        assertEquals(term, evidence.term)
        assertEquals(saved.id, evidence.anchor.exhibitionId)
    }

    @Test
    fun `one artist in a group show receives less credit than a solo exact match`() {
        val shared = ExhibitionArtist("artist-shared", "공통", "Shared")
        val saved = exhibition("saved", artists = listOf(shared))
        val solo = exhibition("solo", artists = listOf(shared), galleryId = "solo-gallery")
        val group =
            exhibition(
                "group",
                artists =
                    listOf(
                        shared,
                        ExhibitionArtist("artist-2", "둘", "Two"),
                        ExhibitionArtist("artist-3", "셋", "Three"),
                        ExhibitionArtist("artist-4", "넷", "Four"),
                    ),
                galleryId = "group-gallery",
            )

        val result = recommend(listOf(group, solo, saved), bookmarks = setOf(saved.id))

        assertEquals(listOf("solo", "group"), result.map { it.exhibition.id })
        assertTrue(result.first().scoreBasisPoints > result.last().scoreBasisPoints)
    }

    @Test
    fun `text similarity remains truthful generic evidence`() {
        val saved = exhibition("saved", descriptionEn = "experimental cyanotype photography")
        val candidate = exhibition("candidate", descriptionEn = "cyanotype photographic experiment")

        val result = recommend(listOf(candidate, saved), bookmarks = setOf(saved.id)).single()

        val evidence = assertIs<RecommendationEvidence.TextSimilarity>(result.evidence.single())
        assertEquals(saved.id, evidence.anchor.exhibitionId)
        assertTrue(result.evidence.none { it is RecommendationEvidence.ArtistMatch })
        assertTrue(result.evidence.none { it is RecommendationEvidence.ArtTermMatch })
    }

    @Test
    fun `zero evidence candidates are omitted instead of receiving id tie breaks`() {
        val unexplained = exhibition("a-unexplained")
        val featured = exhibition("z-featured", isFeatured = true)

        val result = recommend(listOf(unexplained, featured))

        assertEquals(listOf(featured.id), result.map { it.exhibition.id })
        assertEquals(listOf(RecommendationEvidence.Featured), result.single().evidence)
        assertTrue(result.all { it.evidence.size in 1..2 })
    }

    @Test
    fun `matching labels without stable term identity do not claim a term match`() {
        val saved =
            exhibition(
                "saved",
                artTerms = listOf(ArtTerm("style:a", ArtTermCategory.STYLE, "추상", "Abstract")),
            )
        val candidate =
            exhibition(
                "candidate",
                artTerms = listOf(ArtTerm("style:b", ArtTermCategory.STYLE, "추상", "Abstract")),
            )

        val result = recommend(listOf(candidate, saved), bookmarks = setOf(saved.id))

        assertTrue(result.flatMap { it.evidence }.none { it is RecommendationEvidence.ArtTermMatch })
    }

    @Test
    fun `metadata change invalidates the prepared index`() {
        val catalogue = listOf(exhibition("one"), exhibition("two"))
        val prepared = recommender.prepare(catalogue)
        val changed =
            catalogue.map { exhibition ->
                if (exhibition.id == "one") {
                    exhibition.copy(
                        artists = listOf(ExhibitionArtist("artist-new", "새 작가", "New Artist")),
                    )
                } else {
                    exhibition
                }
            }

        assertNotSame(prepared, recommender.prepare(changed, prepared))
    }

    @Test
    fun `catalogue input order preserves ranked evidence anchors and scores`() {
        val artist = ExhibitionArtist("artist", "작가", "Artist")
        val saved = exhibition("saved", artists = listOf(artist))
        val candidate = exhibition("candidate", artists = listOf(artist))
        val forward = recommend(listOf(saved, candidate), bookmarks = setOf(saved.id))
        val reverse = recommend(listOf(candidate, saved), bookmarks = setOf(saved.id))

        assertEquals(forward, reverse)
    }

    private fun recommend(
        catalogue: List<Exhibition>,
        bookmarks: Set<String> = emptySet(),
    ): List<ExhibitionRecommendation> =
        recommender.prepare(catalogue).recommend(
            RecommendationContext(
                bookmarkedExhibitionIds = bookmarks,
                today = today,
            ),
        )

    private fun exhibition(
        id: String,
        descriptionEn: String = "",
        artists: List<ExhibitionArtist> = emptyList(),
        artTerms: List<ArtTerm> = emptyList(),
        galleryId: String = "gallery-$id",
        isFeatured: Boolean = false,
    ) = Exhibition(
        id = id,
        nameKo = id,
        nameEn = id,
        venueNameKo = "갤러리 $id",
        venueNameEn = "Gallery $id",
        cityKo = "서울",
        cityEn = "Seoul",
        regionKo = "종로구",
        regionEn = "Jongno-gu",
        openingDate = LocalDate(2026, 8, 1),
        closingDate = LocalDate(2026, 9, 30),
        isFeatured = isFeatured,
        latitude = 37.57,
        longitude = 126.98,
        descriptionKo = "",
        descriptionEn = descriptionEn,
        addressKo = "",
        addressEn = "",
        coverImageUrl = null,
        galleryId = galleryId,
        artists = artists,
        artTerms = artTerms,
    )
}
