package com.gallr.app.ui.tabs.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.gallr.shared.data.model.ExhibitionMapPin
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import NMapsMap.NMFCameraPosition
import NMapsMap.NMFCameraUpdate
import NMapsMap.NMFMapView
import NMapsMap.NMFMarker
import NMapsMap.NMGLatLng
import platform.CoreGraphics.CGRect
import platform.UIKit.UIScreen
import platform.UIKit.UIView

private const val SEOUL_LAT = 37.5665
private const val SEOUL_LNG = 126.9780
private const val INITIAL_ZOOM = 10.0

/**
 * UIView container that prevents NMFMapView's Metal layer from receiving a
 * zero drawable size when CMP 1.8.0 temporarily sets the interop view frame
 * to CGRect.zero during its layout measurement pass.
 *
 * layoutSubviews is skipped when bounds are zero so the zero frame is never
 * propagated to NMFMapView's CAMetalLayer. All non-zero frame updates from
 * CMP's layout pass propagate normally, keeping Metal always at a valid size.
 */
@OptIn(ExperimentalForeignApi::class)
private class NMFMapContainerView @OverrideInit constructor(
    frame: CValue<CGRect>,
) : UIView(frame = frame) {

    override fun layoutSubviews() {
        super.layoutSubviews()
        val b = bounds
        val nonZero = b.useContents { size.width > 0.0 && size.height > 0.0 }
        if (nonZero) {
            subviews.forEach { (it as? UIView)?.setFrame(b) }
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun MapView(
    pins: List<ExhibitionMapPin>,
    onMarkerTap: (ExhibitionMapPin) -> Unit,
    modifier: Modifier,
) {
    val activeMarkers = remember { mutableListOf<NMFMarker>() }
    val mapRef = remember { arrayOfNulls<NMFMapView>(1) }

    // Box participates in the Column's weight layout (390×541dp available).
    // UIKitView fills the Box with fillMaxSize(), giving the embedded UIKit
    // view a concrete non-zero frame so NMFMapView's CAMetalLayer stays valid.
    Box(modifier = modifier) {
        UIKitView(
            factory = {
                val screenBounds = UIScreen.mainScreen.bounds
                val mapView = NMFMapView(frame = screenBounds)
                val target = NMGLatLng.latLngWithLat(SEOUL_LAT, lng = SEOUL_LNG)
                val cameraPosition = NMFCameraPosition.cameraPosition(target, zoom = INITIAL_ZOOM)
                mapView.moveCamera(NMFCameraUpdate.cameraUpdateWithPosition(cameraPosition))
                mapRef[0] = mapView

                val container = NMFMapContainerView(frame = screenBounds)
                container.addSubview(mapView)
                container
            },
            modifier = Modifier.fillMaxSize(),
            properties = UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
            ),
            update = { _ ->
                val mapView = mapRef[0] ?: return@UIKitView
                activeMarkers.forEach { it.mapView = null }
                activeMarkers.clear()
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
}
