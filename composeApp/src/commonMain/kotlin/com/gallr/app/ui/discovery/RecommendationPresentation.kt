package com.gallr.app.ui.discovery

import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.Exhibition
import com.gallr.shared.recommendation.ExhibitionRecommendation
import com.gallr.shared.recommendation.RecommendationReason

internal data class RecommendationScreenCopy(
    val title: String,
    val deviceOnlyLabel: String,
    val explanation: String,
    val loading: String,
    val emptyTitle: String,
    val emptyBody: String,
    val browseFeatured: String,
    val errorTitle: String,
    val errorBody: String,
    val retry: String,
    val back: String,
)

internal data class RecommendationCardPresentation(
    val exhibition: Exhibition,
    val reasonLabels: List<String>,
)

internal fun recommendationScreenCopy(language: AppLanguage): RecommendationScreenCopy =
    when (language) {
        AppLanguage.KO -> {
            RecommendationScreenCopy(
                title = "내 취향 추천",
                deviceOnlyLabel = "이 기기에서 계산됨",
                explanation = "저장, 방문, 팔로우 기록을 기기 밖으로 보내지 않고 추천을 계산합니다.",
                loading = "추천 전시를 계산하고 있습니다.",
                emptyTitle = "추천할 수 있는 전시가 없습니다.",
                emptyBody = "현재 보거나 곧 열리는 전시가 추가되면 다시 확인해 주세요.",
                browseFeatured = "추천 전시 보기",
                errorTitle = "! 추천을 계산하지 못했습니다.",
                errorBody = "기기 안에서 다시 계산해 보세요.",
                retry = "다시 시도",
                back = "뒤로",
            )
        }

        AppLanguage.EN -> {
            RecommendationScreenCopy(
                title = "FOR YOU",
                deviceOnlyLabel = "COMPUTED ON THIS DEVICE",
                explanation =
                    "Recommendations use your saves, visits, and follows without sending that history off this device.",
                loading = "Computing recommendations on this device…",
                emptyTitle = "No recommendations right now.",
                emptyBody = "Check again when more current or upcoming exhibitions are available.",
                browseFeatured = "BROWSE FEATURED",
                errorTitle = "! Recommendations couldn’t be computed.",
                errorBody = "Try the on-device calculation again.",
                retry = "Retry",
                back = "Back",
            )
        }
    }

internal fun localizedRecommendationReasons(
    reasons: List<RecommendationReason>,
    language: AppLanguage,
): List<String> =
    reasons
        .distinct()
        .take(MAX_RECOMMENDATION_REASONS)
        .map { it.label(language) }

internal fun recommendationCardPresentations(
    recommendations: List<ExhibitionRecommendation>,
    language: AppLanguage,
): List<RecommendationCardPresentation> =
    recommendations
        .take(MAX_RECOMMENDATION_CARDS)
        .map { recommendation ->
            RecommendationCardPresentation(
                exhibition = recommendation.exhibition,
                reasonLabels = localizedRecommendationReasons(recommendation.reasons, language),
            )
        }

private const val MAX_RECOMMENDATION_CARDS = 6
private const val MAX_RECOMMENDATION_REASONS = 2
