package com.gallr.app.ui.tabs.featured

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gallr.app.ui.components.ExhibitionCard
import com.gallr.app.ui.components.GallrEmptyState
import com.gallr.app.ui.components.GallrLoadingState
import com.gallr.app.ui.theme.GallrMotion
import com.gallr.app.viewmodel.ExhibitionListState
import com.gallr.app.viewmodel.TabsViewModel

@Composable
fun FeaturedScreen(
    viewModel: TabsViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.featuredState.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()

    // Staggered entry animation trigger (US4 — FR-011)
    var listVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { listVisible = true }

    Column(modifier = modifier.fillMaxSize()) {
        // ── Section header (US3) ──────────────────────────────────────────
        Text(
            text = "FEATURED",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        // 4dp black rule separating section header from card list
        HorizontalDivider(
            thickness = 4.dp,
            color = MaterialTheme.colorScheme.onBackground,
        )

        when (val s = state) {
            is ExhibitionListState.Loading -> {
                // FR-008: thin animated horizontal line, not a spinner
                GallrLoadingState(modifier = Modifier.fillMaxWidth())
            }

            is ExhibitionListState.Error -> {
                GallrEmptyState(
                    message = "Could not load exhibitions.",
                    actionLabel = "Retry",
                    onAction = { viewModel.loadFeaturedExhibitions() },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is ExhibitionListState.Success -> {
                if (s.exhibitions.isEmpty()) {
                    GallrEmptyState(
                        message = "No featured exhibitions right now.",
                        actionLabel = "Refresh",
                        onAction = { viewModel.loadFeaturedExhibitions() },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(s.exhibitions, key = { _, it -> it.id }) { index, exhibition ->
                            // FR-011: staggered reveal — 8dp slide-in, 200ms, 50ms delay per item
                            AnimatedVisibility(
                                visible = listVisible,
                                enter = slideInVertically(
                                    animationSpec = tween(
                                        durationMillis = GallrMotion.staggeredItemDurationMs,
                                        delayMillis = index * GallrMotion.staggeredItemDelayMs,
                                    ),
                                    initialOffsetY = { it },
                                ) + fadeIn(
                                    animationSpec = tween(
                                        durationMillis = GallrMotion.staggeredItemDurationMs,
                                        delayMillis = index * GallrMotion.staggeredItemDelayMs,
                                    ),
                                ),
                            ) {
                                ExhibitionCard(
                                    exhibition = exhibition,
                                    isBookmarked = exhibition.id in bookmarkedIds,
                                    onBookmarkToggle = { viewModel.toggleBookmark(exhibition.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
