# Implementation Plan: Three-Tab Exhibition Discovery Navigation

**Branch**: `001-exhibition-tabs` | **Date**: 2026-03-18 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-exhibition-tabs/spec.md`

## Summary

Build the core gallr app shell: three bottom-navigation tabs (Featured, List, Map) with
exhibition discovery, real-time filter state shared across tabs, map display with
Filtered/All modes, and device-local bookmark persistence. All business logic lives in
the `shared/` KMP module; all UI lives in `composeApp/` per Principle VI.

## Technical Context

**Language/Version**: Kotlin 2.0+ (2.3.0 recommended), Compose Multiplatform 1.8.0+
**Primary Dependencies**: Ktor 2.9+ (networking), DataStore Preferences 1.1+ (bookmarks), AndroidX ViewModel 2.8.0+, kotlinx.serialization 1.7+, kotlinx-datetime
**Storage**: DataStore Preferences (device-local bookmark store); remote REST API (exhibitions data)
**Testing**: kotlin.test + JUnit4 — shared module unit tests for FilterState logic and BookmarkRepository operations
**Target Platform**: Android API 26+ (Android 8.0), iOS 14.0+
**Project Type**: KMP mobile app (Android + iOS, single codebase — Compose Multiplatform)
**Performance Goals**: Featured tab content visible ≤3s on standard mobile connection (SC-001); map mode switch ≤2s (SC-004)
**Constraints**: All dependencies must be KMP-compatible; map provider is pluggable via expect/actual composable (FR-017); no GPS/geolocation required
**Scale/Scope**: 3 tabs, 4 user stories, MVP — no auth, no pagination, no exhibition detail screen

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Phase 0

| Principle | Gate | Status | Notes |
|-----------|------|--------|-------|
| I. Spec-First | spec.md complete before this plan | ✅ PASS | spec.md approved 2026-03-18 |
| II. Test-First | Unit tests planned for shared module logic | ✅ PASS | FilterState.matches() and BookmarkRepository in scope |
| III. Simplicity | DataStore over SQLDelight; single ViewModel; no extra nav library | ✅ PASS | No premature abstraction |
| IV. Incremental Delivery | 4 user stories independently testable | ✅ PASS | US1→US2→US3→US4 each has independent test in spec |
| V. Observability | Ktor Logging plugin in commonMain; crash reporter deferred to prod | ✅ PASS | Logging in scope; crash reporter out of scope per spec |
| VI. Shared-First | All models, repos, networking confirmed in shared/; all UI in composeApp/ | ✅ PASS | See Project Structure below |

**Gate: ALL PASS — Phase 0 research authorised.**

### Post-Phase 1

| Principle | Status | Notes |
|-----------|--------|-------|
| VI. Shared-First | ✅ PASS | Exhibition/FilterState/Bookmark/MapDisplayMode/ExhibitionMapPin in shared/commonMain. ExhibitionRepository and BookmarkRepository interfaces + implementations in shared/commonMain. Map rendering is expect/actual composable in composeApp only — zero business logic in platform modules. |

**Gate: ALL PASS — implementation authorised.**

## Project Structure

### Documentation (this feature)

```text
specs/001-exhibition-tabs/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── api-exhibitions.md   # Phase 1 output
└── checklists/
    └── requirements.md
```

### Source Code (repository root)

```text
gallr/
├── shared/                                           # KMP business logic module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/com/gallr/shared/
│       │   ├── data/
│       │   │   ├── model/
│       │   │   │   ├── Exhibition.kt
│       │   │   │   ├── FilterState.kt
│       │   │   │   ├── MapDisplayMode.kt
│       │   │   │   ├── Bookmark.kt
│       │   │   │   └── ExhibitionMapPin.kt
│       │   │   └── network/
│       │   │       ├── ExhibitionApiClient.kt
│       │   │       └── dto/ExhibitionDto.kt
│       │   ├── repository/
│       │   │   ├── ExhibitionRepository.kt          # interface
│       │   │   ├── BookmarkRepository.kt             # interface
│       │   │   ├── ExhibitionRepositoryImpl.kt       # Ktor impl
│       │   │   └── BookmarkRepositoryImpl.kt         # DataStore impl
│       │   └── platform/
│       │       └── DataStorePath.kt                  # expect fun createDataStore()
│       ├── androidMain/kotlin/com/gallr/shared/
│       │   └── platform/DataStorePath.android.kt     # actual
│       └── iosMain/kotlin/com/gallr/shared/
│           └── platform/DataStorePath.ios.kt         # actual
│
├── composeApp/                                       # Compose Multiplatform UI module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/com/gallr/app/
│       │   ├── App.kt                               # Root composable + BottomNavigation
│       │   ├── ui/
│       │   │   ├── tabs/
│       │   │   │   ├── featured/FeaturedScreen.kt
│       │   │   │   ├── list/ListScreen.kt
│       │   │   │   └── map/
│       │   │   │       ├── MapScreen.kt
│       │   │   │       └── MapView.kt               # expect composable
│       │   │   └── components/
│       │   │       ├── ExhibitionCard.kt
│       │   │       └── BookmarkButton.kt
│       │   └── viewmodel/TabsViewModel.kt
│       ├── androidMain/kotlin/com/gallr/app/
│       │   ├── MainActivity.kt
│       │   └── ui/tabs/map/MapView.android.kt       # actual composable
│       └── iosMain/kotlin/com/gallr/app/
│           ├── MainViewController.kt
│           └── ui/tabs/map/MapView.ios.kt           # actual composable
│
├── iosApp/                                          # Xcode project
│   └── iosApp/
│       └── ContentView.swift
│
└── specs/001-exhibition-tabs/                       # Feature docs (this directory)
```

**Structure Decision**: Two-module KMP pattern enforced at build-system level.
`shared/` has no Compose dependency — pure Kotlin + Ktor + DataStore. `composeApp/`
has all UI. Map rendering is the only expect/actual in `composeApp/` (no business logic
leaks to platform modules). This is the minimum structure that satisfies Principle VI
without premature modularisation (Principle III).

## Complexity Tracking

> No constitution violations. Table intentionally empty.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| — | — | — |
