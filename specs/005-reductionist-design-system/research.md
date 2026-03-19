# Phase 0 Research: Reductionist Design System

**Feature**: 005-reductionist-design-system
**Date**: 2026-03-19

---

## Decision 1: Neo-Grotesque Typeface Selection

**Decision**: Inter (by Rasmus Andersson)

**Rationale**: Inter is purpose-built for screen readability, is open-source (SIL OFL 1.1), ships as TTF which is compatible with the existing composeResources font-loading mechanism, and is the de-facto standard for editorial/functional digital interfaces (referenced by are.na and similar). The variable font axis allows a single file to cover Regular → Bold without multiple TTFs, but static TTFs are used here for Compose compatibility.

Required weights: Regular (400), Medium (500), Bold (700) — this covers the full typographic hierarchy without expressive variation.

**Alternatives considered**:
- Helvetica Neue: Not freely licensable; cannot be bundled as an app asset. Rejected on licensing grounds.
- System sans-serif default: On Android this is Roboto (acceptable fallback); on iOS it is SF Pro (not licensable for bundling). Rejected because cross-platform visual consistency requires a bundled asset.
- DM Sans: Open-source, but less editorial character than Inter; narrower ecosystem adoption.

---

## Decision 2: Accent Color Token Placement

**Decision**: Extend the existing `GallrColors` object with a single new token `accent = Color(0xFFFF5400)` and add three semantic role tokens: `ctaPrimary`, `activeIndicator`, `interactionFeedback` — all three mapping to `accent`.

**Rationale**: The current `GallrColors.kt` is a thin custom object (not Material3 `ColorScheme`). Adding a single token maintains the same pattern without introducing a new abstraction. Three semantic aliases make intent explicit — any future divergence in the three roles is possible without a structural change.

**Alternatives considered**:
- Map orange into Material3 `ColorScheme.primary`: Material3 applies `primary` broadly (including tonal surfaces, containers, backgrounds), which would violate the spec constraint that orange is never on large surfaces. Rejected.
- Single `accent` token, no semantic aliases: Simpler but makes FR-001's intent (orange only for 3 roles) less enforceable by convention. Rejected.

---

## Decision 3: Font Bundling Mechanism (composeResources)

**Decision**: Add Inter TTF files to `composeApp/src/commonMain/composeResources/font/` using the same pattern as the existing Playfair Display / JetBrains Mono fonts. Load via `Font(Res.font.inter_regular)` in `GallrTypography.kt`.

**Rationale**: This is the exact mechanism used in 002 and supported by Compose Multiplatform 1.8.0 `compose-resources`. No additional dependency required.

**Files needed**:
- `Inter_Regular.ttf` — weight 400
- `Inter_Medium.ttf` — weight 500
- `Inter_Bold.ttf` — weight 700

**Alternatives considered**:
- Variable font (Inter[wght].ttf): Not supported by Compose Multiplatform `Font()` API as of 1.8.0. Rejected.
- System font fallback: Produces Roboto on Android, SF Pro on iOS — inconsistent cross-platform. Rejected for display/title contexts.

---

## Decision 4: Interaction State Without Motion

**Decision**: Remove the staggered content-reveal `AnimationSpec` from `GallrMotion.kt`. Retain only the `pressResponseMs = 100` constant (used for immediate press feedback). State transitions use `animateColorAsState` with `tween(durationMillis = 0)` (snap) or direct composition — no easing curves.

**Rationale**: Spec FR-009 explicitly prohibits positional animation for feedback. The existing `GallrMotion.kt` staggered-reveal constants (200ms slide, 50ms offset) are incompatible with this spec. Retaining them without use creates dead code. The press-response constant is still needed by FR-002 (100ms response) for the color/opacity shift.

**Alternatives considered**:
- Keep stagger constants but leave unused: Creates confusion about intent. Rejected.
- Remove `GallrMotion.kt` entirely: Press-response timing is still spec-required; some constant is needed. Retained in slimmer form.

---

## Decision 5: Handling Existing Serif/Mono Fonts

**Decision**: Retain existing Playfair Display, Source Serif 4, and JetBrains Mono TTF files in the repository. Remove their references from `GallrTypography.kt` but do not delete the font files in this feature.

**Rationale**: Deleting bundled font files is a destructive change that could affect builds or other branches. The spec states this feature "supersedes" 002 for the app's visual language; removing files is an implementation clean-up task that can be deferred (Out of Scope per spec). TTF files are static assets with no runtime cost if unreferenced.

---

## Decision 6: Grid System Specification

**Decision**: Use an 8pt base unit grid. Recommended column structure: 4-column grid on mobile with 16pt margins and 8pt gutters. Card padding: 16pt. Section spacing: 32pt between major zones. These values are encoded as `GallrSpacing` tokens (a new token object, thin extension of the existing pattern).

**Rationale**: 8pt grids are industry standard for mobile design systems. 16pt margins align with Material3 defaults and maintain visual breathing room on 360dp–430dp viewports. No new dependency — pure `Dp` constants.

**Alternatives considered**:
- 4pt base unit: More granular but produces inconsistent visual results at scale. Rejected.
- Material3 spacing tokens: Decouple badly from the custom design system direction; Material3 spacing is grid-agnostic. Rejected.
