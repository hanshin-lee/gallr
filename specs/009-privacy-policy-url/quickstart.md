# Quickstart: Verify Privacy Policy URL

**Branch**: `009-privacy-policy-url`

## Prerequisites

- Android emulator or iOS simulator running with gallr installed
- `https://gallrmap.com/privacy` deployed and accessible

## Build & Run (Android)

```bash
./gradlew :composeApp:installDebug
adb shell am start -n com.gallr.app/.MainActivity
```

## Build & Run (iOS)

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  -configuration Debug \
  build

xcrun simctl install booted \
  $(find ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/Debug-iphonesimulator -name "iosApp.app" -maxdepth 1 | head -1)
xcrun simctl launch booted com.gallr.app
```

## Acceptance Verification

### SC-001 — Privacy policy page accessible at gallrmap.com

1. In a browser, navigate to `https://gallrmap.com/privacy`
2. Page must return HTTP 200 and display privacy policy content

**Pass**: Page loads with privacy policy content within 3 seconds
**Fail**: 404, blank page, or timeout

### SC-002 — Privacy Policy reachable in 2 taps from any main tab

1. Open the app (starts on FEATURED tab)
2. Locate the info icon ⓘ in the top-right of the app bar — **this is 1 tap away**
3. Tap the icon

**Pass**: Device browser opens `https://gallrmap.com/privacy`
**Fail**: Icon missing, tapping does nothing, or wrong URL opens

### SC-003 — Link works from all tabs

1. Tap the info icon from the FEATURED tab → browser opens `https://gallrmap.com/privacy` → return to app
2. Navigate to LIST tab → tap info icon → browser opens `https://gallrmap.com/privacy` → return to app
3. Navigate to MAP tab → tap info icon → browser opens `https://gallrmap.com/privacy`

**Pass**: All three tabs open the correct URL
**Fail**: Link broken on any tab

### SC-004 — No crash when offline

1. Disable device network (airplane mode or emulator network settings)
2. Tap the info icon

**Pass**: Browser opens and shows a standard offline/network error — the gallr app is unaffected
**Fail**: gallr app crashes or hangs

### App Store Metadata (manual verification)

- **Apple App Store Connect** → gallr app → App Information → Privacy Policy URL: `https://gallrmap.com/privacy`
- **Google Play Console** → gallr → Store Listing → Privacy Policy: `https://gallrmap.com/privacy`
