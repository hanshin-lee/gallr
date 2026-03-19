# Feature Specification: Fix Map Not Visible on iOS Map Tab

**Feature Branch**: `008-fix-ios-map-render`
**Created**: 2026-03-20
**Status**: Draft
**Input**: User description: "fix naver map not visible on map tab for ios."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Map Renders on iOS Map Tab (Priority: P1)

A user opens the gallr iOS app and navigates to the Map tab. They expect to see an interactive map centred on Seoul with gallery/exhibition location pins. Currently the map area is blank — only the tab header and toggle buttons are visible.

**Why this priority**: The Map tab is a core navigation surface of the app. A blank map means the feature is completely non-functional on iOS, making the tab useless. This is a regression that must be fixed before any release.

**Independent Test**: Open the gallr iOS app on any supported iOS device or simulator. Tap the MAP tab. A fully rendered map centred on Seoul must be visible within 3 seconds.

**Acceptance Scenarios**:

1. **Given** the iOS app is installed and the device has a network connection, **When** the user taps the MAP tab, **Then** a rendered map of the Seoul area is visible within 3 seconds — no blank or white area.
2. **Given** the map tab is open, **When** the user pans or zooms the map, **Then** the map responds to touch gestures and new tiles load correctly.
3. **Given** the map tab is open, **When** exhibition data is available, **Then** location pins are shown on the map at correct coordinates.

---

### User Story 2 — Map Renders Correctly After Tab Navigation (Priority: P2)

A user navigates away from the Map tab to the Featured or List tab, then returns to Map. The map must still be rendered without requiring any extra action from the user.

**Why this priority**: A fix that only works on the first visit but breaks on re-entry would leave users unable to revisit the map during a session. Tab switching is routine expected behaviour.

**Independent Test**: Open MAP tab (confirm map renders), tap FEATURED tab, tap MAP tab again — map must still be fully rendered.

**Acceptance Scenarios**:

1. **Given** the map tab has been opened and the map rendered, **When** the user navigates to another tab and returns to MAP, **Then** the map is still fully rendered — it does not go blank on re-entry.
2. **Given** the app has been backgrounded and foregrounded, **When** the user navigates to the MAP tab, **Then** the map renders correctly without requiring any user action.

---

### User Story 3 — Map Fills Available Area on All Screen Sizes (Priority: P3)

A user opens the Map tab on any supported iOS device. The map must fill the entire available area below the header and controls — no blank strips, white margins, or unrendered regions.

**Why this priority**: Visual correctness matters for first impressions and professionalism. Partial rendering (e.g. map visible in one corner only) is still a broken experience even if technically "rendered".

**Independent Test**: Open MAP tab on both a small (iPhone SE-sized) and a large (iPhone Plus-sized) simulator — map must fill the full available area on each.

**Acceptance Scenarios**:

1. **Given** the app is open on a small-screen device, **When** the MAP tab is shown, **Then** the map fills 100% of the area below the controls with no blank regions.
2. **Given** the device is rotated to landscape orientation, **When** the map is visible, **Then** the map resizes to fill the new dimensions without going blank.

---

### Edge Cases

- What happens when the device has no network connection when the Map tab is opened? The map area must show a graceful empty state rather than crashing or hanging indefinitely.
- What happens when the device screen size or orientation changes while the map is visible? The map must resize and continue rendering correctly without going blank.
- What happens if the user navigates to MAP before exhibition data has finished loading? The base map (tiles and Seoul centre point) must still render; pins may appear once data is ready.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The map MUST be visible and interactive when a user opens the Map tab on iOS — no blank or white rendering area.
- **FR-002**: The map MUST render correctly on both iOS physical devices and the iOS Simulator.
- **FR-003**: The map MUST remain rendered when the user returns to the Map tab after having navigated to another tab.
- **FR-004**: The map MUST remain rendered after the app is backgrounded and foregrounded.
- **FR-005**: The map MUST resize correctly when the device changes orientation or the screen dimensions change.
- **FR-006**: The map MUST fill the entire available map area with no blank strips or white regions on any supported device screen size.
- **FR-007**: When no network connection is available, the Map tab MUST NOT crash or hang — it must display a visible empty or offline state.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of test runs on supported iOS simulators and devices show a rendered map on the Map tab within 3 seconds of opening.
- **SC-002**: The map remains visible across 100% of tab navigation round-trips (MAP → other tab → MAP) during a session.
- **SC-003**: The map remains visible across 100% of app background/foreground cycles during a session.
- **SC-004**: Zero crash reports attributable to map rendering on the Map tab after the fix is shipped.
- **SC-005**: The map fills 100% of the available map area with no blank strips or white regions on all supported device screen sizes.

## Assumptions

- The fix applies to the iOS platform only; Android map rendering is working correctly and is out of scope.
- Map service authentication credentials are valid and correctly configured — this fix addresses the rendering surface, not authentication.
- "Supported iOS" means iOS 16.0 and above, consistent with the existing app deployment target.
- Network connectivity is a prerequisite for loading map tiles; an offline graceful state (FR-007) is in scope but full offline tile caching is out of scope.
- The base map must render even when exhibition pin data has not yet loaded — pins are additive to a working map.
