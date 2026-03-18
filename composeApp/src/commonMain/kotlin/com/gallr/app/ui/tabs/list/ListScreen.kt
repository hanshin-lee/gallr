package com.gallr.app.ui.tabs.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.gallr.app.ui.components.ExhibitionCard
import com.gallr.app.ui.components.GallrEmptyState
import com.gallr.app.ui.components.GallrLoadingState
import com.gallr.app.ui.theme.GallrMotion
import com.gallr.app.viewmodel.ExhibitionListState
import com.gallr.app.viewmodel.TabsViewModel
import com.gallr.shared.data.model.FilterState

@Composable
fun ListScreen(
    viewModel: TabsViewModel,
    modifier: Modifier = Modifier,
) {
    val filter by viewModel.filterState.collectAsState()
    val state by viewModel.filteredExhibitions.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()

    // Staggered entry animation trigger (US4)
    var listVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { listVisible = true }

    Column(modifier = modifier.fillMaxSize()) {
        // ── Filter chips ──────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "FILTERS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Row {
                GallrFilterChip(
                    selected = filter.showFeatured,
                    onClick = { viewModel.updateFilter { copy(showFeatured = !showFeatured) } },
                    label = "FEATURED",
                    modifier = Modifier.padding(end = 8.dp),
                )
                GallrFilterChip(
                    selected = filter.showEditorsPick,
                    onClick = { viewModel.updateFilter { copy(showEditorsPick = !showEditorsPick) } },
                    label = "EDITOR'S PICKS",
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            Row {
                GallrFilterChip(
                    selected = filter.openingThisWeek,
                    onClick = { viewModel.updateFilter { copy(openingThisWeek = !openingThisWeek) } },
                    label = "OPENING THIS WEEK",
                    modifier = Modifier.padding(end = 8.dp),
                )
                GallrFilterChip(
                    selected = filter.closingThisWeek,
                    onClick = { viewModel.updateFilter { copy(closingThisWeek = !closingThisWeek) } },
                    label = "CLOSING THIS WEEK",
                )
            }
        }

        // ── 4dp black section rule separating filters from results (US3) ──
        HorizontalDivider(
            thickness = 4.dp,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // ── Exhibition list ───────────────────────────────────────────────
        when (val s = state) {
            is ExhibitionListState.Loading -> {
                GallrLoadingState(modifier = Modifier.fillMaxWidth())
            }

            is ExhibitionListState.Error -> {
                GallrEmptyState(
                    message = "Could not load exhibitions.",
                    actionLabel = "Retry",
                    onAction = { /* retry handled by ViewModel */ },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is ExhibitionListState.Success -> {
                if (s.exhibitions.isEmpty()) {
                    GallrEmptyState(
                        message = "No exhibitions match the current filters.",
                        actionLabel = "Clear Filters",
                        onAction = { viewModel.updateFilter { FilterState() } },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(s.exhibitions, key = { _, it -> it.id }) { index, exhibition ->
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

@Composable
private fun GallrFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    // FR-010: inactive = white bg + black border + black text; active = black bg + white text
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        modifier = modifier,
        shape = RectangleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            labelColor = Color.Black,
            selectedContainerColor = Color.Black,
            selectedLabelColor = Color.White,
            disabledContainerColor = Color.White,
            disabledLabelColor = Color.Black,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.Black,
            selectedBorderColor = Color.Black,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp,
        ),
    )
}
