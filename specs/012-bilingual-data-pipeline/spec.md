# Feature Specification: Bilingual Data Pipeline

**Feature Branch**: `012-bilingual-data-pipeline`
**Created**: 2026-03-23
**Status**: Draft
**Input**: User description: "Improve the Google Sheets to Supabase to App data pipeline to support bilingual (Korean/English) content and reduce the maintenance burden when adding or modifying columns. Currently, every schema change requires coordinated updates across three layers (Google Sheet columns, Apps Script sync logic, and app data models)."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Bilingual Exhibition Display (Priority: P1)

As a gallery visitor, I want to see exhibition information in both Korean and English so that I can understand exhibition details regardless of my language preference.

Today, the `name` column contains a single value (usually Korean). The curator must choose one language, losing the other. With bilingual support, each text field carries both Korean and English variants, and the app displays the appropriate one based on the user's device language -- or both when useful (e.g., Korean name with English subtitle).

**Why this priority**: This is the core user-facing value. Without bilingual content, international users cannot use the app effectively, and curators cannot serve both audiences from a single data source.

**Independent Test**: Can be tested by entering bilingual data in the spreadsheet and verifying that the app displays the correct language variant based on device locale settings.

**Acceptance Scenarios**:

1. **Given** a curator enters both Korean and English exhibition names in the spreadsheet, **When** the data syncs and a user opens the app with their device set to English, **Then** the English name is displayed as the primary name.
2. **Given** a curator enters both Korean and English exhibition names, **When** a user opens the app with their device set to Korean, **Then** the Korean name is displayed as the primary name.
3. **Given** a curator enters only a Korean name (English left blank), **When** any user views the exhibition, **Then** the Korean name is displayed regardless of device language.
4. **Given** bilingual data exists for venue name, description, and exhibition name, **When** the app displays an exhibition card, **Then** all text fields respect the user's language preference consistently.

---

### User Story 2 - Simplified Spreadsheet Schema Management (Priority: P1)

As a curator/maintainer, I want adding a new data field to require changes in only one place (the spreadsheet), with the sync pipeline automatically adapting -- so I don't need to update the Apps Script column mappings every time.

Today, adding a column (e.g., "artist_name") requires: (1) adding a column to the Google Sheet, (2) updating the Apps Script to read the new column index and map it, (3) updating the Supabase schema, (4) updating ExhibitionDto and Exhibition in the app. This is error-prone and slow.

The improved pipeline should use a **header-driven approach**: the Apps Script reads column headers from row 1 and dynamically maps them to Supabase columns. New columns in the spreadsheet are automatically picked up without script changes. The app-side models still need updating for new fields it wants to display, but the sync layer becomes zero-maintenance.

**Why this priority**: This directly addresses the maintainability pain point. Without this, every data change is a multi-layer coordination effort.

**Independent Test**: Can be tested by adding a new column to the Google Sheet with a matching header name, running the sync, and verifying the data appears in Supabase without any script modifications.

**Acceptance Scenarios**:

1. **Given** the sync script uses header-driven column mapping, **When** a curator adds a new column "artist_name" with a matching header, **Then** the sync script automatically includes this field in the Supabase upsert without any code changes to the script.
2. **Given** a curator reorders columns in the spreadsheet, **When** the sync runs, **Then** data is correctly mapped based on header names, not column positions.
3. **Given** a curator adds a column whose header does not match any Supabase column, **When** the sync runs, **Then** the unknown column is ignored and logged, and the sync completes successfully.
4. **Given** required columns (name_ko, venue_name_ko, opening_date, etc.) are present but in a different order, **When** the sync runs, **Then** validation and record construction work correctly.

---

### User Story 3 - Bilingual Column Convention in Spreadsheet (Priority: P2)

As a curator, I want a clear and simple convention for entering bilingual data in the spreadsheet so that I don't need to learn a complex system or use multiple sheets.

The convention uses **paired columns with a language suffix**: for any text field that needs bilingual support, the curator creates two columns with `_ko` and `_en` suffixes. For example: `name_ko`, `name_en`, `venue_name_ko`, `venue_name_en`, `description_ko`, `description_en`. The sync script recognizes these pairs and stores them appropriately.

Non-bilingual fields (dates, booleans, coordinates) remain single columns with no suffix.

**Why this priority**: This establishes the data entry convention that makes User Story 1 possible. It's a prerequisite for bilingual display but separated because it's about the curator's workflow.

**Independent Test**: Can be tested by setting up a spreadsheet with paired `_ko`/`_en` columns and verifying the sync correctly identifies and processes bilingual pairs.

