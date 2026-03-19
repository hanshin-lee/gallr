# Data Model: Interactive Map with Exhibition Pins

**Feature**: `004-naver-map-api` | **Date**: 2026-03-19

---

## Existing Entities (no changes required)

### ExhibitionMapPin

Defined in `shared/src/commonMain/kotlin/com/gallr/shared/data/model/ExhibitionMapPin.kt`.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Exhibition identifier — used to match tap events to the correct pin |
| `name` | `String` | Exhibition title — shown in the pin dialog |
| `venueName` | `String` | Gallery/venue name — shown in the pin dialog |
| `latitude` | `Double` | WGS-84 latitude — passed directly to the map SDK marker |
| `longitude` | `Double` | WGS-84 longitude — passed directly to the map SDK marker |
| `openingDate` | `LocalDate` | Opening date — shown in the pin dialog |
| `closingDate` | `LocalDate` | Closing date — shown in the pin dialog |

Only exhibitions where both `latitude` and `longitude` are non-null are projected into `ExhibitionMapPin` (enforced by `Exhibition.toMapPin()`). No changes to this model.

### MapDisplayMode

Defined in `shared/src/commonMain/kotlin/com/gallr/shared/data/model/MapDisplayMode.kt`.

| Value | Meaning |
|-------|---------|
| `FILTERED` | Show only pins matching active filters (`filteredMapPins` from `TabsViewModel`) |
| `ALL` | Show all geolocated exhibition pins (`allMapPins` from `TabsViewModel`) |

No changes to this model.

---

## New Configuration (platform-local, not shared model)

### MapCameraPosition (platform-local constant)

Not a shared data class — defined as a local constant in each platform's `MapView` implementation.

| Parameter | Value | Description |
|-----------|-------|-------------|
| `latitude` | `37.5665` | Seoul city centre latitude |
| `longitude` | `126.9780` | Seoul city centre longitude |
| `zoom` | `10.0` | Metro-area zoom level |

This is not persisted or shared across features. If camera state persistence is needed in a future feature, it should be promoted to a shared model at that time.

---

## Data Flow (unchanged)

```
Exhibition (shared)
  └─► Exhibition.toMapPin()
        └─► ExhibitionMapPin (shared)
              └─► TabsViewModel.filteredMapPins / allMapPins (shared)
                    └─► MapScreen (composeApp commonMain) — passes list to MapView
                          └─► MapView actual (androidMain / iosMain) — renders pins on native map
```

No new shared data entities are introduced by this feature. The entire change is confined to the two platform `actual` implementations of `MapView`.
