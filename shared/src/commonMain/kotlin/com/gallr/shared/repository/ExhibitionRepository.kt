package com.gallr.shared.repository

import com.gallr.shared.data.model.Exhibition
import com.gallr.shared.data.model.FilterState

interface ExhibitionRepository {
    suspend fun getFeaturedExhibitions(): Result<List<Exhibition>>
    suspend fun getExhibitions(filter: FilterState): Result<List<Exhibition>>
}
