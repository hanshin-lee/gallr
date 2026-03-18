package com.gallr.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import gallr.composeapp.generated.resources.JetBrainsMono_Regular
import gallr.composeapp.generated.resources.PlayfairDisplay_Bold
import gallr.composeapp.generated.resources.PlayfairDisplay_BoldItalic
import gallr.composeapp.generated.resources.PlayfairDisplay_Italic
import gallr.composeapp.generated.resources.PlayfairDisplay_Regular
import gallr.composeapp.generated.resources.Res
import gallr.composeapp.generated.resources.SourceSerif4_Bold
import gallr.composeapp.generated.resources.SourceSerif4_Regular
import org.jetbrains.compose.resources.Font

// ── Font families (must be created inside a @Composable) ─────────────────────

@Composable
private fun playfairDisplay() = FontFamily(
    Font(Res.font.PlayfairDisplay_Regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.PlayfairDisplay_Bold, FontWeight.Bold, FontStyle.Normal),
    Font(Res.font.PlayfairDisplay_Italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.PlayfairDisplay_BoldItalic, FontWeight.Bold, FontStyle.Italic),
)

@Composable
private fun sourceSerif4() = FontFamily(
    Font(Res.font.SourceSerif4_Regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.SourceSerif4_Bold, FontWeight.Bold, FontStyle.Normal),
)

@Composable
private fun jetBrainsMono() = FontFamily(
    Font(Res.font.JetBrainsMono_Regular, FontWeight.Normal, FontStyle.Normal),
)

// ── Typography scale ──────────────────────────────────────────────────────────

@Composable
fun gallrTypography(): Typography {
    val display = playfairDisplay()
    val serif = sourceSerif4()
    val mono = jetBrainsMono()

    return Typography(
        // Display — PlayfairDisplay, large headers
        displayLarge = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 56.sp,
            letterSpacing = (-0.025).em,
            lineHeight = 64.sp,
        ),
        displayMedium = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            letterSpacing = (-0.025).em,
            lineHeight = 48.sp,
        ),
        displaySmall = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            letterSpacing = (-0.025).em,
            lineHeight = 40.sp,
        ),
        // Headline — PlayfairDisplay, section titles
        headlineLarge = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            letterSpacing = (-0.025).em,
            lineHeight = 48.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            letterSpacing = (-0.025).em,
            lineHeight = 40.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            letterSpacing = (-0.025).em,
            lineHeight = 32.sp,
        ),
        // Title — PlayfairDisplay for card names
        titleLarge = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            letterSpacing = (-0.025).em,
            lineHeight = 32.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            letterSpacing = 0.em,
            lineHeight = 28.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            letterSpacing = 0.em,
            lineHeight = 24.sp,
        ),
        // Body — SourceSerif4 for readable prose
        bodyLarge = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            letterSpacing = 0.em,
            lineHeight = 28.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            letterSpacing = 0.em,
            lineHeight = 24.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            letterSpacing = 0.em,
            lineHeight = 20.sp,
        ),
        // Label — JetBrainsMono for dates, metadata, chips
        labelLarge = TextStyle(
            fontFamily = mono,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            letterSpacing = 0.1.em,
            lineHeight = 20.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = mono,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            letterSpacing = 0.1.em,
            lineHeight = 16.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = mono,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            letterSpacing = 0.1.em,
            lineHeight = 16.sp,
        ),
    )
}
