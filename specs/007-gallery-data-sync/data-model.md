# Data Model: Gallery Data Sync from Google Sheets (007)

**Feature Branch**: `007-gallery-data-sync`
**Generated**: 2026-03-20

---

## Supabase Postgres Schema

### Table: `exhibitions`

| Column | Type | Nullable | Default | Notes |
|--------|------|----------|---------|-------|
| `id` | `TEXT` | NOT NULL | — | Primary key; script-generated hash of name+venue+opening_date |
| `name` | `TEXT` | NOT NULL | — | Exhibition title |
| `venue_name` | `TEXT` | NOT NULL | — | Gallery/venue name |
| `city` | `TEXT` | NOT NULL | — | City |
| `region` | `TEXT` | NOT NULL | — | District or region |
| `opening_date` | `DATE` | NOT NULL | — | Opening date |
| `closing_date` | `DATE` | NOT NULL | — | Closing date |
| `is_featured` | `BOOLEAN` | NOT NULL | `false` | Featured flag |
| `is_editors_pick` | `BOOLEAN` | NOT NULL | `false` | Editor's pick flag |
| `latitude` | `DOUBLE PRECISION` | NULL | `null` | Geographic latitude |
| `longitude` | `DOUBLE PRECISION` | NULL | `null` | Geographic longitude |
| `description` | `TEXT` | NOT NULL | `''` | Exhibition description |
| `cover_image_url` | `TEXT` | NULL | `null` | HTTPS URL to cover image |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | `now()` | Set by sync script on each upsert |

**Primary key**: `id`
**Row Level Security**: Enabled
- `SELECT` allowed for `anon` role (public read)
- `INSERT`/`UPDATE`/`DELETE` allowed only for `service_role` (sync script)

```sql
-- Migration: 001_create_exhibitions.sql
CREATE TABLE IF NOT EXISTS exhibitions (
  id              TEXT PRIMARY KEY,
  name            TEXT NOT NULL,
  venue_name      TEXT NOT NULL,
  city            TEXT NOT NULL,
  region          TEXT NOT NULL,
  opening_date    DATE NOT NULL,
  closing_date    DATE NOT NULL,
  is_featured     BOOLEAN NOT NULL DEFAULT false,
  is_editors_pick BOOLEAN NOT NULL DEFAULT false,
  latitude        DOUBLE PRECISION,
  longitude       DOUBLE PRECISION,
  description     TEXT NOT NULL DEFAULT '',
  cover_image_url TEXT,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE exhibitions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Public read"
  ON exhibitions FOR SELECT
  USING (true);
```

---

## KMP Shared Module — Existing Models (No Changes Required)

### Exhibition.kt (existing — no change)

```kotlin
data class Exhibition(
    val id: String,
    val name: String,
    val venueName: String,
    val city: String,
    val region: String,
    val openingDate: LocalDate,
    val closingDate: LocalDate,
    val isFeatured: Boolean,
    val isEditorsPick: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val description: String,
    val coverImageUrl: String?,
)
```

### ExhibitionDto.kt (existing — @SerialName annotations must be updated)

Supabase PostgREST returns JSON with `snake_case` column names. The existing `ExhibitionDto` currently uses **camelCase** `@SerialName` values (e.g. `"venueName"`, `"openingDate"`) which will not deserialize correctly from Supabase. These must be changed to snake_case:

| DTO field | Current `@SerialName` | Required `@SerialName` | Supabase column |
|-----------|----------------------|----------------------|-----------------|
| `id` | (none) | (none) | `id` ✅ |
| `venueName` | `"venueName"` ❌ | `"venue_name"` | `venue_name` |
| `city` | (none) | (none) | `city` ✅ |
| `region` | (none) | (none) | `region` ✅ |
| `openingDate` | `"openingDate"` ❌ | `"opening_date"` | `opening_date` |
| `closingDate` | `"closingDate"` ❌ | `"closing_date"` | `closing_date` |
| `isFeatured` | `"isFeatured"` ❌ | `"is_featured"` | `is_featured` |
| `isEditorsPick` | `"isEditorsPick"` ❌ | `"is_editors_pick"` | `is_editors_pick` |
| `latitude` | (none) | (none) | `latitude` ✅ |
| `longitude` | (none) | (none) | `longitude` ✅ |
| `description` | (none) | (none) | `description` ✅ |
| `coverImageUrl` | `"coverImageUrl"` ❌ | `"cover_image_url"` | `cover_image_url` |

