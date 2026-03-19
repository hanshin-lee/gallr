# Contract: Sync Pipeline (Google Apps Script → Supabase)

**Feature**: 007-gallery-data-sync
**Consumer**: Google Apps Script (sync script)
**Provider**: Supabase PostgREST (service-role)

---

## Authentication

All write operations use the **service role key** (bypasses RLS). This key is stored in Apps Script Script Properties — never in the mobile app binary.

```
apikey: <SUPABASE_SERVICE_ROLE_KEY>
Authorization: Bearer <SUPABASE_SERVICE_ROLE_KEY>
Content-Type: application/json
Prefer: return=minimal
```

---

## Step 1: Delete All Existing Rows

```
DELETE /rest/v1/exhibitions?id=neq.IMPOSSIBLE_VALUE
```

**Why**: PostgREST requires at least one filter for DELETE. `id=neq.IMPOSSIBLE_VALUE` matches all real rows (no row has ID `IMPOSSIBLE_VALUE`).

**Response**: `204 No Content`

**Error responses**:
- `401 Unauthorized` — invalid service role key
- `400 Bad Request` — missing filter param

---

## Step 2: Batch Insert All Valid Rows

```
POST /rest/v1/exhibitions
```

**Body**: JSON array of validated exhibition objects

```json
[
  {
    "id": "a3f2b1c9d4e7f8a2",
    "name": "Zen Master Eyeball",
    "venue_name": "Kukje Gallery K1",
    "city": "Seoul",
    "region": "Seoul",
    "opening_date": "2026-03-19",
    "closing_date": "2026-05-10",
    "is_featured": true,
    "is_editors_pick": false,
    "latitude": 37.5796,
    "longitude": 126.9784,
    "description": "",
    "cover_image_url": null,
    "updated_at": "2026-03-20T10:00:00Z"
  }
]
```

**Headers**:
```
Prefer: resolution=merge-duplicates
```

**Response**: `201 Created` (with `return=minimal`, body is empty)

**Error responses**:
- `401 Unauthorized` — invalid service role key
- `400 Bad Request` — malformed JSON or type mismatch
- `409 Conflict` — duplicate ID without `Prefer: resolution=merge-duplicates`

---

## ID Generation Contract

The sync script generates a stable deterministic ID for each row:

```javascript
function generateId(name, venueName, openingDate) {
  const raw = `${name}|${venueName}|${openingDate}`.toLowerCase().trim();
  const digest = Utilities.computeDigest(Utilities.DigestAlgorithm.SHA_256, raw);
  return digest.slice(0, 8).map(b => (b & 0xff).toString(16).padStart(2, '0')).join('');
  // Returns a stable 16-char hex ID, e.g. "a3f2b1c9d4e7f8a2"
}
```

- Input: `name`, `venue_name`, `opening_date` (ISO string, e.g. `2026-03-19`)
- Output: 16-char lowercase hex string
- Determinism: Same inputs always produce the same ID across sync runs

---

## Row Validation Rules (pre-insert)

| Field | Required | Validation |
|-------|----------|------------|
| `name` | Yes | Non-empty string |
| `venue_name` | Yes | Non-empty string |
| `city` | Yes | Non-empty string |
| `region` | Yes | Non-empty string |
| `opening_date` | Yes | Parseable as ISO date (YYYY-MM-DD) or Korean date (YYYY.MM.DD) |
| `closing_date` | Yes | Same as opening_date |
| `latitude` | No | Numeric or blank (null) |
| `longitude` | No | Numeric or blank (null) |
| `is_featured` | No | `TRUE`/`FALSE`, `1`/`0`, or blank (default false) |
| `is_editors_pick` | No | Same as is_featured |
| `description` | No | Free text or blank (default `''`) |
| `cover_image_url` | No | HTTPS URL or blank (null) |

Rows failing required-field or date-format validation are **skipped** and logged with the row number and reason. They do NOT cause the sync to abort.

---

## Sync Log Fields (Apps Script execution log)

| Field | Type | Description |
|-------|------|-------------|
| `timestamp` | ISO datetime | When the sync ran |
| `status` | `SUCCESS` \| `FAILURE` | Outcome |
| `rows_read` | Integer | Total rows in sheet (excl. header) |
| `rows_inserted` | Integer | Valid rows successfully written |
| `rows_skipped` | Integer | Invalid rows (with per-row reason) |
| `error` | String \| null | Error message if FAILURE |

---

## Trigger Configuration

| Trigger Type | Setting | Purpose |
|--------------|---------|---------|
| `onEdit` | Spreadsheet edit event | Near-real-time sync on every cell save |
| Time-based | Every 5 minutes | Backup trigger; catches missed onEdit events |

The time-based trigger guarantees the 5-minute propagation SLA (SC-001) even if `onEdit` fails.
