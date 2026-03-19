# Implementation Plan: Fix Map Not Visible on iOS Map Tab

**Branch**: `008-fix-ios-map-render` | **Date**: 2026-03-20 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/008-fix-ios-map-render/spec.md`

## Summary

The Naver map was blank on the iOS Map tab because Compose Multiplatform 1.8.0's `UIKitView` temporarily sets the embedded view's frame to `CGRect.zero` during its layout measurement pass. `NMFMapView`'s `CAMetalLayer` receives a `setDrawableSize(0, 0)` call and cannot allocate a Metal drawable, causing all subsequent tile renders to silently fail despite successful HTTP 200 responses from the Naver Maps API.

The fix wraps `NMFMapView` in a plain `UIView` container returned from the `UIKitView` factory. CMP resizes the container; `UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight` on the inner `NMFMapView` propagates size changes through UIKit's own path — which never goes through zero — so the Metal layer always receives valid dimensions.

## Technical Context

**Language/Version**: Kotlin 2.1.20 (Kotlin/Native, iosSimulatorArm64 / iosArm64 targets)
**Primary Dependencies**: Compose Multiplatform 1.8.0 (`UIKitView` interop), NMapsMap iOS SDK 3.23.0 (via SPM + cinterop)
**Storage**: N/A
**Testing**: Manual visual verification on iOS Simulator and device (platform-specific UI layer — exempt from unit tests per Constitution Principle II)
**Target Platform**: iOS 16.0+ (simulator and physical device)
**Project Type**: KMP mobile app — iOS platform module only
**Performance Goals**: Map tiles visible within 3 seconds of opening the Map tab
**Constraints**: Fix must be isolated to `iosMain`; no changes to `shared/`, Android, or web
**Scale/Scope**: Single file change — `composeApp/src/iosMain/kotlin/com/gallr/app/ui/tabs/map/MapView.ios.kt`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Spec-First | ✅ Pass | `spec.md` written and validated before implementation |
| II. Test-First | ✅ Pass (exempt) | Platform-specific UI layer — Constitution explicitly exempts from unit test requirement. Manual acceptance criteria defined in `quickstart.md`. |
| III. Simplicity & YAGNI | ✅ Pass | Minimal change: one container UIView + two `autoresizingMask` assignments. No new abstractions, no new dependencies. |
| IV. Incremental Delivery | ✅ Pass | Single independently testable story (P1): map visible on Map tab. |
| V. Observability | ✅ Pass | No new silent failure paths introduced. Metal layer errors eliminated rather than suppressed. |
| VI. Shared-First | ✅ Pass | Fix lives entirely in `composeApp/src/iosMain/` — the correct platform module. No business logic involved. `shared/` is untouched. |

No violations. Complexity Tracking table not required.

## Project Structure

### Documentation (this feature)

```text
specs/008-fix-ios-map-render/
├── plan.md          ← this file
├── spec.md          ← feature specification
├── research.md      ← Phase 0: root cause and fix rationale
├── quickstart.md    ← Phase 1: acceptance verification steps
└── checklists/
    └── requirements.md
```

### Source Code (affected files only)

```text
composeApp/
└── src/
    └── iosMain/
        └── kotlin/
            └── com/gallr/app/
                └── ui/tabs/map/
                    └── MapView.ios.kt   ← sole change
```

No changes to:
- `shared/` (no business logic affected)
- `androidMain/` (Android map rendering unaffected)
- `iosApp/` (NCP key auth already correctly set)
- `commonMain/` (expect declaration unchanged)

**Structure Decision**: iOS platform module only. The fix is a UIKit/Metal rendering integration concern — exactly the kind of platform-specific code Principle VI permits in `iosMain/`.

## Implementation

### What changes in `MapView.ios.kt`

**Before** (broken):
```
UIKitView(
    factory = {
        val mapView = NMFMapView(frame = UIScreen.mainScreen.bounds)
        // CMP later calls setFrame(CGRect.zero) on mapView directly
        // → CAMetalLayer receives setDrawableSize(0,0) → blank map
        mapView
    },
    update = { mapView -> /* add markers to mapView */ }
)
```

**After** (fixed):
```
UIKitView(
    factory = {
        val container = UIView(frame = screenBounds)
        container.autoresizingMask = FlexibleWidth or FlexibleHeight

        val mapView = NMFMapView(frame = container.bounds)
        mapView.autoresizingMask = FlexibleWidth or FlexibleHeight
        container.addSubview(mapView)
        // CMP resizes container; UIKit autoresizing propagates to mapView
        // without ever passing through zero → Metal layer stays valid

        mapRef[0] = mapView   // held for update block
        container
    },
    update = { _ ->
        val mapView = mapRef[0] ?: return@UIKitView
        // add/remove markers on mapView
    }
)
```

The change is already applied in the working tree. The implementation task is to commit it on this branch and verify acceptance criteria.

## Phase 0 Findings Summary

See [`research.md`](research.md) for full details.

- Root cause: CMP 1.8.0 UIKitView zero-frame layout pass → `CAMetalLayer.setDrawableSize(0,0)` → silent Metal render failure
- Fix: container UIView + autoresizingMask shields `NMFMapView` from the zero-size update
- Auth: NCP key valid (all Naver API calls return HTTP 200); no auth changes needed
- Scope: `iosMain` only; Android, shared, and web unaffected
- Testing: Manual visual verification; unit tests exempt per Constitution Principle II

## Acceptance Verification

See [`quickstart.md`](quickstart.md) for step-by-step verification of all five success criteria (SC-001 through SC-005).
