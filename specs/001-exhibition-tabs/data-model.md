# Data Model: Three-Tab Exhibition Discovery Navigation

**Branch**: `001-exhibition-tabs` | **Date**: 2026-03-18
**Module**: `shared/src/commonMain/kotlin/com/gallr/shared/data/model/`

---

## Entities

### Exhibition

The core domain object. Fetched from the remote API and displayed across all three tabs.

| Field           | Type            | Nullable | Constraints                              |
|-----------------|-----------------|----------|------------------------------------------|
| `id`            | String          | No       | Unique; stable across fetches            |
| `name`          | String          | No       | Non-empty                                |
| `venueName`     | String          | No       | Non-empty                                |
| `city`          | String          | No       | Non-empty                                |
| `region`        | String          | No       | Used for region filter matching          |
| `openingDate`   | LocalDate       | No       | ISO 8601; must be ≤ `closingDate`        |
| `closingDate`   | LocalDate       | No       | ISO 8601; must be ≥ `openingDate`        |
| `isFeatured`    | Boolean         | No       | Set by editorial backend flag            |
| `isEditorsPick` | Boolean         | No       | Distinct from `isFeatured`               |
| `latitude`      | Double          | Yes      | WGS-84; null = excluded from map         |
| `longitude`     | Double          | Yes      | WGS-84; null = excluded from map         |
| `description`   | String          | No       | May be empty string                      |
| `coverImageUrl` | String          | Yes      | Absolute URL; null = no image            |

**Notes**:
- `latitude`/`longitude` are both null or both non-null (never partial).
- Exhibitions with null coordinates are visible in Featured and List tabs but excluded
  from the Map tab's marker set.

---

### FilterState

Holds the user's active filter selections. Lives in `TabsViewModel`; session-scoped.

| Field              | Type          | Default | Semantics                                |
|--------------------|---------------|---------|------------------------------------------|
| `regions`          | List\<String> | empty   | OR within list; empty = no region filter |
| `showFeatured`     | Boolean       | false   | If true, show only `isFeatured = true`   |
| `showEditorsPick`  | Boolean       | false   | If true, show only `isEditorsPick = true`|
| `openingThisWeek`  | Boolean       | false   | Opening date within next 7 calendar days |
| `closingThisWeek`  | Boolean       | false   | Closing date within next 7 calendar days |

**Filter application logic** (applied by `FilterState.matches(exhibition)`):

```
regions match   = regions.isEmpty() OR exhibition.region IN regions
featured match  = !showFeatured OR exhibition.isFeatured
picks match     = !showEditorsPick OR exhibition.isEditorsPick
week match      = (!openingThisWeek AND !closingThisWeek)
                  OR (openingThisWeek AND exhibition.openingDate IN [today, today+6])
                  OR (closingThisWeek AND exhibition.closingDate IN [today, today+6])

exhibition matches filter = regions match AND featured match AND picks match AND week match
```

**Notes**:
- `openingThisWeek` and `closingThisWeek` are OR'd with each other (see spec edge case).
- All other active filters are AND'd.
- When `FilterState` is default (all fields at default), every exhibition matches.

---

### MapDisplayMode

Enum controlling the Map tab's display.

| Value      | Behaviour                                          |
|------------|----------------------------------------------------|
| `FILTERED` | Map shows only exhibitions matching current `FilterState` |
| `ALL`      | Map shows all exhibitions with coordinates, ignoring `FilterState` |

---

### Bookmark

Persisted to device-local DataStore Preferences across app restarts.

| Field          | Type    | Constraints                              |
|----------------|---------|------------------------------------------|
| `exhibitionId` | String  | Foreign key to `Exhibition.id`           |
| `savedAt`      | Instant | UTC timestamp of when bookmark was added |

**Storage representation**: `Set<String>` of exhibition IDs in DataStore Preferences
(the `savedAt` timestamp is stored in a companion preferences key `bookmark_ts_{id}`
if timestamp retrieval is needed; otherwise just the ID set suffices for MVP).

