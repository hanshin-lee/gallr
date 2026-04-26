package com.gallr.app.ui.tabs.map

import androidx.compose.runtime.Composable

/**
 * A geographic coordinate pair. Used as the initial-camera target for [MapView].
 */
data class Coordinates(val latitude: Double, val longitude: Double)

/**
 * Returns the device's last-known location as cached by the OS, or null if:
 *   - [enabled] is false (typically because permission has not been granted)
 *   - the OS has no cached fix
 *   - the platform call has not yet resolved (the returned value flips from
 *     null to non-null on a later recomposition)
 *   - the platform call failed
 *
 * No fresh GPS fix is requested. This is intentionally a cached-only read so
 * the Map tab can open without animation or visible delay.
 */
@Composable
expect fun rememberLastKnownCoordinates(enabled: Boolean): Coordinates?
