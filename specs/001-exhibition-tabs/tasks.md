---

description: "Task list for Three-Tab Exhibition Discovery Navigation"
---

# Tasks: Three-Tab Exhibition Discovery Navigation

**Input**: Design documents from `/specs/001-exhibition-tabs/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅

**Tests**: Shared-module unit tests included per Constitution Principle II (shared KMP
logic MUST have unit tests). UI layers are not test-driven unless explicitly requested.

**Organization**: Tasks grouped by user story to enable independent implementation and
testing of each story.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1–US4)
- Exact file paths in all task descriptions

## Path Conventions

```
shared/src/commonMain/kotlin/com/gallr/shared/   # Business logic
shared/src/androidMain/kotlin/com/gallr/shared/  # Android actual implementations
shared/src/iosMain/kotlin/com/gallr/shared/      # iOS actual implementations
shared/src/commonTest/kotlin/com/gallr/shared/   # Shared unit tests
composeApp/src/commonMain/kotlin/com/gallr/app/  # Shared UI
composeApp/src/androidMain/kotlin/com/gallr/app/ # Android UI entry + map actual
composeApp/src/iosMain/kotlin/com/gallr/app/     # iOS UI entry + map actual
iosApp/                                           # Xcode project
```

---

## Phase 1: Setup

**Purpose**: Project scaffold and Gradle configuration. No business logic yet.

- [x] T001 Initialize KMP Gradle project with two-module structure: create `shared/`, `composeApp/`, and `iosApp/` at repository root with root `settings.gradle.kts` including both modules
- [x] T002 [P] Configure `shared/build.gradle.kts`: KMP plugin, Android (minSdk 26) + iOS (14.0) targets, Ktor 2.9+ core/logging/content-negotiation/okhttp/darwin, DataStore Preferences 1.1+, kotlinx.serialization 1.7+, kotlinx-datetime, kotlin.test
- [x] T003 [P] Configure `composeApp/build.gradle.kts`: Compose Multiplatform 1.8.0+, AndroidX ViewModel 2.8.0+, Android target (minSdk 26), iOS target (14.0+), depend on `shared` module
- [x] T004 [P] Create package scaffolds in `shared/src/`: `commonMain/kotlin/com/gallr/shared/data/model/`, `data/network/dto/`, `repository/`, `platform/`; mirror in `androidMain/` and `iosMain/`
- [x] T005 [P] Create package scaffolds in `composeApp/src/`: `commonMain/kotlin/com/gallr/app/ui/tabs/featured/`, `ui/tabs/list/`, `ui/tabs/map/`, `ui/components/`, `viewmodel/`; mirror `androidMain/` and `iosMain/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain models, API client, exhibition repository, ViewModel skeleton,
and app navigation shell. ALL user stories depend on this phase completing first.

**⚠️ CRITICAL**: No user story work begins until this phase is complete.

