# Quickstart: City Filter & Exhibition Detail Page (013)

**Feature Branch**: `013-city-filter-detail-page`
**Generated**: 2026-03-23

---

## Prerequisites

- Feature 012 (bilingual data pipeline) must be merged — this feature depends on bilingual Exhibition fields
- Coil 3 dependency added for async image loading

## Implementation Order

### Step 1: Add Coil 3 Dependency

Add `coil3-compose` to the KMP dependencies for async image loading on the detail screen.

### Step 2: Add City Filter State to ViewModel

- Add `selectedCity: StateFlow<String?>` (null = all cities)
- Derive `distinctCities` from loaded exhibitions
- Update `filteredExhibitions` to include city filter predicate
- Add `setCity(cityKo: String?)` function

### Step 3: Update List Screen with City Chips

- Add country label ("South Korea" / "대한민국") above the city chips
- Add horizontally scrollable chip row with "All Cities" + distinct city names
- Wire chip selection to `viewModel.setCity()`
- City names display in current language

### Step 4: Create Exhibition Detail Screen

- New `ExhibitionDetailScreen` composable in `composeApp/ui/detail/`
- Layout: cover image → name → venue → city/region → address → dates → description → bookmark
- Handle missing cover image (hide section)
- Handle empty description (hide section)
- Back button in top bar

### Step 5: Add Navigation in App.kt

- Add `selectedExhibition: Exhibition?` state
- When non-null, show `ExhibitionDetailScreen` instead of tab layout
- Pass `onBack = { selectedExhibition = null }` to detail screen

### Step 6: Wire Exhibition Card Taps

- Add `onTap: () -> Unit` parameter to `ExhibitionCard`
- Wire taps in FeaturedScreen, ListScreen, and MapScreen dialog
- Each tap sets `selectedExhibition`

## Verification

1. Open List tab — verify country label and city chips appear above filter chips
2. Tap a city chip — verify list filters to that city only
3. Tap "All Cities" — verify all exhibitions return
4. Activate both a city chip and a filter chip — verify AND logic
5. Tap an exhibition card — verify detail screen opens with all fields
6. Verify cover image loads (for exhibitions that have one)
7. Verify exhibitions without cover images show no broken placeholder
8. Tap back — verify return to previous tab with state preserved
9. Switch KO/EN on detail screen — verify content updates
10. Tap exhibition from Featured tab — verify same detail screen opens
