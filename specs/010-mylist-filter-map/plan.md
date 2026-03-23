# Implementation Plan: My List and List Filtering

**Branch**: `010-mylist-filter-map` | **Date**: 2026-03-20 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/010-mylist-filter-map/spec.md`

---

## Summary

Rename `MapDisplayMode.FILTERED` → `MapDisplayMode.MY_LIST` in the shared data model, rewire `TabsViewModel.filteredMapPins` to derive from `bookmarkedIds` instead of `_filterState`, and update `MapScreen.kt` to use the label "MYLIST" and the new enum value. The LIST tab filter chips already work correctly — no changes required there. Three files change; zero new dependencies; zero new abstractions.

---

## Technical Context

**Language/Version**: Kotlin 2.1.20 (KMP); Compose Multiplatform 1.8.0
**Primary Dependencies**: No new dependencies — existing `BookmarkRepository`, `bookmarkedIds: StateFlow<Set<String>>`, and `MutableStateFlow<MapDisplayMode>` are sufficient
**Storage**: N/A — bookmark persistence already implemented via DataStore; no schema changes
**Testing**: No unit tests required — changes are in `composeApp/` UI/ViewModel layer (exempt per Constitution Principle II); manual acceptance via `quickstart.md`
**Target Platform**: Android (minSdk 24+), iOS 16+ (via Compose Multiplatform)
**Project Type**: mobile-app (Kotlin Multiplatform)
**Performance Goals**: MAP MYLIST pins update within 1 second of tab switch (SC-001); filter chip list update within 300ms (SC-002)
**Constraints**: No new KMP dependencies; `shared/` module receives only the enum rename (no business logic in UI layer per Principle VI)
**Scale/Scope**: 3 files modified; 1 enum value renamed; 1 StateFlow rewired; 2 UI labels updated

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Spec-First | ✅ PASS | `spec.md` written and validated (all checklist items ✅) before any code |
| II. Test-First | ✅ PASS | Only change in `shared/` is enum rename — no new logic to test. ViewModel + UI changes are in `composeApp/` (platform UI layer, exempt). Manual acceptance via `quickstart.md`. |
| III. Simplicity & YAGNI | ✅ PASS | Zero new abstractions. Zero new dependencies. The ViewModel already has `bookmarkedIds`; only the `combine(...)` data source changes. |
| IV. Incremental Delivery | ✅ PASS | US1 (MAP label + rewire) is independently deployable and testable. US2 (confirm filter chips) is a verification step. US3 (bookmark ↔ map connection) flows from US1 completion. |
| V. Observability | ✅ PASS | No new async operations. Existing `println` error logging in ViewModel preserved. |
| VI. Shared-First | ✅ PASS | Enum rename stays in `shared/commonMain`. StateFlow rewiring is in `composeApp/commonMain/viewmodel` (application layer, not `shared/`). UI label change is in `composeApp/commonMain/ui` — correct placement. No business logic in platform modules. |

**Post-design re-check**: No new abstractions introduced. Constitution Check still passes.

---

## Project Structure

### Documentation (this feature)

```text
specs/010-mylist-filter-map/
├── plan.md              # This file
├── research.md          # Phase 0 output ✅
├── spec.md              # Feature specification ✅
├── quickstart.md        # Phase 1 output — acceptance test guide
├── checklists/
│   └── requirements.md  # Spec quality validation ✅
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (files changed by this feature)

```text
shared/src/commonMain/kotlin/com/gallr/shared/data/model/
└── MapDisplayMode.kt       ← rename FILTERED → MY_LIST, update kdoc

composeApp/src/commonMain/kotlin/com/gallr/app/
├── viewmodel/
│   └── TabsViewModel.kt    ← rewire filteredMapPins → myListMapPins (bookmarkedIds source)
│                             update _mapDisplayMode initial value → MY_LIST
└── ui/tabs/map/
    └── MapScreen.kt        ← label "FILTERED" → "MYLIST"
                              MapDisplayMode.FILTERED → MapDisplayMode.MY_LIST
                              filteredPins ref → myListPins
                              empty state message update
```

**Structure Decision**: Single-project KMP app. All logic changes in `shared/` (enum); all application-layer changes in `composeApp/commonMain`. No platform-specific (Android/iOS) files touched.

---

## Phase 0: Research Summary

See [research.md](research.md) for full details. Key findings:

- `filteredMapPins` uses `_filterState` as source → must be rewired to `bookmarkedIds`
- `filteredExhibitions` (LIST tab) already works correctly → no changes
- `MapDisplayMode.FILTERED` → `MY_LIST` (rename only, no semantic model changes)
- Rename `filteredMapPins` → `myListMapPins` to avoid misleading name post-rewire
- 3 files total; 0 new dependencies; 0 new abstractions

---

## Phase 1: Implementation Design

### Data Model

No entity changes. `MapDisplayMode` enum rename only:

```kotlin
// Before
enum class MapDisplayMode {
    FILTERED,  // Shows exhibitions matching FilterState
    ALL,
}

// After
enum class MapDisplayMode {
    MY_LIST,   // Shows only exhibitions the user has bookmarked
    ALL,
}
```

### ViewModel Change

In `TabsViewModel.kt`:

1. Rename `filteredMapPins` → `myListMapPins`, rewire to `combine(_allExhibitions, bookmarkedIds)`:

```kotlin
val myListMapPins: StateFlow<List<ExhibitionMapPin>> =
    combine(_allExhibitions, bookmarkedIds) { state, bookmarked ->
        (state as? ExhibitionListState.Success)
            ?.exhibitions
            ?.filter { it.id in bookmarked }
            ?.mapNotNull { it.toMapPin() }
            ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
```

2. Change initial `_mapDisplayMode` value:
```kotlin
private val _mapDisplayMode = MutableStateFlow(MapDisplayMode.MY_LIST)
```

### MapScreen.kt Changes

1. `val myListPins by viewModel.myListMapPins.collectAsState()` (rename from `filteredPins`)
2. `val activePins = if (mapMode == MapDisplayMode.MY_LIST) myListPins else allPins`
3. `MapModeButton(label = "MYLIST", selected = mapMode == MapDisplayMode.MY_LIST, onClick = { viewModel.setMapDisplayMode(MapDisplayMode.MY_LIST) }, ...)`
4. Empty state condition: `if (mapMode == MapDisplayMode.MY_LIST && myListPins.isEmpty())` with message `"Add exhibitions to your list to see them here"`

### Contracts

No external API contracts — this is a pure UI/ViewModel wiring change.

---

## Complexity Tracking

> No Constitution Check violations. Table left blank.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |
