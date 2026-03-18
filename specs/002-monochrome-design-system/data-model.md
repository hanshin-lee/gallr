# Data Model: Minimalist Monochrome Design System

**Feature**: 002-monochrome-design-system
**Date**: 2026-03-18

---

## Overview

This feature introduces no new persistent data entities. All "data" is static design
token values encoded in Kotlin objects. The three key conceptual entities from the spec
(`DesignToken`, `InteractionState`, `AnimationSpec`) map directly to Kotlin objects in
`composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/`.

---

## Entity 1: DesignToken

**Purpose**: Centralises all visual design values — color, typography, spacing, border
weights — as a single source of truth. Prevents one-off inline styling.

**Source file**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/GallrColors.kt`

### Color Tokens

| Token name            | Type          | Value       | Usage                              |
|-----------------------|---------------|-------------|------------------------------------|
| `background`          | `Color`       | `#FFFFFF`   | Screen and card backgrounds        |
| `foreground`          | `Color`       | `#000000`   | Primary text and borders           |
| `muted`               | `Color`       | `#F5F5F5`   | Off-white surface variant          |
| `mutedForeground`     | `Color`       | `#525252`   | Secondary text, placeholders       |
| `accent`              | `Color`       | `#000000`   | Active/inverted element background |
| `accentForeground`    | `Color`       | `#FFFFFF`   | Text on inverted/active elements   |
| `border`              | `Color`       | `#000000`   | Card and container borders         |
| `borderLight`         | `Color`       | `#E5E5E5`   | Hairline dividers                  |

**Validation rules**:
- No other colors may be introduced.
- No gradients; all values are flat `Color` instances.
- `accent` and `foreground` are identical by design (black IS the accent).

### Typography Tokens

**Source file**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/GallrTypography.kt`

| Token name              | FontFamily        | Size    | Usage                        |
|-------------------------|-------------------|---------|------------------------------|
| `displayLarge`          | PlayfairDisplay   | 40sp    | Section titles               |
| `displayMedium`         | PlayfairDisplay   | 32sp    | Subheadings                  |
| `headlineLarge`         | PlayfairDisplay   | 56sp    | Page-level heroes            |
| `headlineMedium`        | PlayfairDisplay   | 40sp    | Card exhibition names        |
| `headlineSmall`         | PlayfairDisplay   | 32sp    | Smaller exhibit titles       |
| `titleLarge`            | PlayfairDisplay   | 24sp    | Card primary titles          |
| `titleMedium`           | PlayfairDisplay   | 20sp    | Card secondary titles        |
| `titleSmall`            | SourceSerif4      | 18sp    | Supporting text              |
| `bodyLarge`             | SourceSerif4      | 18sp    | Body / description text      |
| `bodyMedium`            | SourceSerif4      | 16sp    | Standard body text           |
| `bodySmall`             | SourceSerif4      | 14sp    | Captions                     |
| `labelLarge`            | JetBrainsMono     | 14sp    | Filter chips, section labels |
| `labelMedium`           | JetBrainsMono     | 12sp    | Date ranges, metadata        |
| `labelSmall`            | JetBrainsMono     | 10sp    | Fine print                   |

**Validation rules**:
- Display/headline = `PlayfairDisplay` only.
- Body/title = `SourceSerif4` only.
- Label/mono = `JetBrainsMono` only.
- No system fonts (`Default`, `Monospace`, `SansSerif`, `Serif`) for named text styles.

### Motion Tokens

**Source file**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/GallrMotion.kt`

| Token name                | Type           | Value          | Usage                               |
|---------------------------|----------------|----------------|-------------------------------------|
| `pressDurationMs`         | `Int`          | `100`          | Max press/hover transition duration |
| `staggeredItemDurationMs` | `Int`          | `200`          | Per-item reveal duration            |
| `staggeredItemDelayMs`    | `Int`          | `50`           | Delay between list items            |
| `staggeredSlideOffsetDp`  | `Float`        | `8f`           | Initial Y offset for slide-in       |

---

## Entity 2: InteractionState

**Purpose**: Maps interactive element states (default, pressed, focused, disabled) to
specific token combinations. Implemented as logic inside composables, not as a data class.

**Source file**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/ExhibitionCard.kt`
(press state via `detectTapGestures`)

| State      | Background          | Text/Border             | Implementation                          |
|------------|---------------------|-------------------------|-----------------------------------------|
| `default`  | `GallrColors.card`  | `GallrColors.foreground`| Static — no animation                  |
| `pressed`  | `GallrColors.accent`| `GallrColors.accentForeground` | `detectTapGestures(onPress)` flag |
| `focused`  | `GallrColors.card`  | `GallrColors.foreground`| 3dp solid black outline, 3dp offset     |
| `disabled` | `GallrColors.muted` | `GallrColors.mutedForeground` | Not applicable for MVP               |

**Validation rules**:
- Press detection MUST use `detectTapGestures(onPress)`, NOT `collectIsPressedAsState()`
  (known CMP bug #3417 on iOS).
- Focus indicator MUST be a 3dp solid black outline at 3dp offset on all interactive elements.
- Transition from `default` → `pressed` MUST complete within `GallrMotion.pressDurationMs`.

---

## Entity 3: AnimationSpec

**Purpose**: Defines timing values for entry animations. Stored in `GallrMotion` object.
Applied in `FeaturedScreen` and `ListScreen` via `AnimatedVisibility`.

| Animation            | Type                                     | Value                                 |
|----------------------|------------------------------------------|---------------------------------------|
| `listItemReveal`     | `EnterTransition`                        | `slideInVertically + fadeIn`          |
| `slideInOffset`      | Initial Y px offset (negative = from below) | `staggeredSlideOffsetDp` in dp units |
| `itemAnimationSpec`  | `FiniteAnimationSpec<T>`                 | `tween(200ms)` per item               |
| `itemDelaySpec`      | `FiniteAnimationSpec<T>`                 | `tween(200ms, delayMillis = idx * 50)`|

**State machine**: `visible = false` (initial) → composition → `LaunchedEffect(Unit)` →
`visible = true` → `AnimatedVisibility` triggers entry animation.

**Validation rules**:
- `visible` starts as `false`; set to `true` via `LaunchedEffect(Unit)`.
- Duration per item ≤ 200ms (SC-005).
- Inter-item delay = 50ms × index (FR-011).
- Must not drop frames; verify at 60fps during staggered animation.
