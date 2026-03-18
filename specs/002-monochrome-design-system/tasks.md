# Tasks: Minimalist Monochrome Design System

**Input**: Design documents from `/specs/002-monochrome-design-system/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ui-components.md ✅

**Organization**: Tasks grouped by user story. US1 (visual identity) is the MVP — all
other stories build on it. No tests required (pure visual UI feature per constitution
exemption for platform UI layers).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story this task belongs to (US1–US4)

---

## Phase 1: Setup

**Purpose**: Download and place font assets that CMP compose-resources will bundle.

- [X] T001 Download 7 TTF font files (PlayfairDisplay Regular/Bold/Italic/BoldItalic, SourceSerif4 Regular/Bold, JetBrainsMono Regular) from Google Fonts into `composeApp/src/commonMain/composeResources/font/` — exact filenames: `PlayfairDisplay_Regular.ttf`, `PlayfairDisplay_Bold.ttf`, `PlayfairDisplay_Italic.ttf`, `PlayfairDisplay_BoldItalic.ttf`, `SourceSerif4_Regular.ttf`, `SourceSerif4_Bold.ttf`, `JetBrainsMono_Regular.ttf`

**Checkpoint**: `./gradlew :composeApp:generateCommonMainResourceAccessors` completes without error and `Res.font.PlayfairDisplay_Regular` is accessible.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Theme infrastructure that MUST be in place before any user story UI work.
All four theme files are independent of each other; `GallrTheme.kt` depends on all three.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T002 [P] Create `GallrColors.kt` object with full monochrome `ColorScheme` mapping (background=#FFFFFF, foreground=#000000, muted=#F5F5F5, mutedForeground=#525252, border=#000000, borderLight=#E5E5E5, accent=#000000, accentForeground=#FFFFFF) in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/GallrColors.kt`
- [X] T003 [P] Create `GallrTypography.kt` object: load `Res.font.PlayfairDisplay_*` for display/headline/title styles, `Res.font.SourceSerif4_*` for body/titleSmall styles, `Res.font.JetBrainsMono_Regular` for label styles; return `Typography(...)` with all 15 TextStyles using correct sp sizes per data-model in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/GallrTypography.kt`
- [X] T004 [P] Create `GallrMotion.kt` object with constants: `pressDurationMs = 100`, `staggeredItemDurationMs = 200`, `staggeredItemDelayMs = 50`, `staggeredSlideOffsetDp = 8f` in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/GallrMotion.kt`
- [X] T005 Create `GallrTheme.kt` composable: call `gallrTypography()` for fonts, construct `gallrColorScheme()`, set all `Shapes` to `RectangleShape` (0dp radius everywhere), wrap `MaterialTheme(colorScheme, typography, shapes, content)` in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/theme/GallrTheme.kt` — depends on T002, T003, T004
- [X] T006 Update `App.kt`: replace `MaterialTheme { ... }` with `GallrTheme { ... }` in `composeApp/src/commonMain/kotlin/com/gallr/app/App.kt` — depends on T005

**Checkpoint**: `./gradlew :composeApp:assembleDebug` passes. App launches with white background and no shape/color regressions.

---

## Phase 3: User Story 1 — Cohesive Visual Identity (Priority: P1) 🎯 MVP

**Goal**: Every screen shows black/white palette, serif exhibition titles, mono dates, sharp-edged bordered cards, and a bold black underline nav indicator.

**Independent Test**: Launch app from scratch; all three tabs display black-and-white palette, zero corner radius, serif exhibition titles, and sharp-edged cards.

### Implementation for User Story 1

- [X] T007 [P] [US1] Create `GallrNavigationBar.kt`: `@Composable fun GallrNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit, modifier: Modifier = Modifier)`. Background = `MaterialTheme.colorScheme.background`. Active tab shows a 4dp top `Box` border in `MaterialTheme.colorScheme.primary` (black). Labels use `MaterialTheme.typography.labelLarge` (JetBrainsMono, uppercase). No elevation, no ripple. Min touch target 44dp per tab in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/GallrNavigationBar.kt`
- [X] T008 [US1] Update `App.kt`: replace `NavigationBar { NavigationBarItem(...) }` block with `GallrNavigationBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })` — depends on T007 in `composeApp/src/commonMain/kotlin/com/gallr/app/App.kt`
- [X] T009 [P] [US1] Update `ExhibitionCard.kt`: replace `Card(...)` with `Surface(border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RectangleShape)`. Exhibition name → `MaterialTheme.typography.titleLarge` (PlayfairDisplay 24sp). Venue/city → `MaterialTheme.typography.labelMedium` (JetBrainsMono) with `letterSpacing = 0.1.em`, uppercase text. Date range → `MaterialTheme.typography.labelMedium`. Add `1.dp Divider` in `MaterialTheme.colorScheme.outlineVariant` (borderLight) between metadata block and date row. Long title: `maxLines = 2, overflow = TextOverflow.Ellipsis` in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/ExhibitionCard.kt`
- [X] T010 [P] [US1] Update `BookmarkButton.kt`: replace emoji text with a `Text` using bookmarked="■" / not bookmarked="□" (or equivalent outlined/filled characters) in `MaterialTheme.colorScheme.onBackground` (black). Min 44×44dp touch target via `Modifier.sizeIn(minWidth = 44.dp, minHeight = 44.dp)` in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/BookmarkButton.kt`
- [X] T011 [P] [US1] Update filter chip block in `ListScreen.kt`: wrap each `FilterChip` with `colors = FilterChipDefaults.filterChipColors(containerColor = Color.White, selectedContainerColor = Color.Black, labelColor = Color.Black, selectedLabelColor = Color.White, disabledContainerColor = Color.White)` and `shape = RectangleShape`. Label text style = `MaterialTheme.typography.labelLarge`. Remove existing `Divider()` call — a styled section separator will be added in US3 in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt`
- [X] T012 [P] [US1] Update `MapScreen.kt`: apply `MaterialTheme.typography.titleLarge` (PlayfairDisplay) to the screen title/header text; ensure any `Card` or `Surface` uses `RectangleShape` and black border. Keep map renderer placeholder unchanged; style only header and controls in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/map/MapScreen.kt`

