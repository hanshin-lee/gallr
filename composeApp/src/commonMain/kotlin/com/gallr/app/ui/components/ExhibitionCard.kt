package com.gallr.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gallr.shared.data.model.Exhibition

@Composable
fun ExhibitionCard(
    exhibition: Exhibition,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exhibition.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = exhibition.venueName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = exhibition.city,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${exhibition.openingDate} – ${exhibition.closingDate}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            BookmarkButton(
                isBookmarked = isBookmarked,
                onToggle = onBookmarkToggle,
            )
        }
    }
}
