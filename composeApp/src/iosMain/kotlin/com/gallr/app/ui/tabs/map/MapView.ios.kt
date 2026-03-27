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
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import NMapsMap.NMFCameraPosition
import NMapsMap.NMFCameraUpdate
import NMapsMap.NMFMapView
import NMapsMap.NMFMarker
import NMapsMap.NMFMyPositionMode
import NMapsMap.NMFOverlayImage
import NMapsMap.NMGLatLng
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIBezierPath
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIScreen
import platform.UIKit.UIView

private const val SEOUL_LAT = 37.5665
private const val SEOUL_LNG = 126.9780
private const val INITIAL_ZOOM = 10.0

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

/**
 * Creates a pin-shaped UIImage filled with the exact accent color #FF5400.
 */
@OptIn(ExperimentalForeignApi::class)
private fun createAccentMarkerImage(): UIImage {
    val w = 32.0
    val h = 44.0
    val radius = w / 2.0

    UIGraphicsBeginImageContextWithOptions(CGSizeMake(w, h), false, UIScreen.mainScreen.scale)

    val accent = UIColor(red = 1.0, green = 0.325, blue = 0.0, alpha = 1.0) // #FF5400

    // Circle head
    accent.setFill()
    val circle = UIBezierPath.bezierPathWithOvalInRect(CGRectMake(0.0, 0.0, w, w))
    circle.fill()

    // Pointed tail
    val tail = UIBezierPath()
    tail.moveToPoint(CGPointMake(0.0, radius))
    tail.addLineToPoint(CGPointMake(radius, h))
    tail.addLineToPoint(CGPointMake(w, radius))
    tail.closePath()
    tail.fill()

    // White inner dot
    UIColor.whiteColor.setFill()
    val dot = UIBezierPath.bezierPathWithOvalInRect(
        CGRectMake(radius - radius * 0.35, radius - radius * 0.35, radius * 0.7, radius * 0.7)
    )
    dot.fill()

    val image = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return image!!
}

// CGPoint helper
@OptIn(ExperimentalForeignApi::class)
private fun CGPointMake(x: Double, y: Double) = platform.CoreGraphics.CGPointMake(x, y)

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun MapView(
    locations: List<MapLocation>,
    onLocationTap: (MapLocation) -> Unit,
    modifier: Modifier,
    enableUserLocation: Boolean,
) {
    val activeMarkers = remember { mutableListOf<NMFMarker>() }
    val mapRef = remember { arrayOfNulls<NMFMapView>(1) }
    val markerImage = remember { NMFOverlayImage.overlayImageWithImage(createAccentMarkerImage()) }

    Box(modifier = modifier) {
        UIKitView(
            factory = {
                val screenBounds = UIScreen.mainScreen.bounds
                val mapView = NMFMapView(frame = screenBounds)
                val target = NMGLatLng.latLngWithLat(SEOUL_LAT, lng = SEOUL_LNG)
                val cameraPosition = NMFCameraPosition.cameraPosition(target, zoom = INITIAL_ZOOM)
                mapView.moveCamera(NMFCameraUpdate.cameraUpdateWithPosition(cameraPosition))
                if (enableUserLocation) {
                    // NMFMyPositionMode: 0=Disabled, 1=Normal, 2=Direction, 3=Compass
                    mapView.positionMode = 1u  // Normal — shows location dot, doesn't track
                }
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
                locations.forEach { location ->
                    val marker = NMFMarker()
                    marker.position = NMGLatLng.latLngWithLat(location.latitude, lng = location.longitude)
                    marker.captionText = location.label
                    marker.iconImage = markerImage
                    marker.touchHandler = { _ ->
                        onLocationTap(location)
                        true
                    }
                    marker.mapView = mapView
                    activeMarkers.add(marker)
                }
            },
        )
    }
}