**Checkpoint**: US1 independently verifiable — all three tabs show monochrome palette, serif titles, zero corner radius, black bordered cards, black underline nav indicator.

---

## Phase 4: User Story 2 — Tactile Interactions & Press Feedback (Priority: P2)

**Goal**: Interactive elements (cards, filter chips, bookmark button) invert colors (black bg / white text) within 100ms of press.

**Independent Test**: Press and hold an exhibition card on Featured tab; card inverts within 100ms. Tap a filter chip; it inverts immediately. Tap bookmark; it toggles states instantly.

### Implementation for User Story 2

- [X] T013 [US2] Add press inversion to `ExhibitionCard.kt`: introduce `var isPressed by remember { mutableStateOf(false) }`. Attach `Modifier.pointerInput(Unit) { detectTapGestures(onPress = { isPressed = true; tryAwaitRelease(); isPressed = false }) }` to the card `Surface`. When `isPressed`, flip background to `MaterialTheme.colorScheme.onBackground` (black) and all text colors to `MaterialTheme.colorScheme.background` (white) via `animateColorAsState(targetValue = ..., animationSpec = tween(GallrMotion.pressDurationMs))`. Pass pressed colors down to child `Text` and `Divider` composables in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/ExhibitionCard.kt`
- [X] T014 [P] [US2] Add press state to `BookmarkButton.kt`: use `detectTapGestures(onPress = { ... })` to show momentary inverted state (black bg square behind icon) for ≤100ms before toggle completes. Call `onToggle()` on release in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/BookmarkButton.kt`
- [X] T015 [P] [US2] Verify `FilterChip` inversion in `ListScreen.kt`: confirm `selectedContainerColor = Color.Black, selectedLabelColor = Color.White` produces immediate visual state change on tap (Material3 FilterChip handles the toggle natively — no extra press detection needed). Add a note comment confirming this satisfies FR-005 for chips in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt`

**Checkpoint**: US2 independently verifiable — press any card, chip, or bookmark and see color inversion within 100ms.

---

## Phase 5: User Story 3 — Editorial Exhibition Card Layout (Priority: P2)

**Goal**: Cards read as editorial spreads: dominant serif name, small-caps mono labels, hairline section separator, 4dp black section header rule.

**Independent Test**: View Featured tab with one exhibition loaded; card shows dominant serif name, small-caps mono venue label, mono dates, and a hairline divider between sections.

### Implementation for User Story 3

- [X] T016 [US3] Refine `ExhibitionCard.kt` layout: ensure exhibition name (`titleLarge`, PlayfairDisplay, 24sp) is the visually dominant element with `fontWeight = FontWeight.Bold`. Venue label: `style = MaterialTheme.typography.labelMedium, fontFeatureSettings = "smcp"` (small caps — or use `.uppercase()` transform for JetBrains Mono). City label: same style as venue but slightly reduced alpha. Verify `Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)` renders between the title+venue block and the date block. Padding: `16.dp` internal padding for generosity in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/ExhibitionCard.kt`
- [X] T017 [P] [US3] Add section header rule to `FeaturedScreen.kt`: above the `LazyColumn`, render a `Text("Featured", style = MaterialTheme.typography.labelLarge)` section label followed by a `Divider(thickness = 4.dp, color = MaterialTheme.colorScheme.onBackground)` (4dp black rule separating header from card list) with `16.dp` horizontal padding in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/featured/FeaturedScreen.kt`
- [X] T018 [P] [US3] Add section header rule to `ListScreen.kt`: above the card list area (below filter chips), add `Divider(thickness = 4.dp, color = MaterialTheme.colorScheme.onBackground)` (replaces the removed plain `Divider()` from T011) in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt`

