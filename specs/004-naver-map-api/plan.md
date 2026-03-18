# Implementation Plan: Interactive Map with Exhibition Pins

**Branch**: `004-naver-map-api` | **Date**: 2026-03-19 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/004-naver-map-api/spec.md`

## Summary

Replace the green placeholder `MapView` stub on both Android and iOS with a live Naver Maps interactive map. Exhibition locations are rendered as markers at their lat/lng coordinates. The existing FILTERED/ALL toggle and pin dialog in `MapScreen.kt` are unchanged — only the two platform `actual` implementations of `MapView` are replaced. No shared module changes required.

## Technical Context

**Language/Version**: Kotlin 2.1.20 (KMP), Swift 5.9 (iOS entry point only)
**Primary Dependencies**:
- Android: `com.naver.maps:map-sdk:3.23.0`, `io.github.fornewid:naver-map-compose:1.8.1`
- iOS: NMapsMap framework via SPM (`https://github.com/navermaps/SPM-NMapsMap`) + KMP cinterop
- Shared: no new dependencies

**Storage**: N/A — no new persistence
**Testing**: Platform UI layers are exempt per Constitution Principle II; no new shared module logic introduced
**Target Platform**: Android (minSdk 26) + iOS 16+
**Project Type**: KMP mobile app — `expect`/`actual` pattern for platform map views
**Performance Goals**: Map renders within 3s on standard connection (SC-001); pin update within 300ms on filter toggle (SC-003)
**Constraints**: No location permission required; client secret not used for map display
**Scale/Scope**: 2 files changed (`MapView.android.kt`, `MapView.ios.kt`); 2 platform config changes (AndroidManifest, iOSApp.swift); 2 build config changes (settings.gradle.kts, build.gradle.kts)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Evidence |
|-----------|--------|----------|
| I — Spec-First | ✅ PASS | `specs/004-naver-map-api/spec.md` exists with acceptance criteria |
| II — Test-First | ✅ PASS | `MapView` actual implementations are platform UI — exempt per constitution. No new shared module logic introduced. |
| III — Simplicity | ✅ PASS | Replacing two stub files with SDK wrappers; no new abstractions. `naver-map-compose` eliminates `AndroidView` boilerplate. |
| IV — Incremental Delivery | ✅ PASS | 3 independently deliverable stories: real map (P1) → filtered pins (P2) → tap-for-details (P3) |
| V — Observability | ✅ PASS | Map auth errors logged via `NMFAuthManager` delegate (iOS) and SDK logcat output (Android). Auth failures surface as empty map, not silent crash. |
| VI — Shared-First | ✅ PASS | All business logic (`filteredMapPins`, `allMapPins`, `MapDisplayMode`, `TabsViewModel`) remains in `shared`. Platform modules receive only the final pin list — no logic. `MapView` actual = UI wrapper only. |

**Post-design re-check**: ✅ PASS — data-model.md confirms zero new shared entities; contracts/mapview-contract.md confirms the `actual` implementations are pure UI wrappers.

## Project Structure

### Documentation (this feature)

```text
specs/004-naver-map-api/
├── plan.md                         ← this file
├── research.md                     ← Phase 0 output ✅
├── data-model.md                   ← Phase 1 output ✅
├── quickstart.md                   ← Phase 1 output ✅
├── contracts/
│   └── mapview-contract.md         ← Phase 1 output ✅
└── tasks.md                        ← Phase 2 output (/speckit.tasks)
```

### Source Code (changes only)

```text
# Build configuration
settings.gradle.kts                                     ← add Naver Maps Maven repo
gradle/libs.versions.toml                               ← add naver-map-sdk version aliases
composeApp/build.gradle.kts                             ← add androidMain deps + iOS cinterop config
composeApp/src/nativeInterop/cinterop/NMapsMap.def      ← NEW: iOS cinterop definition

# Android
composeApp/src/androidMain/AndroidManifest.xml          ← add NCP_KEY_ID meta-data
composeApp/src/androidMain/kotlin/
  com/gallr/app/ui/tabs/map/MapView.android.kt          ← REPLACE stub with NaverMap composable

# iOS
iosApp/iosApp/iOSApp.swift                              ← add NMFAuthManager init
composeApp/src/iosMain/kotlin/
  com/gallr/app/ui/tabs/map/MapView.ios.kt              ← REPLACE stub with UIKitView+NMFMapView

# Unchanged (zero modifications)
shared/                                                 ← no changes
composeApp/src/commonMain/.../map/MapView.kt            ← expect interface unchanged
composeApp/src/commonMain/.../map/MapScreen.kt          ← screen/toggle/dialog unchanged
```

