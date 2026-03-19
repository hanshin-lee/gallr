# Tasks: Gallery Data Sync from Google Sheets (007)

**Input**: Design documents from `/specs/007-gallery-data-sync/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on other incomplete tasks)
- **[US1/US2/US3]**: Which user story this task belongs to
- Exact file paths included in every task

## Path Conventions

This feature touches three areas (per plan.md):
- `shared/` — KMP shared module (all networking and DTO logic)
- `composeApp/src/androidMain/` and `composeApp/src/iosMain/` — DI wiring only
- `gas/` — Google Apps Script source (stored for reference; deployed to Google Drive separately)

---

## Phase 1: Setup

**Purpose**: Create the Supabase table and GAS script skeleton. Required before any user story work.

- [x] T001 Create the `exhibitions` table in Supabase by running the SQL migration from `specs/007-gallery-data-sync/data-model.md` in the Supabase dashboard SQL editor; enable Row Level Security and create the "Public read" SELECT policy as shown in the migration
- [x] T002 Create `gas/SyncExhibitions.gs` in the repository root with a file header comment documenting the required Script Properties (`SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`) and the Google Sheet column layout from `specs/007-gallery-data-sync/contracts/sync-pipeline.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Inject Supabase credentials into both platform builds. MUST be complete before US1 DI wiring.

**⚠️ CRITICAL**: US1 platform DI tasks (T008, T009) cannot be completed until this phase is done.

- [x] T003 [P] Add `supabase.url` and `supabase.anon.key` entries to `local.properties` (gitignored); read them in `composeApp/build.gradle.kts` and expose as `BuildConfig.SUPABASE_URL` and `BuildConfig.SUPABASE_ANON_KEY` `buildConfigField` entries under `android { defaultConfig { … } }`
- [x] T004 [P] Create `iosApp/iosApp/Config.swift` (gitignored) with `supabaseUrl` and `supabaseAnonKey` constants; pass them from `ContentView.swift` to `MainViewController(supabaseUrl:anonKey:)`

**Checkpoint**: Credentials available to both Android and iOS builds. Phase 3 DI tasks can now proceed.

---

## Phase 3: User Story 1 — App Displays Live Data (Priority: P1) 🎯 MVP

**Goal**: The KMP app reads real exhibition data from Supabase PostgREST instead of hardcoded stub data. All exhibitions in the Supabase table are displayed in the List, Featured, and Map tabs.

**Independent Test**: Add one row to the Supabase table manually (SQL INSERT); launch the app on Android or iOS — that exhibition should appear in the List tab with correct fields.

### Implementation for User Story 1

- [x] T005 Write a unit test for `ExhibitionDto` snake_case JSON deserialization in `shared/src/commonTest/kotlin/com/gallr/shared/data/network/dto/ExhibitionDtoTest.kt` — parse a JSON string with `venue_name`, `opening_date`, `closing_date`, `is_featured`, `is_editors_pick`, `cover_image_url` keys and assert that the `toDomain()` result has correct field values; **this test must fail before T006 is implemented** (current `@SerialName` annotations are camelCase and will not match)
- [x] T006 Fix all `@SerialName` annotations in `shared/src/commonMain/kotlin/com/gallr/shared/data/network/dto/ExhibitionDto.kt` to use snake_case values per `specs/007-gallery-data-sync/contracts/supabase-api.md`: change `"venueName"` → `"venue_name"`, `"openingDate"` → `"opening_date"`, `"closingDate"` → `"closing_date"`, `"isFeatured"` → `"is_featured"`, `"isEditorsPick"` → `"is_editors_pick"`, `"coverImageUrl"` → `"cover_image_url"`; verify T005 now passes
- [x] T007 Update `shared/src/commonMain/kotlin/com/gallr/shared/data/network/ExhibitionApiClient.kt` to accept `supabaseUrl: String` and `anonKey: String` constructor parameters; set `baseUrl` to `supabaseUrl`; add a default `HttpRequestBuilder` block (or `defaultRequest`) that injects `apikey: anonKey` and `Authorization: Bearer $anonKey` headers on every request; update the fetch URL paths to `/rest/v1/exhibitions?select=*` (all) and `/rest/v1/exhibitions?select=*&is_featured=eq.true` (featured) per `specs/007-gallery-data-sync/contracts/supabase-api.md`
- [x] T008 [P] In the Android DI entry point (`composeApp/src/androidMain/kotlin/`), replace `StubExhibitionRepository()` with `ExhibitionRepositoryImpl(ExhibitionApiClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY))`; import `BuildConfig` from the `composeApp` module
- [x] T009 [P] In the iOS DI entry point (`composeApp/src/iosMain/kotlin/`), replace `StubExhibitionRepository()` with `ExhibitionRepositoryImpl(ExhibitionApiClient(supabaseUrl, anonKey))` where `supabaseUrl` and `anonKey` are read from `Bundle.main.infoDictionary` via a platform helper or passed in from the Swift entry point in `iosApp/`

**Checkpoint**: Build and run the app. The List and Featured tabs display live data from Supabase. No stub data remains.

---

## Phase 4: User Story 2 — Curator Manages Listings Without Technical Assistance (Priority: P2)

**Goal**: A non-technical curator edits the Google Sheet and changes appear in the app within 5 minutes automatically. The Apps Script sync pipeline handles validation, ID generation, and Supabase writes.

**Independent Test**: Add a row to the Google Sheet with a unique exhibition name. Trigger the sync manually by calling `syncToSupabase()` from the Apps Script editor. Open the app — the new exhibition appears. Delete the row; run sync again — exhibition is gone.

### Implementation for User Story 2

- [x] T010 Implement `parseDate(value)` (converts `YYYY.MM.DD` or `YYYY-MM-DD` strings to ISO `YYYY-MM-DD`) and `validateRow(row)` (returns `true` if columns A–F are non-empty and dates parse successfully; logs and returns `false` otherwise) in `gas/SyncExhibitions.gs`
- [x] T011 Implement `generateId(name, venueName, openingDate)` in `gas/SyncExhibitions.gs` using `Utilities.computeDigest(Utilities.DigestAlgorithm.SHA_256, raw)` where `raw = \`${name}|${venueName}|${openingDate}\`.toLowerCase().trim()`; return the first 8 bytes as a 16-char lowercase hex string per `specs/007-gallery-data-sync/contracts/sync-pipeline.md`
- [x] T012 Implement `deleteAllExhibitions(supabaseUrl, serviceKey)` in `gas/SyncExhibitions.gs`: call `DELETE /rest/v1/exhibitions?id=neq.IMPOSSIBLE_VALUE` with `apikey` and `Authorization: Bearer` headers set to `serviceKey`; throw on non-2xx response
- [x] T013 Implement `insertExhibitions(rows, supabaseUrl, serviceKey)` in `gas/SyncExhibitions.gs`: call `POST /rest/v1/exhibitions` with a JSON array body, headers `apikey`, `Authorization: Bearer`, `Content-Type: application/json`, and `Prefer: resolution=merge-duplicates` per `specs/007-gallery-data-sync/contracts/sync-pipeline.md`; throw on non-2xx response
- [x] T014 Implement main `syncToSupabase()` function in `gas/SyncExhibitions.gs`: read credentials from `PropertiesService.getScriptProperties()`; read all rows from the sheet (skip header row 1); call `validateRow()` per row; call `generateId()` per valid row; call `deleteAllExhibitions()` then `insertExhibitions()`; log `{ timestamp, status: 'SUCCESS'|'FAILURE', rows_read, rows_inserted, rows_skipped }` via `Logger.log()`

**Checkpoint**: Run `syncToSupabase()` manually in the Apps Script editor. Execution log shows `SUCCESS` with correct row counts. Supabase table reflects the sheet contents. The app displays the synced data.

---

## Phase 5: User Story 3 — App Remains Functional During Sync Failures (Priority: P3)

**Goal**: If the GAS sync fails (e.g. sheet permission error, Supabase outage), the pipeline logs the failure without crashing, and Supabase retains the last successfully inserted rows. The KMP app continues to display data even when the network is temporarily unreachable.

**Independent Test**: Temporarily set an invalid `SUPABASE_SERVICE_ROLE_KEY` in Script Properties. Run `syncToSupabase()` — it should log `FAILURE` with an error message but NOT corrupt existing Supabase data. Restore the key; run again — `SUCCESS` and data is current.

### Implementation for User Story 3

