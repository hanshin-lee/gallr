# Quickstart: Verify My List and List Filtering

**Branch**: `010-mylist-filter-map`

## Prerequisites

- Android emulator or iOS simulator running with gallr installed
- At least 2 exhibitions loaded in the app (requires network connection)

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

---

## Acceptance Verification

### SC-003 — "MYLIST" label replaces "FILTERED" everywhere

1. Open the app and navigate to the MAP tab
2. Inspect the toggle buttons at the top of the MAP screen

**Pass**: Left button is labelled "MYLIST" (not "FILTERED"); right button is labelled "ALL"
**Fail**: Any visible instance of "FILTERED" text in the MAP tab

---

### SC-001 + FR-002 — MYLIST mode shows only bookmarked exhibitions

1. On the LIST tab, tap ■/□ on exactly 2 exhibitions to bookmark them
2. Navigate to the MAP tab
3. Confirm the toggle shows "MYLIST" selected (default mode)
4. Count the map pins

**Pass**: Exactly 2 pins visible (only the 2 bookmarked exhibitions, assuming both have location data)
**Fail**: More or fewer pins than bookmarked exhibitions with locations

---

### FR-003 — ALL mode shows all exhibitions

1. While on the MAP tab (MYLIST active with 2 bookmarked), tap the "ALL" button
2. Count the map pins

**Pass**: All exhibitions with location data appear as pins (more than the 2 bookmarked)
**Fail**: Pin count unchanged after switching to ALL

---

### FR-008 — Empty state when no bookmarks

1. Remove all bookmarks (tap ■ on each bookmarked exhibition to deselect)
2. Navigate to MAP tab (or stay there)
3. Confirm MYLIST mode is active

**Pass**: Map shows no pins and displays "Add exhibitions to your list to see them here"
**Fail**: Pins still show, or no message displayed

---

### SC-001 — MYLIST reflects bookmark within 1 second

1. Stay on the MAP tab in MYLIST mode (0 or more pins showing)
2. Switch to LIST tab, tap ■ on one exhibition
3. Switch back to MAP tab within 1 second
4. Observe pin count

**Pass**: The newly bookmarked exhibition appears as a pin within 1 second of returning to MAP
**Fail**: Pin does not appear, or requires a refresh/reload

---

### SC-002 — Filter chips update list within 300ms

1. Navigate to LIST tab
2. Tap "FEATURED" filter chip
3. Observe list update

**Pass**: List immediately narrows to show only featured exhibitions (visually instant, ≤300ms)
**Fail**: List doesn't update, or takes noticeable time

---

### FR-004 + FR-005 — Filter chips AND logic and deselect

1. On LIST tab, tap "FEATURED" chip → list narrows
2. Tap "OPENING THIS WEEK" chip → list narrows further (AND logic)
3. Tap "FEATURED" chip again to deselect → list widens to "OPENING THIS WEEK" only
4. Tap "OPENING THIS WEEK" to deselect → full list restored

**Pass**: Each combination narrows/widens correctly; deselecting all returns full list
**Fail**: Multiple chips show broader results, or full list doesn't restore on deselect

---

### FR-004 (edge case) — Empty filter result

1. On LIST tab, select filter chips unlikely to match simultaneously (e.g., FEATURED + CLOSING THIS WEEK if no such exhibitions exist)

**Pass**: List shows "No exhibitions match the current filters." with "Clear Filters" action (or equivalent)
**Fail**: Empty screen with no guidance

---

### FR-002 + FR-006 — Bookmark toggle immediately updates MYLIST map

1. On MAP tab, select MYLIST mode (note current pin count, e.g. 2)
2. Navigate to LIST tab
3. Tap ■ on a new (unbookmarked) exhibition
4. Return to MAP tab

**Pass**: New pin appears for the newly bookmarked exhibition (pin count increases by 1)
**Fail**: New pin missing until manual refresh

---

### SC-004 — 100% of bookmarked exhibitions with locations appear

1. Bookmark 5 exhibitions on LIST tab (choose ones you know have venue info)
2. Navigate to MAP tab → MYLIST mode
3. Count pins

**Pass**: All 5 exhibitions appear as pins (exhibitions without location data are correctly excluded — check that the exhibition has a venue with coordinates)
**Fail**: Any bookmarked exhibition with location data is missing from the map
