# Quickstart: Three-Tab Exhibition Discovery Navigation

**Branch**: `001-exhibition-tabs` | **Date**: 2026-03-18

Use this guide to verify the feature works end-to-end after implementation.

---

## Prerequisites

- Android Studio Ladybug (2024.2+) with the Kotlin Multiplatform plugin installed
- Xcode 15+ (for iOS target)
- JDK 17+
- A physical device or emulator (Android API 26+ / iOS Simulator 14.0+)

---

## Build & Run

### Android

```bash
# From repository root
./gradlew :composeApp:assembleDebug

# Or run directly on connected device/emulator via Android Studio:
# Run > Run 'composeApp' > select Android target
```

### iOS

```bash
# Build the shared framework first
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Then open the Xcode project and run:
open iosApp/iosApp.xcodeproj
# In Xcode: Product > Run (⌘R) on selected simulator
```

---

## Verification Steps

Run these steps after a successful build. Each step maps to a user story in the spec.

### US1 — Featured Tab loads exhibitions

1. Launch the app.
2. **Expect**: App opens to the **Featured** tab (default).
3. **Expect**: A list of at least one exhibition card appears, each showing name, venue,
   city, and opening/closing dates.
4. Kill network access (airplane mode).
5. Force-quit and relaunch the app.
6. **Expect**: An error state with a retry button appears (not a blank screen).

### US2 — List Tab filters work

1. Tap the **List** tab.
2. **Expect**: Filter toggles are visible (Region, Featured, Editor's Picks,
   Opening This Week, Closing This Week).
3. **Expect**: A scrollable list of exhibitions is visible below the filters.
4. Toggle **Opening This Week** on.
5. **Expect**: The exhibition list updates immediately to show only matching exhibitions.
6. Tap the **Map** tab.
7. Ensure the map is in **Filtered** mode.
8. **Expect**: Only exhibitions from step 5 appear as markers.
9. Return to **List** tab.
10. **Expect**: The **Opening This Week** toggle is still on (filter state persisted).

### US3 — Map Tab modes work

1. Navigate to the **Map** tab in **Filtered** mode (with at least one active filter).
2. **Expect**: Only filtered exhibitions appear as markers.
3. Switch to **All** mode.
4. **Expect**: All exhibitions with coordinates appear as markers.
5. Tap any marker.
6. **Expect**: A summary card appears with the exhibition's name, venue, and dates.
7. Switch rapidly between Filtered and All several times.
8. **Expect**: Map stabilises on the last selected mode; no duplicate markers or crashes.
9. With no active filters, switch to Filtered mode.
10. **Expect**: All exhibitions are shown (default FilterState matches everything).

### US4 — Bookmarking persists

1. From the **Featured** tab, tap the bookmark icon on any exhibition card.
2. **Expect**: Icon changes to filled/active state immediately.
3. Tap the same bookmark icon again.
4. **Expect**: Icon returns to inactive state.
5. Bookmark an exhibition again (step 1).
6. Navigate to the **List** tab and find the same exhibition in the results.
7. **Expect**: The bookmark icon is shown in its active state.
8. Force-quit and relaunch the app.
9. **Expect**: The bookmarked exhibition still shows the active bookmark icon.

---

## Common Issues

| Symptom | Likely Cause | Fix |
|---------|--------------|-----|
| Featured tab blank on first launch | API base URL not configured | Set the API URL in the dev environment config |
| iOS build fails on `MapView.ios.kt` | `actual` implementation missing | Implement `MapView.ios.kt` in `composeApp/src/iosMain/` |
| Filter state lost after tab switch | `TabsViewModel` not root-scoped | Ensure `TabsViewModel` is created at the `App.kt` composable level, not inside a tab |
| Bookmark not persisted after restart | DataStore path not initialised | Verify `createDataStore()` `actual` is providing a valid writable file path |
| Map shows no markers | Exhibitions have null coordinates in test data | Use seed data that includes valid `latitude`/`longitude` values |
