# UI Component Contracts: Minimalist Monochrome Design System

**Feature**: 002-monochrome-design-system
**Date**: 2026-03-18

This document defines the public Composable interfaces for new and modified UI components.
These contracts are the stable API surface that screens (`FeaturedScreen`, `ListScreen`,
`MapScreen`) depend on.

---

## GallrTheme

**File**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/GallrTheme.kt`

```kotlin
@Composable
fun GallrTheme(content: @Composable () -> Unit)
```

**Contract**:
- Wraps `MaterialTheme` with the monochrome `ColorScheme` from `GallrColors`.
- Applies `GallrTypography` (Playfair Display / Source Serif 4 / JetBrains Mono).
- Sets `MaterialTheme.shapes` to `Shapes(extraSmall = RectangleShape, small = RectangleShape, medium = RectangleShape, large = RectangleShape, extraLarge = RectangleShape)` (all 0dp radius).
- Must be called at the root of `App.kt`, replacing the bare `MaterialTheme { }` wrapper.
- `content` receives full Material3 theming with monochrome tokens.

**State**: Stateless (tokens are compile-time constants or resource-loaded fonts).

---

## ExhibitionCard (modified)

**File**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/ExhibitionCard.kt`

```kotlin
@Composable
fun ExhibitionCard(
    exhibition: Exhibition,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    modifier: Modifier = Modifier,
    animationIndex: Int = 0,            // 0 = no stagger; >0 = index-based delay
    showEntryAnimation: Boolean = false, // drives AnimatedVisibility
)
```

**Contract**:
- **Border**: 1dp solid `GallrColors.foreground` (black). No corner radius.
- **Background**: `GallrColors.card` (white) by default; inverts to `GallrColors.accent`
  (black) when pressed.
- **Press feedback**: Detected via `detectTapGestures(onPress = { ... })`. Background and
  all text colors invert within `GallrMotion.pressDurationMs` (≤100ms).
- **Exhibition name**: `MaterialTheme.typography.titleLarge` (PlayfairDisplay, 24sp).
  Truncated to 2 lines with ellipsis if over 60 characters.
- **Venue/city**: `MaterialTheme.typography.labelMedium` (JetBrainsMono), uppercase,
  `GallrColors.mutedForeground`.
- **Date range**: `MaterialTheme.typography.labelMedium` (JetBrainsMono),
  `GallrColors.foreground`.
- **Hairline separator**: 1dp `Divider` in `GallrColors.borderLight` between metadata
  and description zones (if description is present).
- **Entry animation**: When `showEntryAnimation = true`, wraps content in
  `AnimatedVisibility` with `slideInVertically + fadeIn`. Delay =
  `animationIndex * GallrMotion.staggeredItemDelayMs`.
- **Focus indicator**: 3dp solid black outline at 3dp offset (via Compose focus modifier).
- **Touch target**: Minimum 44×44dp tappable area for all interactive sub-elements.

**Invariants**:
- `animationIndex` ignored when `showEntryAnimation = false`.
- `onBookmarkToggle` called on tap of BookmarkButton only (not the card body tap).
- Card body press does NOT trigger navigation (tap handler is separate concern).

---

## BookmarkButton (modified)

**File**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/BookmarkButton.kt`

```kotlin
@Composable
fun BookmarkButton(
    isBookmarked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
)
```

**Contract**:
- **Icon style**: Outlined when `isBookmarked = false`; filled/inverted when `true`.
  Rendered as a text character (current approach) or vector icon — visual must clearly
  differentiate the two states.
- **Colors**: Icon is `GallrColors.foreground` (black) in both states; inverted state
  uses filled black icon with white background square (or simply a filled black icon).
- **Press feedback**: Toggles state instantly on tap (`onToggle`). No delay.
- **Touch target**: Minimum 44×44dp.
- **Focus**: 3dp solid black outline at 2dp offset.

---

## FilterChip (styling override, not a new component)

**File**: Applied inline in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt`

**Contract**:
- Use Material3 `FilterChip` with `FilterChipDefaults.filterChipColors(...)`:
  - **Inactive**: white background, black border, black text.
  - **Active**: black background, white text, no border needed (filled).
- Shape override: `RoundedCornerShape(0.dp)` (applied globally via `GallrTheme`).
- Label text style: `MaterialTheme.typography.labelLarge` (JetBrainsMono, uppercase,
  letter-spacing 0.1em).
- No elevation, no shadow, no ripple.

---

## GallrNavigationBar (new)

**File**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/GallrNavigationBar.kt`

```kotlin
@Composable
fun GallrNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
)
```

**Contract**:
- Replaces the bare `NavigationBar { NavigationBarItem(...) }` in `App.kt`.
- **Active indicator**: Bold 4dp top border in `GallrColors.foreground` (black) on the
  selected item. No colored highlight, no filled background pill.
- **Background**: `GallrColors.background` (white). No elevation, no shadow.
- **Labels**: Tab labels in `MaterialTheme.typography.labelLarge` (JetBrainsMono,
  uppercase). Active label in `GallrColors.foreground`; inactive in
  `GallrColors.mutedForeground`.
- **Icons**: Not required for MVP; text labels sufficient.
- **Press feedback**: Active tab indicator transitions instantly (≤100ms).
- **Touch target**: Each tab item ≥ 44dp tall.

---

## LoadingState (new composable)

**File**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/GallrLoadingState.kt`

```kotlin
@Composable
fun GallrLoadingState(modifier: Modifier = Modifier)
```

**Contract**:
- Replaces `CircularProgressIndicator` in all screens.
- Renders an indeterminate `LinearProgressIndicator` styled as a 1dp-height black line
  spanning full width.
- `trackColor = GallrColors.borderLight` (light gray track), `color = GallrColors.foreground` (black progress).
- No text label needed.

---

## EmptyState (new composable)

**File**: `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/GallrEmptyState.kt`

```kotlin
@Composable
fun GallrEmptyState(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
)
```

**Contract**:
- `message`: Large serif text — `MaterialTheme.typography.headlineSmall` (PlayfairDisplay,
  32sp), centered.
- `actionLabel`: Outline-style button — transparent background, 2dp black border,
  black text in `MaterialTheme.typography.labelLarge` (JetBrainsMono, uppercase).
- Button shape: rectangular (no radius) via `GallrTheme`.
- Layout: vertically centered in available space, generous vertical padding.
