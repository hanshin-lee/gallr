package com.gallr.shared.repository

import com.gallr.shared.data.model.Thought

interface ThoughtRepository {
    suspend fun getThoughtsForExhibition(exhibitionId: String, limit: Int = 20): List<Thought>
    suspend fun submitThought(exhibitionId: String, content: String)
    suspend fun updateThought(thoughtId: String, content: String)
    suspend fun deleteThought(thoughtId: String)
    suspend fun getUserThoughtForExhibition(exhibitionId: String): Thought?
    suspend fun getUserThoughtCount(userId: String): Int
}
