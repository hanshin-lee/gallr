# Quickstart: Naver Maps Integration

**Branch**: `004-naver-map-api` | **Date**: 2026-03-19

---

## Prerequisites

- Android Studio Meerkat (or later) with KMP plugin
- Xcode 16+ with SPM support
- Naver Cloud Platform account with Maps API enabled
- NCP Key ID for `com.gallr.app` — obtain from [console.ncloud.com](https://console.ncloud.com)

---

## Step 1: Add Naver Maps Maven Repository (Android)

In `settings.gradle.kts`, add the Naver Maps repo inside `dependencyResolutionManagement.repositories`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google { /* existing */ }
        mavenCentral()
        maven("https://repository.map.naver.com/archive/maven")  // ADD THIS
    }
}
```

---

## Step 2: Add Android Dependencies

In `composeApp/build.gradle.kts`, add to the `androidMain.dependencies` block:

```kotlin
androidMain.dependencies {
    // existing ...
    implementation("com.naver.maps:map-sdk:3.23.0")
    implementation("io.github.fornewid:naver-map-compose:1.8.1")
}
```

---

## Step 3: Configure cinterop for iOS

**3a.** Create `composeApp/src/nativeInterop/cinterop/NMapsMap.def`:

```
language = Objective-C
modules = NMapsMap
package = NMapsMap
```

**3b.** In `composeApp/build.gradle.kts`, add cinterop config for all three iOS targets:

```kotlin
listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
        baseName = "composeApp"
        isStatic = true
    }
    iosTarget.compilations.getByName("main") {
        val NMapsMap by cinterops.creating {
            definitionFile.set(project.file("src/nativeInterop/cinterop/NMapsMap.def"))
        }
    }
}
```

---

## Step 4: Add Naver Maps iOS SDK via SPM

1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. **File → Add Package Dependencies…**
3. Enter URL: `https://github.com/navermaps/SPM-NMapsMap`
4. Select the latest release → **Add Package**.
5. Add `NMapsMap` to the `iosApp` target.

---

## Step 5: Inject Client ID

**Android** — in `composeApp/src/androidMain/AndroidManifest.xml`, inside `<application>`:

```xml
<meta-data
    android:name="com.naver.maps.map.NCP_KEY_ID"
    android:value="YOUR_NCP_KEY_ID" />
```

**iOS** — in `iosApp/iosApp/iOSApp.swift`, add an `init()` to the App struct:

```swift
import NMapsMap

@main
struct iOSApp: App {
    init() {
        NMFAuthManager.shared().ncpKeyId = "YOUR_NCP_KEY_ID"
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

> ⚠️ Do not commit real credentials to source control. Use a local config mechanism (e.g., `local.properties` + `BuildConfig` for Android; a `.xcconfig` excluded from git for iOS).

---

## Step 6: Implement MapView.android.kt

Replace the stub in `composeApp/src/androidMain/kotlin/com/gallr/app/ui/tabs/map/MapView.android.kt` with the `NaverMap` + `Marker` composable implementation (see tasks).

---

## Step 7: Implement MapView.ios.kt

Replace the stub in `composeApp/src/iosMain/kotlin/com/gallr/app/ui/tabs/map/MapView.ios.kt` with the `UIKitView` + `NMFMapView` implementation (see tasks).

---

## Step 8: Sync and Build

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS — after Gradle sync, build from Xcode or:
./gradlew :composeApp:iosSimulatorArm64Binaries
xcodebuild -project iosApp/iosApp.xcodeproj \
           -scheme iosApp \
           -destination "id=<simulator-udid>" \
           build
```

---

## Verify

1. Launch the app on Android emulator or iOS simulator.
2. Navigate to the **Map** tab.
3. A real Naver Maps tile should render — no green placeholder, no "Map SDK: TBD" text.
4. Exhibition pins appear at their venue coordinates.
5. Tapping a pin shows the exhibition name, venue, and dates.
6. Toggle FILTERED/ALL and verify pin count changes correctly.

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|-------------|-----|
| Map renders but no tiles | Invalid / missing NCP Key ID | Check AndroidManifest meta-data or iOSApp.swift init |
| iOS build fails: `NMapsMap not found` | SPM package not resolved | In Xcode: File → Packages → Resolve Package Versions |
| iOS Gradle fails: cinterop error | NMapsMap framework not in search path | Ensure SPM package is linked to the target before running Gradle |
| Markers appear but tap does nothing | `onMarkerTap` not wired in actual | Confirm marker click/touch handler calls `onMarkerTap(pin)` |