**Notes**:
- Bookmark state must be consistent wherever the same exhibition appears (FR-013).
- The `BookmarkRepository` exposes a `Flow<Set<String>>` that both the Featured and List
  screens observe to keep their UI in sync.

---

### ExhibitionMapPin

A lightweight projection of `Exhibition` for use by the `MapView` composable. Carries
only the data needed to render and identify a map marker.

| Field          | Type   | Source                    |
|----------------|--------|---------------------------|
| `id`           | String | `Exhibition.id`           |
| `name`         | String | `Exhibition.name`         |
| `venueName`    | String | `Exhibition.venueName`    |
| `latitude`     | Double | `Exhibition.latitude!!`   |
| `longitude`    | Double | `Exhibition.longitude!!`  |
| `openingDate`  | LocalDate | `Exhibition.openingDate` |
| `closingDate`  | LocalDate | `Exhibition.closingDate` |

**Notes**:
- Only constructed from exhibitions where `latitude != null && longitude != null`.
- Passed to `MapView` composable; the platform implementation renders the markers.

---

## Repository Interfaces

Located in `shared/src/commonMain/kotlin/com/gallr/shared/repository/`.

### ExhibitionRepository

```
interface ExhibitionRepository {
    suspend fun getFeaturedExhibitions(): Result<List<Exhibition>>
    suspend fun getExhibitions(filter: FilterState): Result<List<Exhibition>>
}
```

**Notes**:
- Both methods return `Result<T>` to propagate network errors to the ViewModel without
  throwing.
- Filtering may be applied server-side (via query params) or client-side depending on the
  backend implementation. The interface is agnostic.
- Caching strategy is out of scope for this feature.

### BookmarkRepository

```
interface BookmarkRepository {
    fun observeBookmarkedIds(): Flow<Set<String>>
    suspend fun addBookmark(exhibitionId: String)
    suspend fun removeBookmark(exhibitionId: String)
    suspend fun isBookmarked(exhibitionId: String): Boolean
}
```

**Notes**:
- `observeBookmarkedIds()` is a hot flow from DataStore; collects in ViewModel.
- `addBookmark`/`removeBookmark` are `suspend` for DataStore write safety.
- `isBookmarked` is a convenience for one-shot checks (used in UI for initial render).

---

## Network DTOs

Located in `shared/src/commonMain/kotlin/com/gallr/shared/data/network/dto/`.
DTOs are separate from domain models to decouple API shape from app domain.

### ExhibitionDto

```
@Serializable
data class ExhibitionDto(
    val id: String,
    val name: String,
    val venueName: String,
    val city: String,
    val region: String,
    val openingDate: String,   // ISO 8601: "YYYY-MM-DD"
    val closingDate: String,   // ISO 8601: "YYYY-MM-DD"
    val isFeatured: Boolean,
    val isEditorsPick: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val description: String,
    val coverImageUrl: String?
)
```

**Mapping**: `ExhibitionDto.toDomain()` converts to `Exhibition` (parse ISO date strings
to `LocalDate` using `kotlinx-datetime`).

---

## State Transitions

### Bookmark Toggle

```
Unbookmarked → [tap bookmark icon] → Bookmarked
Bookmarked   → [tap bookmark icon] → Unbookmarked
```

BookmarkRepository write is fire-and-forget from the UI perspective; the `Flow<Set<String>>`
from `observeBookmarkedIds()` drives re-render automatically.

### FilterState Update

```
Any FilterState → [toggle any filter] → New FilterState (immutable replace)
```

`TabsViewModel` exposes `fun updateFilter(update: FilterState.() -> FilterState)`.
Every update triggers recomputation of the filtered exhibition list via `combine()`
on the exhibitions flow and filter state flow.

### MapDisplayMode Toggle

```
FILTERED → [tap "All" button] → ALL
ALL      → [tap "Filtered" button] → FILTERED
```

Stored in `TabsViewModel` as `MutableStateFlow<MapDisplayMode>`.
