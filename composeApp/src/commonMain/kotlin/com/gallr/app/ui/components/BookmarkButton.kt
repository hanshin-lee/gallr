package com.gallr.app.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Monochrome bookmark button.
 *
 * ■ = bookmarked (filled), □ = not bookmarked (outlined).
 * tintColor flows from ExhibitionCard to support press-inversion.
 * detectTapGestures used for CMP iOS compatibility (avoids collectIsPressedAsState bug).
 */
@Composable
fun BookmarkButton(
    isBookmarked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    tintColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    var isPressed by remember { mutableStateOf(false) }

    IconButton(
        onClick = onToggle,
        modifier = modifier
            .sizeIn(minWidth = 44.dp, minHeight = 44.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                )
            },
    ) {
        Text(
            text = if (isBookmarked) "■" else "□",
            color = tintColor,
            fontSize = 20.sp,
        )
    }
}