**Action**: Update all `@SerialName` annotations to snake_case to match Supabase PostgREST column names. This is a required code change.

---

## KMP Shared Module — ExhibitionApiClient (update required)

### Current state
`ExhibitionApiClient` is an existing Ktor HTTP client. Its base URL points to a placeholder or mock endpoint.

### Required changes
Configure it to call Supabase PostgREST:

```
Base URL:   https://<project-ref>.supabase.co/rest/v1/
Headers:    apikey: <SUPABASE_ANON_KEY>
            Authorization: Bearer <SUPABASE_ANON_KEY>
            Content-Type: application/json
```

**Query for all exhibitions**:
```
GET /rest/v1/exhibitions?select=*
```

**Query for featured exhibitions**:
```
GET /rest/v1/exhibitions?select=*&is_featured=eq.true
```

**Query with filter (e.g. by region)**:
```
GET /rest/v1/exhibitions?select=*&region=eq.Seoul
```

---

## Sync Pipeline — Google Apps Script Data Flow

### Input: Google Sheet row
```
[name, venue_name, city, region, opening_date, closing_date,
 is_featured, is_editors_pick, latitude, longitude, description, cover_image_url]
```

### Validation rules (per row)
- Columns A–F (name, venue_name, city, region, opening_date, closing_date) MUST be non-empty
- `opening_date` and `closing_date` MUST be parseable as dates (ISO or YYYY.MM.DD)
- `latitude` and `longitude`, if present, MUST be numeric
- Invalid rows are **skipped** (logged, not fatal)

### ID generation (deterministic hash)
```javascript
// Google Apps Script (V8)
function generateId(name, venueName, openingDate) {
  const raw = `${name}|${venueName}|${openingDate}`.toLowerCase().trim();
  // Use Utilities.computeDigest for SHA-256 in Apps Script
  const digest = Utilities.computeDigest(Utilities.DigestAlgorithm.SHA_256, raw);
  return digest.slice(0, 8).map(b => (b & 0xff).toString(16).padStart(2, '0')).join('');
  // Produces a stable 16-char hex ID, e.g. "a3f2b1c9d4e7f8a2"
}
```

### Supabase write operation (full replace)
1. `DELETE` from `exhibitions` (via PostgREST: `DELETE /rest/v1/exhibitions?id=neq.IMPOSSIBLE` with service role key)
2. `POST /rest/v1/exhibitions` with `Prefer: resolution=merge-duplicates` to batch insert all valid rows

Or equivalently: call a Supabase Edge Function / RPC that performs a `TRUNCATE + INSERT` in a transaction (safer for atomicity).

---

## Sync Pipeline — SyncLog (Apps Script script properties)

Not persisted to Supabase — stored in Apps Script execution logs (viewable in the Apps Script dashboard):

| Field | Value |
|-------|-------|
| `timestamp` | ISO datetime of sync run |
| `status` | `SUCCESS` or `FAILURE` |
| `rows_read` | Total rows in sheet (excluding header) |
| `rows_inserted` | Valid rows written to Supabase |
| `rows_skipped` | Invalid rows (with reason) |
| `error` | Error message if FAILURE |

---

## DI / Wiring Change in KMP App

Currently `StubExhibitionRepository` is wired as the active repository. This must be changed to `ExhibitionRepositoryImpl` (which already exists and uses `ExhibitionApiClient`).

| Module | Current | New |
|--------|---------|-----|
| `composeApp/androidMain` | `StubExhibitionRepository()` | `ExhibitionRepositoryImpl(ExhibitionApiClient(supabaseUrl, anonKey))` |
| `composeApp/iosMain` | `StubExhibitionRepository()` | `ExhibitionRepositoryImpl(ExhibitionApiClient(supabaseUrl, anonKey))` |
