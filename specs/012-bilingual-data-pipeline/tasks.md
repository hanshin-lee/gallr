# Tasks: Bilingual Data Pipeline

**Input**: Design documents from `/specs/012-bilingual-data-pipeline/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md

**Tests**: Test tasks included for shared module logic per Constitution Principle II (Test-First for shared KMP module).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup

**Purpose**: Schema migration and project-level configuration changes

- [x] T001 Run Supabase migration to rename columns and add `_en` columns per specs/012-bilingual-data-pipeline/data-model.md migration SQL
- [x] T002 Update Google Sheet headers from single columns to `_ko`/`_en` pairs (name→name_ko, add name_en, etc.)
- [x] T003 Configure `ignoreUnknownKeys = true` in JSON serialization setup in shared/src/commonMain/kotlin/com/gallr/shared/data/network/ExhibitionApiClient.kt

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core data model and language infrastructure that ALL user stories depend on

**CRITICAL**: No user story work can begin until this phase is complete

- [x] T004 [P] Create AppLanguage enum in shared/src/commonMain/kotlin/com/gallr/shared/data/model/AppLanguage.kt
- [x] T005 [P] Create LanguageRepository interface in shared/src/commonMain/kotlin/com/gallr/shared/repository/LanguageRepository.kt
- [x] T006 Create LanguageRepositoryImpl backed by DataStore in shared/src/commonMain/kotlin/com/gallr/shared/repository/LanguageRepositoryImpl.kt
- [x] T007 Add platform-specific device locale detection via expect/actual for getSystemLanguage() in shared/src/commonMain, shared/src/androidMain, shared/src/iosMain
- [x] T008 Update Exhibition data class with bilingual fields (nameKo, nameEn, venueNameKo, venueNameEn, cityKo, cityEn, regionKo, regionEn, descriptionKo, descriptionEn) and localized*() accessor methods in shared/src/commonMain/kotlin/com/gallr/shared/data/model/Exhibition.kt
- [x] T009 Update ExhibitionDto with bilingual @SerialName annotations (_ko/_en) and updated toDomain() mapping in shared/src/commonMain/kotlin/com/gallr/shared/data/network/dto/ExhibitionDto.kt
- [x] T010 Write unit tests for ExhibitionDto bilingual deserialization (both languages present, English missing, unknown fields ignored) in shared/src/commonTest/kotlin/com/gallr/shared/data/network/dto/ExhibitionDtoTest.kt
- [x] T011 Wire LanguageRepository into Android entry point (MainActivity.kt) and iOS entry point (MainViewController.kt), passing to App composable

**Checkpoint**: Foundation ready — bilingual data model, language persistence, and deserialization all working

---

## Phase 3: User Story 1 — Bilingual Exhibition Display (Priority: P1) MVP

**Goal**: App displays exhibition data in Korean or English based on device locale, with Korean fallback

**Independent Test**: Set device locale to English → verify English exhibition names display. Set to Korean → verify Korean names display. Remove English value → verify Korean fallback works.

### Implementation for User Story 1

- [x] T012 [US1] Update ExhibitionCard composable to use exhibition.localizedName(lang), localizedVenueName(lang), localizedCity(lang), localizedRegion(lang) in composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/ExhibitionCard.kt
- [x] T013 [US1] Add language StateFlow to TabsViewModel from LanguageRepository, expose current AppLanguage to UI in composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt
- [x] T014 [US1] Pass current language from TabsViewModel to FeaturedScreen and its ExhibitionCard instances in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/featured/FeaturedScreen.kt
- [x] T015 [US1] Pass current language to ListScreen and its ExhibitionCard instances in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt
- [x] T016 [US1] Pass current language to MapScreen for marker labels and dialog content in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapScreen.kt

**Checkpoint**: App displays bilingual exhibition data based on device locale. Korean fallback works when English is empty.

---

## Phase 4: User Story 2 — Simplified Spreadsheet Schema Management (Priority: P1)

**Goal**: Sync script reads headers dynamically — new columns require zero script changes

**Independent Test**: Add a new column "artist_name" to Google Sheet with matching Supabase column → run sync → verify data appears without script modification. Reorder columns → verify sync still works.

### Implementation for User Story 2

- [x] T017 [US2] Refactor syncToSupabase() to read row 1 headers, build headerName→columnIndex map, normalize headers (lowercase, trim) in gas/SyncExhibitions.gs
- [x] T018 [US2] Refactor validateRow() to use header map instead of positional indices, validate required headers exist (name_ko, venue_name_ko, city_ko, region_ko, opening_date, closing_date) in gas/SyncExhibitions.gs
- [x] T019 [US2] Refactor buildRecord() to dynamically construct record from header map, log skipped unknown columns in gas/SyncExhibitions.gs
- [x] T020 [US2] Update generateId() to use header-mapped name_ko, venue_name_ko, opening_date values in gas/SyncExhibitions.gs
- [x] T021 [US2] Trigger sync and verify data populates correctly in Supabase with new bilingual columns

**Checkpoint**: Sync script is header-driven. Adding/reordering columns in the spreadsheet requires zero script changes.

---

## Phase 5: User Story 3 — Bilingual Column Convention in Spreadsheet (Priority: P2)

**Goal**: Spreadsheet uses _ko/_en paired columns; sync handles them correctly

**Independent Test**: Fill in name_ko and name_en for several exhibitions → sync → verify both columns in Supabase. Leave name_en blank → verify row accepted with empty English value.

### Implementation for User Story 3

- [x] T022 [US3] Populate English values (name_en, venue_name_en, city_en, region_en, description_en) for existing exhibitions in Google Sheet
- [x] T023 [US3] Run sync and verify both _ko and _en columns populated correctly in Supabase
- [x] T024 [US3] Verify rows with blank _en values are accepted and synced with empty strings

**Checkpoint**: Spreadsheet uses bilingual convention. Sync handles _ko/_en pairs correctly.

---

## Phase 6: User Story 4 — In-App Language Toggle (Priority: P2)

**Goal**: KO/EN button in top bar lets users manually switch language, persisted across restarts

**Independent Test**: Tap KO button → shows "EN" → all text switches to English. Kill app → reopen → English still selected. Tap EN → switches back to Korean.

### Implementation for User Story 4

- [x] T025 [P] [US4] Create bilingual UI string resources in composeApp/src/commonMain/composeResources/values/strings.xml (English defaults) and composeApp/src/commonMain/composeResources/values-ko/strings.xml (Korean)
- [x] T026 [US4] Add language toggle button ("KO"/"EN") to TopAppBar actions row, next to info button, wired to TabsViewModel.setLanguage() in composeApp/src/commonMain/kotlin/com/gallr/app/App.kt
- [x] T027 [US4] Add setLanguage(AppLanguage) function to TabsViewModel that calls LanguageRepository.setLanguage() in composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt
- [x] T028 [P] [US4] Replace hardcoded tab labels with localized strings in composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/GallrNavigationBar.kt
- [x] T029 [P] [US4] Replace hardcoded strings (section header, error/empty messages, retry/refresh labels) with localized strings in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/featured/FeaturedScreen.kt
- [x] T030 [P] [US4] Replace hardcoded strings (FILTERS header, filter chip labels, error/empty messages, Clear Filters) with localized strings in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt
- [x] T031 [P] [US4] Replace hardcoded strings (MAP header, MYLIST/ALL toggle labels, empty state text, CLOSE button) with localized strings in composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapScreen.kt

**Checkpoint**: Language toggle works. All UI labels and exhibition data switch language instantly. Preference persists across restarts.

---

## Phase 7: User Story 5 — Graceful App Handling of New Fields (Priority: P3)

**Goal**: App ignores unknown fields from Supabase without crashing

**Independent Test**: Add an unknown column to Supabase → fetch from app → verify no crash and exhibitions load normally.

### Implementation for User Story 5

- [x] T032 [US5] Write unit test: ExhibitionDto deserializes successfully when JSON contains unknown fields (e.g., "artist_name": "test") in shared/src/commonTest/kotlin/com/gallr/shared/data/network/dto/ExhibitionDtoTest.kt
- [x] T033 [US5] Verify ignoreUnknownKeys = true is configured (done in T003) and test passes

**Checkpoint**: Forward-compatible deserialization confirmed. Unknown fields ignored gracefully.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and cleanup

- [x] T034 [P] Update ExhibitionMapPin model to use bilingual fields for marker labels in shared/src/commonMain/kotlin/com/gallr/shared/data/model/ExhibitionMapPin.kt
- [x] T035 [P] Update StubExhibitionRepository test data with bilingual fields in shared/src/commonMain/kotlin/com/gallr/shared/repository/StubExhibitionRepository.kt
- [x] T036 Run full quickstart.md verification: bilingual display, language toggle, sync with new column, persistence across restart
- [x] T037 Update specs/012-bilingual-data-pipeline/spec.md status from Draft to Complete

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Setup completion — BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Foundational (Phase 2)
- **US2 (Phase 4)**: Depends on Setup (Phase 1) only — can run in parallel with Phase 2/3 (sync script is independent of app code)
- **US3 (Phase 5)**: Depends on US2 (Phase 4) — needs updated sync script
- **US4 (Phase 6)**: Depends on US1 (Phase 3) — needs bilingual display working first
- **US5 (Phase 7)**: Depends on Foundational (Phase 2) — only needs ignoreUnknownKeys config
- **Polish (Phase 8)**: Depends on all user stories

### User Story Dependencies

- **US1 (P1)**: Foundational → US1 (bilingual display)
- **US2 (P1)**: Setup → US2 (sync script — independent of app code)
- **US3 (P2)**: US2 → US3 (needs updated sync to populate data)
- **US4 (P2)**: US1 → US4 (needs bilingual display before adding toggle)
- **US5 (P3)**: Foundational → US5 (needs ignoreUnknownKeys)

### Parallel Opportunities

- T004 + T005 can run in parallel (different files)
- US2 (sync script) can run in parallel with US1 (app code) — completely independent codebases
- US5 can run in parallel with US1/US4 — only needs T003
- T028 + T029 + T030 + T031 can all run in parallel (different screen files)
- T034 + T035 can run in parallel (different files)

---

## Parallel Example: User Story 4

```bash
# Launch all UI string replacements in parallel (different files):
Task: "Replace hardcoded tab labels in GallrNavigationBar.kt"
Task: "Replace hardcoded strings in FeaturedScreen.kt"
Task: "Replace hardcoded strings in ListScreen.kt"
Task: "Replace hardcoded strings in MapScreen.kt"
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2)

1. Complete Phase 1: Setup (migration + sheet headers)
2. Complete Phase 2: Foundational (data models + language repo)
3. Complete Phase 3: US1 — bilingual display works
4. Complete Phase 4: US2 — sync script is header-driven (can be parallel with Phase 2+3)
5. **STOP and VALIDATE**: App shows bilingual data, sync is maintainable
6. Deploy/demo if ready

### Full Delivery

1. Setup → Foundational → US1 + US2 (parallel) → US3 → US4 → US5 → Polish
2. Each story adds value without breaking previous stories

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Commit after each task or logical group
- US2 (sync script in Google Apps Script) is completely independent from app code — can be developed in parallel
- Constitution Principle II: Tests written for shared module logic (ExhibitionDto deserialization)
- Constitution Principle VI: All business logic in shared/ module; only UI composables in composeApp/
