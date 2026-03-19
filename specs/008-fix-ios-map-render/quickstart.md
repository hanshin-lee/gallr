# Quickstart: Verify iOS Map Fix

**Branch**: `008-fix-ios-map-render`

## Prerequisites

- Xcode installed with iPhone simulator available
- iosApp SPM packages resolved (open `iosApp/iosApp.xcodeproj` in Xcode once if not already done)

## Build & Run

```bash
# Build for booted simulator
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  -configuration Debug \
  build

# Install and launch
xcrun simctl install booted \
  $(find ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/Debug-iphonesimulator -name "iosApp.app" -maxdepth 1 | head -1)

xcrun simctl launch booted com.gallr.app
```

## Acceptance Verification

### SC-001 — Map renders within 3 seconds

1. Launch the app
2. Tap **MAP** tab
3. A Naver map centred on Seoul must be visible within 3 seconds

**Pass**: Map tiles render, Seoul area visible
**Fail**: Blank white area below the FILTERED/ALL toggle

### SC-002 — Map persists across tab navigation

1. Open MAP tab (confirm map renders)
2. Tap **FEATURED** tab
3. Tap **MAP** tab again

**Pass**: Map is still fully rendered on return
**Fail**: Map goes blank on re-entry

### SC-003 — Map persists after background/foreground

1. Open MAP tab (confirm map renders)
2. Press Home button (background app)
3. Re-open app

**Pass**: Map renders on MAP tab after foreground
**Fail**: Map goes blank after returning from background

### SC-005 — Map fills full available area

1. Open MAP tab on a small device (e.g. iPhone SE simulator)
2. Open MAP tab on a large device (e.g. iPhone 16 Plus simulator)

**Pass**: Map fills the full area below controls on both sizes with no blank strips
**Fail**: White strips or partial rendering visible

## Check for Regressions

Confirm no Metal errors in simulator logs after navigating to MAP tab:

```bash
xcrun simctl spawn booted log show \
  --predicate 'process == "iosApp"' \
  --last 30s | grep -i "CAMetalLayer\|setDrawableSize\|nextDrawable"
```

**Pass**: No output (no Metal errors)
**Fail**: Lines containing `setDrawableSize width=0.000000` or `nextDrawable returning nil`
