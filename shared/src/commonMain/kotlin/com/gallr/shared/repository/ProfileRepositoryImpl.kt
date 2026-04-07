package com.gallr.shared.repository

import com.gallr.shared.data.model.Profile
import com.gallr.shared.data.network.dto.ProfileDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class ProfileRepositoryImpl(
    private val supabaseClient: SupabaseClient,
) : ProfileRepository {

    override suspend fun getProfile(userId: String): Profile? =
        supabaseClient.postgrest
            .from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<ProfileDto>()
            ?.toDomain()

    override suspend fun updateProfile(userId: String, displayName: String, bio: String) {
        supabaseClient.postgrest
            .from("profiles")
            .update({
                set("display_name", displayName)
                set("bio", bio)
            }) { filter { eq("id", userId) } }
    }

    /**
     * Fallback profile creation if the database trigger failed.
     * Uses upsert so it's safe to call even when the profile already exists.
     */
    override suspend fun ensureProfileExists(userId: String, displayName: String, avatarUrl: String?) {
        supabaseClient.postgrest
            .from("profiles")
            .upsert(ProfileDto(
                id = userId,
                displayName = displayName,
                avatarUrl = avatarUrl,
            )) { onConflict = "id" }
    }
}
