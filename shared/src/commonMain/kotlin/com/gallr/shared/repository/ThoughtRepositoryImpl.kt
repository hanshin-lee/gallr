package com.gallr.shared.repository

import com.gallr.shared.data.model.Thought
import com.gallr.shared.data.network.dto.ThoughtDto
import com.gallr.shared.data.network.dto.ThoughtInsert
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class ThoughtRepositoryImpl(
    private val supabaseClient: SupabaseClient,
) : ThoughtRepository {

    override suspend fun getThoughtsForExhibition(exhibitionId: String, limit: Int): List<Thought> =
        supabaseClient.postgrest
            .from("thoughts")
            .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, profiles(display_name, avatar_url)")) {
                filter {
                    eq("exhibition_id", exhibitionId)
                    eq("is_approved", true)
                }
                order("created_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<ThoughtDto>()
            .map { it.toDomain() }

    override suspend fun submitThought(exhibitionId: String, content: String) {
        supabaseClient.postgrest
            .from("thoughts")
            .insert(ThoughtInsert(exhibitionId = exhibitionId, content = content))
    }

    override suspend fun updateThought(thoughtId: String, content: String) {
        supabaseClient.postgrest
            .from("thoughts")
            .update({ set("content", content) }) {
                filter { eq("id", thoughtId) }
            }
    }

    override suspend fun deleteThought(thoughtId: String) {
        supabaseClient.postgrest
            .from("thoughts")
            .delete { filter { eq("id", thoughtId) } }
    }

    override suspend fun getUserThoughtForExhibition(exhibitionId: String): Thought? {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return null
        return supabaseClient.postgrest
            .from("thoughts")
            .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, profiles(display_name, avatar_url)")) {
                filter {
                    eq("exhibition_id", exhibitionId)
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<ThoughtDto>()
            ?.toDomain()
    }

    override suspend fun getUserThoughtCount(userId: String): Int =
        supabaseClient.postgrest
            .from("thoughts")
            .select { filter { eq("user_id", userId) } }
            .decodeList<ThoughtDto>()
            .size
}
