package com.gallr.app.ui.tabs.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.gallr.shared.data.model.ExhibitionMapPin
import kotlinx.cinterop.ExperimentalForeignApi
import NMapsMap.NMFCameraPosition
import NMapsMap.NMFCameraUpdate
import NMapsMap.NMFMapView
import NMapsMap.NMFMarker
import NMapsMap.NMGLatLng

private const val SEOUL_LAT = 37.5665
private const val SEOUL_LNG = 126.9780
private const val INITIAL_ZOOM = 10.0

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun MapView(
    pins: List<ExhibitionMapPin>,
    onMarkerTap: (ExhibitionMapPin) -> Unit,
    modifier: Modifier,
) {
    val activeMarkers = remember { mutableListOf<NMFMarker>() }

    UIKitView(
        factory = {
            val mapView = NMFMapView()
            val target = NMGLatLng.latLngWithLat(SEOUL_LAT, lng = SEOUL_LNG)
            val cameraPosition = NMFCameraPosition.cameraPosition(target, zoom = INITIAL_ZOOM)
            mapView.moveCamera(NMFCameraUpdate.cameraUpdateWithPosition(cameraPosition))
            mapView
        },
        modifier = modifier,
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
        ),
        update = { mapView ->
            // Remove existing markers
            activeMarkers.forEach { it.mapView = null }
            activeMarkers.clear()

            // Add one marker per pin
            pins.forEach { pin ->
                val marker = NMFMarker()
                marker.position = NMGLatLng.latLngWithLat(pin.latitude, lng = pin.longitude)
                marker.captionText = pin.name
                marker.touchHandler = { _ ->
                    onMarkerTap(pin)
                    true
                }
                marker.mapView = mapView
                activeMarkers.add(marker)
            }
        },
    )
}
