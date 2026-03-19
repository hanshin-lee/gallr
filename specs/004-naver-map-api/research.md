# Research: Interactive Map with Exhibition Pins

**Feature**: `004-naver-map-api` | **Date**: 2026-03-19

---

## Decision 1: Android Map SDK

**Decision**: `naver-map-compose:1.8.1` + `com.naver.maps:map-sdk:3.23.0`

**Rationale**: `naver-map-compose` provides a Compose-native `NaverMap` composable and `Marker` composable that integrate directly with Jetpack Compose state. This is the minimal-code path for `MapView.android.kt` — no `AndroidView` wrapper needed. The underlying `map-sdk:3.23.0` is the current stable release.

**Alternatives considered**:
- Raw `map-sdk` with `AndroidView` interop — more boilerplate, no benefit for this use case.
- Google Maps Compose — wrong provider; user specified Naver Maps.

**Repository**: `https://repository.map.naver.com/archive/maven` — must be added to `settings.gradle.kts` `dependencyResolutionManagement`.

**Key note**: `naver-map-compose` is Jetpack Compose only (not Compose Multiplatform). This is correct — it lives exclusively in `androidMain`, which uses Jetpack Compose.

---

## Decision 2: iOS Map SDK Integration

**Decision**: Naver Maps iOS SDK via SPM (`https://github.com/navermaps/SPM-NMapsMap`) + KMP cinterop + `UIKitView`

**Rationale**: The Naver Maps iOS SDK (`NMapsMap` framework) is the matching SDK for iOS. SPM is the current recommended distribution method (CocoaPods was primary for ≤3.16.1). KMP cinterop exposes the Objective-C framework to Kotlin/Native. `UIKitView` (CMP 1.8.0) embeds the native `NMFMapView` inside the Compose composable with full touch/gesture passthrough via `UIKitInteropInteractionMode.NonCooperative`.

**Alternatives considered**:
- MapKit (Apple) — different map provider, different tile appearance; not Naver Maps.
- Pure Swift bridge via SwiftUI UIViewRepresentable — would require passing a UIViewController through the KMP boundary, more complex than cinterop.
- Mapbox — third-party SDK, not Naver.

**cinterop .def file location**: `composeApp/src/nativeInterop/cinterop/NMapsMap.def`

**Applies to targets**: `iosArm64`, `iosSimulatorArm64`, `iosX64`

---

## Decision 3: Client ID Injection

**Decision**:
- **Android**: `meta-data` in `AndroidManifest.xml` with `android:name="com.naver.maps.map.NCP_KEY_ID"` — the SDK reads this automatically on init.
- **iOS**: `NMFAuthManager.shared().ncpKeyId = "..."` called from `iOSApp.swift init()` — the SwiftUI `@main` App struct's `init()` runs before any view is constructed, making it the correct lifecycle hook.

**Client secret**: Not required for map tile rendering or marker display. The Naver Maps SDK uses only the NCP Key ID for map access. The client secret is for server-side REST API calls (geocoding, directions) which are out of scope for this feature.

**Security**: The NCP Key ID will be visible in the compiled app binary (same as Google Maps API key). Restrict it to `com.gallr.app` in the Naver Cloud Platform console. Do not commit the key to source — inject via `local.properties` (Android) or a build-time configuration mechanism (iOS).

**Rationale for AndroidManifest over `NMapsInitializer` in code**: No custom `Application` class exists in the project. The manifest approach works without one and is the documented default.

---

## Decision 4: Initial Camera Position

**Decision**: Seoul city centre — latitude `37.5665`, longitude `126.9780`, zoom level `10`.

**Rationale**: gallr targets Korean art exhibitions. Seoul is the primary market. Zoom level 10 shows the metropolitan area, giving users a useful starting view that includes most gallery clusters. The user can pan/zoom freely after load.

**Alternatives considered**: User's current GPS location — out of scope per spec Assumption 5 (location permission excluded).

---

## Decision 5: Marker Visual Style

**Decision**: Default Naver Maps marker (red teardrop) for the initial implementation. Custom monochrome marker (black pin) is a polish item tracked as a separate lower-priority task.

**Rationale**: The default marker works correctly and keeps the implementation minimal (Principle III). Custom marker artwork requires additional design work. The spec does not mandate a specific marker design beyond consistency with the monochrome system — this can be iterated.

---

## Resolved Unknowns

| Unknown | Resolution |
|---------|-----------|
| Map provider | Naver Maps (specified by user) |
| Android SDK | naver-map-compose 1.8.1 + map-sdk 3.23.0 |
| iOS SDK | NMapsMap via SPM-NMapsMap + cinterop |
| Client secret usage | Not needed for map display |
| Initial camera | Seoul (37.5665, 126.9780), zoom 10 |
| iOS init hook | iOSApp.swift `init()` (SwiftUI @main) |
| Marker style | Default SDK marker (monochrome polish = later) |
