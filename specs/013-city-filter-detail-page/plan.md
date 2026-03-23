# Implementation Plan: City Filter & Exhibition Detail Page

**Branch**: `013-city-filter-detail-page` | **Date**: 2026-03-23 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/013-city-filter-detail-page/spec.md`

## Summary

Add city filtering to the List tab via a horizontally scrollable chip row (with "All Cities" default), and add a full-screen exhibition detail page accessible by tapping any exhibition card. The detail page displays cover image, all bilingual fields, dates, address, and a bookmark button. A country selector ("South Korea" only) is placed above the city chips as a future expansion placeholder. Navigation uses simple composable-level state — no navigation library needed.

## Technical Context

**Language/Version**: Kotlin 2.1.20 (KMP)
**Primary Dependencies**: Compose Multiplatform 1.8.0, Ktor 2.9+ (for image loading via URL), coil3 or similar (async image loading)
**Storage**: Supabase Postgres (existing), DataStore Preferences (existing)
**Testing**: Kotlin unit tests (shared module)
**Target Platform**: Android, iOS 15+
**Project Type**: Mobile app (KMP cross-platform)
**Performance Goals**: City filter updates < 1 second, detail page navigation < 1 second
**Constraints**: No new API calls for city data (derived client-side from loaded exhibitions)
**Scale/Scope**: ~72 exhibitions, ~10 distinct cities, 1 new screen (detail page)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Spec-First Development | PASS | Spec written and clarified before planning |
| II. Test-First | PASS | City extraction logic in shared module will have unit tests |
| III. Simplicity & YAGNI | PASS | City list derived from data (no separate city table); navigation via composable state (no nav library) |
| IV. Incremental Delivery | PASS | City filter and detail page are independently deliverable |
| V. Observability | PASS | Navigation and filter changes logged via ViewModel |
| VI. Shared-First Architecture | PASS | City extraction logic and Exhibition model in shared/; only UI composables (detail screen, city chips) in composeApp/ |

No violations. Gate passes.

## Project Structure

### Documentation (this feature)

```text
specs/013-city-filter-detail-page/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
composeApp/src/commonMain/kotlin/com/gallr/app/
├── App.kt                                    # Updated: navigation state (list vs detail)
├── ui/
│   ├── components/
│   │   └── ExhibitionCard.kt                 # Updated: onTap callback for detail navigation
│   ├── tabs/
│   │   ├── list/
│   │   │   └── ListScreen.kt                 # Updated: country selector + city chip row above filters
│   │   ├── featured/
│   │   │   └── FeaturedScreen.kt             # Updated: onTap for detail navigation
│   │   └── map/
│   │       └── MapScreen.kt                  # Updated: dialog "View Details" for detail navigation
│   └── detail/
│       └── ExhibitionDetailScreen.kt         # NEW: full-screen detail page
└── viewmodel/
    └── TabsViewModel.kt                      # Updated: selectedCity state, distinct cities, selectedExhibition

shared/src/commonMain/kotlin/com/gallr/shared/data/model/
└── Exhibition.kt                             # No change (localized methods already exist)
```

**Structure Decision**: Follows existing KMP architecture. New detail screen in `composeApp/ui/detail/`. City extraction logic stays in ViewModel (simple enough — no repository needed). Per Principle III, navigation is managed via a nullable `selectedExhibition` state in App.kt — no navigation library.

## Complexity Tracking

No violations to justify.
