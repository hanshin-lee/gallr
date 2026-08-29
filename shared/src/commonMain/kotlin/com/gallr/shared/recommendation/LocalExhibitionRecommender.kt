package com.gallr.shared.recommendation

import com.gallr.shared.data.model.Exhibition
import com.gallr.shared.data.model.galleryKey
import com.gallr.shared.data.model.map.GeoPoint
import com.gallr.shared.map.geographicDistanceKm
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Zero-network bilingual content recommender for the current organic catalogue. */
class LocalExhibitionRecommender : ExhibitionRecommender {
    /** Returns deterministic, diverse recommendations without persisting a taste vector. */
    override fun recommend(
        catalogue: List<Exhibition>,
        context: RecommendationContext,
    ): List<ExhibitionRecommendation> {
        if (context.limit == 0) return emptyList()
        val indexable = catalogue.sortedBy(Exhibition::id)
        val eligible =
            indexable
                .filter { it.isLocallyDiscoverable(context.today) }
        if (eligible.isEmpty()) return emptyList()

        val vectorizer = LocalContentVectorizer(indexable)
        val vectors = indexable.associate { it.id to vectorizer.vector(it) }
        val visitedIds = context.visits.mapTo(mutableSetOf(), { it.exhibitionId })
        val savedProfile =
            weightedProfile(
                indexable.filter { it.id in context.bookmarkedExhibitionIds },
                vectors,
            )
        val visitedProfile = weightedProfile(indexable.filter { it.id in visitedIds }, vectors)
        val followedIds = context.followedGalleries.mapNotNullTo(mutableSetOf()) { it.galleryId }
        val followedKeys = context.followedGalleries.mapTo(mutableSetOf()) { it.galleryKey }

        val ranked =
            eligible
                .asSequence()
                .filterNot { it.id in context.bookmarkedExhibitionIds || it.id in visitedIds }
                .mapNotNull { exhibition ->
                    val distanceKm = context.origin?.let { origin -> exhibition.distanceFrom(origin) }
                    if (
                        context.maxDistanceKm != null &&
                        (distanceKm == null || distanceKm > context.maxDistanceKm)
                    ) {
                        return@mapNotNull null
                    }
                    val vector = vectors.getValue(exhibition.id)
                    val savedSimilarity = cosine(vector, savedProfile)
                    val visitedSimilarity = cosine(vector, visitedProfile)
                    val followed =
                        exhibition.galleryId?.let(followedIds::contains) == true ||
                            galleryKey(exhibition.venueNameKo, exhibition.venueNameEn) in followedKeys
                    val proximity = distanceKm?.let(::proximityScore) ?: 0.0
                    val daysUntilClose = context.today.daysUntil(exhibition.closingDate)
                    val closingSoon = daysUntilClose in 0..7
                    val editorPick = exhibition.editorId != null
                    val followedScore = if (followed) FOLLOWED_GALLERY_WEIGHT else 0.0
                    val featuredScore = if (exhibition.isFeatured) FEATURED_WEIGHT else 0.0
                    val editorScore = if (editorPick) EDITOR_WEIGHT else 0.0
                    val closingScore = if (closingSoon) CLOSING_WEIGHT else 0.0
                    val score =
                        savedSimilarity * SAVED_SIMILARITY_WEIGHT +
                            visitedSimilarity * VISITED_SIMILARITY_WEIGHT +
                            followedScore +
                            proximity * PROXIMITY_WEIGHT +
                            featuredScore +
                            editorScore +
                            closingScore
                    val reasons =
                        buildList {
                            if (savedSimilarity > SIMILARITY_REASON_THRESHOLD) {
                                add(
                                    RecommendationReason.SIMILAR_TO_SAVED to
                                        savedSimilarity * SAVED_SIMILARITY_WEIGHT,
                                )
                            }
                            if (visitedSimilarity > SIMILARITY_REASON_THRESHOLD) {
                                add(
                                    RecommendationReason.SIMILAR_TO_VISITED to
                                        visitedSimilarity * VISITED_SIMILARITY_WEIGHT,
                                )
                            }
                            if (followed) add(RecommendationReason.FOLLOWED_GALLERY to followedScore)
                            if (proximity >= NEARBY_REASON_THRESHOLD) {
                                add(RecommendationReason.NEARBY to proximity * PROXIMITY_WEIGHT)
                            }
                            if (exhibition.isFeatured) add(RecommendationReason.FEATURED to featuredScore)
                            if (editorPick) add(RecommendationReason.EDITOR_CURATED to editorScore)
                            if (closingSoon) add(RecommendationReason.CLOSING_SOON to closingScore)
                        }.sortedWith(
                            compareByDescending<Pair<RecommendationReason, Double>> { it.second }
                                .thenBy { it.first.ordinal },
                        ).take(MAX_REASONS)
                            .map(Pair<RecommendationReason, Double>::first)
                    ExhibitionRecommendation(
                        exhibition = exhibition,
                        scoreBasisPoints = (score / MAX_SCORE * 10_000).roundToInt().coerceIn(0, 10_000),
                        reasons = reasons,
                    )
                }.sortedWith(
                    compareByDescending<ExhibitionRecommendation> { it.scoreBasisPoints }
                        .thenBy { it.exhibition.id },
                ).toList()
        return diversify(ranked, context.limit)
    }

