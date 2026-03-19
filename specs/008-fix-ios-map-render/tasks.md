# Tasks: Fix Map Not Visible on iOS Map Tab

**Input**: Design documents from `/specs/008-fix-ios-map-render/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, quickstart.md ✅

**Tests**: Not requested — platform-specific UI layer, exempt per Constitution Principle II. Manual acceptance verification via quickstart.md.

**Organization**: Tasks grouped by user story. The core fix is already applied in the working tree (`MapView.ios.kt`). Tasks cover verification, edge case handling, and acceptance testing of each user story increment.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2, US3)

## Path Conventions

```text
composeApp/src/iosMain/kotlin/com/gallr/app/ui/tabs/map/MapView.ios.kt  ← sole changed file
specs/008-fix-ios-map-render/quickstart.md                               ← acceptance checklist
```

---

## Phase 1: Setup

**Purpose**: Confirm the fix is correctly applied in the working tree on branch `008-fix-ios-map-render` and the project compiles cleanly.

- [X] T001 Read `composeApp/src/iosMain/kotlin/com/gallr/app/ui/tabs/map/MapView.ios.kt` and confirm it contains: (a) `UIView` container with `UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight`, (b) `NMFMapView` as subview with same `autoresizingMask`, (c) `mapRef` array holding the inner map reference, (d) `update` block reading from `mapRef[0]`
- [X] T002 Build iOS app for simulator: `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16e' -configuration Debug build` — confirm `BUILD SUCCEEDED` with zero errors

---

## Phase 2: Foundational

**Purpose**: Install the built app and confirm no Metal layer errors on launch — prerequisite for all user story acceptance.

**⚠️ CRITICAL**: Both tasks must pass before user story verification begins.

- [X] T003 Install and launch the app on the booted simulator: `xcrun simctl install booted <APP_PATH> && xcrun simctl launch booted com.gallr.app`
- [X] T004 Wait 5 seconds after launch, then check simulator logs for Metal errors: `xcrun simctl spawn booted log show --predicate 'process == "iosApp"' --last 30s | grep -i "CAMetalLayer\|setDrawableSize\|nextDrawable"` — confirm zero output (no Metal errors on app launch)

**Checkpoint**: App installed, no Metal errors at launch — user story verification can begin.

---

## Phase 3: User Story 1 — Map Renders on iOS Map Tab (Priority: P1) 🎯 MVP

**Goal**: Navigate to the MAP tab and see a rendered Naver map of Seoul within 3 seconds. No blank or white area.

**Independent Test**: Open the app → tap MAP tab → map tiles visible within 3 seconds. Verify via screenshot (`xcrun simctl io booted screenshot ~/Desktop/gallr-map-us1.png`).

### Implementation for User Story 1

- [X] T005 [US1] Navigate to the MAP tab in the running simulator, take a screenshot (`xcrun simctl io booted screenshot ~/Desktop/gallr-map-us1.png`), and read the screenshot — confirm Naver map tiles (street/area tiles for Seoul) are visible in the map area below the FILTERED/ALL toggle. Confirm the area is not blank white.
- [X] T006 [US1] Verify zero Metal errors during MAP tab navigation: run `xcrun simctl spawn booted log show --predicate 'process == "iosApp"' --last 30s | grep -i "CAMetalLayer\|setDrawableSize\|nextDrawable"` immediately after opening MAP — confirm no output
- [X] T007 [US1] Verify map responds to interaction: confirm `UIKitInteropInteractionMode.NonCooperative` is set in `composeApp/src/iosMain/kotlin/com/gallr/app/ui/tabs/map/MapView.ios.kt` — this ensures Naver map handles all touch events (pan/zoom) independently from Compose

**Checkpoint**: MAP tab shows rendered Naver map. US1 complete and independently verified.

---

## Phase 4: User Story 2 — Map Persists After Tab Navigation & Backgrounding (Priority: P2)

**Goal**: The map stays rendered when the user returns to MAP from another tab, and after backgrounding/foregrounding the app.

**Independent Test**: MAP renders → navigate to FEATURED → navigate back to MAP → map still rendered. Verify via screenshot comparison.

### Implementation for User Story 2

- [X] T008 [US2] Verify `mapRef` survives Compose recomposition: read `composeApp/src/iosMain/kotlin/com/gallr/app/ui/tabs/map/MapView.ios.kt` and confirm `mapRef` is declared with `remember { arrayOfNulls<NMFMapView>(1) }` — `remember` preserves the reference across recompositions so the `update` block always finds the live map view on re-entry
- [X] T009 [US2] Take a screenshot of MAP tab (`~/Desktop/gallr-map-before-nav.png`), then take a screenshot after navigating FEATURED → MAP (`~/Desktop/gallr-map-after-nav.png`), read both screenshots — confirm the map area is equally rendered in both (no blank area on return)
- [X] T010 [US2] Verify app-lifecycle persistence: background the app (via simulator Home button or `xcrun simctl io booted press Home`), foreground it, navigate to MAP, take screenshot `~/Desktop/gallr-map-after-bg.png` — confirm map is still rendered