- [x] T006 Create `Exhibition` domain model in `shared/src/commonMain/kotlin/com/gallr/shared/data/model/Exhibition.kt` (fields: id, name, venueName, city, region, openingDate, closingDate, isFeatured, isEditorsPick, latitude?, longitude?, description, coverImageUrl?)
- [x] T007 [P] Create `FilterState` data class with field definitions (no matches() logic yet) in `shared/src/commonMain/kotlin/com/gallr/shared/data/model/FilterState.kt` (fields: regions, showFeatured, showEditorsPick, openingThisWeek, closingThisWeek; default = all false/empty)
- [x] T008 [P] Create `MapDisplayMode` enum in `shared/src/commonMain/kotlin/com/gallr/shared/data/model/MapDisplayMode.kt` (values: FILTERED, ALL; default: FILTERED)
- [x] T009 [P] Create `ExhibitionDto` with `@Serializable` and `toDomain()` mapping to `Exhibition` in `shared/src/commonMain/kotlin/com/gallr/shared/data/network/dto/ExhibitionDto.kt` (depends T006)
- [x] T010 Define `ExhibitionRepository` interface in `shared/src/commonMain/kotlin/com/gallr/shared/repository/ExhibitionRepository.kt` (`getFeaturedExhibitions(): Result<List<Exhibition>>`, `getExhibitions(filter: FilterState): Result<List<Exhibition>>`) (depends T006, T007)
- [x] T011 Implement `ExhibitionApiClient` using Ktor in `shared/src/commonMain/kotlin/com/gallr/shared/data/network/ExhibitionApiClient.kt` — configures OkHttp/Darwin engines, Content Negotiation, Logging; exposes `fetchFeatured()` and `fetchExhibitions()` (depends T009)
- [x] T012 Implement `ExhibitionRepositoryImpl` in `shared/src/commonMain/kotlin/com/gallr/shared/repository/ExhibitionRepositoryImpl.kt` — wraps `ExhibitionApiClient`, returns `Result<T>`, applies client-side `FilterState` fallback (depends T010, T011)
- [x] T013 Create `TabsViewModel` skeleton in `composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt` — `featuredExhibitions: StateFlow<Result<List<Exhibition>>>`, `mapDisplayMode: MutableStateFlow<MapDisplayMode>`; injects `ExhibitionRepository` (depends T010, T008)
- [x] T014 Create `App.kt` root composable in `composeApp/src/commonMain/kotlin/com/gallr/app/App.kt` — `BottomNavigation` with three tabs (Featured, List, Map); `TabsViewModel` scoped at root; routes each tab to its screen placeholder (depends T013)
- [x] T015 Create `MainActivity` in `composeApp/src/androidMain/kotlin/com/gallr/app/MainActivity.kt` — sets content to `App()` composable (depends T014)
- [x] T016 [P] Create `MainViewController` in `composeApp/src/iosMain/kotlin/com/gallr/app/MainViewController.kt` — returns UIViewController wrapping `App()` composable (depends T014)

**Checkpoint**: App shell launches on Android and iOS showing three empty tabs.

---

## Phase 3: User Story 1 — Browse Featured Exhibitions (Priority: P1) 🎯 MVP

**Goal**: Featured tab displays a curated exhibition list with loading, empty, and
error states. Bookmark icon renders but is not yet functional (US4).

**Independent Test**: Launch app → Featured tab shows at least one exhibition card with
name, venue, city, and dates. Airplane mode → error state with retry button (not blank).

### Tests for User Story 1 (Constitution Principle II — shared module logic) ⚠️

> **Write these FIRST. Verify they compile before implementing.**

- [x] T017 [P] [US1] Unit test: `ExhibitionApiClient` maps `ExhibitionDto` JSON correctly to `Exhibition` domain model in `shared/src/commonTest/kotlin/com/gallr/shared/data/network/ExhibitionApiClientTest.kt`

### Implementation for User Story 1

- [x] T018 [US1] Create `ExhibitionCard` composable in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/ExhibitionCard.kt` — displays name, venueName, city, openingDate, closingDate; bookmark icon slot (non-functional for now)
- [x] T019 [US1] Create `FeaturedScreen` composable in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/featured/FeaturedScreen.kt` — observes `featuredExhibitions` StateFlow; renders loading indicator, list of `ExhibitionCard`, empty state, error state with retry (depends T018)
- [x] T020 [US1] Wire `FeaturedScreen` into `App.kt` Featured tab slot; trigger `loadFeaturedExhibitions()` on `TabsViewModel` init in `composeApp/src/commonMain/kotlin/com/gallr/app/App.kt` and `TabsViewModel.kt` (depends T013, T019)

**Checkpoint**: US1 independently testable. Featured tab shows exhibitions, handles all data states.

---

## Phase 4: User Story 2 — Filter via List Tab (Priority: P2)

**Goal**: List tab shows filter toggles (Region, Featured, Editor's Picks, Opening This
Week, Closing This Week) with a live-updating exhibition list below. Filter state persists
when switching tabs. Map tab in Filtered mode reflects active filters.

