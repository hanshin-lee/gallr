package com.gallr.app.ui.tabs.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

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

/**
 * Returns `true` once it is safe to compose the map for the first time.
 *
 * Use this to defer composing [MapView] briefly while [rememberLastKnownCoordinates]
 * resolves, so the camera can be initialized at the user's location instead of
 * jumping from Seoul to the user's location after the first frame.
 *
 * Returns `true` immediately when:
 *   - [permissionGranted] is false (no point waiting; we will use the Seoul fallback), OR
 *   - [coordsResolved] is true (cached coords are available now)
 *
 * Otherwise returns `false` until [timeoutMillis] elapse, after which it returns
 * `true` regardless of whether coords arrived. The Seoul fallback handles the
 * timeout case gracefully.
 */
@Composable
fun rememberMapReadiness(
    permissionGranted: Boolean,
    coordsResolved: Boolean,
    timeoutMillis: Long = 300L,
): Boolean {
    if (!permissionGranted || coordsResolved) return true

    var timedOut by remember { mutableStateOf(false) }
    LaunchedEffect(permissionGranted) {
        timedOut = false
        delay(timeoutMillis)
        timedOut = true
    }
    return timedOut
}