    private fun diversify(
        ranked: List<ExhibitionRecommendation>,
        limit: Int,
    ): List<ExhibitionRecommendation> {
        val galleryCounts = mutableMapOf<String, Int>()
        val result = mutableListOf<ExhibitionRecommendation>()
        val diversityFeatures = ranked.associate { it.exhibition.id to it.exhibition.diversityFeatures() }
        for (recommendation in ranked) {
            val gallery = recommendation.exhibition.galleryIdentity()
            if ((galleryCounts[gallery] ?: 0) >= MAX_PER_GALLERY) continue
            val candidateFeatures = diversityFeatures.getValue(recommendation.exhibition.id)
            val nearDuplicates =
                result.filter { selected ->
                    jaccard(candidateFeatures, diversityFeatures.getValue(selected.exhibition.id)) >=
                        NEAR_DUPLICATE_JACCARD
                }
            if (
                nearDuplicates.any { it.exhibition.galleryIdentity() == gallery } ||
                nearDuplicates.size >= MAX_PER_CONTENT_CLUSTER
            ) {
                continue
            }
            result += recommendation
            galleryCounts[gallery] = (galleryCounts[gallery] ?: 0) + 1
            if (result.size == limit) break
        }
        return result
    }
}

internal fun Exhibition.isLocallyDiscoverable(today: kotlinx.datetime.LocalDate): Boolean =
    closingDate >= today && openingDate <= today.plus(UPCOMING_VISIBILITY_DAYS, DateTimeUnit.DAY)

private class LocalContentVectorizer(
    catalogue: List<Exhibition>,
) {
    private val documentCount = catalogue.size.toDouble()
    private val inverseDocumentFrequency: Map<Int, Double> =
        catalogue
            .flatMap { rawFeatures(it).keys }
            .groupingBy { it }
            .eachCount()
            .mapValues { (_, count) -> ln((documentCount + 1.0) / (count + 1.0)) + 1.0 }

    fun vector(exhibition: Exhibition): Map<Int, Double> {
        val weighted =
            rawFeatures(exhibition).mapValues { (feature, count) ->
                (1.0 + ln(count.toDouble())) * inverseDocumentFrequency.getValue(feature)
            }
        val norm = sqrt(weighted.values.sumOf { it * it })
        return if (norm == 0.0) emptyMap() else weighted.mapValues { it.value / norm }
    }

    private fun rawFeatures(exhibition: Exhibition): Map<Int, Int> {
        val text =
            listOf(
                exhibition.nameKo,
                exhibition.nameEn,
                exhibition.venueNameKo,
                exhibition.venueNameEn,
                exhibition.cityKo,
                exhibition.cityEn,
                exhibition.regionKo,
                exhibition.regionEn,
                exhibition.descriptionKo,
                exhibition.descriptionEn,
                exhibition.creditsKo,
                exhibition.creditsEn,
            ).joinToString(" ")
        val normalized = text.canonicalSearchCodePoints()
        val features = mutableMapOf<Int, Int>()
        for (size in MIN_NGRAM_SIZE..MAX_NGRAM_SIZE) {
            if (normalized.size < size) continue
            for (index in 0..normalized.size - size) {
                val feature = stableFeatureHash(normalized.subList(index, index + size))
                features[feature] = (features[feature] ?: 0) + 1
            }
        }
        listOfNotNull(
            exhibition.galleryId?.let { "gallery:$it" },
            exhibition.eventId?.let { "event:$it" },
            exhibition.editorId?.let { "editor:$it" },
            "region:${exhibition.regionEn.ifBlank { exhibition.regionKo }.trim().lowercase()}",
        ).forEach { label ->
            val feature = stableFeatureHash(label.canonicalSearchCodePoints())
            features[feature] = (features[feature] ?: 0) + 2
        }
        return features
    }
}

