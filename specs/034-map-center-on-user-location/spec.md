# 034 — Map: center on user's location

**Status:** Design
**Priority:** p2
**Source:** `260425-map-center-on-user-location-p2.md`
**Date:** 2026-04-25

## Problem

The MAP tab always opens centered on Seoul (37.5665, 126.9780) even when the user has already granted location permission on a previous app launch. The initial camera position should reflect the user's actual location when available and reasonable.

`MapScreen.kt:69–72` requests location permission on first composition. Both platform `MapView` implementations initialize the camera to Seoul regardless of whether permission was granted:

- Android: `MapView.android.kt:74–76` — `CameraPosition(SEOUL, INITIAL_ZOOM)`
- iOS: `MapView.ios.kt:125–127` — hardcoded Seoul coordinates passed to `NMFCameraUpdate`

`enableUserLocation` is already passed to `MapView` but only enables the location dot — it never moves the initial camera.

## Desired behavior

1. **Permission granted + cached fix inside Korea** → center on user's cached location at `INITIAL_ZOOM`.
2. **Permission granted + cached fix outside Korea** → fall back to Seoul.
3. **Permission denied / undetermined / no cached fix** → fall back to Seoul.

Korea bounding box (inclusive):
- Lat: 33.0 – 38.9
- Lng: 124.6 – 131.9

## Behavior decisions (locked during brainstorm)

| Question | Decision | Rationale |
|---|---|---|
| Coordinate freshness | Cached / last-known fix only — no waiting for fresh GPS | Simplest, no animation/jump UX |
| Korea bounding-box check location | `commonMain` shared helper | Single tested implementation, no duplication |
| First-launch UX (permission not yet granted at first MapView composition) | Accept Seoul fallback on first launch; subsequent launches use cached location | Consistent with no-animation rule |
| Cached-fix recency check | None — accept any cached fix that passes the Korea bounding box | A possibly-stale Korean location is more useful than Seoul |
| Mid-session re-entry into Map tab | Re-center on every Map tab visit | Matches current "first composition decides camera" framing |
| Deferral while waiting for cached fix | Defer `MapView` composition up to ~300ms while `coords` resolve, else Seoul fallback | Avoids "Seoul → user-location" jump; iOS resolves <1ms, Android typically tens of ms |

## Architecture

### New shared abstraction (commonMain)

```kotlin
// composeApp/src/commonMain/.../map/UserLocation.kt
data class Coordinates(val latitude: Double, val longitude: Double)

@Composable
expect fun rememberLastKnownCoordinates(enabled: Boolean): Coordinates?
```

`enabled` lets the caller pass `locationPermission.isGranted`. Returns `null` for permission denied, no cached fix, platform error, or while the platform call is still pending.

### Korea bounds helper (commonMain, pure, unit-testable)

```kotlin
// shared/src/commonMain/.../util/KoreaBounds.kt
fun isInsideKorea(lat: Double, lng: Double): Boolean =
    lat in 33.0..38.9 && lng in 124.6..131.9
```

(The helper lives in the `shared` module's `util` package — alongside `HexColor.kt`, `FabLabel.kt`, `Validators.kt` — so it must be `public` for the `composeApp` module's `MapScreen` to call it.)

### MapView signature change (commonMain)

```kotlin
expect fun MapView(
    locations: List<MapLocation>,
    onLocationTap: (MapLocation) -> Unit,
    modifier: Modifier = Modifier,
    enableUserLocation: Boolean = false,
    initialCenter: Coordinates? = null,   // NEW; null → platform default (Seoul)
)
```

### Wiring in MapScreen

Inside `MapScreen`'s existing `Column { ... }` (the same one currently wrapping `TabRow`, `HorizontalDivider`, the empty-state Text, and `MapView`), replace the current `MapView(...)` call with:

```kotlin
val cachedCoords = rememberLastKnownCoordinates(enabled = locationPermission.isGranted)
val initialCenter = cachedCoords?.takeIf { isInsideKorea(it.latitude, it.longitude) }

// Defer MapView composition up to 300ms while waiting for cached coords
val mapReady = rememberMapReadiness(
    permissionGranted = locationPermission.isGranted,
    coordsResolved = cachedCoords != null,
    timeoutMillis = 300,
)
if (mapReady) {
    MapView(
        locations = locations,
        onLocationTap = { /* unchanged from current */ },
        modifier = Modifier.weight(1f),
        enableUserLocation = locationPermission.isGranted,
        initialCenter = initialCenter,
    )
} else {
    // Placeholder matching map background — invisible "loading" region
    Box(modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.background))
}
```

