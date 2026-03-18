# Implementation Plan: Minimalist Monochrome Design System

**Branch**: `002-monochrome-design-system` | **Date**: 2026-03-18 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/002-monochrome-design-system/spec.md`

---

## Summary

Apply the Minimalist Monochrome design system to all three app tabs (Featured, List, Map)
of the gallr Compose Multiplatform app. This involves: (1) creating a `GallrTheme`
composable with a monochrome `MaterialTheme` override using bundled serif/mono fonts;
(2) refactoring `ExhibitionCard`, `BookmarkButton`, and screen-level loading/empty states
to match the editorial aesthetic; (3) replacing `CircularProgressIndicator` with a linear
loading indicator; (4) adding staggered entry animations to list screens; and (5) styling
filter chips and the navigation bar per the Minimalist Monochrome token system.

All implementation is in `composeApp/src/commonMain/` (shared KMP module). No
platform-specific changes are required.

---

## Technical Context

**Language/Version**: Kotlin 2.1.x, Compose Multiplatform 1.8.0
**Primary Dependencies**: Compose Multiplatform, Material3, compose-resources (CMP 1.8.0
stable), `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4`
**Storage**: N/A — design tokens are compile-time Kotlin objects; font TTF files bundled
via `composeResources/font/`
**Testing**: No automated tests for this feature (pure UI visual polish; manual
verification against Success Criteria SC-001 through SC-006)
**Target Platform**: Android (API 24+) + iOS (16+) via CMP
**Project Type**: Mobile app (KMP, Compose Multiplatform)
**Performance Goals**: Entry animation ≤200ms per item, ≤50ms stagger; press feedback
≤100ms; 60fps during staggered list reveal
**Constraints**: Light-mode only (dark mode deferred); fonts bundled as assets (no
network download); `composeResources/font/` path for CMP 1.8.0 stable resource API;
`Font(Res.font.X)` is `@Composable` — FontFamily creation must be inside composable
**Scale/Scope**: 3 tabs, 2 shared components (`ExhibitionCard`, `BookmarkButton`),
3 new composables (`GallrLoadingState`, `GallrEmptyState`, `GallrNavigationBar`),
1 theme module (5 files)

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Check | Status |
|-----------|-------|--------|
| **I. Spec-First** | `spec.md` written and approved before any code | ✅ PASS |
| **II. Test-First** | No tested paths in this feature (pure visual UI); constitution exempts platform-specific UI layers | ✅ PASS (exempt) |
| **III. Simplicity & YAGNI** | Using `MaterialTheme` wrapper (not custom `CompositionLocal` system); no dark mode scaffolding; no multi-theme variants | ✅ PASS |
| **IV. Incremental Delivery** | US1 (visual identity) independently testable before US2–US4; each story is a complete, verifiable increment | ✅ PASS |
| **V. Observability** | UI-only feature; no new data operations requiring logging. Existing observability patterns unchanged | ✅ PASS |
| **VI. Shared-First** | ALL code lives in `composeApp/src/commonMain/`. No platform-specific changes (fonts via CMP compose-resources, press state via `detectTapGestures` — both are CMP commonMain APIs) | ✅ PASS |

**Post-Phase 1 re-check**: No violations introduced by Phase 1 design. Component
contracts confirm all logic stays in `composeApp/src/commonMain/`.

---

## Project Structure

### Documentation (this feature)

```text
specs/002-monochrome-design-system/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0 decisions (fonts, press state, animations, tokens)
├── data-model.md        # DesignToken, InteractionState, AnimationSpec entities
├── quickstart.md        # Build steps and visual verification guide
├── contracts/
│   └── ui-components.md # GallrTheme, ExhibitionCard, BookmarkButton, etc.
├── checklists/
│   └── requirements.md  # 16/16 checks pass
└── tasks.md             # Phase 2 output (/speckit.tasks — not yet created)
```

### Source Code (repository root)

```text
composeApp/src/commonMain/
├── composeResources/
│   └── font/
│       ├── PlayfairDisplay_Regular.ttf       [NEW — asset, download from Google Fonts]
│       ├── PlayfairDisplay_Bold.ttf          [NEW]
│       ├── PlayfairDisplay_Italic.ttf        [NEW]
│       ├── PlayfairDisplay_BoldItalic.ttf    [NEW]
│       ├── SourceSerif4_Regular.ttf          [NEW]
│       ├── SourceSerif4_Bold.ttf             [NEW]
│       └── JetBrainsMono_Regular.ttf         [NEW]
└── kotlin/com/gallr/app/
    ├── App.kt                                [MODIFY — wrap in GallrTheme, use GallrNavigationBar]
    ├── ui/
    │   ├── theme/                            [NEW package]
    │   │   ├── GallrColors.kt               [NEW — monochrome color scheme object]
    │   │   ├── GallrTypography.kt           [NEW — serif/mono typography object]
    │   │   ├── GallrMotion.kt               [NEW — animation timing constants]
    │   │   └── GallrTheme.kt               [NEW — root composable wrapping MaterialTheme]
    │   ├── components/
    │   │   ├── ExhibitionCard.kt            [MODIFY — press inversion, serif, entry anim]
    │   │   ├── BookmarkButton.kt            [MODIFY — monochrome styling]
    │   │   ├── GallrNavigationBar.kt        [NEW — nav bar with black underline indicator]
    │   │   ├── GallrLoadingState.kt         [NEW — linear progress, 1dp black line]
    │   │   └── GallrEmptyState.kt           [NEW — serif empty state + outline action]
    │   └── tabs/
    │       ├── featured/
    │       │   └── FeaturedScreen.kt        [MODIFY — use GallrLoadingState, GallrEmptyState, staggered anim]
    │       ├── list/
    │       │   └── ListScreen.kt            [MODIFY — filter chip styling, GallrLoadingState, GallrEmptyState]
    │       └── map/
    │           └── MapScreen.kt             [MODIFY — apply GallrTheme tokens to header/controls only]
```

**Structure Decision**: Single KMP project, `composeApp/src/commonMain/` only. All theme
code is new under `ui/theme/`; no new modules or top-level projects needed. Font TTFs are
build-time assets, not compiled code.

---

## Complexity Tracking

> No constitution violations — table is empty.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| — | — | — |
