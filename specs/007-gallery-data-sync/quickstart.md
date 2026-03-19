# Quickstart: Gallery Data Sync (007)

**Feature**: 007-gallery-data-sync
**Purpose**: End-to-end integration guide + acceptance test scenarios

---

## Components Overview

| Component | Where | What it does |
|-----------|-------|-------------|
| Supabase `exhibitions` table | Supabase dashboard | Persistent store the app reads |
| Google Apps Script | Google Drive (linked to sheet) | Reads the sheet; writes to Supabase |
| KMP `ExhibitionApiClient` | `shared/` module | Reads Supabase via PostgREST |
| `ExhibitionRepositoryImpl` | `shared/` module | Converts DTO to domain model |
| Android/iOS DI wiring | platform modules | Constructs the real repository |

---

## Setup: Supabase

### 1. Create the exhibitions table

Run the migration in the Supabase SQL editor:

```sql
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

### 2. Collect credentials

From Supabase → Settings → API:
- **Project URL**: `https://<project-ref>.supabase.co`
- **anon (public) key**: used in the KMP app (read-only)
- **service_role key**: used in the Apps Script (write access) — keep secret

---

## Setup: Google Apps Script

### 1. Open the Apps Script editor

From your Google Sheet: Extensions → Apps Script

### 2. Store credentials as Script Properties

Apps Script → Project settings → Script Properties:

| Property | Value |
|----------|-------|
| `SUPABASE_URL` | `https://<project-ref>.supabase.co` |
| `SUPABASE_SERVICE_ROLE_KEY` | `<service_role_key>` |
| `SHEET_ID` | Spreadsheet ID from the URL |

### 3. Deploy the sync script

1. Open your Google Sheet → Extensions → Apps Script
2. Paste the contents of `gas/SyncExhibitions.gs` from the repo into the editor
3. Click **Save** (disk icon)

### 4. Install triggers

**Installable onEdit trigger** (runs on every sheet save):
1. In Apps Script editor → Triggers (clock icon on left sidebar)
2. Click **Add Trigger** (bottom-right)
3. Configure:
   - Function: `syncToSupabase`
   - Deployment: `Head`
   - Event source: `From spreadsheet`
   - Event type: `On edit`
4. Click **Save** — authorize when prompted

**Time-based backup trigger** (guarantees 5-minute SLA):
1. Click **Add Trigger** again
2. Configure:
   - Function: `syncToSupabase`
   - Deployment: `Head`
   - Event source: `Time-driven`
   - Type: `Minutes timer`
   - Interval: `Every 5 minutes`
3. Click **Save**

> **Why two triggers?** The `onEdit` trigger fires immediately when a curator saves, giving near-instant updates. The time-based trigger is a safety net in case an edit event is missed (e.g. batch paste operations that don't fire `onEdit`).

### 4. Expected Sheet column order

| Column | Field | Required |
|--------|-------|----------|
| A | name | Yes |
| B | venue_name | Yes |
| C | city | Yes |
| D | region | Yes |
| E | opening_date | Yes (YYYY-MM-DD or YYYY.MM.DD) |
| F | closing_date | Yes |
| G | is_featured | No (TRUE/FALSE) |
| H | is_editors_pick | No |
| I | latitude | No |
| J | longitude | No |
| K | description | No |
| L | cover_image_url | No |

---

## Setup: KMP App

### 1. Configure credentials

**Android** — `composeApp/src/androidMain/res/values/strings.xml` (or `local.properties`):
```
supabase_url=https://<project-ref>.supabase.co
supabase_anon_key=<anon_key>
```

**iOS** — `iosApp/Configuration/Debug.xcconfig` (gitignored):
```
SUPABASE_URL = https://<project-ref>.supabase.co
SUPABASE_ANON_KEY = <anon_key>
```

### 2. DI wiring (to be done during implementation)

Replace `StubExhibitionRepository` with `ExhibitionRepositoryImpl(ExhibitionApiClient(supabaseUrl, anonKey))` in:
- `composeApp/src/androidMain/…`
- `composeApp/src/iosMain/…`

---

## Acceptance Test Scenarios

### SC-001: End-to-end update within 5 minutes

1. Open your Google Sheet and add a row with a distinctive name: `TEST GALLERY 001`
2. Fill in all required fields (columns A–F) with valid data
3. Save the sheet
4. Wait up to 5 minutes
5. Open gallr → List tab
6. **Expected**: `TEST GALLERY 001` appears with the correct venue, dates, and region
7. Delete the row from the sheet; wait 5 minutes
8. **Expected**: `TEST GALLERY 001` is gone from the app

---

### SC-002: Curator self-service (no developer needed)

1. Share the Google Sheet with a non-technical person (edit access)
2. Ask them to add 5 new exhibition rows and edit the venue of one existing row
3. Wait 5 minutes
4. **Expected**: All 5 new exhibitions appear; the edited venue name is updated — no developer action taken

---

### SC-003: Row count parity

1. Count data rows in the Google Sheet (exclude header)
2. Open gallr → List tab and count displayed items
3. **Expected**: Counts match (all valid rows appear)

---

### SC-004: Invalid row is silently skipped

1. Add a row with column A (name) left blank; leave all other fields valid
2. Wait for sync
3. **Expected**: The invalid row does not appear in the app; all other valid rows appear; no crash or empty-state error in the app

---

### SC-005: Stale data during sync failure

1. Temporarily remove the sync trigger (or revoke the service role key)
2. Wait more than 5 minutes
3. Open gallr
4. **Expected**: The last successfully synced exhibitions are still displayed; no empty list, no crash

---

### SC-006: 500-row load test

1. Populate the Google Sheet with 500 valid exhibition rows
2. Trigger a manual sync
3. Open gallr → List tab and Map tab
4. **Expected**: All 500 rows are present and the UI scrolls and renders without visible lag

---

### SC-007: Malformed date is skipped

1. Add a row with `opening_date` set to `not-a-date`
2. Wait for sync
3. **Expected**: That row does not appear in the app; other valid rows are unaffected

---

### SC-008: Featured filter

1. Set one row's `is_featured` column to `TRUE` in the sheet
2. Wait for sync
3. Open gallr → Featured tab
4. **Expected**: Only the row(s) with `is_featured = TRUE` appear in the Featured tab

---

## Sync Verification via Apps Script Logs

To check the last sync run:
1. Open Apps Script editor
2. View → Logs (or Executions)
3. Look for log entries from `syncToSupabase`:
   - `rows_read`, `rows_inserted`, `rows_skipped`, `status`
   - Any `rows_skipped` entries include the row number and reason