`rememberMapReadiness` is a new helper in `commonMain` (same file as `UserLocation.kt` or its own `MapReadiness.kt` — implementer's choice). It returns `true` once either `coordsResolved` is true OR `timeoutMillis` has elapsed. If `permissionGranted == false`, returns `true` immediately (no point waiting).

## Platform actuals

### Android — `composeApp/src/androidMain/.../map/UserLocation.android.kt` (new)

```kotlin
@Composable
actual fun rememberLastKnownCoordinates(enabled: Boolean): Coordinates? {
    val context = LocalContext.current
    var coords by remember { mutableStateOf<Coordinates?>(null) }

    LaunchedEffect(enabled) {
        if (!enabled) { coords = null; return@LaunchedEffect }
        coords = runCatching {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val location = client.lastLocation.await()
            location?.let { Coordinates(it.latitude, it.longitude) }
        }.getOrNull()
    }
    return coords
}
```

**Dependencies to verify / add in `composeApp/build.gradle.kts`:**
- `com.google.android.gms:play-services-location` (for `FusedLocationProviderClient`)
- `org.jetbrains.kotlinx:kotlinx-coroutines-play-services` (for `Task.await()`)

### Android — `MapView.android.kt` change

Replace the `rememberCameraPositionState` call:
```kotlin
val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition(
        initialCenter?.let { LatLng(it.latitude, it.longitude) } ?: SEOUL,
        INITIAL_ZOOM,
    )
}
```

### iOS — `composeApp/src/iosMain/.../map/UserLocation.ios.kt` (new)

```kotlin
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberLastKnownCoordinates(enabled: Boolean): Coordinates? {
    val manager = remember { CLLocationManager() }
    var coords by remember { mutableStateOf<Coordinates?>(null) }

    LaunchedEffect(enabled) {
        if (!enabled) { coords = null; return@LaunchedEffect }
        coords = manager.location?.coordinate?.useContents {
            Coordinates(latitude, longitude)
        }
    }
    return coords
}
```

`CLLocationManager.location` is a synchronous property; no new dependencies.

### iOS — `MapView.ios.kt` change

In the `factory` block:
```kotlin
val target = initialCenter
    ?.let { NMGLatLng.latLngWithLat(it.latitude, lng = it.longitude) }
    ?: NMGLatLng.latLngWithLat(SEOUL_LAT, lng = SEOUL_LNG)
val cameraPosition = NMFCameraPosition.cameraPosition(target, zoom = INITIAL_ZOOM)
```

The `factory` closure captures `initialCenter` at first composition. Subsequent recompositions with different `initialCenter` do **not** re-trigger the factory — by design (camera-jump avoided per Q1).

## Data flow

### Cold launch, permission granted from prior session, user inside Korea

```
1. MapScreen composes
2. rememberLocationPermissionState() → isGranted = true
3. rememberLastKnownCoordinates(enabled=true) launches LaunchedEffect
   ├─ Android: FusedLocationProviderClient.lastLocation.await() → ~10–50ms
   └─ iOS:    CLLocationManager.location property read → <1ms
4. Within 300ms timeout, coords flips to Coordinates(...)
5. isInsideKorea(...) → true → initialCenter = those coords
6. mapReady → true → MapView composes with initialCenter
7. NaverMap factory runs ONCE → camera positioned on user's location
```

### Cold launch, permission not yet granted

```
1. MapScreen composes
2. isGranted = false → permission request fires (existing LaunchedEffect)
3. rememberLastKnownCoordinates(enabled=false) returns null immediately
4. rememberMapReadiness sees permissionGranted=false → mapReady = true immediately
5. MapView composes with initialCenter = null → camera = Seoul
6. User taps "Allow" → isGranted flips → recomposition
7. coords arrives, but MapView factory already ran → camera stays on Seoul this session
8. Next Map tab visit (or next launch) → centered on user
```

## Edge cases & error handling

| Scenario | Behavior |
|---|---|
| Permission denied | `enabled=false` → `coords=null` → Seoul |
| Permission granted, no cached fix | Platform returns null → Seoul |
| Permission granted, cached fix outside Korea | `isInsideKorea` rejects → Seoul |
| Permission granted, cached fix inside Korea | User's location, `INITIAL_ZOOM=10.0` |
| Android `lastLocation` throws `SecurityException` | `runCatching` swallows → Seoul |
| Android `lastLocation` task takes >300ms | Timeout fires → Seoul |
| Play Services missing on Android | Task fails → `runCatching` → Seoul |
| iOS `CLLocationManager.location` returns nil | → Seoul |
| User enables permission in Settings, returns to app | Existing iOS delegate flips `isGranted`; on next Map tab visit, centered on them |

No user-facing error UI — all failures degrade silently to Seoul, matching the spec's three desired-behavior cases.

## Testing

### Unit tests — `composeApp/src/commonTest/.../map/KoreaBoundsTest.kt` (new)

Per the project's TDD requirement, these are written **before** the bounding-box helper:

```kotlin
class KoreaBoundsTest {
    @Test fun seoulIsInsideKorea() = assertTrue(isInsideKorea(37.5665, 126.9780))
    @Test fun busanIsInsideKorea() = assertTrue(isInsideKorea(35.1796, 129.0756))
    @Test fun jejuIsInsideKorea() = assertTrue(isInsideKorea(33.4996, 126.5312))
    @Test fun tokyoIsOutsideKorea() = assertFalse(isInsideKorea(35.6762, 139.6503))
    @Test fun newYorkIsOutsideKorea() = assertFalse(isInsideKorea(40.7128, -74.0060))
    @Test fun southOfBoundIsOutside() = assertFalse(isInsideKorea(32.9, 127.0))
    @Test fun northOfBoundIsOutside() = assertFalse(isInsideKorea(39.0, 127.0))
    @Test fun westOfBoundIsOutside() = assertFalse(isInsideKorea(35.0, 124.5))
    @Test fun eastOfBoundIsOutside() = assertFalse(isInsideKorea(35.0, 132.0))
    @Test fun edgesAreInclusive() {
        assertTrue(isInsideKorea(33.0, 124.6))
        assertTrue(isInsideKorea(38.9, 131.9))
    }
}
```

### Manual QA (Android + iOS)

1. Cold install, deny permission → map opens on Seoul.
2. Cold install, grant permission, kill app, reopen → map opens on user's location (assuming user is in Korea).
3. Toggle airplane mode + clear location cache → permission still granted, no cached fix → Seoul.
4. Pan away from current location, switch to Featured tab, back to Map → re-centers on user's location.
5. Run app on a device set to a US location (simulator with NYC GPS) → Seoul.

No integration tests for the platform actuals — `FusedLocationProviderClient` and `CLLocationManager` are platform SDKs; mocking adds more friction than value for a UI-only behavior change.

## Affected files

**New:**
- `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/UserLocation.kt` (declares `Coordinates` and `expect rememberLastKnownCoordinates`; may also host `rememberMapReadiness`)
- `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/KoreaBounds.kt`
- `composeApp/src/androidMain/kotlin/com/gallr/app/ui/tabs/map/UserLocation.android.kt`
- `composeApp/src/iosMain/kotlin/com/gallr/app/ui/tabs/map/UserLocation.ios.kt`
- `composeApp/src/commonTest/kotlin/com/gallr/app/ui/tabs/map/KoreaBoundsTest.kt`

**Modified:**
- `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapScreen.kt` — wire `rememberLastKnownCoordinates`, `isInsideKorea`, and 300ms-deferred render
- `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapView.kt` — add `initialCenter: Coordinates?` parameter to `expect fun`
- `composeApp/src/androidMain/kotlin/com/gallr/app/ui/tabs/map/MapView.android.kt` — use `initialCenter` in `rememberCameraPositionState`
- `composeApp/src/iosMain/kotlin/com/gallr/app/ui/tabs/map/MapView.ios.kt` — use `initialCenter` in `factory` block
- `composeApp/build.gradle.kts` — verify/add `play-services-location` and `kotlinx-coroutines-play-services`

## Out of scope

- Fresh GPS fix (vs. cached `lastLocation`) — Q1 decision.
- Camera animation when permission flips mid-session — Q3 decision.
- Persisting camera position across tab switches — Q5 decision.
- Recency check on cached fixes — Q4 decision.
- Recenter-to-me FAB (could be a follow-up p3).
