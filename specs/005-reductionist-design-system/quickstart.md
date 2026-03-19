# Quickstart: Reductionist Design System

**Feature**: 005-reductionist-design-system
**Date**: 2026-03-19

---

## What This Feature Does

Replaces gallr's serif/monochrome visual language (002) with a strictly utilitarian, reductionist system:

- **Typeface**: Inter (neo-grotesque) replaces Playfair Display / Source Serif 4 / JetBrains Mono
- **Palette**: Black/white/gray base + #FF5400 accent for CTAs and active states only
- **Motion**: All decorative animation removed; only immediate press feedback (< 100ms) retained
- **Layout**: 8pt grid spacing system codified as `GallrSpacing` tokens

---

## Files Changed

| File | Change |
|------|--------|
| `GallrColors.kt` | Add `accent`, `ctaPrimary`, `activeIndicator`, `interactionFeedback` tokens |
| `GallrTypography.kt` | Replace all font families with Inter; update all type scale entries |
| `GallrMotion.kt` | Remove stagger/slide constants; retain `pressResponseMs` only |
| `GallrTheme.kt` | Wire updated color and typography tokens |
| `GallrSpacing.kt` | **New file** — 8pt grid spacing constants |
| `composeResources/font/` | Add `Inter_Regular.ttf`, `Inter_Medium.ttf`, `Inter_Bold.ttf` |

---

## Getting the Inter Fonts

Download from [Google Fonts](https://fonts.google.com/specimen/Inter) or [rsms.me/inter](https://rsms.me/inter/):
- Select: Regular (400), Medium (500), Bold (700) — static fonts, not variable
- Place TTF files in: `composeApp/src/commonMain/composeResources/font/`
- Naming convention: `Inter_Regular.ttf`, `Inter_Medium.ttf`, `Inter_Bold.ttf`

---

## Applying the Accent Color

**Permitted uses of `GallrColors.ctaPrimary` / `activeIndicator` / `interactionFeedback`**:

```kotlin
// Primary CTA button
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = GallrColors.ctaPrimary,
        contentColor = GallrColors.white
    )
)

// Active tab indicator
if (isSelected) indicator color = GallrColors.activeIndicator

// Active filter chip
FilterChip(
    selected = isActive,
    colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = GallrColors.activeIndicator,
        selectedLabelColor = GallrColors.white
    )
)
```

**Never use orange for**:
- `backgroundColor` of any screen, card, or sheet
- Decorative borders or dividers
- Text color (fails WCAG AA at small sizes on white)
- Icon tints outside primary CTA context

---

## Grid Usage

```kotlin
// Screen edge padding
Modifier.padding(horizontal = GallrSpacing.screenMargin)

// Card internal padding
Modifier.padding(GallrSpacing.md)

// Section gap
Spacer(modifier = Modifier.height(GallrSpacing.xl))

// Item gap in a list/grid
Arrangement.spacedBy(GallrSpacing.lg)
```

---

## Building & Testing

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS (from Xcode or via KMP tooling)
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

**Visual acceptance criteria** (from spec SC-001 → SC-007):
1. Open each of the 3 tabs — #FF5400 appears only on the active tab indicator and nowhere else in the resting state
2. Tap a filter chip — it activates in orange; all others remain monochrome
3. Locate the primary CTA — it is the sole orange element on that screen
4. Inspect any card — zero border radius, no shadows, no gradients
5. Check body text contrast — black (#000) on white (#FFF) = 21:1 ✅
