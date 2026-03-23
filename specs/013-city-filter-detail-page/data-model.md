# Data Model: City Filter & Exhibition Detail Page (013)

**Feature Branch**: `013-city-filter-detail-page`
**Generated**: 2026-03-23

---

## No Schema Changes Required

This feature uses existing data only. No Supabase migration needed.

### Exhibition (existing — no changes)

All fields already exist from feature 012 (bilingual data pipeline):
- `name_ko`, `name_en`, `venue_name_ko`, `venue_name_en`
- `city_ko`, `city_en`, `region_ko`, `region_en`
- `address_ko`, `address_en`, `description_ko`, `description_en`
- `opening_date`, `closing_date`, `is_featured`, `is_editors_pick`
- `latitude`, `longitude`, `cover_image_url`

### Derived Data: Distinct Cities

Cities are extracted client-side from the loaded exhibition list:

```text
Input:  List<Exhibition> (all loaded exhibitions)
Output: List<CityOption> where CityOption = (cityKo: String, cityEn: String)
Logic:  exhibitions.map { (it.cityKo, it.cityEn) }.distinct().sortedBy { it.cityKo }
```

No persistence needed — derived on each data load.

---

## ViewModel State Changes

### TabsViewModel (updated)

| New State | Type | Default | Purpose |
|-----------|------|---------|---------|
| `selectedCity` | `StateFlow<String?>` | `null` | Currently selected city filter (null = all cities). Uses `cityKo` as key. |
| `distinctCities` | `StateFlow<List<Pair<String, String>>>` | `emptyList()` | Distinct (cityKo, cityEn) pairs from loaded exhibitions |
| `selectedExhibition` | — | — | Managed in App.kt, not ViewModel. Exhibition passed to detail screen. |

### FilterState (updated)

No change to FilterState needed — city filtering is applied as an additional predicate in `filteredExhibitions` alongside the existing FilterState logic, rather than embedded inside FilterState.

---

## Navigation State

Managed in `App.kt` via composable state:

```text
selectedExhibition: Exhibition? = null

When null:  Show tabbed layout (Featured / List / Map)
When set:   Show ExhibitionDetailScreen(exhibition, onBack = { clear it })
```

No navigation library. No deep linking. Simple state toggle.
