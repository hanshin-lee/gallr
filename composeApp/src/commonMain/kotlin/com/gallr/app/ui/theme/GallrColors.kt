package com.gallr.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Monochrome palette ────────────────────────────────────────────────────────
// Rule: only black, white, off-white, and a single secondary gray. No accent colors.

private val Black = Color(0xFF000000)
private val White = Color(0xFFFFFFFF)
private val OffWhite = Color(0xFFF5F5F5)          // muted surface
private val SecondaryGray = Color(0xFF525252)      // mutedForeground — secondary text
private val BorderLight = Color(0xFFE5E5E5)        // hairline dividers

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
