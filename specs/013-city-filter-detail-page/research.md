# Research: City Filter & Exhibition Detail Page (013)

**Feature Branch**: `013-city-filter-detail-page`
**Generated**: 2026-03-23

---

## R1: Navigation Pattern for Detail Screen

**Decision**: Use composable-level state (`selectedExhibition: Exhibition?`) in App.kt. When non-null, show `ExhibitionDetailScreen`; when null, show the tabbed layout.

**Rationale**: The app has a flat tab structure with no existing navigation framework. Adding a nav library (e.g., voyager, decompose) for a single detail screen violates Principle III (Simplicity). A nullable state is the minimum viable approach — setting it to an Exhibition shows the detail page, clearing it returns to tabs.

**Alternatives considered**:
- **Jetpack Navigation / Voyager**: Full navigation libraries. Rejected — over-engineering for one additional screen.
- **Bottom sheet / modal**: Rejected per spec clarification — detail page is a full-screen page.

---

## R2: Async Image Loading for Cover Images

**Decision**: Use Coil 3 for Compose Multiplatform (`coil3-compose`) to load cover images from URLs.

**Rationale**: Coil 3 has first-class KMP support (coil3-compose works on both Android and iOS via Compose Multiplatform). It handles async loading, caching, error states, and placeholder display. This is the standard approach for image loading in Compose apps.

**Alternatives considered**:
- **Kamel**: Another KMP image loader. Less mature than Coil 3, smaller community.
- **Manual Ktor download + bitmap conversion**: Too much boilerplate. Violates Principle III.
- **No image loading (skip cover images)**: Rejected — cover images are a key spec requirement.

---

## R3: City Extraction Strategy

**Decision**: Extract distinct cities from the already-loaded exhibition list in the ViewModel. No separate API call or data source needed.

**Rationale**: The exhibitions are already fetched on app launch (`loadAllExhibitions()`). Extracting distinct `cityKo`/`cityEn` pairs is a simple `.map { }.distinct()` operation. This avoids additional network calls and keeps the architecture simple.

**Implementation approach**:
1. Add `selectedCity: StateFlow<String?>` to TabsViewModel (null = all cities)
2. Derive `distinctCities: StateFlow<List<Pair<String, String>>>` from `_allExhibitions` (pairs of cityKo/cityEn)
3. Update `filteredExhibitions` to also filter by `selectedCity`
4. City matching uses `cityKo` as the canonical key (consistent with regionKo for filter matching)

---

## R4: Detail Screen Layout

**Decision**: Scrollable column layout with: cover image (full-width, if available) → name → venue → city/region → address → date range → description → bookmark button.

**Rationale**: Standard exhibition/event detail pattern. Cover image at the top creates visual impact. Information flows from identification (name/venue) to location (city/region/address) to timing (dates) to context (description). Bookmark button at the bottom lets users decide after reading.

**Back navigation**: A back arrow icon button in the top-left, or the system back gesture on both platforms. On iOS, the swipe-back gesture works automatically with composable state navigation.
