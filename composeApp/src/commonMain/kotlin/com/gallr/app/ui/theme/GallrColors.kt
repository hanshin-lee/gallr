package com.gallr.app.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Monochrome base palette ────────────────────────────────────────────────────
private val Black = Color(0xFF000000)
private val White = Color(0xFFFFFFFF)
private val OffWhite = Color(0xFFF5F5F5)          // muted surface
private val SecondaryGray = Color(0xFF525252)      // secondary text
private val BorderLight = Color(0xFFE5E5E5)        // hairline dividers

// ── Accent (single, intentional — use only for the three permitted roles below) ─
// Rule: NEVER for backgrounds, large surfaces, decoration, or text on small targets.
private val Accent = Color(0xFFFF5400)

/**
 * Semantic accent tokens.
 *
 * All three roles resolve to the same #FF5400 value.
 * Named aliases make intent explicit and prevent accidental overuse.
 */
object GallrAccent {
    /** Fill color for primary call-to-action buttons. */
    val ctaPrimary: Color = Accent

    /** Indicator color for the active tab and selected filter chips. */
    val activeIndicator: Color = Accent

    /** Color shift for immediate pressed/active feedback on primary controls. */
    val interactionFeedback: Color = Accent
}

fun gallrColorScheme() = lightColorScheme(
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = OffWhite,
    onSurfaceVariant = SecondaryGray,
    primary = Black,
    onPrimary = White,
    primaryContainer = Black,
    onPrimaryContainer = White,
    secondary = Black,
    onSecondary = White,
    secondaryContainer = OffWhite,
    onSecondaryContainer = Black,
    tertiary = SecondaryGray,
    onTertiary = White,
    error = Black,
    onError = White,
    outline = Black,               // card borders (1dp)
    outlineVariant = BorderLight,  // hairline dividers (1dp subtle)
    scrim = Black,
    inverseSurface = Black,
    inverseOnSurface = White,
    inversePrimary = White,
)