private fun weightedProfile(
    exhibitions: List<Exhibition>,
    vectors: Map<String, Map<Int, Double>>,
): Map<Int, Double> {
    if (exhibitions.isEmpty()) return emptyMap()
    val profile = mutableMapOf<Int, Double>()
    exhibitions.forEach { exhibition ->
        vectors.getValue(exhibition.id).forEach { (feature, weight) ->
            profile[feature] = (profile[feature] ?: 0.0) + weight
        }
    }
    val norm = sqrt(profile.values.sumOf { it * it })
    return if (norm == 0.0) emptyMap() else profile.mapValues { it.value / norm }
}

private fun cosine(
    first: Map<Int, Double>,
    second: Map<Int, Double>,
): Double {
    if (first.isEmpty() || second.isEmpty()) return 0.0
    val smaller = if (first.size <= second.size) first else second
    val larger = if (smaller === first) second else first
    return smaller.entries.sumOf { (feature, weight) -> weight * (larger[feature] ?: 0.0) }
}

private fun Exhibition.distanceFrom(origin: GeoPoint): Double? {
    val latitude = latitude ?: return null
    val longitude = longitude ?: return null
    val point = runCatching { GeoPoint(latitude, longitude) }.getOrNull() ?: return null
    return geographicDistanceKm(origin, point)
}

private fun proximityScore(distanceKm: Double): Double = (1.0 - distanceKm / PROXIMITY_RANGE_KM).coerceIn(0.0, 1.0)

private fun Exhibition.galleryIdentity(): String =
    galleryId ?: "${galleryKey(venueNameKo, venueNameEn)}:$latitude:$longitude"

private const val UPCOMING_VISIBILITY_DAYS = 14
private const val MIN_NGRAM_SIZE = 2
private const val MAX_NGRAM_SIZE = 3
private const val SAVED_SIMILARITY_WEIGHT = 0.45
private const val VISITED_SIMILARITY_WEIGHT = 0.30
private const val FOLLOWED_GALLERY_WEIGHT = 0.18
private const val PROXIMITY_WEIGHT = 0.20
private const val FEATURED_WEIGHT = 0.08
private const val EDITOR_WEIGHT = 0.05
private const val CLOSING_WEIGHT = 0.05
private const val MAX_SCORE =
    SAVED_SIMILARITY_WEIGHT + VISITED_SIMILARITY_WEIGHT + FOLLOWED_GALLERY_WEIGHT +
        PROXIMITY_WEIGHT + FEATURED_WEIGHT + EDITOR_WEIGHT + CLOSING_WEIGHT
private const val PROXIMITY_RANGE_KM = 5.0
private const val SIMILARITY_REASON_THRESHOLD = 0.05
private const val NEARBY_REASON_THRESHOLD = 0.50
private const val MAX_REASONS = 2
private const val MAX_PER_GALLERY = 2
private const val MAX_PER_CONTENT_CLUSTER = 2
private const val NEAR_DUPLICATE_JACCARD = 0.80

private fun String.canonicalSearchCodePoints(): List<Int> {
    val source = lowercase().toCodePoints()
    val composed = mutableListOf<Int>()
    source.forEach { codePoint ->
        val previous = composed.lastOrNull()
        when {
            previous != null && previous in HANGUL_L_BASE until HANGUL_L_BASE + HANGUL_L_COUNT &&
                codePoint in HANGUL_V_BASE until HANGUL_V_BASE + HANGUL_V_COUNT -> {
                composed[composed.lastIndex] =
                    HANGUL_S_BASE +
                    (previous - HANGUL_L_BASE) * HANGUL_N_COUNT +
                    (codePoint - HANGUL_V_BASE) * HANGUL_T_COUNT
            }

            previous != null && previous in HANGUL_S_BASE until HANGUL_S_BASE + HANGUL_S_COUNT &&
                (previous - HANGUL_S_BASE) % HANGUL_T_COUNT == 0 &&
                codePoint in HANGUL_T_BASE + 1 until HANGUL_T_BASE + HANGUL_T_COUNT -> {
                composed[composed.lastIndex] = previous + codePoint - HANGUL_T_BASE
            }

            codePoint in COMBINING_MARK_START..COMBINING_MARK_END -> {
                // Accent marks are folded into their base letter for search.
            }

            else -> {
                composed += foldLatinCodePoint(codePoint)
            }
        }
    }
    return composed.filter { codePoint ->
        codePoint <= Char.MAX_VALUE.code && codePoint.toChar().isLetterOrDigit()
    }
}

