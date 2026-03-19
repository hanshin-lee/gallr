package com.gallr.app.ui.tabs.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.rememberCameraPositionState
import com.gallr.shared.data.model.ExhibitionMapPin

private val SEOUL = LatLng(37.5665, 126.9780)
private const val INITIAL_ZOOM = 10.0

@OptIn(ExperimentalNaverMapApi::class)
@Composable
actual fun MapView(
    pins: List<ExhibitionMapPin>,
    onMarkerTap: (ExhibitionMapPin) -> Unit,
    modifier: Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(SEOUL, INITIAL_ZOOM)
    }

    NaverMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
    ) {
        pins.forEach { pin ->
            Marker(
                state = MarkerState(position = LatLng(pin.latitude, pin.longitude)),
                captionText = pin.name,
                onClick = {
                    onMarkerTap(pin)
                    true
                },
            )
        }
    }
}