**Checkpoint**: Map survives tab switching and app lifecycle transitions. US2 complete.

---

## Phase 5: User Story 3 — Map Fills Full Available Area on All Screen Sizes (Priority: P3)

**Goal**: The map fills 100% of the area below the MAP header and FILTERED/ALL controls on all supported device sizes, including landscape orientation.

**Independent Test**: Boot an iPhone SE-sized simulator, open MAP tab, take screenshot — map fills full available area with no blank strips.

### Implementation for User Story 3

- [X] T011 [P] [US3] Boot iPhone 16e simulator (small screen), install and launch app, navigate to MAP, take screenshot `~/Desktop/gallr-map-small.png`, read screenshot — confirm map fills the full area below controls with no blank strips or white margins
- [X] T012 [P] [US3] Boot iPhone 16 Plus simulator (large screen) if available, install and launch app, navigate to MAP, take screenshot `~/Desktop/gallr-map-large.png`, read screenshot — confirm map fills the full area below controls on the larger screen
- [X] T013 [US3] Verify resize behaviour: `NMFMapContainerView.layoutSubviews()` sets all subviews to current bounds on every non-zero layout pass — equivalent to `autoresizingMask` and ensures correct resize for any screen size or orientation change

**Checkpoint**: Map fills full available area on all tested device sizes. US3 complete.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Offline edge case verification, spec documentation alignment, and commit.

- [X] T014 [P] Verify offline edge case (FR-007): temporarily disable network on the simulator (`xcrun simctl io booted setPermission network denied` or via Settings), launch app, navigate to MAP — confirm the app does NOT crash and the map area shows either empty tiles or a graceful empty state (not a crash or infinite hang). Re-enable network after verification.
- [X] T015 [P] Run full quickstart.md acceptance checklist from `specs/008-fix-ios-map-render/quickstart.md` — confirm all five success criteria (SC-001 through SC-005) pass
- [X] T016 Commit all changes to branch `008-fix-ios-map-render`: stage `composeApp/src/iosMain/kotlin/com/gallr/app/ui/tabs/map/MapView.ios.kt` and all spec files under `specs/008-fix-ios-map-render/`, write commit message describing the Metal layer zero-size fix

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 (T001, T002) — BLOCKS all user story phases
- **US1 (Phase 3)**: Depends on Foundational — MVP deliverable
- **US2 (Phase 4)**: Depends on Foundational — can start after US1 checkpoint or in parallel
- **US3 (Phase 5)**: Depends on Foundational — can run in parallel with US2 (T011/T012 marked [P])
- **Polish (Phase 6)**: Depends on US1–US3 complete

### User Story Dependencies

- **US1 (P1)**: Can start immediately after Foundational. No dependency on US2 or US3.
- **US2 (P2)**: Can start after Foundational. Independent of US3.
- **US3 (P3)**: Can start after Foundational. T011 and T012 are parallel (different device simulators).

### Within Each User Story

- Read/verify implementation before running acceptance screenshots
- Screenshots taken after confirms criteria
- US2/US3 verification uses app built for US1

### Parallel Opportunities

- T011 and T012 (US3 device size tests) can run simultaneously on separate simulators
- T014 (offline) and T015 (quickstart checklist) can run in parallel in Phase 6

---

## Parallel Example: User Story 3

```bash
# Boot both simulators and install in parallel:
xcrun simctl boot <iPhone-16e-UDID>       # small screen
xcrun simctl boot <iPhone-16-Plus-UDID>   # large screen

# Run T011 and T012 simultaneously:
# T011: screenshot MAP on iPhone 16e
# T012: screenshot MAP on iPhone 16 Plus
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Verify fix + compile
2. Complete Phase 2: Install + confirm no Metal errors
3. Complete Phase 3: US1 — map visible on MAP tab
4. **STOP and VALIDATE**: Screenshot confirms rendered map, zero Metal errors
5. US1 alone is a complete, independently shippable fix

### Incremental Delivery

1. Phase 1 + 2 → Build confirmed
2. US1 → Map visible → MVP ✅
3. US2 → Map persists across navigation
4. US3 → Map correct on all screen sizes
5. Polish → Offline verified, committed

### Single Developer Strategy

Work sequentially: T001 → T002 → T003 → T004 → T005 → T006 → T007 (US1 complete) → T008 → T009 → T010 (US2 complete) → T011/T012 in parallel if two terminals → T013 (US3 complete) → T014/T015 → T016.

---

## Notes

- [P] tasks = different simulators or independent checks, no shared file conflicts
- The implementation fix is already in the working tree — tasks T001 and above verify and validate it
- No unit tests — platform-specific UI layer, exempt per Constitution Principle II
- Commit (T016) is the final task — do not commit until all acceptance criteria pass
- Each user story checkpoint is independently demonstrable to stakeholders
