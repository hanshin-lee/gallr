# Data Model: Reductionist Design System

**Feature**: 005-reductionist-design-system
**Date**: 2026-03-19

---

## Overview

This feature introduces no new data entities in the `shared/` KMP module. All entities are UI-layer token objects living in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/`. They are compile-time Kotlin objects (no runtime persistence, no serialization).

---

## Token Objects

### GallrColors (updated)

**Location**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/GallrColors.kt`

**Purpose**: Single source of truth for all color values used in the app.

| Token | Value | Role |
|-------|-------|------|
| `black` | `#000000` | Primary text, borders, filled active states |
| `white` | `#FFFFFF` | Primary background, inverse text |
| `grayLight` | `#F5F5F5` | Secondary surface, card background |
| `grayMid` | `#E5E5E5` | Borders, dividers |
| `grayText` | `#525252` | Secondary/supporting text |
| `disabled` | `#A0A0A0` | Disabled element fill and text |
| `accent` | `#FF5400` | Base accent value |
| `ctaPrimary` | → `accent` | Primary CTA button fill |
| `activeIndicator` | → `accent` | Tab indicator, active filter chip |
| `interactionFeedback` | → `accent` | Active/pressed state signal on primary controls |

**Constraints**:
- `ctaPrimary`, `activeIndicator`, `interactionFeedback` are the ONLY roles that may reference `accent`.
- No other token may use `#FF5400` or any orange-family value.
- Orange is never used as a `background` or `surface` token.

**State table for interactive elements**:

| State | Background | Foreground | Border |
|-------|------------|------------|--------|
| Default (standard control) | `white` | `black` | `black` |
| Default (primary CTA) | `ctaPrimary` | `white` | none |
| Hover / Focus | `grayLight` | `black` | `black` |
| Active / Selected | `activeIndicator` | `white` | none |
| Pressed | opacity 0.7 on current fill | — | — |
| Disabled | `disabled` (opacity 0.4) | `grayText` | `grayMid` |

---

### GallrTypography (updated)

**Location**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/GallrTypography.kt`

**Purpose**: Defines all type styles using the neo-grotesque Inter typeface.

| Style | Typeface | Weight | Size | Line Height | Letter Spacing | Usage |
|-------|----------|--------|------|-------------|----------------|-------|
| `displayLarge` | Inter | Bold 700 | 40sp | 48sp | -0.5sp | Screen hero titles |
| `displayMedium` | Inter | Bold 700 | 32sp | 40sp | -0.25sp | Section titles |
| `titleLarge` | Inter | Medium 500 | 24sp | 32sp | 0sp | Card titles |
| `titleMedium` | Inter | Medium 500 | 18sp | 26sp | 0sp | List item headers |
| `bodyLarge` | Inter | Regular 400 | 16sp | 24sp | 0sp | Body text |
| `bodyMedium` | Inter | Regular 400 | 14sp | 20sp | 0sp | Secondary body |
| `labelLarge` | Inter | Medium 500 | 13sp | 18sp | 0.4sp | Tab labels, chip labels |
| `labelSmall` | Inter | Regular 400 | 11sp | 16sp | 0.5sp | Metadata, dates, captions |

**Constraints**:
- All styles use Inter exclusively; no fallback to serif or monospaced variants.
- No italic, oblique, or condensed weights.
- Letter spacing on labels is the only stylistic variation permitted.

**Font files required**:
- `composeResources/font/Inter_Regular.ttf` (weight 400)
- `composeResources/font/Inter_Medium.ttf` (weight 500)
- `composeResources/font/Inter_Bold.ttf` (weight 700)

---

### GallrSpacing (new)

**Location**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/GallrSpacing.kt`

**Purpose**: Defines the 8pt grid spacing system. All layout decisions reference these tokens.

| Token | Value | Usage |
|-------|-------|-------|
| `unit` | 8.dp | Base unit |
| `xs` | 4.dp | Tight internal padding (icon margins) |
| `sm` | 8.dp | Chip internal padding, label gap |
| `md` | 16.dp | Card padding, screen margin |
| `lg` | 24.dp | Card-to-card gap, section sub-spacing |
| `xl` | 32.dp | Section spacing (major zones) |
| `xxl` | 48.dp | Full-screen section breaks |
| `gutterWidth` | 8.dp | Column gutter |
| `screenMargin` | 16.dp | Left/right screen edge padding |

---

### GallrMotion (updated)

**Location**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/GallrMotion.kt`

**Purpose**: Defines the minimal set of timing constants. Animation is limited to immediate state feedback.

| Token | Value | Usage |
|-------|-------|-------|
| `pressResponseMs` | 100 | Max duration for press/active color shift |
| `stateTransitionMs` | 0 | Snap for all non-press state changes (no easing) |

**Removed from 002**:
- `staggeredRevealMs` (200) — eliminated; no content reveal animation
- `staggerDelayMs` (50) — eliminated
- `slideDistanceDp` (8) — eliminated

---

### GallrShapes (updated)

**Location**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/GallrTheme.kt` (inline or separate)

**Purpose**: Enforces zero-radius corners on all shapes.

| Token | Value | Usage |
|-------|-------|-------|
| `small` | `RoundedCornerShape(0.dp)` | Chips, small buttons |
| `medium` | `RoundedCornerShape(0.dp)` | Cards, inputs |
| `large` | `RoundedCornerShape(0.dp)` | Sheets, dialogs |

No change from 002 — already enforced. Documented here for completeness.

---

## State Transitions

### InteractionState machine (per interactive element)

```
DEFAULT ──[hover/focus]──> FOCUSED
DEFAULT ──[press]────────> PRESSED
DEFAULT ──[activate]─────> ACTIVE
ACTIVE  ──[deactivate]───> DEFAULT
PRESSED ──[release]──────> DEFAULT or ACTIVE
ANY     ──[disable]──────> DISABLED
DISABLED──[enable]───────> DEFAULT
```

**State → Token mapping**:
- `DEFAULT`: `GallrColors.white` bg, `GallrColors.black` fg (standard) OR `GallrColors.ctaPrimary` bg (primary CTA)
- `FOCUSED`: `GallrColors.grayLight` bg, `GallrColors.black` fg + black 2dp outline
- `PRESSED`: opacity 0.7 on current fill (within `GallrMotion.pressResponseMs`)
- `ACTIVE`: `GallrColors.activeIndicator` signal (tab: underline/fill; chip: fill; CTA: already orange)
- `DISABLED`: `GallrColors.disabled` @ 0.4 opacity
