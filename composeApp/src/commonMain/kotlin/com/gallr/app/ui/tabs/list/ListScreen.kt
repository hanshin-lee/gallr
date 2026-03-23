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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.gallr.app.ui.components.ExhibitionCard
import com.gallr.app.ui.components.GallrEmptyState
import com.gallr.app.ui.components.GallrLoadingState
import com.gallr.app.ui.theme.GallrAccent
import com.gallr.app.ui.theme.GallrSpacing
import com.gallr.app.viewmodel.ExhibitionListState
import com.gallr.app.viewmodel.TabsViewModel
import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.FilterState

@Composable
fun ListScreen(
    viewModel: TabsViewModel,
    modifier: Modifier = Modifier,
) {
    val filter by viewModel.filterState.collectAsState()
    val state by viewModel.filteredExhibitions.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val lang by viewModel.language.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = GallrSpacing.screenMargin, vertical = GallrSpacing.sm)) {
            Text(
                text = if (lang == AppLanguage.KO) "필터" else "FILTERS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(GallrSpacing.sm))
            Row {
                GallrFilterChip(
                    selected = filter.showFeatured,
                    onClick = { viewModel.updateFilter { copy(showFeatured = !showFeatured) } },
                    label = if (lang == AppLanguage.KO) "추천" else "FEATURED",
                    modifier = Modifier.padding(end = GallrSpacing.sm),
                )
                GallrFilterChip(
                    selected = filter.showEditorsPick,
                    onClick = { viewModel.updateFilter { copy(showEditorsPick = !showEditorsPick) } },
                    label = if (lang == AppLanguage.KO) "에디터 픽" else "EDITOR'S PICKS",
                    modifier = Modifier.padding(end = GallrSpacing.sm),
                )
            }
            Row {
                GallrFilterChip(
                    selected = filter.openingThisWeek,
                    onClick = { viewModel.updateFilter { copy(openingThisWeek = !openingThisWeek) } },
                    label = if (lang == AppLanguage.KO) "이번 주 오픈" else "OPENING THIS WEEK",
                    modifier = Modifier.padding(end = GallrSpacing.sm),
                )
                GallrFilterChip(
                    selected = filter.closingThisWeek,
                    onClick = { viewModel.updateFilter { copy(closingThisWeek = !closingThisWeek) } },
                    label = if (lang == AppLanguage.KO) "이번 주 마감" else "CLOSING THIS WEEK",
                )
            }
        }

        Spacer(Modifier.height(GallrSpacing.sm))

        when (val s = state) {
            is ExhibitionListState.Loading -> {
                GallrLoadingState(modifier = Modifier.fillMaxWidth())
            }

            is ExhibitionListState.Error -> {
                GallrEmptyState(
                    message = if (lang == AppLanguage.KO) "전시 정보를 불러올 수 없습니다." else "Could not load exhibitions.",
                    actionLabel = if (lang == AppLanguage.KO) "다시 시도" else "Retry",
                    onAction = { /* retry handled by ViewModel */ },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is ExhibitionListState.Success -> {
                if (s.exhibitions.isEmpty()) {
                    GallrEmptyState(
                        message = if (lang == AppLanguage.KO) "필터에 맞는 전시가 없습니다." else "No exhibitions match the current filters.",
                        actionLabel = if (lang == AppLanguage.KO) "필터 초기화" else "Clear Filters",
                        onAction = { viewModel.updateFilter { FilterState() } },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(GallrSpacing.md),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(s.exhibitions, key = { it.id }) { exhibition ->
                            ExhibitionCard(
                                exhibition = exhibition,
                                isBookmarked = exhibition.id in bookmarkedIds,
                                onBookmarkToggle = { viewModel.toggleBookmark(exhibition.id) },
                                lang = lang,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = GallrSpacing.md),
                            )
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
            containerColor = MaterialTheme.colorScheme.background,
            labelColor = MaterialTheme.colorScheme.onBackground,
            selectedContainerColor = GallrAccent.activeIndicator,
            selectedLabelColor = MaterialTheme.colorScheme.background,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = GallrAccent.activeIndicator,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp,
        ),
    )
}
