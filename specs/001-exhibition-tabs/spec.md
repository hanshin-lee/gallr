# Feature Specification: Three-Tab Exhibition Discovery Navigation

**Feature Branch**: `001-exhibition-tabs`
**Created**: 2026-03-18
**Status**: Draft
**Input**: User description: "The app should contain three tabs at the bottom. First
`featured`, providing list of featured exhibitions. second `list` providing different
filters to populate list of exhibitions where you can toggle on or off to be shown in
the third tab, which is `map`. Finally, `map` is the last tab which has two options to
show my map which only shows exhibitions that were filtered based on `list` preferences
and another one showing all exhibitions on the map."

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Browse Featured Exhibitions (Priority: P1)

A user opens the app. The app launches directly to the **Featured** tab, presenting a
curated, editorially-selected list of exhibitions. Each exhibition card shows the exhibition
name, venue, city, and opening/closing dates. The user scrolls to discover what's on.

**Why this priority**: First impression of the app and the primary passive-discovery
surface. Must work end-to-end before any other tab is built.

**Independent Test**: Launch the app with seeded exhibition data. Verify the Featured tab
loads and displays at least one featured exhibition with correct name, venue, and dates.

**Acceptance Scenarios**:

1. **Given** the app is launched, **When** the Featured tab is active,
   **Then** a scrollable list of featured exhibitions is displayed, each showing name,
   venue, city, and opening/closing dates.
2. **Given** there are no featured exhibitions, **When** the Featured tab is active,
   **Then** an empty-state message is shown (e.g., "No featured exhibitions right now").
3. **Given** exhibition data is loading, **When** the network request is in progress,
   **Then** a loading indicator is shown until content appears.
4. **Given** the network request fails, **When** no cached data is available,
   **Then** an error state with a retry option is displayed.

---

### User Story 2 - Filter Exhibitions via List Tab (Priority: P2)

A user navigates to the **List** tab to refine which exhibitions they want to explore.
The tab presents a set of filter toggles (Region, Featured, Editor's Picks, Opening This
Week, Closing This Week). The user toggles filters on or off. Below the filter controls, a scrollable list of
exhibitions matching the active filters is displayed in real time. Filter selections also
persist when the user switches to the Map tab, which reflects the same filtered set in
its Filtered mode.

**Why this priority**: Filtering is the personalisation engine of gallr. It drives the
Map tab's content and enables city-specific discovery.

**Independent Test**: Open the List tab, toggle "Opening This Week" on. Switch to the
Map tab in Filtered mode. Verify only opening-this-week exhibitions appear as markers.

**Acceptance Scenarios**:

1. **Given** the List tab is open, **When** the user toggles a filter on,
   **Then** the toggle is visually marked as active.
2. **Given** active filters exist, **When** the user navigates away and returns to the
   List tab, **Then** the filter state is unchanged.
3. **Given** all filters are off, **When** the user views the Map tab in Filtered mode,
   **Then** all exhibitions are shown (no filter restriction applied).
4. **Given** one or more filters are active, **When** the user switches to the Map tab
   in Filtered mode, **Then** only matching exhibitions appear as map markers.

---

### User Story 3 - Explore Exhibitions on Map (Priority: P3)

A user navigates to the **Map** tab. Exhibition locations are plotted as markers on an
interactive map. The tab offers two display modes: **Filtered** (shows only exhibitions
matching the active List tab filters) and **All** (shows every exhibition regardless of
filter state). The user can switch between modes freely. Tapping a marker reveals a
brief summary card for that exhibition.

**Why this priority**: The map is the spatial discovery experience that differentiates
gallr. It depends on filter state from the List tab, so it is built after filtering works.

**Independent Test**: With at least one filter active in the List tab, open the Map tab.
Verify Filtered mode shows only matching exhibitions; All mode shows every exhibition.
Tap a marker and verify the summary card appears.

**Acceptance Scenarios**:

1. **Given** the Map tab is open in Filtered mode, **When** active filters exist in the
   List tab, **Then** only exhibitions matching those filters are shown as markers.
2. **Given** the Map tab is open in All mode, **When** any filter state exists in the
   List tab, **Then** every available exhibition is shown as a marker, ignoring filters.
3. **Given** the user taps a map marker, **When** the tap registers,
   **Then** a summary card appears showing the exhibition's name, venue, and dates.
4. **Given** no exhibitions match the active filters, **When** Filtered mode is selected,
   **Then** no markers appear and a message indicates no results for the current filters.
5. **Given** map data is loading, **When** the fetch is in progress,
   **Then** a loading indicator is displayed.

---

### User Story 4 - Bookmark an Exhibition (Priority: P2)

A user sees an exhibition they want to remember — on either the Featured tab or the List
tab results. They tap a bookmark icon on the exhibition card. The exhibition is saved to
their bookmarks and the icon fills to confirm the saved state. Tapping again removes the
bookmark. Bookmarked state is visible on cards wherever the exhibition appears.

**Why this priority**: P2 — bookmarking is a core gallr capability (established in the
project constitution). It adds meaningful retention value for returning users, but the
app is usable without it.

**Independent Test**: From the Featured tab, tap the bookmark icon on any exhibition card.
Verify the icon changes to its active state. Tap again and verify it returns to inactive.

**Acceptance Scenarios**:

1. **Given** an exhibition card is visible (Featured or List tab), **When** the user taps
   the bookmark icon, **Then** the icon changes to a filled/active state and the exhibition
   is saved to bookmarks.