**Independent Test**: Toggle "Opening This Week" on in List tab → list updates immediately →
switch to Map tab → only opening-this-week exhibitions appear in Filtered mode.

### Tests for User Story 2 (Constitution Principle II) ⚠️

> **Write these FIRST. Verify they FAIL before adding matches() logic.**

- [x] T021 [P] [US2] Unit test: `FilterState.matches()` — all filter combinations (all-off returns true, individual flags, regions, opening/closing week OR logic, AND between groups) in `shared/src/commonTest/kotlin/com/gallr/shared/data/model/FilterStateTest.kt`

### Implementation for User Story 2

- [x] T022 [US2] Implement `FilterState.matches(exhibition: Exhibition): Boolean` in `shared/src/commonMain/kotlin/com/gallr/shared/data/model/FilterState.kt` per the logic in `data-model.md` (OR for opening/closing week; AND between other groups) — T021 tests must pass (depends T007, T021)
- [x] T023 [US2] Add `filterState: MutableStateFlow<FilterState>`, `filteredExhibitions: StateFlow<Result<List<Exhibition>>>`, and `fun updateFilter(update: FilterState.() -> FilterState)` to `TabsViewModel` in `composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt` (depends T013, T022)
- [x] T024 [US2] Create `ListScreen` composable in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt` — renders filter toggles for all five filter types; shows `filteredExhibitions` list using `ExhibitionCard` below toggles; calls `updateFilter()` on toggle (depends T018, T023)
- [x] T025 [US2] Wire `ListScreen` into `App.kt` List tab slot in `composeApp/src/commonMain/kotlin/com/gallr/app/App.kt` (depends T014, T024)

**Checkpoint**: US2 independently testable. Filter state persists across tab navigation.

---

## Phase 5: User Story 4 — Bookmark an Exhibition (Priority: P2)

**Goal**: Every exhibition card in Featured and List tabs has a functional bookmark icon.
Tap to bookmark; tap again to remove. Bookmark state persists across app restarts. State
is consistent wherever the same exhibition card appears.

**Independent Test**: Tap bookmark icon on Featured tab card → icon fills → force-quit →
relaunch → icon still filled on same exhibition. Also filled in List tab results.

### Tests for User Story 4 (Constitution Principle II) ⚠️

> **Write these FIRST. Verify they compile before implementing BookmarkRepositoryImpl.**

- [x] T026 [P] [US4] Unit test: `BookmarkRepositoryImpl.addBookmark()`, `removeBookmark()`, `observeBookmarkedIds()` flow emits correct values, `isBookmarked()` returns correct state in `shared/src/commonTest/kotlin/com/gallr/shared/repository/BookmarkRepositoryTest.kt`

### Implementation for User Story 4

- [x] T027 [P] [US4] Create `Bookmark` model in `shared/src/commonMain/kotlin/com/gallr/shared/data/model/Bookmark.kt` (fields: exhibitionId: String, savedAt: Instant)
- [x] T028 [P] [US4] Define `BookmarkRepository` interface in `shared/src/commonMain/kotlin/com/gallr/shared/repository/BookmarkRepository.kt` (`observeBookmarkedIds(): Flow<Set<String>>`, `addBookmark(id)`, `removeBookmark(id)`, `isBookmarked(id): Boolean`)
- [x] T029 [US4] Implement DataStore expect/actual: `expect fun createDataStore(): DataStore<Preferences>` in `shared/src/commonMain/kotlin/com/gallr/shared/platform/DataStorePath.kt`; platform implementations in `shared/src/androidMain/kotlin/com/gallr/shared/platform/DataStorePath.android.kt` and `shared/src/iosMain/kotlin/com/gallr/shared/platform/DataStorePath.ios.kt` (depends T028)
- [x] T030 [US4] Implement `BookmarkRepositoryImpl` using DataStore Preferences in `shared/src/commonMain/kotlin/com/gallr/shared/repository/BookmarkRepositoryImpl.kt` — stores/reads `Set<String>` of exhibition IDs via DataStore; T026 tests must pass (depends T028, T029, T026)
- [x] T031 [US4] Add `bookmarkedIds: StateFlow<Set<String>>` and `fun toggleBookmark(exhibitionId: String)` to `TabsViewModel` in `composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt` (depends T013, T030)
- [x] T032 [US4] Create `BookmarkButton` composable in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/BookmarkButton.kt` — filled/unfilled icon based on `isBookmarked: Boolean`; calls `onToggle()` on tap
- [x] T033 [US4] Integrate `BookmarkButton` into `ExhibitionCard` in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/ExhibitionCard.kt` — pass `isBookmarked` from `bookmarkedIds` and `onToggle` from `TabsViewModel.toggleBookmark()` (depends T031, T032)

**Checkpoint**: US4 independently testable. Bookmarks persist; state consistent across tabs.

---

## Phase 6: User Story 3 — Explore Exhibitions on Map (Priority: P3)

**Goal**: Map tab shows exhibition markers. Two display modes: Filtered (respects List tab
filter state) and All (all exhibitions with coordinates). Tap a marker to see a summary
card (name, venue, dates). Map provider is a pluggable expect/actual composable.

**Independent Test**: With "Opening This Week" filter active → Map tab Filtered mode shows
only matching markers → switch to All → all exhibitions visible → tap marker → summary card
appears.

### Implementation for User Story 3

- [x] T034 [P] [US3] Create `ExhibitionMapPin` projection in `shared/src/commonMain/kotlin/com/gallr/shared/data/model/ExhibitionMapPin.kt` (fields: id, name, venueName, latitude, longitude, openingDate, closingDate; constructed only from exhibitions where latitude != null)
- [x] T035 [US3] Add `filteredMapPins: StateFlow<List<ExhibitionMapPin>>` and `allMapPins: StateFlow<List<ExhibitionMapPin>>` to `TabsViewModel` in `composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt` — derived from filteredExhibitions/allExhibitions filtered to non-null coordinates (depends T034, T023)
- [x] T036 [US3] Define `expect fun MapView(pins: List<ExhibitionMapPin>, onMarkerTap: (ExhibitionMapPin) -> Unit, modifier: Modifier)` composable in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapView.kt`
- [x] T037 [P] [US3] Implement `MapView` actual for Android in `composeApp/src/androidMain/kotlin/com/gallr/app/ui/tabs/map/MapView.android.kt` — stub using placeholder markers (final map SDK TBD; use Google Maps Compose or Mapbox v9 when provider decided) (depends T036)
- [x] T038 [P] [US3] Implement `MapView` actual for iOS in `composeApp/src/iosMain/kotlin/com/gallr/app/ui/tabs/map/MapView.ios.kt` — stub using UIKitView with MapKit (final provider TBD) (depends T036)
- [x] T039 [US3] Create `MapScreen` composable in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapScreen.kt` — Filtered/All mode toggle, passes correct pin list to `MapView`, shows summary card on marker tap (depends T035, T036)
- [x] T040 [US3] Wire `MapScreen` into `App.kt` Map tab slot in `composeApp/src/commonMain/kotlin/com/gallr/app/App.kt` (depends T014, T039)

**Checkpoint**: US3 independently testable. Both map modes work; marker tap shows summary.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Observability, final validation, documentation.

- [x] T041 [P] Configure Ktor Logging plugin in `ExhibitionApiClient` for structured network logging (Principle V) in `shared/src/commonMain/kotlin/com/gallr/shared/data/network/ExhibitionApiClient.kt`
- [x] T042 [P] Add error event logging in `TabsViewModel` for all `Result.failure` paths in `composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt` (Principle V)
- [x] T043 [P] Run `quickstart.md` validation for all four user stories on Android
- [x] T044 [P] Run `quickstart.md` validation for all four user stories on iOS simulator

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — **blocks all user stories**
- **Phase 3 (US1 P1)**: Depends on Phase 2 completion
- **Phase 4 (US2 P2)**: Depends on Phase 2; independent of Phase 3 (can start in parallel with US1 if staffed)
- **Phase 5 (US4 P2)**: Depends on Phase 2 and Phase 3 (ExhibitionCard defined in US1)
- **Phase 6 (US3 P3)**: Depends on Phase 2, Phase 4 (FilterState logic), Phase 5 (TabsViewModel complete)
- **Phase 7 (Polish)**: Depends on all user story phases

### User Story Dependencies

- **US1 (P1)**: Can start after Foundational — no other story dependencies
- **US2 (P2)**: Can start after Foundational — independent of US1 (but shares ExhibitionCard from US1; coordinate if parallel)
- **US4 (P2)**: Start after US1 (needs ExhibitionCard) — independent of US2
- **US3 (P3)**: Start after US2 (needs FilterState.matches()) and US4 (needs complete TabsViewModel)

### Within Each User Story

- Tests MUST be written and FAIL before implementing the logic they test (Principle II)
- Models before services
- Services/repositories before ViewModel integration
- ViewModel integration before screen composables
- Story complete → validate independently before moving on

### Parallel Opportunities

- All Phase 1 tasks marked [P] can run in parallel
- T002, T003, T004, T005 can all run simultaneously after T001
- Within Foundational: T007, T008 can run in parallel with T009 (after T006)
- T015, T016 can run in parallel
- T017 (US1 test), T021 (US2 test), T026 (US4 test) can all run in parallel with each other
- T035, T036, T037, T038 (US3 map) — T037 and T038 run in parallel after T036
- Phase 7: T041, T042, T043, T044 all parallel

---

## Parallel Example: Foundational Phase

```bash
# After T001 (project init), launch in parallel:
Task: "Configure shared/build.gradle.kts — T002"
Task: "Configure composeApp/build.gradle.kts — T003"
Task: "Create shared/ package scaffolds — T004"
Task: "Create composeApp/ package scaffolds — T005"

