package com.gallr.app.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

// Using Unicode bookmark symbol via text to avoid requiring compose-icons dependency.
// Replace with Icons.Default.Bookmark / BookmarkBorder once icons are added.
@Composable
fun BookmarkButton(
    isBookmarked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onToggle, modifier = modifier) {
        androidx.compose.material3.Text(
            text = if (isBookmarked) "🔖" else "🏷",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