**Checkpoint**: US3 independently verifiable — card shows dominant serif name, small-caps mono venue, hairline divider between sections, 4dp black section rule above list.

---

## Phase 6: User Story 4 — Animated State Transitions (Priority: P3)

**Goal**: Loading shows a thin pulsing black line; empty state shows bold serif text + outline action; list items appear with staggered slide-in from 8dp below over 200ms with 50ms inter-item delay.

**Independent Test**: Simulate slow/no network on Featured tab; loading state shows thin animated horizontal line (not a spinner). Force empty results; empty state shows large serif text + outline button. Reload; items appear with staggered reveal.

### Implementation for User Story 4

- [X] T019 [P] [US4] Create `GallrLoadingState.kt`: `@Composable fun GallrLoadingState(modifier: Modifier = Modifier)`. Render `LinearProgressIndicator(modifier = modifier.fillMaxWidth().height(1.dp), color = MaterialTheme.colorScheme.onBackground, trackColor = MaterialTheme.colorScheme.outlineVariant)` — 1dp height, black progress bar, light gray track, full width in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/GallrLoadingState.kt`
- [X] T020 [P] [US4] Create `GallrEmptyState.kt`: `@Composable fun GallrEmptyState(message: String, actionLabel: String, onAction: () -> Unit, modifier: Modifier = Modifier)`. Layout: vertically centered `Column`. `Text(message, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)`. Below: `OutlinedButton(onClick = onAction, shape = RectangleShape, border = BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground)) { Text(actionLabel.uppercase(), style = MaterialTheme.typography.labelLarge) }` in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/components/GallrEmptyState.kt`
- [X] T021 [US4] Update `FeaturedScreen.kt`: (1) Replace `CircularProgressIndicator` with `GallrLoadingState(modifier = Modifier.fillMaxWidth())` at top of screen. (2) Replace empty-state `Text` with `GallrEmptyState(message = "No featured exhibitions right now.", actionLabel = "Refresh", onAction = { viewModel.loadFeaturedExhibitions() })`. (3) Wrap `LazyColumn` items in `AnimatedVisibility`: introduce `var listVisible by remember { mutableStateOf(false) }`, `LaunchedEffect(Unit) { listVisible = true }`, each item wrapped in `AnimatedVisibility(visible = listVisible, enter = slideInVertically(animationSpec = tween(GallrMotion.staggeredItemDurationMs, delayMillis = index * GallrMotion.staggeredItemDelayMs)) { it } + fadeIn(animationSpec = tween(GallrMotion.staggeredItemDurationMs, delayMillis = index * GallrMotion.staggeredItemDelayMs)))` — use `itemsIndexed` instead of `items` to get index in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/featured/FeaturedScreen.kt`
- [X] T022 [US4] Update `ListScreen.kt`: same three changes as T021 — replace loading indicator with `GallrLoadingState`, replace empty `Text` with `GallrEmptyState(message = "No exhibitions match the current filters.", actionLabel = "Clear Filters", onAction = { viewModel.updateFilter { ExhibitionFilter() } })`, wrap `LazyColumn` items in staggered `AnimatedVisibility` using `itemsIndexed` and same `tween` animation spec from `GallrMotion` in `composeApp/src/commonMain/kotlin/com/gallr/app/ui/tabs/list/ListScreen.kt`

**Checkpoint**: US4 independently verifiable — loading shows thin black line, empty state shows serif text + outline button, list reveal shows staggered slide-in animation.

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Final consistency audit, build verification, and visual QA.

- [X] T023 Visual audit: open both Android and iOS builds side by side; confirm all three tabs show (a) 0dp corner radius everywhere, (b) no non-monochrome colors, (c) serif names in all cards, (d) mono dates/labels, (e) black underline nav indicator — fix any inconsistencies found across `composeApp/src/commonMain/kotlin/com/gallr/app/`
- [X] T024 [P] Build verification: run `./gradlew :composeApp:assembleDebug` and confirm clean build with no warnings about missing resources or unresolved font references
- [X] T025 [P] Run quickstart.md US1–US4 verification checklists on Android device/emulator: document pass/fail against SC-001 through SC-006
- [X] T026 [P] Run quickstart.md US1–US4 verification checklists on iOS Simulator (iPhone 16 Pro): document pass/fail against SC-001 through SC-006

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately (manual font download)
- **Foundational (Phase 2)**: Depends on T001 (fonts must exist before GallrTypography.kt can compile) — BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Phase 2 completion (needs GallrTheme)
- **US2 (Phase 4)**: Depends on Phase 3 (press state added to US1 components)
- **US3 (Phase 5)**: Depends on Phase 3 (refines US1 card layout)
- **US4 (Phase 6)**: Depends on Phase 2 (needs GallrMotion), can start after foundational
- **Polish (Phase N)**: Depends on all desired phases complete

### User Story Dependencies

- **US1 (P1)**: Depends only on Foundational — the MVP. **Start here.**
- **US2 (P2)**: Depends on US1 (modifies same ExhibitionCard and BookmarkButton components)
- **US3 (P2)**: Depends on US1 (refines same ExhibitionCard layout)
- **US4 (P3)**: Depends on Foundational (GallrMotion); can overlap with US2/US3

### Within Each User Story

- All [P]-marked tasks touch different files — run in parallel
- T008 depends on T007 (App.kt uses GallrNavigationBar)
- T013 modifies ExhibitionCard; should not run in parallel with T016

### Parallel Opportunities

```bash
# Phase 2 — run all four in parallel:
T002 GallrColors.kt
T003 GallrTypography.kt
T004 GallrMotion.kt
# then:
T005 GallrTheme.kt  (after T002, T003, T004)
T006 App.kt         (after T005)

