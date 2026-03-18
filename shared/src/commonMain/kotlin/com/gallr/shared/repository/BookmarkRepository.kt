package com.gallr.shared.repository

import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun observeBookmarkedIds(): Flow<Set<String>>
    suspend fun addBookmark(exhibitionId: String)
    suspend fun removeBookmark(exhibitionId: String)
    suspend fun isBookmarked(exhibitionId: String): Boolean
}
