package com.gallr.shared.data.network

import com.gallr.shared.data.model.Exhibition
import com.gallr.shared.data.model.FilterState
import com.gallr.shared.data.network.dto.ExhibitionDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ExhibitionApiClient(
    private val baseUrl: String,
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.INFO
        }
    }

    suspend fun fetchFeatured(): List<Exhibition> =
        client.get("$baseUrl/api/v1/exhibitions/featured")
            .body<List<ExhibitionDto>>()
            .map { it.toDomain() }

    suspend fun fetchExhibitions(filter: FilterState): List<Exhibition> {
        val dtos = client.get("$baseUrl/api/v1/exhibitions") {
            if (filter.showFeatured) parameter("featured", true)
            if (filter.showEditorsPick) parameter("editors_pick", true)
            filter.regions.forEach { parameter("region", it) }
        }.body<List<ExhibitionDto>>()
        // Apply client-side filter as fallback (FR-017 / data-model.md)
        return dtos.map { it.toDomain() }.filter { filter.matches(it) }
    }
}
