# Tasks: My List and List Filtering

**Input**: Design documents from `/specs/010-mylist-filter-map/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, quickstart.md ✅

**Tests**: Not requested — UI/ViewModel changes in `composeApp/` are exempt per Constitution Principle II. Manual acceptance via quickstart.md.

**Organization**: Three user stories. US1 (MAP label + rewire) is the only code-change story. US2 (filter chips) and US3 (bookmark↔map) are verification stories — US2 is independent; US3 flows from US1 completion.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2, US3)

## Path Conventions

```text
shared/src/commonMain/kotlin/com/gallr/shared/data/model/MapDisplayMode.kt  ← US1: enum rename
composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt    ← US1: rewire myListMapPins
composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapScreen.kt      ← US1: label + enum + empty state
composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt    ← US2 + US3: verify
specs/010-mylist-filter-map/quickstart.md                                    ← acceptance checklist
```

---

## Phase 1: Setup

**Purpose**: Confirm existing code state before making changes.

- [X] T001 Read `shared/src/commonMain/kotlin/com/gallr/shared/data/model/MapDisplayMode.kt` and `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapScreen.kt` and `composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt` — confirm: (a) `FILTERED` enum value exists; (b) `filteredMapPins` uses `combine(_allExhibitions, _filterState)`; (c) MapScreen label is `"FILTERED"`; (d) `bookmarkedIds` StateFlow exists in ViewModel

---

## Phase 2: User Story 1 — MAP Shows My Bookmarked Exhibitions (Priority: P1) 🎯 MVP

**Goal**: Rename `MapDisplayMode.FILTERED` → `MY_LIST`, rewire `myListMapPins` to use `bookmarkedIds`, and update MapScreen with the new label, enum reference, and empty-state message.

**Independent Test**: Build and install → open MAP tab → confirm toggle shows "MYLIST" / "ALL" (not "FILTERED" / "ALL") → bookmark 2 exhibitions on LIST tab → return to MAP, select MYLIST → confirm exactly 2 pins.

### Implementation for User Story 1

- [X] T002 [US1] In `shared/src/commonMain/kotlin/com/gallr/shared/data/model/MapDisplayMode.kt`: rename enum value `FILTERED` to `MY_LIST` and update its kdoc from "Shows only exhibitions matching the current FilterState." to "Shows only exhibitions the user has bookmarked."
- [X] T003 [US1] In `composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt`: (1) rename `filteredMapPins` property to `myListMapPins`; (2) change its `combine(...)` from `combine(_allExhibitions, _filterState)` to `combine(_allExhibitions, bookmarkedIds)` with lambda parameters `state, bookmarked` and filter condition `{ it.id in bookmarked }` instead of `{ filter.matches(it) }`; (3) change `_mapDisplayMode` initial value from `MapDisplayMode.FILTERED` to `MapDisplayMode.MY_LIST` — do NOT remove `_filterState` (still used by `filteredExhibitions`)
- [X] T004 [US1] In `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapScreen.kt`: (1) rename `val filteredPins by viewModel.filteredMapPins.collectAsState()` to `val myListPins by viewModel.myListMapPins.collectAsState()`; (2) update `activePins` assignment to `if (mapMode == MapDisplayMode.MY_LIST) myListPins else allPins`; (3) change `MapModeButton` label from `"FILTERED"` to `"MYLIST"`, its `selected` condition from `mapMode == MapDisplayMode.FILTERED` to `mapMode == MapDisplayMode.MY_LIST`, and its `onClick` to `viewModel.setMapDisplayMode(MapDisplayMode.MY_LIST)`; (4) update empty-state condition from `mapMode == MapDisplayMode.FILTERED && filteredPins.isEmpty()` to `mapMode == MapDisplayMode.MY_LIST && myListPins.isEmpty()`; (5) update empty-state message from `"No exhibitions match the current filters."` to `"Add exhibitions to your list to see them here"`
- [X] T005 [US1] Build app to verify no compile errors: run `./gradlew :composeApp:assembleDebug` from repo root — confirm BUILD SUCCESSFUL

**Checkpoint**: App builds. MAP tab shows "MYLIST"/"ALL" toggle. MYLIST mode shows only bookmarked exhibitions as pins. SC-003 satisfied (zero "FILTERED" instances). US1 complete.

---

## Phase 3: User Story 2 — LIST Tab Filter Chips Narrow the Exhibition List (Priority: P2)

**Goal**: Confirm filter chips on the LIST tab already filter correctly. Add empty-state message for zero-match case if missing.

**Independent Test**: Install app → on LIST tab, tap "FEATURED" chip → confirm list narrows to featured only → tap again to deselect → confirm full list returns.

### Implementation for User Story 2

- [X] T006 [US2] Read `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt` — confirm: (a) `GallrFilterChip` composables call `viewModel.updateFilter(...)` for each chip; (b) exhibition list is driven by `viewModel.filteredExhibitions.collectAsState()`; (c) when `filteredExhibitions` yields `Success` with an empty list and at least one filter chip is active, an empty-state message is displayed — if (c) is absent, add it now: show `"No exhibitions match the current filters."` and a `TextButton` labelled `"Clear Filters"` that calls `viewModel.updateFilter { FilterState() }`
- [X] T007 [US2] Build app to verify no regressions: run `./gradlew :composeApp:assembleDebug` — confirm BUILD SUCCESSFUL

**Checkpoint**: Filter chips correctly narrow the list; empty-state shown when no matches; full list restored when all chips deselected. US2 complete and independently verifiable.

---

## Phase 4: User Story 3 — Bookmark Button Updates My List (Priority: P3)

**Goal**: Confirm the ■/□ bookmark button in ListScreen reactively updates the MAP MYLIST view. This story is delivered by US1; US3 is an end-to-end verification.

**Independent Test**: Bookmark one exhibition on LIST tab (■ fills) → switch to MAP tab → MYLIST mode shows that exhibition as a pin.

### Implementation for User Story 3

- [X] T008 [US3] Read `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt` — confirm: (a) `BookmarkButton` icon state is driven by `bookmarkedIds.collectAsState()` (i.e., it reads `exhibition.id in bookmarkedIds`); (b) tapping the button calls `viewModel.toggleBookmark(exhibition.id)` — if (a) is not reactive (e.g., state is stored locally), fix it to derive from `viewModel.bookmarkedIds`
- [X] T009 [US3] Build and install app: run `./gradlew :composeApp:installDebug && adb shell am start -n com.gallr.app/.MainActivity` — manually test: (1) navigate to LIST tab, tap □ on one exhibition to bookmark it (icon becomes ■); (2) switch to MAP tab; (3) confirm MYLIST is selected and that exhibition appears as a pin; (4) return to LIST tab, tap ■ to unbookmark; (5) return to MAP tab, confirm pin disappears

**Checkpoint**: ■/□ button immediately reflects in MAP MYLIST. SC-001 satisfied (pin appears within 1 second of tab switch). US3 complete.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Full acceptance verification and commit.

- [X] T010 [P] Run quickstart.md acceptance checklist (`specs/010-mylist-filter-map/quickstart.md`): verify SC-001 (MYLIST pin appears within 1s), SC-002 (filter list within 300ms), SC-003 (zero "FILTERED" instances), SC-004 (100% bookmarked exhibitions with locations appear as pins) — document pass/fail for each criterion
- [X] T011 Commit all changes on branch `010-mylist-filter-map`: stage `shared/src/commonMain/kotlin/com/gallr/shared/data/model/MapDisplayMode.kt`, `composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt`, `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapScreen.kt`, any `ListScreen.kt` changes, and all spec files under `specs/010-mylist-filter-map/` — write commit message describing the MYLIST rename and map pin rewiring

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **US1 (Phase 2)**: Depends on Phase 1 (T001) — confirms code state before editing
- **US2 (Phase 3)**: Independent of Phase 1 and US1 — can start any time (different file)
- **US3 (Phase 4)**: T008 independent; T009 (install+test) depends on T005 (build successful)
- **Polish (Phase 5)**: Depends on all user stories complete

### User Story Dependencies

- **US1 (P1)**: Depends on T001 (setup read). Sequential: T002 → T003 → T004 → T005
- **US2 (P2)**: No dependencies. Fully independent — ListScreen.kt not touched by US1.
- **US3 (P3)**: T008 independent. T009 depends on T005 (app installed).

### Within User Story 1

- T002 must precede T003 — ViewModel references `MapDisplayMode.MY_LIST` (added in T002)
- T003 must precede T004 — MapScreen references `myListMapPins` (renamed in T003)
- T005 must follow T002, T003, T004 — verifies all changes compile together

### Parallel Opportunities

- T006 (US2, ListScreen read/verify) can run in parallel with T002–T004 (US1, different files)
- T008 (US3, ListScreen read/confirm) can run after T006 completes
- T010 (acceptance checklist) can run in parallel with final commit prep

---

## Parallel Example: US1 and US2

```bash
# Both stories work on different files — can proceed simultaneously:
# US1: edit MapDisplayMode.kt, TabsViewModel.kt, MapScreen.kt
# US2: read/verify ListScreen.kt (and add empty-state if missing)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Read files (T001)
2. Complete Phase 2: Rename enum (T002) → rewire ViewModel (T003) → update MapScreen (T004) → build verify (T005)
3. **STOP and VALIDATE**: MAP tab shows "MYLIST"; MYLIST mode shows only bookmarked pins — US1 complete
4. US1 alone delivers SC-003 (no "FILTERED" label) and FR-002 (MYLIST = bookmarks)

### Incremental Delivery

1. Phase 1 → files confirmed
2. US1 → MAP label + rewire complete → MVP ✅ (FR-001, FR-002, FR-003, FR-007, FR-008)
3. US2 → List filter chips verified/fixed → FR-004, FR-005 confirmed ✅
4. US3 → Bookmark↔map connection verified end-to-end → FR-006, SC-001 confirmed ✅
5. Polish → Acceptance checklist passed, committed

### Single Developer Strategy

Work sequentially: T001 → T002 → T003 → T004 → T005 (US1 complete) → T006 → T007 (US2 complete) → T008 → T009 (US3 complete) → T010 → T011.

---

## Notes

- [P] tasks = different files, no shared state
- No unit tests — ViewModel + UI changes exempt per Constitution Principle II
- `_filterState` MUST NOT be removed from TabsViewModel — still used by `filteredExhibitions` (LIST tab)
- US2 and US3 are primarily verification stories — the code already works; only the empty-state message (US2) and reactive bookmark state (US3) may need additions
- Commit (T011) is the final task — do not commit until acceptance checklist (T010) passes