# After T006 (Exhibition model), launch in parallel:
Task: "Create FilterState skeleton — T007"
Task: "Create MapDisplayMode enum — T008"
Task: "Create ExhibitionDto + mapping — T009"
```

## Parallel Example: US3 Map Phase

```bash
# After T036 (MapView expect definition):
Task: "Implement MapView.android.kt — T037"
Task: "Implement MapView.ios.kt — T038"
```

---

## Implementation Strategy

### MVP First (US1 Only — ~14 tasks)

1. Complete Phase 1: Setup (T001–T005)
2. Complete Phase 2: Foundational (T006–T016) — CRITICAL GATE
3. Complete Phase 3: US1 (T017–T020)
4. **STOP and VALIDATE**: Run quickstart US1 steps on Android and iOS
5. Demo: App launches to Featured tab showing exhibition list

### Incremental Delivery

1. Setup + Foundational → app shell with empty tabs
2. **US1**: Featured tab functional → first demo-able state
3. **US2**: Filter + List tab functional → filter state shared with Map
4. **US4**: Bookmarking functional → persistent across restarts
5. **US3**: Map tab functional → full feature complete
6. Polish → production-ready logging and validation

### Parallel Team Strategy

With two developers after Foundational completes:

- Developer A: US1 (Featured) → US4 (Bookmark)
- Developer B: US2 (Filters) → US3 (Map, depends on US2)

---

## Notes

- [P] = different files, no unresolved dependencies
- Story labels (US1–US4) map to user stories in spec.md
- Test tasks MUST come before the implementation they test within each phase
- Map provider (T037, T038) is a stub — final SDK chosen when map API decision is made
- T033 updates ExhibitionCard (defined in US1/T018): coordinate between US1 and US4 if parallel
- FilterState.matches() (T022) is the only logic connecting US2 and US3 — ensure US2 checkpoint passes before starting US3
