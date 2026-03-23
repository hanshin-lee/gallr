# Research: Bilingual Data Pipeline (012)

**Feature Branch**: `012-bilingual-data-pipeline`
**Generated**: 2026-03-23

---

## R1: Compose Multiplatform String Resources for Localization

**Decision**: Use CMP's built-in `compose-resources` string system with `values/strings.xml` (English default) and `values-ko/strings.xml` (Korean).

**Rationale**: The project already uses `compose-resources` for fonts and drawables. CMP 1.8.0 supports `Res.string.*` accessors generated from XML string files. This is the native, zero-dependency approach. However, since the language toggle must override the device locale at runtime, strings will be loaded programmatically using a language-aware composable wrapper rather than relying on system locale auto-detection.

**Alternatives considered**:
- **lyricist / moko-resources**: Third-party KMP localization libraries. Rejected — adds a dependency for a problem the built-in system handles. Violates Principle III (Simplicity).
- **Hardcoded map of strings**: A `Map<String, Map<Lang, String>>` in Kotlin. Rejected — loses IDE support and compile-time safety that `Res.string.*` provides.
- **Android-style `strings.xml` only**: Not cross-platform; iOS wouldn't benefit.

**Implementation note**: For runtime language switching (not following device locale), use a `CompositionLocal` that provides the current language. UI components read from a language-aware string accessor function rather than directly from `Res.string.*`, enabling instant switching without app restart.

---

## R2: Header-Driven Sync Script Architecture

**Decision**: Refactor `SyncExhibitions.gs` to read column headers from row 1, build a `headerName → columnIndex` map, and use header names to construct Supabase records dynamically.

**Rationale**: Current script uses hardcoded column indices (A=0, B=1, etc.), which breaks when columns are reordered or added. Header-driven mapping makes the sync script resilient to spreadsheet layout changes.

**Alternatives considered**:
- **Named ranges in Google Sheets**: Would require manual range setup per column. Rejected — more setup overhead, still breaks on column additions.
- **Separate config sheet**: A sheet listing column→field mappings. Rejected — over-engineering for the current scale (~15 columns). Violates Principle III.
- **Google Forms as data entry**: Completely different data entry paradigm. Rejected — curator prefers spreadsheet flexibility.

**Implementation approach**:
1. Read row 1 as headers, normalize (lowercase, trim)
2. Build `Map<String, Number>` of headerName → columnIndex
3. Validate required headers exist (name_ko, venue_name_ko, opening_date, closing_date, city_ko, region_ko)
4. For each data row, build a record object using header names as keys
5. Skip headers that don't match Supabase columns (log as info)
6. ID generation uses `headers['name_ko']` value instead of positional `row[0]`

---

## R3: Supabase Schema Migration Strategy

**Decision**: Rename existing single-language columns to `_ko` variants, add new `_en` columns with defaults. Run a single migration, then re-sync from the updated spreadsheet.

**Rationale**: The existing `name`, `venue_name`, `city`, `region`, `description` columns contain Korean values. Renaming to `_ko` preserves data and keeps exhibition IDs stable (since ID hash uses the same value). Adding `_en` columns with empty-string defaults ensures backward compatibility.

**Alternatives considered**:
- **Drop and recreate table**: Simpler migration but loses any manual database edits. Acceptable since the sync does full-replace anyway, but rename is cleaner.
- **Add new columns without renaming**: Keep `name` alongside `name_ko`. Rejected — creates ambiguity and the old `name` column becomes dead weight.

**Migration SQL**:
```sql
ALTER TABLE exhibitions RENAME COLUMN name TO name_ko;
ALTER TABLE exhibitions RENAME COLUMN venue_name TO venue_name_ko;
ALTER TABLE exhibitions RENAME COLUMN city TO city_ko;
ALTER TABLE exhibitions RENAME COLUMN region TO region_ko;
ALTER TABLE exhibitions RENAME COLUMN description TO description_ko;

ALTER TABLE exhibitions ADD COLUMN name_en TEXT NOT NULL DEFAULT '';
ALTER TABLE exhibitions ADD COLUMN venue_name_en TEXT NOT NULL DEFAULT '';
ALTER TABLE exhibitions ADD COLUMN city_en TEXT NOT NULL DEFAULT '';
ALTER TABLE exhibitions ADD COLUMN region_en TEXT NOT NULL DEFAULT '';
ALTER TABLE exhibitions ADD COLUMN description_en TEXT NOT NULL DEFAULT '';
```

---

## R4: Language Preference Persistence Pattern

**Decision**: Create a `LanguageRepository` interface in `shared/` with a `LanguageRepositoryImpl` backed by the existing DataStore instance. Follow the identical pattern used by `BookmarkRepositoryImpl`.

**Rationale**: DataStore is already initialized on both Android and iOS. Adding a new preference key is trivial. The repository pattern is established and follows Principle VI (Shared-First).

**Alternatives considered**:
- **Platform-specific storage (SharedPreferences / UserDefaults)**: Requires `expect`/`actual` declarations. Rejected — DataStore already abstracts this.
- **In-memory only (no persistence)**: Simpler but doesn't satisfy FR-008 (persist across restarts).

**Interface**:
```kotlin
interface LanguageRepository {
    fun observeLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
}

enum class AppLanguage { KO, EN }
```

---

## R5: Runtime Language Switching in Compose

**Decision**: Use a `CompositionLocal` (`LocalAppLanguage`) to provide the current `AppLanguage` throughout the composable tree. A helper function `localizedString(ko: String, en: String)` selects the appropriate variant based on the current local value.

**Rationale**: `CompositionLocal` is the standard Compose mechanism for ambient values. When the language StateFlow changes, the CompositionLocal updates, and all composables reading it recompose automatically — achieving instant language switching (FR-010).

**For data fields**: Exhibition model exposes `localizedName(lang)`, `localizedVenueName(lang)`, etc. that return the `_ko` or `_en` variant with `_ko` fallback.

**For UI labels**: Compose string resources (`Res.string.*`) are used but wrapped in a language-aware accessor. Alternatively, a simple `Strings` object with `Map<AppLanguage, String>` per label is sufficient for ~30 strings.

**Alternatives considered**:
- **Recreate Activity / relaunch app**: Android-style locale change. Rejected — poor UX, doesn't work on iOS.
- **ViewModel-driven string map**: Pass all strings through ViewModel. Rejected — couples ViewModel to UI concerns.

---

## R6: Forward-Compatible Deserialization

**Decision**: Configure kotlinx.serialization `Json` instance with `ignoreUnknownKeys = true` in the Ktor HttpClient setup.

**Rationale**: This is a one-line change that makes the app resilient to new Supabase columns. Already a best practice for API clients. Satisfies FR-011 and SC-005.

**Alternatives considered**:
- **Explicit `select=` in API queries**: Only fetch known columns. Rejected — defeats the purpose of forward compatibility and requires query updates for each new field.
- **Custom deserializer**: Over-engineering. `ignoreUnknownKeys` handles this natively.
