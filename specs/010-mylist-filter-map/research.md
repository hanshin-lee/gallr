# Research: My List and List Filtering

**Branch**: `010-mylist-filter-map` | **Date**: 2026-03-20
**Phase**: Phase 0 — resolve unknowns before Phase 1 design

---

## Decision 1 — How `filteredMapPins` currently derives data

**Decision**: `filteredMapPins` in `TabsViewModel` uses `combine(_allExhibitions, _filterState)` — it applies filter chips (FEATURED, EDITOR'S PICK, etc.) to produce the MAP's FILTERED-mode pins. This is the wrong data source for "My List".

**Rationale**: The spec requires MAP MYLIST = bookmarks only, not filter-chip matches. The ViewModel already has `bookmarkedIds: StateFlow<Set<String>>` from `bookmarkRepository.observeBookmarkedIds()`. Rewiring `filteredMapPins` to `combine(_allExhibitions, bookmarkedIds)` is a one-line data source swap with no new dependencies.

**Alternatives considered**: Keeping `filteredMapPins` filter-chip-based and adding a separate `myListMapPins` flow. Rejected — the old `filteredMapPins` would become dead code; cleaner to rename and rewire in place.

---

## Decision 2 — Enum rename: `MapDisplayMode.FILTERED` → `MapDisplayMode.MY_LIST`

**Decision**: Rename the `FILTERED` value to `MY_LIST` in `shared/data/model/MapDisplayMode.kt`. Update the kdoc comment to reflect the new semantics (bookmarks, not filter chips).

**Rationale**: The enum lives in `shared/` which is correct per Principle VI. The rename is a breaking change to an internal model — the only callers are `TabsViewModel` and `MapScreen`, both in `composeApp/`. No external API contract is broken. The name `MY_LIST` matches Kotlin SCREAMING_SNAKE_CASE convention and maps directly to the UI label "MYLIST".

**Alternatives considered**: Adding a new `MY_LIST` value and deprecating `FILTERED`. Rejected — YAGNI. There is no backwards-compatibility requirement for an internal enum. Dead enum values add confusion.

---

## Decision 3 — `filteredExhibitions` on LIST tab: already correct

**Decision**: No changes required to `filteredExhibitions` or `FilterState`. The LIST tab already uses `combine(_allExhibitions, _filterState)` to produce the filtered list view, which is exactly what US2 requires.

**Rationale**: Reading `ListScreen.kt` confirmed `GallrFilterChip` composables already call `viewModel.updateFilter(...)` and `viewModel.filteredExhibitions` drives the list. US2 is "confirm working" — no code change.

**Alternatives considered**: N/A — already correct.

---

## Decision 4 — ViewModel property rename: `filteredMapPins` → `myListMapPins`

**Decision**: Rename the public `filteredMapPins` property to `myListMapPins` in `TabsViewModel`.

**Rationale**: After rewiring to `bookmarkedIds`, the name `filteredMapPins` would be semantically misleading (it no longer filters by `FilterState`). Renaming removes ambiguity. The only consumer is `MapScreen.kt` — one reference to update.

**Alternatives considered**: Keeping `filteredMapPins` as the name. Rejected — misleading names create future maintenance bugs, and this is a safe refactor.

---

## Decision 5 — Empty state messages

**Decision**:
- MAP MYLIST empty state (no bookmarks): `"Add exhibitions to your list to see them here"`
- LIST filter chips empty state (no matches): `"No exhibitions match the current filters."` — already exists in `MapScreen.kt` but is scoped to FILTERED mode; will move to ListScreen (see plan)

**Rationale**: The spec (FR-008, acceptance scenario 4) specifies the MYLIST empty state message. The filter chips empty state for LIST is defined in US2 scenario 4.

---

## Affected Files (full scope)

| File | Change | Why |
|------|--------|-----|
| `shared/src/commonMain/kotlin/com/gallr/shared/data/model/MapDisplayMode.kt` | Rename `FILTERED` → `MY_LIST`, update kdoc | Enum lives in `shared/` per Principle VI |
| `composeApp/src/commonMain/kotlin/com/gallr/app/viewmodel/TabsViewModel.kt` | Rewire `filteredMapPins` → `myListMapPins` using `bookmarkedIds`; update `_mapDisplayMode` initial value | ViewModel is application layer, single consumer of shared model |
| `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapScreen.kt` | Label `"FILTERED"` → `"MYLIST"`; `MapDisplayMode.FILTERED` → `MapDisplayMode.MY_LIST`; `filteredPins` reference → `myListPins`; empty state message update | UI label change only; all consumer references updated |

No new files. No new dependencies. No database changes.
