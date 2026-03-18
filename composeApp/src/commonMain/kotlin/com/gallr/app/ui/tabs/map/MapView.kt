package com.gallr.app.ui.tabs.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gallr.shared.data.model.ExhibitionMapPin

/**
 * Platform-specific map composable.
 *
 * Implementations:
 * - [MapView.android.kt] — Android (Google Maps Compose or Mapbox v9, TBD)
 * - [MapView.ios.kt]     — iOS (MapKit via UIKitView, or Mapbox v9, TBD)
 *
 * @param pins Exhibition locations to render as markers.
 * @param onMarkerTap Called when a marker is tapped; receives the tapped pin.
 */
@Composable
expect fun MapView(
    pins: List<ExhibitionMapPin>,
    onMarkerTap: (ExhibitionMapPin) -> Unit,
    modifier: Modifier = Modifier,
)
