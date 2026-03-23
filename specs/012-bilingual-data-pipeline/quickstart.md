# Quickstart: Bilingual Data Pipeline (012)

**Feature Branch**: `012-bilingual-data-pipeline`
**Generated**: 2026-03-23

---

## Prerequisites

- Kotlin 2.1.20 with KMP configured
- Compose Multiplatform 1.8.0
- Supabase project with `exhibitions` table (from feature 007)
- Google Apps Script access to the sync spreadsheet
- Android Studio / Xcode for building

## Implementation Order

### Step 1: Supabase Schema Migration

Run the migration SQL against your Supabase project:

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

### Step 2: Update Google Sheet Headers

Rename spreadsheet column headers to match the new schema:
- `name` → `name_ko`, add `name_en`
- `venue_name` → `venue_name_ko`, add `venue_name_en`
- `city` → `city_ko`, add `city_en`
- `region` → `region_ko`, add `region_en`
- `description` → `description_ko`, add `description_en`

### Step 3: Update Sync Script

Replace `gas/SyncExhibitions.gs` with the header-driven version. The script should:
1. Read headers from row 1
2. Build headerName → columnIndex map
3. Validate required headers exist
4. Construct records dynamically from headers

### Step 4: Update KMP Data Models

Update in `shared/src/commonMain/kotlin/com/gallr/shared/`:
1. Add `AppLanguage.kt` enum
2. Update `Exhibition.kt` with bilingual fields and `localized*()` methods
3. Update `ExhibitionDto.kt` with `_ko`/`_en` SerialName annotations
4. Add `LanguageRepository.kt` interface
5. Add `LanguageRepositoryImpl.kt` (DataStore-backed)
6. Configure `ignoreUnknownKeys = true` in JSON serialization

### Step 5: Add String Resources

Create localized string files:
- `composeApp/src/commonMain/composeResources/values/strings.xml` (English)
- `composeApp/src/commonMain/composeResources/values-ko/strings.xml` (Korean)

Include all UI labels: tab names, filter chips, section headers, button text, empty/error states.

### Step 6: Update UI Layer

1. Add language toggle button ("KO"/"EN") to top app bar in `App.kt`
2. Wire `LanguageRepository` into `TabsViewModel`
3. Update all screens to use localized strings and `exhibition.localizedName(lang)` pattern
4. Update `ExhibitionCard`, `GallrNavigationBar`, and all screen composables

### Step 7: Re-sync Data

Trigger the sync script to populate the new `_en` columns from the updated spreadsheet.

## Verification

1. Add English values to a few exhibitions in the spreadsheet
2. Run sync — verify both `_ko` and `_en` columns populated in Supabase
3. Open app — verify Korean text displays by default
4. Tap "KO" button → should toggle to "EN" → verify English text displays
5. Kill and reopen app → verify language preference persists
6. Add a new column to the spreadsheet → verify sync picks it up without script changes
