package com.gallr.app.ui.tabs.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gallr.app.ui.components.ExhibitionCard
import com.gallr.app.viewmodel.ExhibitionListState
import com.gallr.app.viewmodel.TabsViewModel

@Composable
fun ListScreen(
    viewModel: TabsViewModel,
    modifier: Modifier = Modifier,
) {
    val filter by viewModel.filterState.collectAsState()
    val state by viewModel.filteredExhibitions.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // ── Filter chips ──────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text("Filters", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Row {
                FilterChip(
                    selected = filter.showFeatured,
                    onClick = { viewModel.updateFilter { copy(showFeatured = !showFeatured) } },
                    label = { Text("Featured") },
                    modifier = Modifier.padding(end = 8.dp),
                )
                FilterChip(
                    selected = filter.showEditorsPick,
                    onClick = { viewModel.updateFilter { copy(showEditorsPick = !showEditorsPick) } },
                    label = { Text("Editor's Picks") },
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            Row {
                FilterChip(
                    selected = filter.openingThisWeek,
                    onClick = { viewModel.updateFilter { copy(openingThisWeek = !openingThisWeek) } },
                    label = { Text("Opening This Week") },
                    modifier = Modifier.padding(end = 8.dp),
                )
                FilterChip(
                    selected = filter.closingThisWeek,
                    onClick = { viewModel.updateFilter { copy(closingThisWeek = !closingThisWeek) } },
                    label = { Text("Closing This Week") },
                )
            }
            // Region filter: displayed as a simple toggle for the first available region.
            // Full region picker is deferred to a future enhancement.
        }

        Divider()

        // ── Filtered results list ─────────────────────────────────────────
        when (val s = state) {
            is ExhibitionListState.Loading -> {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }

            is ExhibitionListState.Error -> {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Could not load exhibitions.", style = MaterialTheme.typography.bodyLarge)
                }
            }

            is ExhibitionListState.Success -> {
                if (s.exhibitions.isEmpty()) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No exhibitions match the current filters.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(s.exhibitions, key = { it.id }) { exhibition ->
                            ExhibitionCard(
                                exhibition = exhibition,
                                isBookmarked = exhibition.id in bookmarkedIds,
                                onBookmarkToggle = { viewModel.toggleBookmark(exhibition.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
