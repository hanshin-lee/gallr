package com.gallr.shared.recommendation

import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.Exhibition
import com.gallr.shared.data.model.ExhibitionVisit
import com.gallr.shared.data.model.FollowedGallery
import com.gallr.shared.data.model.map.GeoPoint
import kotlinx.datetime.LocalDate

/** Explainable, locally measured contribution to an exhibition recommendation. */
enum class RecommendationReason {
    SIMILAR_TO_SAVED,
    SIMILAR_TO_VISITED,
    FOLLOWED_GALLERY,
    NEARBY,
    FEATURED,
    EDITOR_CURATED,
    CLOSING_SOON,
    ;

    val isSimilarityReason: Boolean
        get() = this == SIMILAR_TO_SAVED || this == SIMILAR_TO_VISITED

    fun label(language: AppLanguage): String =
        when (this) {
            SIMILAR_TO_SAVED -> if (language == AppLanguage.KO) "저장한 전시와 비슷함" else "SIMILAR TO YOUR SAVES"
            SIMILAR_TO_VISITED -> if (language == AppLanguage.KO) "방문한 전시와 비슷함" else "SIMILAR TO YOUR VISITS"
            FOLLOWED_GALLERY -> if (language == AppLanguage.KO) "팔로우한 갤러리" else "FROM A GALLERY YOU FOLLOW"
            NEARBY -> if (language == AppLanguage.KO) "가까운 전시" else "NEARBY"
            FEATURED -> if (language == AppLanguage.KO) "추천 전시" else "FEATURED"
            EDITOR_CURATED -> if (language == AppLanguage.KO) "에디터 큐레이션" else "EDITOR CURATED"
            CLOSING_SOON -> if (language == AppLanguage.KO) "곧 종료" else "CLOSING SOON"
        }
}

/** Ranked organic exhibition with a quantized deterministic score and reasons. */
data class ExhibitionRecommendation(
    val exhibition: Exhibition,
    val scoreBasisPoints: Int,
    val reasons: List<RecommendationReason>,
)

/** User-controlled and locally persisted signals used for one recommendation run. */
data class RecommendationContext(
    val bookmarkedExhibitionIds: Set<String> = emptySet(),
    val visits: List<ExhibitionVisit> = emptyList(),
    val followedGalleries: List<FollowedGallery> = emptyList(),
    val origin: GeoPoint? = null,
    val today: LocalDate,
    val limit: Int = 6,
    val maxDistanceKm: Double? = null,
) {
    init {
        require(limit >= 0) { "limit must not be negative" }
        require(maxDistanceKm == null || maxDistanceKm >= 0.0) {
            "maxDistanceKm must not be negative"
        }
    }
}

/** Replaceable contract for local recommendation strategies. */
fun interface ExhibitionRecommender {
    fun recommend(
        catalogue: List<Exhibition>,
        context: RecommendationContext,
    ): List<ExhibitionRecommendation>
}
