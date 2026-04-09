@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.gallr.shared.repository

import com.gallr.shared.data.model.AuthState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest

/**
 * BookmarkRepository wrapper that delegates to local DataStore (anonymous)
 * or cloud Supabase (authenticated) based on the current auth state.
 *
 * When authenticated, local bookmarks act as an immediate cache: the user
 * sees their bookmarks instantly while the cloud refresh happens in the
 * background. Once the cloud loads, the union of both sources is shown
 * so nothing disappears during the transition.
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
                is AuthState.Authenticated -> {
                    // Combine local (instant, disk-cached) + cloud (network-loaded)
                    // so the user always sees at least their local bookmarks while
                    // the cloud refresh completes.
                    combine(
                        localRepository.observeBookmarkedIds(),
                        cloudRepository.observeBookmarkedIds(),
                    ) { local, cloud -> local + cloud }
                }
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
        if (isAuthenticated) {
            cloudRepository.isBookmarked(exhibitionId) ||
                localRepository.isBookmarked(exhibitionId)
        } else {
            localRepository.isBookmarked(exhibitionId)
        }

    override suspend fun clearAll() {
        localRepository.clearAll()
        // Cloud bookmarks are not cleared — use delete account for that
    }

    /**
     * Migrate local bookmarks to cloud and refresh cloud state.
     * Called after successful authentication. The bulk insert may fail
     * (RLS policy, network) but refresh must still run so the user
     * sees their cloud bookmarks.
     */
    suspend fun migrateLocalToCloud() {
        val localIds = localRepository.observeBookmarkedIds().first()
        if (localIds.isNotEmpty()) {
            try {
                cloudRepository.bulkInsert(localIds)
            } catch (e: Exception) {
                println("WARN [SyncBookmarkRepository] Local→cloud migration failed (RLS or network): ${e.message}")
            }
        }
        refreshCloudWithRetry()
    }

    /**
     * Refresh cloud bookmarks with retry. On failure the local cache
     * still shows via the combine() in observeBookmarkedIds().
     */
    suspend fun refreshCloudWithRetry(maxAttempts: Int = 3) {
        var lastError: Exception? = null
        repeat(maxAttempts) { attempt ->
            try {
                cloudRepository.refresh()
                return
            } catch (e: Exception) {
                lastError = e
                if (attempt < maxAttempts - 1) {
                    delay(1000L * (attempt + 1))
                }
            }
        }
        println("WARN [SyncBookmarkRepository] Cloud refresh failed after $maxAttempts attempts: ${lastError?.message}")
    }
}
