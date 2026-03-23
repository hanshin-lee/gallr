package com.gallr.app.ui.tabs.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gallr.app.ui.components.ExhibitionCard
import com.gallr.app.ui.components.GallrEmptyState
import com.gallr.app.ui.components.GallrLoadingState
import com.gallr.app.ui.theme.GallrAccent
import com.gallr.app.ui.theme.GallrSpacing
import com.gallr.app.viewmodel.ExhibitionListState
import com.gallr.app.viewmodel.TabsViewModel
import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.Exhibition
import com.gallr.shared.data.model.FilterState

@Composable
fun ListScreen(
    viewModel: TabsViewModel,
    onExhibitionTap: (Exhibition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filter by viewModel.filterState.collectAsState()
    val state by viewModel.filteredExhibitions.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val lang by viewModel.language.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val distinctCities by viewModel.distinctCities.collectAsState()
    val showMyListOnly by viewModel.showMyListOnly.collectAsState()

    val hasActiveFilters = filter != FilterState() || selectedCity != null

    Column(modifier = modifier.fillMaxSize()) {
        // ── Segmented control: All Exhibitions / My List ──────────────────
        SegmentedControl(
            showMyListOnly = showMyListOnly,
            onSelectAll = { viewModel.setShowMyListOnly(false) },
            onSelectMyList = { viewModel.setShowMyListOnly(true) },
            lang = lang,
        )

        // ── Country + city chips (single scrollable row) ─────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = GallrSpacing.screenMargin, vertical = GallrSpacing.xs),
        ) {
            CountryDropdown(lang = lang)
            Spacer(Modifier.width(GallrSpacing.sm))
            GallrFilterChip(
                selected = selectedCity == null,
                onClick = { viewModel.setCity(null) },
                label = if (lang == AppLanguage.KO) "전체" else "All",
            )
            Spacer(Modifier.width(GallrSpacing.sm))
            distinctCities.forEach { (cityKo, cityEn) ->
                GallrFilterChip(
                    selected = selectedCity == cityKo,
                    onClick = { viewModel.setCity(cityKo) },
                    label = if (lang == AppLanguage.KO) cityKo else cityEn.ifEmpty { cityKo },
                )
                Spacer(Modifier.width(GallrSpacing.sm))
            }
        }

        // ── Filter chips (single horizontally scrollable row) ────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = GallrSpacing.screenMargin),
        ) {
            GallrFilterChip(
                selected = filter.showFeatured,
                onClick = { viewModel.updateFilter { copy(showFeatured = !showFeatured) } },
                label = if (lang == AppLanguage.KO) "추천" else "FEATURED",
            )
            Spacer(Modifier.width(GallrSpacing.sm))
            GallrFilterChip(
                selected = filter.showEditorsPick,
                onClick = { viewModel.updateFilter { copy(showEditorsPick = !showEditorsPick) } },
                label = if (lang == AppLanguage.KO) "에디터 픽" else "EDITOR'S PICKS",
            )
            Spacer(Modifier.width(GallrSpacing.sm))
            GallrFilterChip(
                selected = filter.openingThisWeek,
                onClick = { viewModel.updateFilter { copy(openingThisWeek = !openingThisWeek) } },
                label = if (lang == AppLanguage.KO) "이번 주 오픈" else "OPENING THIS WEEK",
            )
            Spacer(Modifier.width(GallrSpacing.sm))
            GallrFilterChip(
                selected = filter.closingThisWeek,
                onClick = { viewModel.updateFilter { copy(closingThisWeek = !closingThisWeek) } },
                label = if (lang == AppLanguage.KO) "이번 주 종료" else "CLOSING THIS WEEK",
            )
        }

        // ── Action buttons ────────────────────────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = GallrSpacing.screenMargin),
        ) {
            if (hasActiveFilters) {
                TextButton(onClick = { viewModel.clearAllFilters() }) {
                    Text(
                        text = if (lang == AppLanguage.KO) "필터 초기화" else "Clear Filters",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (showMyListOnly && bookmarkedIds.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearAllBookmarks() }) {
                    Text(
                        text = if (lang == AppLanguage.KO) "내 리스트 비우기" else "Clear My List",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (!hasActiveFilters && !(showMyListOnly && bookmarkedIds.isNotEmpty())) {
            Spacer(Modifier.height(GallrSpacing.xs))
        }

        // ── Exhibition list ───────────────────────────────────────────────
        when (val s = state) {
            is ExhibitionListState.Loading -> {
                GallrLoadingState(modifier = Modifier.fillMaxWidth())
            }

            is ExhibitionListState.Error -> {
                GallrEmptyState(
                    message = if (lang == AppLanguage.KO) "전시 정보를 불러올 수 없습니다." else "Could not load exhibitions.",
                    actionLabel = if (lang == AppLanguage.KO) "다시 시도" else "Retry",
                    onAction = { viewModel.loadAllExhibitions() },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is ExhibitionListState.Success -> {
                if (s.exhibitions.isEmpty()) {
                    val cityName = selectedCity?.let { city ->
                        if (lang == AppLanguage.KO) city
                        else distinctCities.firstOrNull { it.first == city }?.second?.ifEmpty { city } ?: city
                    }
                    GallrEmptyState(
                        message = when {
                            showMyListOnly && bookmarkedIds.isEmpty() ->
                                if (lang == AppLanguage.KO) "저장한 전시가 없습니다.\n전시를 북마크하면 여기에 표시됩니다."
                                else "No saved exhibitions yet.\nBookmark exhibitions to see them here."
                            showMyListOnly ->
                                if (lang == AppLanguage.KO) "필터에 맞는 저장 전시가 없습니다."
                                else "No saved exhibitions match the current filters."
                            cityName != null ->
                                if (lang == AppLanguage.KO) "${cityName}에 전시가 없습니다."
                                else "No exhibitions in $cityName."
                            else ->
                                if (lang == AppLanguage.KO) "필터에 맞는 전시가 없습니다."
                                else "No exhibitions match the current filters."
                        },
                        actionLabel = if (showMyListOnly && bookmarkedIds.isEmpty()) null
                            else if (lang == AppLanguage.KO) "필터 초기화" else "Clear Filters",
                        onAction = { viewModel.clearAllFilters() },
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
                                onTap = { onExhibitionTap(exhibition) },
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

// ── Segmented control ────────────────────────────────────────────────────────

@Composable
private fun SegmentedControl(
    showMyListOnly: Boolean,
    onSelectAll: () -> Unit,
    onSelectMyList: () -> Unit,
    lang: AppLanguage,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        SegmentTab(
            label = if (lang == AppLanguage.KO) "전체 전시" else "All Exhibitions",
            selected = !showMyListOnly,
            onClick = onSelectAll,
            modifier = Modifier.weight(1f),
        )
        SegmentTab(
            label = if (lang == AppLanguage.KO) "내 리스트" else "My List",
            selected = showMyListOnly,
            onClick = onSelectMyList,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SegmentTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = GallrSpacing.sm),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(GallrSpacing.xs))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    if (selected) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.surfaceVariant,
                ),
        )
    }
}

// ── Country dropdown ─────────────────────────────────────────────────────────

@Composable
private fun CountryDropdown(lang: AppLanguage) {
    var expanded by remember { mutableStateOf(false) }
    val countries = listOf("대한민국" to "South Korea")
    val selected = countries.first()

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(
                text = (if (lang == AppLanguage.KO) selected.first else selected.second) + " ▾",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.background,
            border = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.outline,
            ),
            shape = RectangleShape,
        ) {
            countries.forEach { (ko, en) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (lang == AppLanguage.KO) ko else en,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    onClick = { expanded = false },
                )
            }
        }
    }
}

// ── Filter chip ──────────────────────────────────────────────────────────────

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