2. **Given** an exhibition is bookmarked, **When** the user taps the bookmark icon again,
   **Then** the bookmark is removed and the icon returns to its inactive state.
3. **Given** an exhibition is bookmarked in the Featured tab, **When** it also appears in
   the List tab results, **Then** its bookmark icon is shown in the active state in both
   places.
4. **Given** the app is restarted, **When** the user views the Featured or List tab,
   **Then** previously bookmarked exhibitions retain their bookmarked state.

---

### Edge Cases

- What happens when the device has no network connection and no cached data?
  → Each tab shows an offline/error state with a retry action.
- What happens when location permissions are denied and the map needs a city context?
  → The map defaults to showing all exhibitions without location focus; the user can
  manually set a region via the List tab filter.
- What if an exhibition has no geographic coordinates?
  → It is excluded from the Map tab but remains visible in the Featured and List tabs.
- What if the user switches map display modes rapidly?
  → Only the latest mode's data is displayed; in-flight stale requests are ignored.
- What if "Opening This Week" and "Closing This Week" filters are both active simultaneously?
  → Exhibitions satisfying either condition are shown (OR logic between these two filters).

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The app MUST provide exactly three bottom navigation tabs: Featured, List,
  and Map, always visible and reachable in a single tap from any tab.
- **FR-002**: The Featured tab MUST display a scrollable list of curated/featured
  exhibitions.
- **FR-003**: Each exhibition entry MUST display at minimum: exhibition name, venue name,
  city/region, and opening and closing dates.
- **FR-004**: The List tab MUST provide the following independently toggleable filter
  controls: Region, Featured, Editor's Picks, Opening This Week, Closing This Week.
- **FR-005**: The List tab MUST display a scrollable list of exhibitions matching the
  active filters, updated in real time as filters change.
- **FR-006**: Filter state MUST persist across tab navigation within a single app session.
- **FR-007**: The Map tab MUST support two display modes: Filtered and All, switchable
  without leaving the tab.
- **FR-008**: In Filtered mode, the map MUST show only exhibitions that satisfy all
  currently active List tab filters.
- **FR-009**: In All mode, the map MUST show every available exhibition regardless of
  filter state.
- **FR-010**: Tapping a map marker MUST reveal a summary card showing the exhibition's
  name, venue, and dates.
- **FR-011**: Every exhibition card in the Featured and List tabs MUST display a bookmark
  icon indicating whether the exhibition is bookmarked.
- **FR-012**: Tapping the bookmark icon MUST toggle the bookmarked state of the exhibition.
- **FR-013**: Bookmark state MUST be consistent across all views where the same exhibition
  card appears (Featured tab and List tab results).
- **FR-014**: Bookmark state MUST persist across app restarts.
- **FR-015**: Every tab MUST display a loading state while data is being fetched.
- **FR-016**: Every tab MUST display a meaningful empty or error state — no blank screens
  under any data condition.
- **FR-017**: The map rendering provider is intentionally unspecified; it MUST be treated
  as a pluggable dependency in the implementation.

### Key Entities

- **Exhibition**: Represents a single art or cultural exhibition.
  Attributes: unique id, name, hosting venue, city/region, opening date, closing date,
  is_featured flag, is_editors_pick flag, geographic coordinates (latitude/longitude),
  short description, cover image reference.

- **FilterState**: The user's current set of active filter selections.
  Attributes: selected regions (list), show_featured (bool), show_editors_pick (bool),
  opening_this_week (bool), closing_this_week (bool).
  Scope: session-only — persists across tabs within a session; persistence across app
  restarts is out of scope for this feature.

- **MapDisplayMode**: The Map tab's active display choice.
  Values: FILTERED (respects FilterState), ALL (ignores FilterState).

- **Bookmark**: A user's saved reference to an exhibition.
  Attributes: exhibition id, saved timestamp.
  Scope: persists across app restarts (device-local storage).

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can open the app and see featured exhibitions within 3 seconds on a
  standard mobile data connection.
- **SC-002**: All three tabs are reachable from any tab in a single tap.
- **SC-003**: 100% of filter changes in the List tab are reflected in the Map tab's
  Filtered mode on the next visit within the same session.
- **SC-004**: Switching between Filtered and All map modes completes within 2 seconds.
- **SC-005**: No tab ever displays a blank screen; every data state (loading, empty, error)
  has a visible, user-understandable message.
- **SC-006**: A user with no prior knowledge of the app can apply a filter and view
  filtered results on the map without assistance.
- **SC-007**: A user can bookmark an exhibition and find it still bookmarked after
  closing and reopening the app.

---

## Assumptions

- "Opening This Week" and "Closing This Week" mean exhibitions whose opening or closing
  date falls within the next 7 calendar days from today.
- "Featured" exhibitions are designated by a backend editorial flag, not by user actions.
- "Editor's Picks" is a distinct curatorial category from "Featured".
- Region filtering operates on the city/region attribute of an exhibition, not the
  device's GPS location. No automatic geolocation is required for this feature.
- Exhibition data is fetched from a remote API; the exact API contract is out of scope
  for this spec.
- Filter state is session-scoped only. Cross-session persistence is deferred.
- The map provider is TBD and treated as a pluggable dependency.

---

## Out of Scope

- Exhibition detail screen (full-page view on tapping a card).
- User authentication or accounts.
- Persistent filter preferences across app restarts.
- The marketing website.
- Push notifications.
- Map SDK or provider selection.