**Structure Decision**: Option 3 (Mobile platform) — changes are isolated to `androidMain` and `iosMain` platform modules plus build config. The `shared` module and `commonMain` are untouched, satisfying Principle VI exactly.

## Complexity Tracking

> No Constitution violations. Table empty.

---

## Phase 0: Research — COMPLETE ✅

See [`research.md`](research.md) for all decisions and resolved unknowns.

Key decisions:
1. **Android SDK**: `naver-map-compose:1.8.1` (Jetpack Compose wrapper, `androidMain` only)
2. **iOS SDK**: `NMapsMap` via `SPM-NMapsMap` + KMP cinterop + `UIKitView`
3. **Client ID injection**: AndroidManifest meta-data (Android), `iOSApp init()` (iOS)
4. **Client secret**: Not required for map display
5. **Initial camera**: Seoul (37.5665, 126.9780), zoom 10

---

## Phase 1: Design & Contracts — COMPLETE ✅

- [`data-model.md`](data-model.md) — No new shared entities; existing `ExhibitionMapPin` and `MapDisplayMode` documented
- [`contracts/mapview-contract.md`](contracts/mapview-contract.md) — Behavioural contract for both `actual` implementations
- [`quickstart.md`](quickstart.md) — Step-by-step developer setup

---

## Phase 2: Implementation Plan

*Executed by `/speckit.tasks`*

### Story 1 (P1): Build Configuration + Android MapView

**Goal**: Naver Maps compiles and renders a real map on Android.

1. Add `maven("https://repository.map.naver.com/archive/maven")` to `settings.gradle.kts`
2. Add version aliases to `gradle/libs.versions.toml`: `naver-map-sdk = "3.23.0"`, `naver-map-compose = "1.8.1"`
3. Add `androidMain.dependencies` entries in `composeApp/build.gradle.kts`
4. Add `NCP_KEY_ID` `meta-data` to `AndroidManifest.xml`
5. Replace `MapView.android.kt` stub with `NaverMap` + `Marker` composable implementation:
   - Initial camera position: Seoul, zoom 10
   - One `Marker` per pin at `LatLng(pin.latitude, pin.longitude)`
   - `Marker.onClick` calls `onMarkerTap(pin)` and returns `true`
6. Build `./gradlew :composeApp:assembleDebug` — must succeed

### Story 2 (P2): iOS Build Config + NMFMapView

**Goal**: Naver Maps compiles and renders on iOS simulator.

1. Add SPM package `https://github.com/navermaps/SPM-NMapsMap` to Xcode project
2. Create `composeApp/src/nativeInterop/cinterop/NMapsMap.def`
3. Add cinterop configuration to `composeApp/build.gradle.kts` for all three iOS targets
4. Add `NMFAuthManager.shared().ncpKeyId` init to `iOSApp.swift`
5. Replace `MapView.ios.kt` stub with `UIKitView` + `NMFMapView` implementation:
   - `UIKitInteropInteractionMode.NonCooperative` for touch passthrough
   - `factory`: create `NMFMapView`, set initial camera to Seoul zoom 10
   - `update`: clear existing markers, add one `NMFMarker` per pin; wire touch handler to `onMarkerTap`
6. Build via Xcode — must succeed on simulator

### Story 3 (P3): Filter Integration Verification

**Goal**: FILTERED/ALL toggle correctly reflects pin counts on both platforms.

1. Run the app on Android emulator — toggle FILTERED/ALL, verify pin count matches list
2. Run on iOS simulator — same verification
3. Confirm empty-filter state shows zero pins and "No exhibitions match" message (existing `MapScreen` logic)

### Polish (post-MVP, lower priority)

- Replace default red teardrop marker with monochrome black marker matching the design system
- Handle auth error state with a visible error message instead of blank map
