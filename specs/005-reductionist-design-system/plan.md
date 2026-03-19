# Implementation Plan: Reductionist Design System

**Branch**: `005-reductionist-design-system` | **Date**: 2026-03-19 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/005-reductionist-design-system/spec.md`

## Summary

Replace gallr's serif/monochrome visual language (002-monochrome-design-system) with a strictly utilitarian, reductionist system: Inter neo-grotesque typeface throughout, monochrome base palette supplemented by a single #FF5400 accent restricted to primary CTAs and active states, all decorative animation removed, and a new 8pt grid spacing token object added. All changes are confined to `composeApp/src/commonMain` (UI layer) — no shared module changes, no new dependencies beyond bundled font assets.

## Technical Context

**Language/Version**: Kotlin 2.1.20 (KMP), Swift 5.9 (iOS entry point — no changes needed)
**Primary Dependencies**: Compose Multiplatform 1.8.0, compose-resources (font loading) — no new dependencies
**Storage**: N/A — compile-time token objects only, no runtime persistence
**Testing**: kotlin-test (existing); visual acceptance tested by manual inspection per spec SC-001–SC-007
**Target Platform**: Android 26+ and iOS (via Compose Multiplatform); changes in `composeApp/commonMain` only
**Project Type**: KMP mobile app — UI layer update
**Performance Goals**: All interactive state changes visible within 100ms (SC-002); no dropped frames from motion removal
**Constraints**: No new Gradle dependencies; font TTF files bundled as composeResources assets; no changes to `shared/` module
**Scale/Scope**: 4 token files updated/created, 3 Inter TTF font files added, all 3 app tabs updated

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status | Notes |
|-----------|------|--------|-------|
| I. Spec-First | spec.md exists with acceptance criteria | ✅ PASS | spec.md complete, checklist all-pass |
| II. Test-First | No new logic paths; visual-only changes — UI layer is exempt per constitution | ✅ PASS | No shared module logic added |
| III. Simplicity/YAGNI | Extending 4 existing token files + 1 new spacing file; no new patterns or abstractions | ✅ PASS | See Complexity Tracking — no violations |
| IV. Incremental Delivery | US1 (visual identity) → US2 (active states) → US3 (CTA) → US4 (grid) are independently deliverable | ✅ PASS | Each story ships usable value alone |
| V. Observability | UI-only change; no new significant operations or error paths | ✅ PASS | N/A for this feature |
| VI. Shared-First | Design tokens are UI-layer concerns; all changes stay in `composeApp/commonMain`. Zero changes to `shared/`. | ✅ PASS | Verified: no business logic, no shared module changes |

**Post-Phase-1 re-check**: ✅ All gates pass. `GallrSpacing.kt` (new) lives in `composeApp/commonMain/kotlin/com/gallr/app/ui/theme/` — correctly placed in UI layer. No shared module impact.

## Project Structure

### Documentation (this feature)

```text
specs/005-reductionist-design-system/
├── plan.md              # This file
├── research.md          # Phase 0 output ✅
├── data-model.md        # Phase 1 output ✅
├── quickstart.md        # Phase 1 output ✅
├── checklists/
│   └── requirements.md  # Spec quality checklist ✅
└── tasks.md             # Phase 2 output (/speckit.tasks — not yet created)
```

### Source Code (repository root)

```text
composeApp/src/commonMain/
├── composeResources/
│   └── font/
│       ├── Inter_Regular.ttf           # NEW — weight 400
│       ├── Inter_Medium.ttf            # NEW — weight 500
│       ├── Inter_Bold.ttf              # NEW — weight 700
│       ├── PlayfairDisplay_*.ttf       # RETAINED (not deleted; unreferenced)
│       ├── SourceSerif4_*.ttf          # RETAINED (not deleted; unreferenced)
│       └── JetBrainsMono_Regular.ttf   # RETAINED (not deleted; unreferenced)
└── kotlin/com/gallr/app/ui/theme/
    ├── GallrColors.kt                  # UPDATED — add accent + 3 semantic role tokens
    ├── GallrTypography.kt              # UPDATED — replace all font families with Inter
    ├── GallrMotion.kt                  # UPDATED — remove stagger constants
    ├── GallrTheme.kt                   # UPDATED — wire updated tokens
    └── GallrSpacing.kt                 # NEW — 8pt grid spacing constants

shared/                                 # NO CHANGES
```

**Structure Decision**: UI-layer-only update within `composeApp/commonMain`. No structural change to the KMP module layout. `shared/` is untouched. The existing theme directory is extended, not reorganized.

## Phase 0: Research Summary

See [research.md](research.md) for full details.

| Unknown | Resolution |
|---------|------------|
| Font choice for neo-grotesque | **Inter** (SIL OFL, TTF, composeResources-compatible, no new dependency) |
| Helvetica Neue licensing | Not freely licensable — cannot be bundled. Inter selected instead. |
| Accent color token placement | Extend `GallrColors` object; add `accent` + 3 semantic aliases. Do NOT use Material3 `ColorScheme.primary`. |
| Font bundling mechanism | Same `composeResources/font/` pattern as 002. No new setup needed. |
| Handling existing fonts | Retain files, remove references from `GallrTypography.kt`. File deletion deferred. |
| Grid specification | 8pt base unit. `GallrSpacing` object: `xs=4`, `sm=8`, `md=16`, `lg=24`, `xl=32`, `screenMargin=16` dp. |
| Motion removal | Remove stagger/slide from `GallrMotion.kt`. Retain `pressResponseMs=100`. |

## Phase 1: Design

See [data-model.md](data-model.md) and [quickstart.md](quickstart.md) for full detail.

### Token Architecture

The design system is four Kotlin objects + one new object:

```
GallrColors      — color tokens (updated: + accent, ctaPrimary, activeIndicator, interactionFeedback)
GallrTypography  — type scale (updated: Inter replaces all existing font families)
GallrSpacing     — spacing/grid (NEW: 8pt grid, xs/sm/md/lg/xl/screenMargin/gutterWidth)
GallrMotion      — timing (updated: stagger constants removed; pressResponseMs retained)
GallrTheme       — wires all tokens into MaterialTheme (updated: no new shape changes needed)
```

### Key Design Constraints (from spec, encoded in tokens)

1. **Orange restriction**: `ctaPrimary` / `activeIndicator` / `interactionFeedback` are the ONLY token assignments for `accent`; no token maps `accent` to a surface or background role.
2. **Zero motion**: `stateTransitionMs = 0`; all state changes are immediate except press (100ms opacity shift).
3. **Zero radius**: `GallrShapes` already enforces `RoundedCornerShape(0.dp)` — no change needed.
4. **Type-only hierarchy**: `GallrColors.black` is the only text color token; no orange text.

### Contracts

This feature exposes no external interfaces (pure internal UI token system). No `contracts/` directory is required.

## Complexity Tracking

No complexity violations identified. All changes are direct token updates to existing files. The new `GallrSpacing.kt` file is justified by the spec's grid requirement (FR-010) and follows the existing token-object pattern — no new abstraction.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |
