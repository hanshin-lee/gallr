# Implementation Plan: Bilingual Data Pipeline

**Branch**: `012-bilingual-data-pipeline` | **Date**: 2026-03-23 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/012-bilingual-data-pipeline/spec.md`

## Summary

Add bilingual (Korean/English) support across the entire gallr app: data pipeline (Google Sheets → Supabase → App), UI labels, and an in-app language toggle. Refactor the sync script from position-based to header-driven column mapping so new spreadsheet columns require zero script changes. Migrate the Supabase schema to use `_ko`/`_en` suffix pairs for all text fields. Add a KO/EN toggle button in the top app bar that persists via DataStore.

## Technical Context

**Language/Version**: Kotlin 2.1.20 (KMP), Google Apps Script V8, SQL (Supabase Postgres)
**Primary Dependencies**: Compose Multiplatform 1.8.0, Ktor 2.9+, DataStore Preferences 1.1+, kotlinx.serialization 1.7+, compose-resources (CMP string resources)
**Storage**: Supabase Postgres (exhibitions table), DataStore Preferences (language preference + bookmarks)
**Testing**: Kotlin unit tests (shared module), manual testing (UI)
**Target Platform**: Android (minSdk per project config), iOS 15+
**Project Type**: Mobile app (KMP cross-platform)
**Performance Goals**: Language toggle switches content instantly (<100ms perceived)
**Constraints**: Backward-compatible deserialization (unknown fields ignored), bookmark IDs must remain stable across migration
**Scale/Scope**: ~50 exhibitions, 3 screens, ~30 UI strings to localize

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Spec-First Development | PASS | Spec written and clarified before planning |
| II. Test-First | PASS | Shared module logic (LanguageRepository, updated ExhibitionDto) will have unit tests written before implementation |
| III. Simplicity & YAGNI | PASS | No new abstractions — reuses existing DataStore/Repository pattern; compose-resources for strings is the built-in CMP solution |
| IV. Incremental Delivery | PASS | 5 user stories independently deliverable (sync pipeline → schema → bilingual display → language toggle → forward compatibility) |
| V. Observability | PASS | Sync script already logs structured JSON; language preference changes logged in ViewModel |
| VI. Shared-First Architecture | PASS | LanguageRepository, Exhibition model, ExhibitionDto all in `shared/` module; only UI composables in `composeApp/` |

No violations. Gate passes.

## Project Structure

### Documentation (this feature)

```text
specs/012-bilingual-data-pipeline/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
shared/src/commonMain/kotlin/com/gallr/shared/
├── data/
│   ├── model/
│   │   └── Exhibition.kt              # Updated: bilingual fields (nameKo, nameEn, etc.)
│   └── network/
│       ├── ExhibitionApiClient.kt     # Updated: deserialize bilingual columns
│       └── dto/
│           └── ExhibitionDto.kt       # Updated: _ko/_en fields, ignoreUnknownKeys
├── repository/
│   ├── BookmarkRepository.kt         # Existing (no change)
│   ├── BookmarkRepositoryImpl.kt     # Existing (no change)
│   ├── LanguageRepository.kt         # NEW: interface for language preference
│   └── LanguageRepositoryImpl.kt     # NEW: DataStore-backed implementation

shared/src/commonTest/kotlin/com/gallr/shared/
└── data/network/dto/
    └── ExhibitionDtoTest.kt           # Updated: bilingual deserialization tests

composeApp/src/commonMain/
├── composeResources/
│   ├── values/strings.xml             # NEW: default strings (English)
│   └── values-ko/strings.xml          # NEW: Korean strings
├── kotlin/com/gallr/app/
│   ├── App.kt                         # Updated: language toggle in top bar, pass language to screens
│   ├── ui/
│   │   ├── components/
│   │   │   ├── ExhibitionCard.kt      # Updated: display localized fields
│   │   │   └── GallrNavigationBar.kt  # Updated: localized tab labels
│   │   └── tabs/
│   │       ├── featured/FeaturedScreen.kt  # Updated: localized strings
│   │       ├── list/ListScreen.kt          # Updated: localized filter chips, strings
│   │       └── map/MapScreen.kt            # Updated: localized toggle labels, strings
│   └── viewmodel/
│       └── TabsViewModel.kt           # Updated: language StateFlow, setLanguage()

gas/
└── SyncExhibitions.gs                  # Updated: header-driven mapping
```

**Structure Decision**: Follows existing KMP architecture. All business logic (LanguageRepository, updated Exhibition model) in `shared/` module per Principle VI. UI changes in `composeApp/`. Sync script changes in `gas/`. No new modules or projects needed.

## Complexity Tracking

No violations to justify.
