package com.gallr.app.ui.detail

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.gallr.app.ui.components.BookmarkButton
import com.gallr.app.ui.theme.GallrAccent
import com.gallr.app.ui.theme.GallrSpacing
import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.Exhibition
import com.gallr.shared.data.model.exhibitionStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExhibitionDetailScreen(
    exhibition: Exhibition,
    lang: AppLanguage,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
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
            // ── Cover image with placeholder ─────────────────────────────
            exhibition.coverImageUrl?.let { url ->
                if (url.isNotBlank()) {
                    AsyncImage(
                        model = url,
                        contentDescription = exhibition.localizedName(lang),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
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

                // ── Date range (localized) ────────────────────────────────
                Text(
                    text = exhibition.localizedDateRange(lang),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                // ── Status label (Upcoming / Closing Soon) ──────────────
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val statusLabel = exhibitionStatus(
                    exhibition.openingDate, exhibition.closingDate, today,
                ).label(lang)
                if (statusLabel != null) {
                    Spacer(Modifier.height(GallrSpacing.sm))
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = GallrAccent.activeIndicator,
                    )
                }

                // ── Reception date (orange label) ────────────────────────
                val receptionLabel = exhibition.receptionDate?.let {
                    receptionDateLabel(it, exhibition.closingDate, lang)
                }
                if (receptionLabel != null) {
                    Spacer(Modifier.height(GallrSpacing.sm))
                    Text(
                        text = receptionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = GallrAccent.activeIndicator,
                    )
                }

                // ── Hours ────────────────────────────────────────────────
                val hours = exhibition.hours
                if (!hours.isNullOrBlank()) {
                    Spacer(Modifier.height(GallrSpacing.sm))
                    Text(
                        text = hours,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // ── Contact (tappable mailto: or tel:) ──────────────────
                val contact = exhibition.contact
                if (!contact.isNullOrBlank()) {
                    val uriHandler = LocalUriHandler.current
                    val isEmail = contact.trim().matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
                    val isPhone = !isEmail && contact.trim().matches(Regex("^[+\\d\\s()-]+$"))
                    val uri = when {
                        isEmail -> "mailto:${contact.trim()}"
                        isPhone -> "tel:${contact.trim().replace(Regex("[\\s()-]"), "")}"
                        else -> null
                    }
                    Spacer(Modifier.height(GallrSpacing.sm))
                    Text(
                        text = contact,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uri != null) GallrAccent.activeIndicator
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = if (uri != null) Modifier.clickable {
                            try { uriHandler.openUri(uri) } catch (_: Exception) { /* no-op */ }
                        } else Modifier,
                    )
                }

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

// ── Reception date label logic ──────────────────────────────────────────────
// Returns null when the label should be hidden.
private fun receptionDateLabel(
    receptionDate: LocalDate,
    closingDate: LocalDate,
    lang: AppLanguage,
): String? {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    // Exhibition ended → hide
    if (closingDate < today) return null

    // Find Monday of the current week
    val daysSinceMonday = (today.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
    val thisMonday = today.plus(-daysSinceMonday, DateTimeUnit.DAY)
    val nextMonday = thisMonday.plus(7, DateTimeUnit.DAY)

    return when {
        // More than 1 week away → hide
        receptionDate >= nextMonday -> null
        // Today
        receptionDate == today -> if (lang == AppLanguage.KO) "오프닝 오늘" else "Opening today"
        // Tomorrow
        receptionDate == today.plus(1, DateTimeUnit.DAY) -> if (lang == AppLanguage.KO) "오프닝 내일" else "Opening tomorrow"
        // Within this week (future)
        receptionDate in thisMonday..< nextMonday && receptionDate > today -> {
            val dayName = when (receptionDate.dayOfWeek) {
                DayOfWeek.MONDAY -> if (lang == AppLanguage.KO) "월요일" else "Monday"
                DayOfWeek.TUESDAY -> if (lang == AppLanguage.KO) "화요일" else "Tuesday"
                DayOfWeek.WEDNESDAY -> if (lang == AppLanguage.KO) "수요일" else "Wednesday"
                DayOfWeek.THURSDAY -> if (lang == AppLanguage.KO) "목요일" else "Thursday"
                DayOfWeek.FRIDAY -> if (lang == AppLanguage.KO) "금요일" else "Friday"
                DayOfWeek.SATURDAY -> if (lang == AppLanguage.KO) "토요일" else "Saturday"
                DayOfWeek.SUNDAY -> if (lang == AppLanguage.KO) "일요일" else "Sunday"
            }
            if (lang == AppLanguage.KO) "오프닝 $dayName" else "Opening $dayName"
        }
        // Past but exhibition still running → show full date
        receptionDate < today -> {
            val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            if (lang == AppLanguage.KO) {
                "오프닝 ${receptionDate.monthNumber}월 ${receptionDate.dayOfMonth}일"
            } else {
                "Opening ${months[receptionDate.monthNumber - 1]} ${receptionDate.dayOfMonth}"
            }
        }
        else -> null
    }
}
