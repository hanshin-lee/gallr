# Research: Fix Map Not Visible on iOS Map Tab

**Branch**: `008-fix-ios-map-render` | **Date**: 2026-03-20

---

## Finding 1: Root Cause — CMP UIKitView Zero-Frame Layout Pass

**Decision**: The blank map is caused by Compose Multiplatform 1.8.0's `UIKitView` temporarily setting the embedded view's frame to zero (`CGRect.zero`) during its initial layout measurement pass. `NMFMapView` uses a `CAMetalLayer` as its rendering surface. When `CAMetalLayer.setDrawableSize` receives `{0, 0}`, Metal cannot allocate a drawable and silently stops rendering — all subsequent tile-load network calls succeed (HTTP 200) but nothing is drawn.

**Evidence**:
- Log: `CAMetalLayer ignoring invalid setDrawableSize width=0.000000 height=0.000000`
- Log: `[CAMetalLayer nextDrawable] returning nil because allocation failed.` (repeated ~20×)
- Network calls to `maps.apigw.ntruss.com/map-dynamic/v1/mobile/v3/props` returned HTTP 200 — auth is valid, tiles are being fetched, but not rendered.

**Rationale**: CMP's layout system computes the composable's actual size after initial placement. During the measurement phase, the UIKit interop layer transiently sets the view frame to zero before the real layout constraints resolve. `NMFMapView` directly owns the `CAMetalLayer`, so it receives this zero-size update and the Metal drawable becomes permanently broken for that frame of the view lifecycle.

**Alternatives considered**:
- **`NMFMapView(frame: UIScreen.mainScreen.bounds)` directly** — Tried first; did not fix the issue because CMP still calls `setFrame(CGRect.zero)` on the view after factory runs, overwriting the initial bounds and breaking the Metal layer.
- **Reinitialise on resize via `onResize` callback** — Not available in CMP 1.8.0's `UIKitView` API.

---

## Finding 2: Fix — Container UIView with autoresizingMask

**Decision**: Wrap `NMFMapView` in a plain `UIView` container. CMP resizes the container; `autoresizingMask` on `NMFMapView` propagates those changes to the Metal layer without CMP ever directly setting `NMFMapView`'s frame to zero.

**Rationale**: CMP sets the frame of the view returned from `factory` (the container). The container starts at full screen bounds and is then resized by CMP to its layout slot. `NMFMapView` lives inside the container and uses `UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight` — UIKit's own autoresizing path does not go through zero during a frame change, so the Metal layer receives only valid non-zero sizes.

The `mapRef` array holds the `NMFMapView` reference out of the factory lambda so the `update` block can add/remove markers directly on the inner map view rather than the container.

**Alternatives considered**:
- **Changing `UIKitInteropInteractionMode` to `Cooperative`** — Not a fix for the zero-frame issue; interaction mode controls gesture routing, not frame updates.
- **Delaying map init with `LaunchedEffect`** — Would hide the symptom but not fix the underlying zero-size Metal drawable corruption.

---

## Finding 3: Scope Boundary — iOS Platform Module Only

**Decision**: The fix is entirely within `composeApp/src/iosMain/`. No changes to `shared/`, Android, or web.

**Rationale**: This is a platform-specific rendering integration issue between CMP's `UIKitView` and NMapsMap's Metal renderer. The Android implementation uses the Naver Maps Compose library (`io.github.fornewid:naver-map-compose`) which has no equivalent issue. The `shared/` module has no rendering code. Constitution Principle VI (Shared-First) is fully satisfied — the fix lives in the correct platform module.

---

## Finding 4: Testing Approach

**Decision**: Manual visual verification on iOS Simulator + device. No automated unit tests for this fix.

**Rationale**: Constitution Principle II (Test-First) explicitly exempts platform-specific UI layers from unit test requirements. The rendering behaviour depends on the Metal GPU stack and the CMP/UIKit interop runtime — neither of which is testable in a unit test context. The acceptance criteria in the spec (map visible within 3s, persists across tab navigation, persists across background/foreground) are verifiable through manual simulator and device testing.

---

## Finding 5: NCP Key Authentication

**Decision**: No changes required to authentication. The NCP key `dkd2c8bh63` is set in `iOSApp.swift` via `NMFAuthManager.shared().ncpKeyId` and is valid — all Naver Maps API requests returned HTTP 200 during diagnosis.

**Rationale**: The bug is purely a Metal rendering surface issue, not an authentication issue. The map tiles are being fetched successfully; they just cannot be drawn onto a zero-size drawable.