**Acceptance Scenarios**:

1. **Given** a spreadsheet has columns `name_ko` and `name_en`, **When** the sync runs, **Then** both values are stored in Supabase as separate columns (`name_ko` and `name_en`).
2. **Given** a curator fills in `name_ko` but leaves `name_en` blank, **When** the sync validates the row, **Then** the row is accepted (only the `_ko` variant of the primary name is required).
3. **Given** a field like `opening_date` has no bilingual variant, **When** the curator uses a single column without suffix, **Then** it is processed as before with no changes needed.

---

### User Story 4 - In-App Language Toggle (Priority: P2)

As a user, I want to manually switch between Korean and English within the app so that I can choose my preferred language regardless of my device settings.

The app provides a language toggle accessible from the top app bar (info/settings area). The toggle lets the user switch between Korean (KO) and English (EN). The selected language is persisted locally so it is remembered across app restarts. By default, the app uses the device locale, but once the user explicitly selects a language, that choice takes precedence.

**Why this priority**: Device locale detection is a good default, but many bilingual users in Seoul have their phone set to one language while preferring to browse exhibition content in another. An explicit toggle gives users control and is essential for a good bilingual experience.

**Independent Test**: Can be tested by tapping the language toggle and verifying all bilingual text fields switch immediately without restarting the app.

**Acceptance Scenarios**:

1. **Given** a user's device is set to Korean and they have not changed the in-app setting, **When** they view exhibitions, **Then** Korean text is displayed (device locale is the default).
2. **Given** a user taps the language toggle and selects English, **When** they view exhibitions, **Then** all bilingual text fields immediately switch to English variants.
3. **Given** a user has previously selected English via the toggle, **When** they close and reopen the app, **Then** English remains selected (the preference is persisted).
4. **Given** a user has selected English but an exhibition has no English name, **When** they view that exhibition, **Then** the Korean name is displayed as a fallback.
5. **Given** the user is on any tab (Featured, List, or Map), **When** they switch the language toggle, **Then** all visible content updates to the selected language without navigating away.

---

### User Story 5 - Graceful App Handling of New Fields (Priority: P3)

As a developer, I want the app to gracefully handle new fields appearing in the Supabase data that the current app version doesn't know about, so that adding spreadsheet columns doesn't break older app versions.

When new columns appear in Supabase that the app's data model doesn't include, the app should ignore unknown fields during deserialization rather than crashing. This means users on older app versions continue to work while newer versions can adopt new fields.

**Why this priority**: This is a resilience concern. It prevents the sync pipeline from breaking deployed apps when new data fields are added.

**Independent Test**: Can be tested by adding an unknown column to Supabase data and verifying the app still loads and displays exhibitions without errors.

**Acceptance Scenarios**:

1. **Given** the Supabase exhibitions table contains a column `artist_name` that the app model does not define, **When** the app fetches exhibitions, **Then** deserialization succeeds and the unknown field is silently ignored.
2. **Given** a new bilingual pair `artist_name_ko`/`artist_name_en` exists in Supabase, **When** an older app version fetches data, **Then** it loads successfully without crashing.

---

### Edge Cases

