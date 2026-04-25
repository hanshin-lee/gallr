package com.gallr.shared.notifications

import com.gallr.shared.repository.BookmarkRepository
import com.gallr.shared.repository.ExhibitionRepository
import com.gallr.shared.repository.LanguageRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

class NotificationSyncService(
    private val scheduler: NotificationScheduler,
    private val exhibitionRepo: ExhibitionRepository,
    private val bookmarkRepo: BookmarkRepository,
    private val languageRepo: LanguageRepository,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    suspend fun sync() {
        if (!scheduler.hasPermission()) return

        val bookmarkIds = bookmarkRepo.observeBookmarkedIds().first()
        if (bookmarkIds.isEmpty()) {
            scheduler.cancelAll()
            return
        }

        val language = languageRepo.observeLanguage().first()
        val now = clock.now()

        val all = exhibitionRepo.getExhibitions().getOrElse { emptyList() }
        val bookmarked = all.filter { it.id in bookmarkIds }

        val desired = buildSet {
            bookmarked.forEach { ex ->
                addAll(TriggerRules.computeTriggers(ex, now, timeZone, language))
            }
            add(TriggerRules.inactivitySpec(now, timeZone, language))
        }
        val desiredById = desired.associateBy { it.id }
        val existing = scheduler.scheduledIds()

        for (id in existing - desiredById.keys) {
            scheduler.cancel(id)
        }
        for (spec in desired) {
            if (spec.id !in existing) {
                scheduler.schedule(spec)
            }
        }
    }
}