# Phase 3 — run these in parallel:
T007 GallrNavigationBar.kt
T009 ExhibitionCard.kt (US1 token application)
T010 BookmarkButton.kt
T011 ListScreen.kt (filter chip colors)
T012 MapScreen.kt
# then:
T008 App.kt (after T007)

# Phase 4 — T014 and T015 can run parallel to T013:
T013 ExhibitionCard.kt (press inversion)
T014 BookmarkButton.kt (press state)
T015 ListScreen.kt (filter chip verification)

# Phase 5 — T017 and T018 can run parallel to T016:
T016 ExhibitionCard.kt (layout refinement)
T017 FeaturedScreen.kt (section rule)
T018 ListScreen.kt (section rule)

# Phase 6 — T019 and T020 in parallel:
T019 GallrLoadingState.kt
T020 GallrEmptyState.kt
# then:
T021 FeaturedScreen.kt (after T019, T020)
T022 ListScreen.kt (after T019, T020)
```

---

## Implementation Strategy

### MVP First (US1 Only — Phases 1–3)

1. T001: Place fonts
2. T002–T006: Build theme infrastructure
3. T007–T012: Apply visual identity to all three tabs
4. **STOP and VALIDATE**: Visual audit — monochrome palette, serif type, zero radius, black nav
5. Demo if ready — this is the most visible improvement

### Incremental Delivery

1. Phases 1–3 → US1 visual identity ✅ Demo
2. Phase 4 → US2 press feedback ✅ Demo
3. Phase 5 → US3 editorial card layout ✅ Demo
4. Phase 6 → US4 animated states ✅ Demo
5. Phase N → Polish & build verification

---

## Notes

- T001 is a manual step (downloading files) — not automatable
- Fonts use `@Composable` `Font(Res.font.X)` — `FontFamily` creation MUST happen inside `GallrTheme` composable, not at top-level
- Press state uses `detectTapGestures`, NOT `collectIsPressedAsState()` — CMP bug #3417 on iOS
- `RectangleShape` = `RoundedCornerShape(0.dp)` in Compose; use `RectangleShape` for clarity
- `GallrMotion` constants are plain `Int`/`Float` — importable without `@Composable`
- `itemsIndexed` required (not `items`) for staggered animation index access in LazyColumn
- All 26 tasks verified against checklist format: checkbox + ID + optional [P] + optional [Story] + description with file path
