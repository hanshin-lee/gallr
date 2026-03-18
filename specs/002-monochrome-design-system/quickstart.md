# Quickstart: Minimalist Monochrome Design System

**Feature**: 002-monochrome-design-system
**Date**: 2026-03-18

---

## Prerequisites

- Android Studio Meerkat (or later) or IntelliJ IDEA 2024.3+
- Xcode 16.x
- JDK 17+
- Android SDK API 35 (compile), API 24 (min)
- iOS Simulator: iPhone 16 Pro (iOS 18.x) or similar

---

## Font Assets

Before building, download and place TTF files in:

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

**Source**: Google Fonts (open-source, OFL licensed)
- Playfair Display: https://fonts.google.com/specimen/Playfair+Display
- Source Serif 4: https://fonts.google.com/specimen/Source+Serif+4
- JetBrains Mono: https://www.jetbrains.com/lp/mono/

After placing fonts, Compose Resources auto-generates `Res.font.*` accessors at build time.

---

## Build & Run

### Android

```bash
# Build debug APK
./gradlew :composeApp:assembleDebug

# Install on connected device / emulator
./gradlew :composeApp:installDebug

# Or via adb
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
adb shell am start -n com.gallr.app/.MainActivity
```

### iOS Simulator

```bash
# Build the KMP framework (simulator)
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode \
  -PXCODE_CONFIGURATION=Debug \
  -PSDK_NAME=iphonesimulator

# Then open Xcode and run:
xed iosApp/iosApp.xcodeproj
# Select "iPhone 16 Pro" simulator, press Run (⌘R)

# Or via simctl:
xcrun simctl boot "iPhone 16 Pro"
xcodebuild -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 16 Pro' \
  -configuration Debug build
xcrun simctl install booted <path-to-.app>
xcrun simctl launch booted com.gallr.app
```

---

## Verifying the Design System (US1 — Visual Identity)

1. Launch the app.
2. **Color check**: Confirm only black, white, and `#525252` gray appear. No accent
   colors, no gradients.
3. **Typography check**: Exhibition titles use a serif typeface (Playfair Display).
   Dates/metadata use monospaced typeface (JetBrains Mono).
4. **Corner check**: All cards, buttons, and chips have 0px corner radius — sharp edges.
5. **Border check**: All cards have a thin black 1dp border.
6. **Nav check**: Active tab shows bold black indicator, not a colored highlight.

---

## Verifying Press Feedback (US2 — Tactile Interactions)

1. Press and hold an exhibition card on the Featured tab.
   - **Expected**: Card background turns black, text turns white, within 100ms.
2. Tap a filter chip on the List tab.
   - **Expected**: Chip inverts to black fill with white text immediately.
3. Tap the bookmark button.
   - **Expected**: Toggles between outlined and filled states instantly.

---

## Verifying Loading & Empty States (US4 — Animated States)

1. In Android Studio, enable network throttling or disable network on the simulator.
2. Navigate to the Featured tab.
   - **Expected**: A thin black horizontal line pulses across the top (no spinner).
3. Apply filters that return zero results on the List tab.
   - **Expected**: Large serif text announces empty state; outline action button appears.
4. Re-enable network, navigate to Featured tab.
   - **Expected**: Cards appear with staggered slide-in from below (8dp), 50ms apart.

---

## Verifying Staggered List Animation (US4 — FR-011)

1. Clear app data / reload exhibitions on Featured tab.
2. Observe card entry on the first load.
   - **Expected**: Each card slides in from 8dp below, fading to full opacity, 200ms per
     card, 50ms delay between cards. Total animation for N cards = (N-1)×50 + 200ms.
   - **Pass criterion**: Animation completes smoothly at 60fps; no dropped frames visible.

---

## Running Tests

```bash
# Common (shared) unit tests
./gradlew :composeApp:testDebugUnitTest

# All checks
./gradlew check
```

Tests for this feature: None (UI-only feature; visual verification is manual per spec
Success Criteria SC-001 through SC-006).
