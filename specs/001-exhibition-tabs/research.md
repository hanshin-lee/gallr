# Research: Three-Tab Exhibition Discovery Navigation

**Branch**: `001-exhibition-tabs` | **Date**: 2026-03-18
**Input**: Unknowns from Technical Context in plan.md

---

## 1. KMP Project Module Structure

**Decision**: Two-module pattern — `shared/` (pure Kotlin business logic) + `composeApp/`
(Compose Multiplatform UI).

**Rationale**: Enforces the Shared-First constitution principle at the build-system level.
`shared/` carries zero Compose dependencies — just data models, repository interfaces,
implementations, and networking. `composeApp/` holds all UI. This separation is the
Google-endorsed 2025 KMP template structure and scales cleanly as the app grows.

**Alternatives considered**:
- Single `composeApp/` module with package-based separation: simpler to scaffold but
  doesn't enforce the business logic / UI boundary at compile time; violates Principle VI.
- Three separate modules (`shared/`, `androidApp/`, `iosApp/`): over-engineered for a
  Compose Multiplatform project where UI is already shared in commonMain.

---

## 2. Local Storage for Bookmarks

**Decision**: AndroidX DataStore Preferences (`androidx.datastore:datastore-preferences-core`
1.1.0+).

**Rationale**: Bookmarks are a `Set<String>` of exhibition IDs — a key-value store is
sufficient. DataStore is coroutine-native (returns `Flow<Set<String>>`), KMP-stable as
of 1.1.0, and avoids the complexity of SQL schema migrations. It integrates directly with
the `shared/` module with a simple `expect fun createDataStore()` for the platform file
path.

**Alternatives considered**:
- SQLDelight 2.0+: production-proven for KMP, type-safe SQL, but significantly more setup
  (schema files, migrations, generated code) for a feature that only needs a set of IDs.
  Adopt if bookmark data needs richer queries in the future.
- Room-KMP: similar overhead to SQLDelight; still in production stabilisation for KMP.
- Multiplatform-Settings: lightweight, but less integrated with coroutines/Flow than
  DataStore.

---

## 3. Shared Filter State Across Three Tabs

**Decision**: AndroidX ViewModel (`androidx.lifecycle:lifecycle-viewmodel-compose` 2.8.0+)
with `MutableStateFlow<FilterState>`. A single `TabsViewModel` is scoped to the root
composable and shared across all three tabs.

**Rationale**: AndroidX ViewModel 2.8.0+ has stable KMP support (not the deprecated
JetBrains fork). A single root-scoped ViewModel holding `FilterState` as a `StateFlow`
is the simplest design that survives Android configuration changes and keeps filter state
consistent across tab navigation without a separate state management library.

**Alternatives considered**:
- Pure `StateFlow` holder (no ViewModel): loses Android lifecycle awareness and risks
  state loss on configuration change.
- Decompose or Voyager (KMP navigation libraries): adds a full navigation dependency for
  a feature that only needs three bottom-nav tabs with shared state; violates Principle III.

---

## 4. Map Integration — Pluggable Provider

**Decision**: `expect fun MapView(...)` composable in `composeApp/commonMain` with
`actual fun` implementations in `androidMain` and `iosMain`.

**Rationale**: Since the map provider is intentionally TBD (FR-017), the expect/actual
Composable pattern is the correct abstraction — it defers the platform SDK choice while
defining the interface contract now. The `MapView` composable receives a list of
`ExhibitionMapPin` objects and exposes a tap callback; the platform implementation chooses
the rendering library. On Android, Google Maps Compose or Mapbox v9 are both viable.
On iOS, MapKit (via `UIKitView` interop) or Mapbox v9 are viable. Mapbox v10 is NOT
recommended — its Swift-only SDK has poor KMP interop.

**Alternatives considered**:
- moko-maps library: abstracts the map behind a shared interface, but is less flexible for
  custom marker rendering and adds a third-party dependency.
- A shared Kotlin interface with a `lateinit` factory: more verbose than expect/actual for
  a UI component.

---

## 5. Networking — Ktor Client

**Decision**: Ktor Client 2.9+ with:
- `OkHttp` engine in `androidMain`
- `Darwin` engine in `iosMain`
- `ContentNegotiation` + `kotlinx.serialization` (JSON) in `commonMain`
- `Logging` plugin in `commonMain`

**Rationale**: Ktor is the canonical KMP HTTP client. Platform engines are selected per
source set; all request/response logic lives in `commonMain`. The `ContentNegotiation`
plugin + `kotlinx.serialization` provides zero-boilerplate JSON ↔ data class mapping.
The `Logging` plugin satisfies Principle V (Observability) for network calls without
extra code.

**Alternatives considered**:
- Retrofit (Android) + URLSession (iOS): splits networking logic across platforms; violates
  Principle VI.

---

## 6. Stable Versions & Minimum Targets

| Component                         | Version     | Notes                                      |
|-----------------------------------|-------------|--------------------------------------------|
| Kotlin                            | 2.0+ (2.3.0 latest) | Full KMP support                   |
| Compose Multiplatform             | 1.8.0+      | iOS stable since May 2025                  |
| AndroidX ViewModel (KMP)          | 2.8.0+      | Stable KMP support                         |
| AndroidX DataStore Preferences    | 1.1.0+      | Stable KMP support                         |
| Ktor Client                       | 2.9+        | OkHttp + Darwin engines                    |
| kotlinx.serialization             | 1.7+        | Stable                                     |

**Minimum targets**:
- Android: API 26+ (Android 8.0)
- iOS: 14.0+
