# Feature Specification: City Filter & Exhibition Detail Page

**Feature Branch**: `013-city-filter-detail-page`
**Created**: 2026-03-23
**Status**: Draft
**Input**: User description: "On top of filter option on list tab user should be able to select country and city. List should be populated based on selection. Start with South Korea as only country option for now and use city_ko and city_en as city options. Also when clicking on item from the list, this should navigate to description page for the gallery exhibition. In this page, cover image, description and other available informations should be displayed."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - City Filter on List Tab (Priority: P1)

As a gallery visitor browsing the list tab, I want to filter exhibitions by city so that I can focus on exhibitions in my area or a city I plan to visit.

Above the existing filter chips (Featured, Editor's Picks, etc.), the user sees a country selector (defaulting to "South Korea" as the only option) and a city dropdown populated with distinct city values from the exhibition data. Selecting a city filters the exhibition list to show only exhibitions in that city. The city names are displayed in the user's selected language (city_ko or city_en based on the KO/EN toggle). An "All Cities" option shows exhibitions across all cities.

**Why this priority**: City filtering is the most impactful feature for users in a country with exhibitions spread across multiple cities. It directly reduces the effort to find relevant exhibitions.

**Independent Test**: Open list tab, select a city from the dropdown, verify only exhibitions in that city are displayed. Select "All Cities" and verify all exhibitions return.

**Acceptance Scenarios**:

1. **Given** the user is on the list tab, **When** they open the city dropdown, **Then** they see "All Cities" plus a list of distinct cities derived from the exhibition data, displayed in the current language.
2. **Given** the user selects "Seoul" from the city dropdown, **When** the list updates, **Then** only exhibitions located in Seoul are displayed.
3. **Given** the user has selected a city and also has filter chips active (e.g., "Featured"), **When** both filters are applied, **Then** only exhibitions matching both the city AND the active filter chips are displayed.
4. **Given** the user selects "All Cities", **When** the list updates, **Then** exhibitions from all cities are displayed (subject to any active filter chips).
5. **Given** the user switches language via the KO/EN toggle, **When** the city dropdown is open, **Then** city names update to the selected language (e.g., "서울" vs "Seoul").

---

### User Story 2 - Exhibition Detail Page (Priority: P1)

As a gallery visitor, I want to tap on an exhibition in the list to see its full details — including cover image, description, venue, dates, and address — so that I can decide whether to visit.

Tapping any exhibition card (on Featured, List, or Map dialog) navigates to a dedicated detail screen. The detail screen displays all available information for that exhibition in the user's selected language: cover image (if available), exhibition name, venue name, city, region, address, date range, and description. A back button returns to the previous screen.

**Why this priority**: Users currently have no way to see exhibition descriptions or cover images. The detail page is essential for informed decision-making about which exhibitions to visit.

**Independent Test**: Tap any exhibition card, verify the detail screen opens with correct bilingual data, verify the back button returns to the previous tab.

**Acceptance Scenarios**:

1. **Given** the user is viewing the exhibition list, **When** they tap an exhibition card, **Then** a detail screen opens showing that exhibition's full information.
2. **Given** the detail screen is open, **When** the exhibition has a cover image URL, **Then** the cover image is displayed prominently at the top of the screen.
3. **Given** the detail screen is open, **When** the exhibition has no cover image, **Then** the screen displays the remaining information without a broken image placeholder.
4. **Given** the detail screen is open, **When** the user has set the language to English, **Then** all text fields (name, venue, city, region, address, description) display English values with Korean fallback.
5. **Given** the detail screen is open, **When** the user taps the back button, **Then** they return to the previous screen with their scroll position and filter state preserved.
6. **Given** the user taps an exhibition from the Featured tab, **When** the detail screen opens, **Then** it shows the same exhibition data as if accessed from the List tab.

---

### User Story 3 - Country Selector (Priority: P3)

As a user, I see a country selector above the city dropdown that currently shows "South Korea" as the only option. This establishes the UI pattern for future multi-country expansion without requiring app changes when new countries are added.

**Why this priority**: South Korea is the only supported country for now, so this is primarily a UI placeholder. The country selector is visually present but functionally limited to one option. It becomes valuable when the service expands to other countries.

**Independent Test**: Verify the country selector displays "South Korea" (or the localized equivalent) and that it is visible above the city dropdown.

**Acceptance Scenarios**:

1. **Given** the user is on the list tab, **When** they view the filter area, **Then** they see the country selector showing "South Korea" (in Korean: "대한민국") above the city dropdown.
2. **Given** the user taps the country selector, **When** the options appear, **Then** only "South Korea" is available.

---

### Edge Cases

- What happens when no exhibitions exist for a selected city? The list shows an empty state message: "No exhibitions in [city name]" with an option to clear the city filter.
- What happens when the cover image URL is invalid or fails to load? The detail page shows the remaining content without an image, with no broken image icon.
- What happens when the description field is empty? The description section is hidden on the detail page rather than showing blank space.
- What happens when the user navigates to the detail page and then switches language? The detail page content updates to the newly selected language.
- What happens when city data has inconsistent values (e.g., "Seoul" and "seoul")? Cities are matched case-insensitively and displayed using the canonical value from the data.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The list tab MUST display a country selector and a horizontally scrollable city chip row above the existing filter chips. City chips are single-select with "All Cities" selected by default.
- **FR-002**: The city dropdown MUST be populated with distinct city values extracted from the loaded exhibition data.
- **FR-003**: City names in the dropdown MUST be displayed in the user's selected language (city_ko or city_en).
- **FR-004**: Selecting a city MUST filter the exhibition list to show only exhibitions in that city, combined with any active filter chips (AND logic).
- **FR-005**: An "All Cities" option MUST be available in the city dropdown and selected by default.
- **FR-006**: The country selector MUST show "South Korea" as the only option for now, displayed bilingually ("대한민국" / "South Korea").
- **FR-007**: Tapping an exhibition card MUST navigate to a detail screen showing all available exhibition information.
- **FR-008**: The detail screen MUST display: cover image (if available), exhibition name, venue name, city, region, address, date range, description, and a bookmark button — all in the user's selected language with Korean fallback.
- **FR-009**: The detail screen MUST include a back navigation control that returns to the previous screen.
- **FR-010**: The detail screen MUST gracefully handle missing data: hide sections when data is empty (no cover image placeholder, no blank description area).
- **FR-011**: Exhibition cards on all tabs (Featured, List, Map dialog) MUST be tappable to open the detail screen.
- **FR-012**: The detail screen content MUST update immediately when the user switches language via the KO/EN toggle.

### Key Entities

- **Exhibition**: Existing entity, now surfaced fully on the detail screen. All bilingual fields (name, venue, city, region, address, description) and non-bilingual fields (dates, coordinates, cover image URL, flags) are displayed.
- **City Filter**: A derived value from the exhibition data. Not a separate entity — distinct city values are extracted from loaded exhibitions and presented as dropdown options.
- **Country**: Currently a static value ("South Korea"). Not persisted — used only for the UI selector.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can filter exhibitions by city and see results update within 1 second.
- **SC-002**: Users can navigate from any exhibition card to a detail screen and back in under 2 seconds total.
- **SC-003**: 100% of available exhibition data fields are displayed on the detail screen (no information is hidden that could be shown).
- **SC-004**: City filter and existing filter chips work together correctly — selecting both narrows results as expected.
- **SC-005**: The detail screen correctly displays bilingual content and respects the KO/EN toggle.

## Clarifications

### Session 2026-03-23

- Q: Should the bookmark button appear on the detail page? → A: Yes, include the bookmark button on the detail page so users can bookmark after reading the full description.
- Q: What UX pattern should the city filter use? → A: Horizontally scrollable chip row, consistent with the existing filter chips below it. "All Cities" is a chip selected by default; tapping a city chip selects it (single-select).

## Assumptions

- The city dropdown is populated client-side from the already-loaded exhibition data (no additional API call needed).
- South Korea is the only country for now; the country selector is a UI placeholder for future expansion.
- The detail screen is a new full-screen page, not a modal or bottom sheet.
- Cover images are loaded from URLs (cover_image_url field). No local image caching beyond standard HTTP caching.
- The back button preserves the previous screen's state (scroll position, active filters, selected city).
- Exhibition detail navigation works from Featured tab cards, List tab cards, and the Map tab's marker info dialog.
