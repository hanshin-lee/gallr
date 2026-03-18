package com.gallr.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gallr.app.ui.theme.GallrMotion
import com.gallr.shared.data.model.Exhibition

@Composable
fun ExhibitionCard(
    exhibition: Exhibition,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Press state — detectTapGestures, NOT collectIsPressedAsState (CMP bug #3417) ──
    var isPressed by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.onBackground
        else MaterialTheme.colorScheme.background,
        animationSpec = tween(GallrMotion.pressDurationMs),
        label = "cardBackground",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.background
        else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(GallrMotion.pressDurationMs),
        label = "cardContent",
    )
    val secondaryColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.background
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(GallrMotion.pressDurationMs),
        label = "cardSecondary",
    )
    val dividerColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.background
        else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(GallrMotion.pressDurationMs),
        label = "cardDivider",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                )
            },
        shape = RectangleShape,
        color = backgroundColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // ── Exhibition name: dominant serif, PlayfairDisplay Bold ────
                Text(
                    text = exhibition.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))

                // ── Venue & city: JetBrainsMono, uppercase, letter-spaced ───
                Text(
                    text = exhibition.venueName.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = exhibition.city.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryColor,
                )

                Spacer(Modifier.height(8.dp))

                // ── Hairline divider between metadata and date ───────────────
                HorizontalDivider(thickness = 1.dp, color = dividerColor)

                Spacer(Modifier.height(8.dp))

                // ── Date range: JetBrainsMono ────────────────────────────────
                Text(
                    text = "${exhibition.openingDate} – ${exhibition.closingDate}",
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                )
            }

            BookmarkButton(
                isBookmarked = isBookmarked,
                onToggle = onBookmarkToggle,
                tintColor = contentColor,
            )
        }
    }
}
