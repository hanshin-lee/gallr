package com.gallr.app.ui.tabs.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationManager

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberLastKnownCoordinates(enabled: Boolean): Coordinates? {
    val manager = remember { CLLocationManager() }
    var coords by remember { mutableStateOf<Coordinates?>(null) }

    LaunchedEffect(enabled) {
        if (!enabled) {
            coords = null
            return@LaunchedEffect
        }
        coords = manager.location?.coordinate?.useContents {
            Coordinates(latitude, longitude)
        }
    }
    return coords
}
