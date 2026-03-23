# Tasks: City Filter & Exhibition Detail Page

**Input**: Design documents from `/specs/013-city-filter-detail-page/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md

**Tests**: Test tasks included for shared module logic per Constitution Principle II.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup

**Purpose**: Add dependencies and shared infrastructure

- [x] T001 Add coil3-compose dependency for async image loading in composeApp/build.gradle.kts
- [x] T002 Add onExhibitionTap callback parameter to ExhibitionCard in composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/ExhibitionCard.kt

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: ViewModel state changes that both user stories depend on

**CRITICAL**: No user story work can begin until this phase is complete

- [x] T003 Add selectedCity StateFlow (String?, null = all cities), setCity() function, and distinctCities StateFlow (derived from allExhibitions) to TabsViewModel in composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt
- [x] T004 Update filteredExhibitions combine to include selectedCity filter (match on cityKo, AND with existing FilterState logic) in composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt
- [x] T005 Add selectedExhibition state (Exhibition?) and navigation logic (show detail vs tabs) to App.kt in composeApp/src/commonMain/kotlin/com/gallr/app/App.kt

**Checkpoint**: Foundation ready — city state, filtered list, and navigation shell in place

---

## Phase 3: User Story 1 — City Filter on List Tab (Priority: P1) MVP

**Goal**: Users can filter exhibitions by city using a horizontally scrollable chip row

**Independent Test**: Open list tab, tap a city chip, verify list filters. Tap "All Cities", verify all return. Combine with filter chips, verify AND logic.

### Implementation for User Story 1

- [x] T006 [US1] Add country label ("대한민국" / "South Korea" based on lang) and horizontally scrollable city chip row above the existing FILTERS section in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt
- [x] T007 [US1] Create GallrCityChip composable (single-select style, "All Cities" chip + dynamic city chips from viewModel.distinctCities) in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt
- [x] T008 [US1] Wire city chip selection to viewModel.setCity() — "All Cities" passes null, city chips pass cityKo value in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt
- [x] T009 [US1] Add bilingual empty state for city filter: "No exhibitions in [city name]" / "[city name]에 전시가 없습니다" with clear filter action in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt

**Checkpoint**: City filtering works on List tab. Chips scroll horizontally, single-select, bilingual labels.

---

## Phase 4: User Story 2 — Exhibition Detail Page (Priority: P1)

**Goal**: Tapping any exhibition card opens a full-screen detail page with all exhibition info

**Independent Test**: Tap exhibition card → detail screen opens with cover image, name, venue, city, region, address, dates, description, bookmark. Tap back → returns to previous tab.

### Implementation for User Story 2

- [x] T010 [US2] Create ExhibitionDetailScreen composable with scrollable layout: cover image (AsyncImage via Coil), name, venue, city/region, address, date range, description, bookmark button in composeApp/src/commonMain/kotlin/com/gallr/app/ui/detail/ExhibitionDetailScreen.kt
- [x] T011 [US2] Add back button (arrow icon) to ExhibitionDetailScreen top bar, wired to onBack callback in composeApp/src/commonMain/kotlin/com/gallr/app/ui/detail/ExhibitionDetailScreen.kt
- [x] T012 [US2] Handle missing data: hide cover image section when coverImageUrl is null, hide description section when descriptionKo is empty, handle image load failure gracefully in composeApp/src/commonMain/kotlin/com/gallr/app/ui/detail/ExhibitionDetailScreen.kt
- [x] T013 [P] [US2] Wire ExhibitionCard onExhibitionTap in FeaturedScreen to set selectedExhibition in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/featured/FeaturedScreen.kt
- [x] T014 [P] [US2] Wire ExhibitionCard onExhibitionTap in ListScreen to set selectedExhibition in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt
- [x] T015 [P] [US2] Add "View Details" button or tap action in MapScreen marker dialog to set selectedExhibition in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapScreen.kt

**Checkpoint**: Detail page works from all three tabs. All bilingual fields display. Bookmark works. Back preserves state.

---

## Phase 5: User Story 3 — Country Selector (Priority: P3)

**Goal**: Country label shows "South Korea" as UI placeholder for future expansion

**Independent Test**: Verify country label is visible above city chips on list tab.

### Implementation for User Story 3

- [x] T016 [US3] Style the country label as a selectable chip or label (tappable but only one option: "South Korea" / "대한민국") in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt

**Checkpoint**: Country selector visible. Single option. Bilingual label.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and cleanup

- [x] T017 Verify language toggle (KO/EN) updates city chip labels, country label, detail page content, and empty states immediately
- [x] T018 Run full quickstart.md verification: city filter, detail page from all tabs, back navigation, missing data handling
- [x] T019 Update specs/013-city-filter-detail-page/spec.md status from Draft to Complete

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Setup — BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Foundational (Phase 2)
- **US2 (Phase 4)**: Depends on Foundational (Phase 2) — can run in parallel with US1
- **US3 (Phase 5)**: Depends on US1 (Phase 3) — extends the city filter UI
- **Polish (Phase 6)**: Depends on all user stories

### User Story Dependencies

- **US1 (P1)**: Foundational → US1 (city filter on list tab)
- **US2 (P1)**: Foundational → US2 (detail page + navigation) — independent of US1
- **US3 (P3)**: US1 → US3 (extends the filter area UI)

### Parallel Opportunities

- US1 and US2 can run in parallel after Foundational phase
- T013 + T014 + T015 can all run in parallel (different screen files)

---

## Parallel Example: User Story 2

```bash
# Launch card tap wiring in parallel (different files):
Task: "Wire ExhibitionCard tap in FeaturedScreen"
Task: "Wire ExhibitionCard tap in ListScreen"
Task: "Wire ExhibitionCard tap in MapScreen dialog"
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2)

1. Complete Phase 1: Setup (Coil dependency + card tap callback)
2. Complete Phase 2: Foundational (ViewModel state + App navigation)
3. Complete Phase 3: US1 — city filter chips work
4. Complete Phase 4: US2 — detail page works (can be parallel with Phase 3)
5. **STOP and VALIDATE**: Filter by city, tap card to see details, back to list
6. Deploy/demo if ready

### Full Delivery

1. Setup → Foundational → US1 + US2 (parallel) → US3 → Polish

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- No new API calls — city data derived from loaded exhibitions
- Navigation via composable state (no nav library) per Principle III
- Coil 3 for cover image loading (KMP-compatible)
