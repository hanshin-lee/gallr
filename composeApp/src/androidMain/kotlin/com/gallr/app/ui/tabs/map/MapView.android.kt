package com.gallr.app.ui.tabs.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gallr.shared.data.model.ExhibitionMapPin

/**
 * Android stub map implementation.
 *
 * TODO: Replace with Google Maps Compose or Mapbox v9 once the map provider is decided (FR-017).
 * This stub renders a placeholder canvas with dot markers so the project compiles and runs.
 */
@Composable
actual fun MapView(
    pins: List<ExhibitionMapPin>,
    onMarkerTap: (ExhibitionMapPin) -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE0E8D8)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "🗺 Map view (${pins.size} markers)\nMap SDK: TBD",
            style = MaterialTheme.typography.bodyMedium,
        )
        // Stub markers — clicking cycles through pins for verification purposes
        pins.forEachIndexed { index, pin ->
            Box(
                modifier = Modifier
                    .offset(x = (index * 24 - pins.size * 12).dp, y = 40.dp)
                    .size(14.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { onMarkerTap(pin) },
            )
        }
    }
}
