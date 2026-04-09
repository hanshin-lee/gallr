@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.gallr.shared.repository

import com.gallr.shared.data.model.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest

/**
 * BookmarkRepository wrapper that delegates to local DataStore (anonymous)
 * or cloud Supabase (authenticated) based on the current auth state.
 *
 * The auth state flow is injected so this repository reacts automatically
 * when the user signs in or out.
 */
class SyncBookmarkRepository(
    private val localRepository: BookmarkRepositoryImpl,
    private val cloudRepository: CloudBookmarkRepository,
    private val authState: StateFlow<AuthState>,
) : BookmarkRepository {

    private val isAuthenticated: Boolean
        get() = authState.value is AuthState.Authenticated

    override fun observeBookmarkedIds(): Flow<Set<String>> =
        authState.flatMapLatest { state ->
            when (state) {
                is AuthState.Authenticated -> cloudRepository.observeBookmarkedIds()
                else -> localRepository.observeBookmarkedIds()
            }
        }

    override suspend fun addBookmark(exhibitionId: String) {
        // Always write to local (offline cache)
        localRepository.addBookmark(exhibitionId)
        if (isAuthenticated) {
            // Optimistic update: update cloud StateFlow immediately for responsive UI
            cloudRepository.optimisticAdd(exhibitionId)
            try {
                cloudRepository.addBookmark(exhibitionId)
            } catch (_: Exception) {
                // Network failure — local write succeeded, cloud syncs on next foreground
            }
        }
    }

    override suspend fun removeBookmark(exhibitionId: String) {
        localRepository.removeBookmark(exhibitionId)
        if (isAuthenticated) {
            // Optimistic update: update cloud StateFlow immediately for responsive UI
            cloudRepository.optimisticRemove(exhibitionId)
            try {
                cloudRepository.removeBookmark(exhibitionId)
            } catch (_: Exception) {
                // Network failure — local delete succeeded, cloud syncs on next foreground
            }
        }
    }

    override suspend fun isBookmarked(exhibitionId: String): Boolean =
        if (isAuthenticated) cloudRepository.isBookmarked(exhibitionId)
        else localRepository.isBookmarked(exhibitionId)

    override suspend fun clearAll() {
        localRepository.clearAll()
        // Cloud bookmarks are not cleared — use delete account for that
    }

    /**
     * Migrate local bookmarks to cloud on first login.
     * Called once after successful authentication.
     */
    suspend fun migrateLocalToCloud() {
        val localIds = localRepository.observeBookmarkedIds().first()
        if (localIds.isNotEmpty()) {
            cloudRepository.bulkInsert(localIds)
        }
        cloudRepository.refresh()
    }
}