- What happens when a curator uses `name` (no suffix) alongside `name_ko`/`name_en`? The system should prefer the suffixed bilingual columns and ignore the unsuffixed duplicate.
- What happens when the spreadsheet header row is empty or missing? The sync should abort with a clear error log.
- What happens when a bilingual column has only the `_en` variant but no `_ko`? The `_en` value should be accepted; the `_ko` value defaults to empty string.
- How does the system handle columns with extra whitespace or different casing in headers (e.g., "Name_KO" vs "name_ko")? Headers should be normalized (lowercased, trimmed) before matching.
- What happens when the curator deletes a previously existing column from the spreadsheet? The sync should still succeed for remaining columns; the removed column will have NULL values in Supabase after the full-replace sync.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The sync script MUST read column headers from the spreadsheet's first row and map data by header name, not by column position.
- **FR-002**: The sync script MUST support bilingual text columns using the `_ko`/`_en` suffix convention (e.g., `name_ko`, `name_en`, `venue_name_ko`, `venue_name_en`).
- **FR-003**: The sync script MUST normalize column headers (lowercase, trimmed) before matching to Supabase column names.
- **FR-004**: The sync script MUST silently skip columns whose headers do not match any known Supabase column, logging them as informational.
- **FR-005**: The Supabase schema MUST store bilingual text fields as separate columns (e.g., `name_ko TEXT`, `name_en TEXT`).
- **FR-006**: The app MUST default to the user's device locale for language selection (Korean locale shows `_ko` fields; all other locales show `_en` fields with `_ko` as fallback).
- **FR-007**: The app MUST provide a compact text button ("KO" / "EN") in the top app bar, positioned next to the existing info button. Tapping the button cycles the display language between Korean and English.
- **FR-008**: The app MUST persist the user's language choice locally so it is remembered across app restarts.
- **FR-009**: When the user has explicitly selected a language via the toggle, that choice MUST take precedence over the device locale.
- **FR-010**: Switching languages via the toggle MUST immediately update all visible bilingual content without requiring navigation or app restart.
- **FR-011**: The app MUST gracefully handle unknown fields in the API response by ignoring them during deserialization.
- **FR-012**: Required field validation MUST be based on the `_ko` variant for bilingual fields (Korean is the primary language; English is optional).
- **FR-013**: Non-text fields (dates, booleans, numbers, URLs) MUST remain single columns without language suffixes.
- **FR-015**: All data text fields MUST support bilingual treatment, including: name, venue_name, description, city, and region (using `_ko`/`_en` suffix pairs in spreadsheet and database).
- **FR-016**: All UI labels MUST be bilingual, including: tab names (FEATURED, LIST, MAP), filter chip labels (FEATURED, EDITOR'S PICKS, OPENING THIS WEEK, CLOSING THIS WEEK), section headers (FILTERS, MAP), toggle labels (MYLIST, ALL), button text, and empty/loading/error state messages. These are controlled by the same language toggle.
- **FR-014**: The ID generation MUST continue using the deterministic hash approach, using `name_ko` (primary language) as the name component. Since the old `name` column already contains Korean values, renaming it to `name_ko` preserves identical hash inputs, keeping all existing IDs and bookmarks stable.

### Key Entities

- **Exhibition**: Core content entity representing a gallery exhibition. Bilingual text attributes include: name, venue name, description, city, and region. Non-bilingual attributes include: dates, coordinates, flags, and image URL.
- **Sync Pipeline**: The automated process that reads spreadsheet data, validates it, and writes it to the database. Now header-driven rather than position-driven.
- **Language Preference**: The user's chosen display language. Defaults to device locale but can be overridden via an in-app toggle. Persisted locally across sessions. Determines which language variant (`_ko` or `_en`) is displayed throughout the app.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Adding a new non-bilingual column to the spreadsheet requires zero changes to the sync script -- only a matching Supabase column needs to exist.
- **SC-002**: Adding a new bilingual field pair requires zero changes to the sync script -- only matching `_ko`/`_en` Supabase columns need to exist.
- **SC-003**: Users on Korean-locale devices see Korean text by default; users on other locales see English text by default (with Korean fallback when English is unavailable).
- **SC-007**: Users can switch between Korean and English via an in-app toggle, and the change takes effect immediately across all screens without restarting the app.
- **SC-008**: The user's language choice persists across app restarts.
- **SC-004**: Reordering columns in the spreadsheet does not break the sync pipeline.
- **SC-005**: Older app versions continue to function without crashes when new columns are added to the database.
- **SC-006**: The curator can manage all exhibition data (both languages) from a single spreadsheet without needing separate sheets per language.

## Clarifications

### Session 2026-03-23

- Q: How should ID generation handle the column rename from `name` to `name_ko`? → A: Keep IDs stable by ensuring `name_ko` contains the same values as the old `name` column. Since the existing `name` data is Korean, renaming to `name_ko` preserves identical hash inputs, so all exhibition IDs and user bookmarks remain valid.
- Q: What interaction pattern should the language toggle use? → A: A compact text button ("KO" / "EN") in the top app bar, next to the existing info button. Tapping cycles the language. No settings screen needed.
- Q: Should city, region, and other fields also get bilingual treatment? → A: Yes — all data fields (city, region, name, venue_name, description) and all UI labels (tab names, filter chips, section headers, button text, empty states) must support bilingual display. The language toggle controls everything.

## Assumptions

- Korean is the primary language; every bilingual field must have a Korean value. English is optional.
- The app currently targets Korean and international users in Seoul, so Korean + English coverage is sufficient (no need for additional languages at this time).
- The Supabase schema changes (adding `_ko`/`_en` columns, migrating data from the old single columns) will be done via a migration, with a brief data re-sync after migration.
- All text fields including city and region are bilingual. All UI strings (tab names, filter labels, button text, state messages) are also bilingual.
- The existing full-replace sync strategy (DELETE all + INSERT) is acceptable and will be preserved.
