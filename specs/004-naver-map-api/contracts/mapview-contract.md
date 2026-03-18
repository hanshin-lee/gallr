# Contract: MapView expect/actual

**Feature**: `004-naver-map-api` | **Date**: 2026-03-19

---

## Interface

```kotlin
// composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapView.kt
@Composable
expect fun MapView(
    pins: List<ExhibitionMapPin>,
    onMarkerTap: (ExhibitionMapPin) -> Unit,
    modifier: Modifier = Modifier,
)
```

This interface is **unchanged** by this feature. Both `actual` implementations must satisfy the following contract.

---

## Behavioural Contract

| Requirement | Android `actual` | iOS `actual` |
|-------------|-----------------|-------------|
| Renders a real interactive map | `NaverMap` composable (naver-map-compose) | `NMFMapView` via `UIKitView` |
| One marker per pin in `pins` | `Marker` composable per item | `NMFMarker` per item |
| Marker positioned at pin lat/lng | `LatLng(pin.latitude, pin.longitude)` | `NMGLatLng(pin.latitude, pin.longitude)` |
| Tapping a marker calls `onMarkerTap(pin)` | `Marker.onClick` lambda | `NMFOverlayTouchHandler` |
| Pins update when `pins` list changes | Compose recomposition | `update` block in `UIKitView` clears and re-adds markers |
| Map not crash on empty `pins` | ✓ (zero markers rendered) | ✓ (zero markers added) |
| Initial camera position | Seoul (37.5665, 126.9780), zoom 10 | Seoul (37.5665, 126.9780), zoom 10 |
| Touch/gesture events reach map | Default Compose routing | `UIKitInteropInteractionMode.NonCooperative` |

---

## SDK Initialisation (outside MapView)

Client ID must be registered before the first `MapView` composition. This is done in platform app entry points — not inside `MapView` itself.

| Platform | Location | Method |
|----------|----------|--------|
| Android | `AndroidManifest.xml` | `<meta-data android:name="com.naver.maps.map.NCP_KEY_ID" android:value="..."/>` |
| iOS | `iosApp/iosApp/iOSApp.swift init()` | `NMFAuthManager.shared().ncpKeyId = "..."` |

---

## Out of Scope

- Camera state persistence across app sessions
- Clustering overlapping markers
- Custom marker artwork (uses SDK default for initial delivery)
- User location pin
- Map style switching (satellite, terrain, etc.)
