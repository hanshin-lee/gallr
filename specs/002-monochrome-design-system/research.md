# Research: Minimalist Monochrome Design System

**Feature**: 002-monochrome-design-system
**Date**: 2026-03-18

---

## Decision 1: Font Bundling Strategy

**Decision**: Bundle TTF font files via Compose Multiplatform's `composeResources` system.

**Rationale**: CMP 1.8.0 includes stable compose-resources support (`compose.components.resources` already in the project). Fonts placed under `composeApp/src/commonMain/composeResources/font/` are auto-generated as `Res.font.*` accessors and load natively on both Android and iOS with zero platform-specific code. The `Font(Res.font.X)` API is `@Composable`, so FontFamily creation must happen inside a composable (e.g. inside `GallrTheme`).

**Alternatives considered**:
- Platform fonts (system serif): Rejected — system serif varies per device and OS version; editorial quality requires controlled typefaces.
- `expect/actual` font loading: Rejected — unnecessary complexity; compose-resources handles both platforms identically.
- Google Fonts runtime download: Rejected — requires network; offline-first is preferred and latency would delay font render.

**File paths**:
```
composeApp/src/commonMain/composeResources/font/
├── PlayfairDisplay_Regular.ttf
├── PlayfairDisplay_Bold.ttf
├── PlayfairDisplay_Italic.ttf
├── PlayfairDisplay_BoldItalic.ttf
├── SourceSerif4_Regular.ttf
├── SourceSerif4_Bold.ttf
└── JetBrainsMono_Regular.ttf
```

---

## Decision 2: Press/Interaction State Detection

**Decision**: Use `Modifier.pointerInput(Unit) { detectTapGestures(onPress = { isPressed = true; tryAwaitRelease(); isPressed = false }) }` for press state in commonMain.

**Rationale**: `InteractionSource.collectIsPressedAsState()` has a known CMP issue (#3417) where drag interactions fire before press interactions, making the state unreliable on iOS. The `detectTapGestures` approach with a local `mutableStateOf(false)` flag is reliable, composable-friendly, and works identically on both platforms.

**Alternatives considered**:
- `collectIsPressedAsState()`: Rejected — known broken on iOS in CMP (fires as drag before press).
- Platform-specific `Indication` / `rememberRipple`: Rejected — Android-only API; breaks commonMain sharing.
- `Modifier.clickable` with custom Indication: Deferred — `Indication` API in commonMain is limited; the `detectTapGestures` approach is simpler and sufficient.

---

## Decision 3: Design Token Architecture

**Decision**: Custom `GallrTheme` composable wrapping `MaterialTheme`, providing monochrome color scheme and serif typography. Token values stored in plain Kotlin objects (`GallrColors`, `GallrTypography`, `GallrMotion`); no `CompositionLocal` needed for this feature since the theme is global and non-variant.

**Rationale**: The Minimalist Monochrome palette is a strict override of the entire Material3 color scheme — there are no dynamic/contextual color variants. Using `MaterialTheme(colorScheme = monochromeColors, typography = serifTypography)` is the idiomatic CMP approach and requires the least code. Custom `CompositionLocal` would add complexity only needed if multiple theme variants coexist.

**Alternatives considered**:
- Full custom `CompositionLocal` token system: Rejected for this feature per YAGNI — no multi-theme requirement exists. Can be added when/if dark mode is introduced.
- Hardcoding colors inline: Rejected — violates DRY; token centralization is required by constitution simplicity principle.

---

## Decision 4: Staggered List Animation

**Decision**: `AnimatedVisibility` with `slideInVertically + fadeIn`, driven by a boolean `visible` flag set to `true` on first composition via `LaunchedEffect(Unit)`. Index-based delay uses `tween(durationMillis = 200, delayMillis = index * 50)`.

**Rationale**: `AnimatedVisibility`, `slideInVertically`, `fadeIn`, and `tween` are all available in CMP commonMain. The `LaunchedEffect(Unit)` pattern (initial `visible = false`, set to `true` after composition) triggers the entry animation reliably. The 200ms duration and 50ms stagger match the design spec (≤200ms per item, ≤100ms between items).

**Alternatives considered**:
- `Animatable` with coroutine-per-item: More precise but verbose; `AnimatedVisibility` is sufficient.
- `LazyColumn` item animations (`animateItem()`): Available in CMP but limited to reorder/add/remove; cannot control initial entry stagger.

---

## Decision 5: Loading State Visual

**Decision**: Replace `CircularProgressIndicator` with an animated horizontal line (`LinearProgressIndicator` in indeterminate mode, styled as a thin 1dp black line spanning the full width).

**Rationale**: Material3's `LinearProgressIndicator` in indeterminate mode produces the exact visual of a pulsing thin line — matches the design spec ("thin animated horizontal line"). Zero custom animation code required. Tinting it black via `MaterialTheme.colorScheme.onBackground` achieves the monochrome aesthetic.

**Alternatives considered**:
- Custom `Animatable` waveform: More control but unnecessary given `LinearProgressIndicator` already produces the right shape.
- Skeleton screens: Too complex for MVP loading state; deferred to future polish.

---

## Decision 6: Filter Chip Styling

**Decision**: Use Material3 `FilterChip` with a custom `FilterChipDefaults.filterChipColors()` that maps: inactive = white background + black border + black text; active = black background + white text. No shape override needed; `RoundedCornerShape(0.dp)` set globally via `MaterialTheme.shapes`.

**Rationale**: Material3 `FilterChip` handles the checked/unchecked state toggle natively. Overriding colors and shape is the minimum change to achieve the spec. Using built-in components preserves accessibility semantics.

**Alternatives considered**:
- Custom `Box`-based chip: Rejects Material3 semantics; more code for the same result.
- `AssistChip`: Not toggleable by default.
