package com.gallr.app.ui.tabs.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.overlay.OverlayImage

private val SEOUL = LatLng(37.5665, 126.9780)
private const val INITIAL_ZOOM = 10.0
private const val ACCENT_ARGB = 0xFFFF5400.toInt()

/**
 * Creates a simple pin-shaped marker bitmap filled with the exact accent color.
 * Pin shape: circle head with a pointed tail at the bottom.
 */
private fun createAccentMarkerBitmap(): Bitmap {
    val w = 48
    val h = 64
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ACCENT_ARGB }

    // Circle head
    val radius = w / 2f
    canvas.drawCircle(radius, radius, radius, paint)

    // Pointed tail
    val path = Path().apply {
        moveTo(0f, radius)
        lineTo(radius, h.toFloat())
        lineTo(w.toFloat(), radius)
        close()
    }
    canvas.drawPath(path, paint)

    // White inner dot
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
    canvas.drawCircle(radius, radius, radius * 0.35f, dotPaint)

    return bitmap
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
actual fun MapView(
    locations: List<MapLocation>,
    onLocationTap: (MapLocation) -> Unit,
    modifier: Modifier,
    enableUserLocation: Boolean,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(SEOUL, INITIAL_ZOOM)
    }

    val markerIcon = remember { OverlayImage.fromBitmap(createAccentMarkerBitmap()) }

    val properties = remember(enableUserLocation) {
        MapProperties(
            locationTrackingMode = if (enableUserLocation) LocationTrackingMode.Follow
                else LocationTrackingMode.None,
        )
    }

    NaverMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = properties,
    ) {
        locations.forEach { location ->
            Marker(
                state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                captionText = location.label,
                icon = markerIcon,
                onClick = {
                    onLocationTap(location)
                    true
                },
            )
        }
    }
}
