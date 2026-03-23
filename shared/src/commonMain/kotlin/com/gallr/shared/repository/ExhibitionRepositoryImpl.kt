package com.gallr.shared.repository

import com.gallr.shared.data.model.Exhibition
import com.gallr.shared.data.network.ExhibitionApiClient

class ExhibitionRepositoryImpl(
    private val apiClient: ExhibitionApiClient,
) : ExhibitionRepository {

    override suspend fun getFeaturedExhibitions(): Result<List<Exhibition>> =
        runCatching { apiClient.fetchFeatured() }

    override suspend fun getExhibitions(): Result<List<Exhibition>> =
        runCatching { apiClient.fetchExhibitions() }
}
