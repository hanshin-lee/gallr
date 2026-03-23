package com.gallr.app.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.gallr.app.ui.components.BookmarkButton
import com.gallr.app.ui.theme.GallrSpacing
import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.Exhibition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExhibitionDetailScreen(
    exhibition: Exhibition,
    lang: AppLanguage,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onLanguageToggle: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                actions = {
                    BookmarkButton(
                        isBookmarked = isBookmarked,
                        onToggle = onBookmarkToggle,
                        tintColor = MaterialTheme.colorScheme.onBackground,
                    )
                    IconButton(onClick = onLanguageToggle) {
                        Text(
                            text = if (lang == AppLanguage.KO) "KO" else "EN",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Cover image ────────────────────────────────────────────────
            exhibition.coverImageUrl?.let { url ->
                if (url.isNotBlank()) {
                    AsyncImage(
                        model = url,
                        contentDescription = exhibition.localizedName(lang),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                    )
                    Spacer(Modifier.height(GallrSpacing.md))
                }
            }

            Column(modifier = Modifier.padding(horizontal = GallrSpacing.screenMargin)) {
                // ── Exhibition name ────────────────────────────────────────
                Text(
                    text = exhibition.localizedName(lang),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(Modifier.height(GallrSpacing.sm))

                // ── Venue ──────────────────────────────────────────────────
                Text(
                    text = exhibition.localizedVenueName(lang).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(GallrSpacing.xs))

                // ── City / Region ──────────────────────────────────────────
                Text(
                    text = "${exhibition.localizedCity(lang)}, ${exhibition.localizedRegion(lang)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // ── Address ────────────────────────────────────────────────
                val address = exhibition.localizedAddress(lang)
                if (address.isNotBlank()) {
                    Spacer(Modifier.height(GallrSpacing.xs))
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(GallrSpacing.md))
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(GallrSpacing.md))

                // ── Date range ─────────────────────────────────────────────
                Text(
                    text = "${exhibition.openingDate} – ${exhibition.closingDate}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                // ── Description ────────────────────────────────────────────
                val description = exhibition.localizedDescription(lang)
                if (description.isNotBlank()) {
                    Spacer(Modifier.height(GallrSpacing.md))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                Spacer(Modifier.height(GallrSpacing.lg))
            }
        }
    }
}
