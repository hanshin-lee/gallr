# Feature Specification: Interactive Map with Exhibition Pins

**Feature Branch**: `004-naver-map-api`
**Created**: 2026-03-19
**Status**: Draft
**Input**: User description: "replace map placeholder with naver map api. Added location should be pinned to the map and only filtered locations should be shown on map."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Real Interactive Map (Priority: P1)

A user opens the Map tab and sees a fully interactive map of their city instead of the current green placeholder. They can pan and zoom the map freely. Every exhibition that has a location assigned appears as a pin on the map at its venue's coordinates.

**Why this priority**: The placeholder is the current state — without a real map, the entire Map tab delivers zero value. This is the foundational change everything else builds on.

**Independent Test**: Open the app on Android or iOS, navigate to the Map tab, and verify a real scrollable/zoomable map renders with visible exhibition pins. The placeholder background and "Map SDK: TBD" text must be gone.

**Acceptance Scenarios**:

1. **Given** the app is open, **When** the user taps the Map tab, **Then** a real interactive map renders — no placeholder text, no green background.
2. **Given** exhibitions with valid coordinates exist, **When** the map is visible, **Then** each such exhibition appears as a distinct pin at its venue location.
3. **Given** an exhibition has no coordinates, **When** the map is visible, **Then** no pin is rendered for that exhibition and no error occurs.
4. **Given** the map is rendered, **When** the user pans or zooms, **Then** the map responds smoothly and pins remain anchored to their locations.

---

### User Story 2 — Filtered Pins View (Priority: P2)

A user activates filters on the List tab (by category, date range, or other criteria) then switches to the Map tab with "FILTERED" mode selected. Only the exhibitions that match the active filters appear as pins. When no filters are active or the user selects "ALL" mode, all geolocated exhibitions are visible.

**Why this priority**: The FILTERED/ALL toggle and filter logic already exist in the app — this story ensures the real map honours that state correctly, which is the primary discovery use case for the map.

**Independent Test**: Apply a filter that reduces the visible exhibition list, switch to the Map tab, select FILTERED mode — verify pin count matches the filtered list. Select ALL — verify all geolocated exhibitions appear.

**Acceptance Scenarios**:

1. **Given** active filters reduce the exhibition list, **When** the user views the Map tab in FILTERED mode, **Then** only pins for matching exhibitions are shown.
2. **Given** the user switches from FILTERED to ALL mode, **When** the map updates, **Then** all geolocated exhibitions appear as pins.
3. **Given** active filters match zero exhibitions, **When** the user views the Map tab in FILTERED mode, **Then** the map renders with no pins and a "No exhibitions match the current filters" message is visible.
4. **Given** filters are changed while the Map tab is visible, **When** the user returns to FILTERED mode, **Then** the pins immediately reflect the updated filter results.

---

### User Story 3 — Tap Pin for Exhibition Details (Priority: P3)

A user taps any pin on the map and sees a summary of that exhibition — its name, venue, and date range — in a dialog. They can dismiss the dialog and continue exploring the map.

**Why this priority**: This is already scaffolded in `MapScreen` (the dialog and `onMarkerTap` callback exist) but depends on the real map dispatching tap events correctly per pin.

**Independent Test**: Tap any visible pin — verify the dialog shows the correct exhibition name and venue for that specific pin, not a random or first-in-list result.

**Acceptance Scenarios**:

1. **Given** pins are visible on the map, **When** the user taps a pin, **Then** a dialog appears showing that exhibition's name, venue name, and date range.
2. **Given** the pin dialog is open, **When** the user taps CLOSE, **Then** the dialog dismisses and the map is interactive again.
3. **Given** multiple pins are close together, **When** the user taps a specific pin, **Then** the dialog shows details for that pin only.

---

### Edge Cases

- What happens when the device has no internet connection and the map tiles cannot load?
- How does the map behave when all exhibitions are outside the initial viewport?
- What if two exhibitions share the exact same coordinates (same venue)?
- What if the pin list updates while a pin dialog is already open?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Map tab MUST render a real interactive map (pannable, zoomable) replacing the current placeholder on both Android and iOS.
- **FR-002**: Each exhibition with valid coordinates MUST be rendered as a map pin at its latitude/longitude position.
- **FR-003**: Tapping a map pin MUST trigger the existing `onMarkerTap` callback with the correct `ExhibitionMapPin` data for that pin.
- **FR-004**: In FILTERED mode, the map MUST display only the pins in the `filteredMapPins` list; in ALL mode, it MUST display all pins in `allMapPins`.
- **FR-005**: The map MUST re-render its pins without a full screen reload when the active pin list changes (filter applied, mode toggled).
- **FR-006**: Exhibitions without latitude/longitude coordinates MUST be silently excluded — no crash, no empty pin.
- **FR-007**: The map MUST function on both Android and iOS using the same `MapView` expect/actual contract already defined in `MapView.kt`.

### Key Entities

- **ExhibitionMapPin**: The existing projection model — `id`, `name`, `venueName`, `latitude`, `longitude`, `openingDate`, `closingDate`. No changes required.
- **MapDisplayMode**: The existing `FILTERED` / `ALL` enum that drives which pin list is passed to `MapView`. No changes required.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The map tab renders a real interactive map within 3 seconds on a standard mobile connection.
- **SC-002**: All geolocated exhibitions visible in the List tab have corresponding pins on the map in ALL mode — zero discrepancy.
- **SC-003**: Switching between FILTERED and ALL mode updates the visible pins within 300ms with no full map reload.
- **SC-004**: Tapping a pin opens the correct exhibition dialog 100% of the time — no mismatch between tapped pin and displayed data.
- **SC-005**: The app does not crash when the map is loaded with zero pins, one pin, or fifty-plus pins.

## Assumptions

- The map is centred on Seoul, South Korea by default (Naver Maps coverage area), with a zoom level that shows the metro area.
- Map credentials are injected at build time via environment/config; they are not stored in source code or spec files.
- Marker visual style uses the monochrome design system (black pin on white, or white pin on black) consistent with the existing app aesthetic, but exact marker design is left to the planning phase.
- The existing `MapScreen.kt` FILTERED/ALL toggle and pin dialog are not modified — only the `MapView` `actual` implementations are replaced.
- Location permission is out of scope for this feature; the map does not need to centre on the user's current GPS position.