private fun String.toCodePoints(): List<Int> {
    val result = mutableListOf<Int>()
    var index = 0
    while (index < length) {
        val first = this[index]
        if (first.isHighSurrogate() && index + 1 < length && this[index + 1].isLowSurrogate()) {
            val high = first.code - HIGH_SURROGATE_BASE
            val low = this[index + 1].code - LOW_SURROGATE_BASE
            result += SUPPLEMENTARY_BASE + (high shl 10) + low
            index += 2
        } else {
            result += first.code
            index += 1
        }
    }
    return result
}

private fun stableFeatureHash(codePoints: List<Int>): Int =
    codePoints.fold(FNV_OFFSET_BASIS) { hash, codePoint -> (hash xor codePoint) * FNV_PRIME }

private fun Exhibition.diversityFeatures(): Set<Int> {
    val descriptive = listOf(descriptionKo, descriptionEn, creditsKo, creditsEn).joinToString(" ")
    val text = descriptive.ifBlank { listOf(nameKo, nameEn).joinToString(" ") }
    val codePoints = text.canonicalSearchCodePoints()
    if (codePoints.size < MAX_NGRAM_SIZE) return setOf(stableFeatureHash(codePoints))
    return (0..codePoints.size - MAX_NGRAM_SIZE)
        .mapTo(mutableSetOf()) { index ->
            stableFeatureHash(codePoints.subList(index, index + MAX_NGRAM_SIZE))
        }
}

private fun jaccard(
    first: Set<Int>,
    second: Set<Int>,
): Double {
    if (first.isEmpty() && second.isEmpty()) return 1.0
    val intersection = first.count(second::contains)
    val union = first.size + second.size - intersection
    return if (union == 0) 0.0 else intersection.toDouble() / union
}

private fun foldLatinCodePoint(codePoint: Int): Int =
    when (codePoint.toChar()) {
        'á', 'à', 'â', 'ä', 'ã', 'å' -> 'a'.code
        'ç', 'ć', 'ĉ', 'ċ', 'č' -> 'c'.code
        'é', 'è', 'ê', 'ë' -> 'e'.code
        'í', 'ì', 'î', 'ï' -> 'i'.code
        'ñ' -> 'n'.code
        'ó', 'ò', 'ô', 'ö', 'õ' -> 'o'.code
        'ú', 'ù', 'û', 'ü' -> 'u'.code
        'ý', 'ÿ' -> 'y'.code
        'š', 'ś', 'ŝ', 'ş' -> 's'.code
        'ž', 'ź', 'ż' -> 'z'.code
        'ř', 'ŕ' -> 'r'.code
        'ľ', 'ĺ', 'ļ', 'ł' -> 'l'.code
        'ğ', 'ĝ', 'ġ', 'ģ' -> 'g'.code
        else -> codePoint
    }

private const val HANGUL_S_BASE = 0xAC00
private const val HANGUL_L_BASE = 0x1100
private const val HANGUL_V_BASE = 0x1161
private const val HANGUL_T_BASE = 0x11A7
private const val HANGUL_L_COUNT = 19
private const val HANGUL_V_COUNT = 21
private const val HANGUL_T_COUNT = 28
private const val HANGUL_N_COUNT = HANGUL_V_COUNT * HANGUL_T_COUNT
private const val HANGUL_S_COUNT = HANGUL_L_COUNT * HANGUL_N_COUNT
private const val COMBINING_MARK_START = 0x0300
private const val COMBINING_MARK_END = 0x036F
private const val HIGH_SURROGATE_BASE = 0xD800
private const val LOW_SURROGATE_BASE = 0xDC00
private const val SUPPLEMENTARY_BASE = 0x10000
private const val FNV_OFFSET_BASIS = -0x7ee3623b
private const val FNV_PRIME = 16_777_619
