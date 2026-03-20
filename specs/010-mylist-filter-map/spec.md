# Feature Specification: My List and List Filtering

**Feature Branch**: `010-mylist-filter-map`
**Created**: 2026-03-20
**Status**: Draft
**Input**: User description: "update filtered to mylist. clicking on filters on list should only show filtered gallery listings and clicking on right checkbox should update mylist that is shown in map tab."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — MAP Shows My Bookmarked Exhibitions (Priority: P1)

A user has bookmarked several exhibitions by tapping the ■ button on exhibition cards. When they switch to the MAP tab and select "MYLIST", they see only their bookmarked exhibitions as pins on the map. The toggle previously labelled "FILTERED" is renamed "MYLIST" throughout the app.

**Why this priority**: This is the core intent of the feature. The current "FILTERED" mode on the map shows exhibitions based on filter chips, which is unintuitive — users expect their personally saved list, not filter results. Renaming and rewiring MYLIST to bookmarks makes the MAP personally relevant.

**Independent Test**: Bookmark 2 exhibitions on the LIST tab → open MAP → select MYLIST → confirm only the 2 bookmarked exhibitions appear as pins. Select ALL → confirm all exhibitions appear.

**Acceptance Scenarios**:

1. **Given** the user has bookmarked at least one exhibition, **When** they open the MAP tab and tap MYLIST, **Then** only their bookmarked exhibitions appear as map pins
2. **Given** the MAP is in MYLIST mode, **When** the user taps ALL, **Then** all exhibitions appear as map pins regardless of bookmark status
3. **Given** the MAP tab shows the toggle, **When** the user sees it, **Then** the left button is labelled "MYLIST" (not "FILTERED")
4. **Given** MYLIST mode is active and the user has no bookmarks, **When** they view the map, **Then** the map shows no pins and displays a message "Add exhibitions to your list to see them here" (or equivalent)

---

### User Story 2 — LIST Tab Filter Chips Narrow the Exhibition List (Priority: P2)

A user wants to browse only featured or opening-this-week exhibitions. They tap filter chips on the LIST tab and the list updates instantly to show only exhibitions matching the selected filters. Selecting multiple chips narrows the list further (AND logic). Deselecting all chips returns the full list.

**Why this priority**: Filtering is a key discovery mechanism. Users need to be able to narrow the exhibition list to find what they're looking for. The filter chips exist in the UI and the behaviour should be clearly confirmed as working correctly.

**Independent Test**: On LIST tab, tap "FEATURED" filter chip → confirm only featured exhibitions appear in the list. Tap it again to deselect → confirm all exhibitions return.

**Acceptance Scenarios**:

1. **Given** the full list of exhibitions is showing, **When** the user taps a filter chip (e.g. "FEATURED"), **Then** the list updates immediately to show only exhibitions matching that filter
2. **Given** one filter chip is active, **When** the user taps a second chip (e.g. "OPENING THIS WEEK"), **Then** the list narrows to show only exhibitions matching both filters
3. **Given** filter chips are active and the list is narrowed, **When** the user deselects all chips, **Then** the full list of exhibitions is restored
4. **Given** active filters produce no matches, **When** the list is empty, **Then** a "No exhibitions match the current filters" message is shown with a "Clear Filters" action

---

### User Story 3 — Bookmark Button Updates My List (Priority: P3)

A user sees the ■/□ button on the right side of each exhibition card. Tapping it bookmarks or unbookmarks the exhibition. This bookmark is immediately reflected in the MAP tab's MYLIST view — the exhibition appears or disappears as a map pin without any additional action.

**Why this priority**: This completes the My List loop: the user understands that the ■ button is the way to add exhibitions to the map's MYLIST view. It should feel immediate and connected.

**Independent Test**: On LIST tab, tap ■ on one exhibition (should become ■/filled) → switch to MAP tab → tap MYLIST → confirm that exact exhibition appears as a pin.

**Acceptance Scenarios**:

1. **Given** an unbookmarked exhibition (□), **When** the user taps the □ button, **Then** it immediately shows as ■ (bookmarked/in list)
2. **Given** a bookmarked exhibition (■), **When** the user taps the ■ button, **Then** it immediately shows as □ (removed from list) and the corresponding pin disappears from MAP MYLIST
3. **Given** the user bookmarks an exhibition while the MAP MYLIST is showing, **When** they switch to MAP, **Then** the newly bookmarked exhibition appears as a pin without requiring a refresh

---

### Edge Cases

- What if the user has no bookmarks and selects MYLIST on MAP? The map shows an empty state with guidance to bookmark exhibitions on the LIST tab.
- What if the user applies multiple filter chips and no exhibitions match? The list shows an empty state with a "Clear Filters" action.
- What if a bookmarked exhibition has no location data? It is excluded from the MAP pins (no location = no pin) but remains bookmarked and visible in the LIST.
- What if the user clears all filter chips while on the LIST tab? The full exhibition list is restored immediately.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The MAP tab toggle MUST be labelled "MYLIST" and "ALL" (replacing the previous "FILTERED" / "ALL" labels)
- **FR-002**: The MAP "MYLIST" mode MUST show only exhibitions the user has bookmarked as map pins
- **FR-003**: The MAP "ALL" mode MUST show all exhibitions with location data as map pins
- **FR-004**: Tapping a filter chip on the LIST tab MUST immediately update the visible exhibition list to show only exhibitions matching all active filter chips
- **FR-005**: When all filter chips are deselected, the LIST tab MUST show the full exhibition list
- **FR-006**: Tapping the ■/□ bookmark button on an exhibition card MUST immediately add or remove that exhibition from the user's personal list
- **FR-007**: Changes to the bookmark list MUST be immediately reflected in the MAP MYLIST view without requiring a tab switch or refresh
- **FR-008**: When MYLIST mode is active on MAP and the user has no bookmarks, the map MUST display an empty-state message guiding the user to bookmark exhibitions on the LIST tab

### Key Entities

- **Exhibition**: An art or cultural exhibition with a name, venue, dates, location, and bookmark status
- **My List (Bookmark List)**: The user's personal collection of bookmarked exhibitions — used as the source for MAP MYLIST pins

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: After bookmarking an exhibition, it appears as a pin in MAP MYLIST mode within 1 second of switching to the MAP tab
- **SC-002**: Tapping a filter chip on the LIST tab updates the list within 300ms
- **SC-003**: The "MYLIST" label replaces "FILTERED" everywhere in the MAP tab — zero instances of "FILTERED" remain visible to users
- **SC-004**: 100% of bookmarked exhibitions with location data appear as pins when MYLIST is selected on MAP — no missing or extra pins

## Assumptions

- The existing bookmark button (■/□) is the correct mechanism for managing My List — no new UI element needed
- Filter chips apply AND logic (selecting multiple chips = narrower list, not broader)
- The filter chips on LIST tab affect only the LIST view; they do NOT affect which pins show in MAP MYLIST mode (MYLIST = bookmarks only, not filter chip matches)
- Bookmark state persists across app sessions (already handled by existing bookmark persistence)
- The feature does NOT rename or change the LIST tab's "FILTERS" header label
