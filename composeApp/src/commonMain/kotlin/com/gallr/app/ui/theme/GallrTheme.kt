package com.gallr.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// All shapes are sharp (0dp radius) — core requirement of Minimalist Monochrome.
private val GallrShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

@Composable
fun GallrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = gallrColorScheme(),
        typography = gallrTypography(),
        shapes = GallrShapes,
        content = content,
    )
}