- [x] T015 Wrap the `deleteAllExhibitions()` + `insertExhibitions()` calls in `syncToSupabase()` in a try-catch block in `gas/SyncExhibitions.gs`; on catch, log `{ status: 'FAILURE', error: e.message, timestamp }` and return early without rethrowing — the pipeline must not crash silently and must preserve existing Supabase data by not calling delete if insert will subsequently fail (consider: only delete after insert succeeds, or use a transaction RPC if available)
- [x] T016 Review `shared/src/commonMain/kotlin/com/gallr/shared/repository/ExhibitionRepositoryImpl.kt`; ensure that network exceptions thrown by `ExhibitionApiClient` are caught and returned as `Result.failure(exception)` rather than propagating as uncaught exceptions; verify that the UI layer (list/map screens) handles `Result.failure` by showing the last displayed data or a non-empty stale state rather than an error screen or empty list

**Checkpoint**: Disable network on a device/simulator. Launch the app — existing Supabase data from last fetch is shown (or graceful error if this is a cold launch with no connection). No crash occurs.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and cleanup across all user stories.

- [x] T017 [P] Run all acceptance test scenarios from `specs/007-gallery-data-sync/quickstart.md` (SC-001 through SC-008) against the live Supabase + GAS setup; mark each as pass/fail in a comment or checklist note
- [x] T018 [P] Add a `gas/README.md` with deployment instructions: how to copy `SyncExhibitions.gs` into a new Apps Script project bound to the Google Sheet, how to set Script Properties, and how to register the installable `onEdit` and 5-minute time-based triggers per `specs/007-gallery-data-sync/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 (T001 must exist before DI wiring references a real table) — BLOCKS T008, T009
- **US1 (Phase 3)**: Depends on Phase 2 completion; T006 must complete before T008/T009; T005 must fail before T006
- **US2 (Phase 4)**: Independent of US1 phase tasks T008/T009 — can start after Phase 2; T010–T011 before T012–T013 before T014
- **US3 (Phase 5)**: Depends on T014 (US2) for GAS error handling; T016 can run alongside Phase 3
- **Polish (Phase 6)**: Depends on all US phases complete

### User Story Dependencies

- **US1 (P1)**: Depends on Phase 2 (credentials). No dependency on US2 or US3.
- **US2 (P2)**: Depends on Phase 1 (table must exist). No dependency on US1.
- **US3 (P3)**: T015 depends on T014 (extends syncToSupabase). T016 is independent.

### Within User Story 1

- T005 (test) → must fail → T006 (fix annotations) → T007 (configure client) → T008 [P] + T009 [P] (DI wiring)

### Within User Story 2

- T010 [P] + T011 [P] (utility functions) → T012 + T013 (API calls) → T014 (main function)

---

## Parallel Example: User Story 1

```
# These run in parallel after T006 + T007 complete:
T008: Wire Android DI in composeApp/src/androidMain/
T009: Wire iOS DI in composeApp/src/iosMain/
```

## Parallel Example: User Story 2

```
# These run in parallel (independent utility functions):
T010: parseDate() + validateRow() in gas/SyncExhibitions.gs
T011: generateId() in gas/SyncExhibitions.gs
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Create Supabase table (T001, T002)
2. Complete Phase 2: Inject credentials (T003, T004)
3. Complete Phase 3: Connect KMP app (T005–T009)
4. **STOP and VALIDATE**: Run app — exhibitions load from Supabase
5. Manually insert a row via Supabase dashboard to confirm end-to-end data flow

### Incremental Delivery

1. Phase 1 + 2 → infrastructure ready
2. Phase 3 (US1) → app reads live data from Supabase ✅ MVP
3. Phase 4 (US2) → curator can manage data via Google Sheet ✅ Full pipeline
4. Phase 5 (US3) → resilient to failures ✅ Production-ready
5. Phase 6 → verified and documented ✅ Ship

---

## Notes

- [P] tasks = touch different files with no shared dependencies
- Constitution Principle II: T005 (unit test) MUST be written and confirmed failing before T006 is implemented
- Constitution Principle VI: All KMP logic changes are in `shared/`; `composeApp/` tasks are DI wiring only
- `gas/SyncExhibitions.gs` is source-controlled as reference; the deployed copy lives in the Apps Script project on Google Drive — it must be manually copied there after editing
- The `local.properties` and `iosApp/Configuration/Supabase.xcconfig` files are gitignored — document their contents in `gas/README.md` and `quickstart.md` so the team can recreate them
