package com.gallr.shared.repository

import com.gallr.shared.data.model.AuthState
import com.gallr.shared.data.model.GallrUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val supabaseClient: SupabaseClient,
) : AuthRepository {

    override fun observeAuthState(): Flow<AuthState> =
        supabaseClient.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    // session.user is null on iOS — always fetch from API
                    val user = try {
                        supabaseClient.auth.retrieveUserForCurrentSession()
                    } catch (_: Exception) {
                        status.session.user
                    }

                    val meta = user?.userMetadata
                    val displayName = (meta?.get("full_name") ?: meta?.get("name"))
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?: ""
                    val avatarUrl = (meta?.get("avatar_url") ?: meta?.get("picture"))
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?.takeIf { it.isNotBlank() && it != "null" }

                    AuthState.Authenticated(
                        GallrUser(
                            id = user?.id ?: "",
                            displayName = displayName,
                            avatarUrl = avatarUrl,
                        )
                    )
                }
                is SessionStatus.NotAuthenticated -> AuthState.Anonymous
                is SessionStatus.Initializing -> AuthState.Loading
                is SessionStatus.RefreshFailure -> AuthState.Anonymous
            }
        }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    override suspend fun deleteAccount() {
        // supabase-kt does not expose a client-side deleteUser().
        // Use a Supabase Edge Function or RPC for production.
        // For now, sign out (account deletion requires server-side implementation).
        supabaseClient.auth.signOut()
    }
}
