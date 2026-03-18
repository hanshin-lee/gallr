# API Contract: Exhibitions

**Branch**: `001-exhibition-tabs` | **Date**: 2026-03-18
**Consumed by**: `shared/src/commonMain/.../network/ExhibitionApiClient.kt`
**Status**: Expected contract — backend implementation TBD

---

## Overview

The gallr app is a consumer of a remote exhibitions API. This document defines the
contract the app expects the backend to satisfy. The backend implementation is out of
scope for this feature; this contract drives the `ExhibitionApiClient` implementation.

**Base URL**: Configurable per environment (dev / staging / prod). Not hardcoded.
**Protocol**: HTTPS
**Format**: JSON (`Content-Type: application/json`)
**Authentication**: None required for this feature (public read endpoints).

---

## Endpoints

### GET /api/v1/exhibitions/featured

Returns the curated list of featured exhibitions for the Featured tab.

**Request**

```
GET /api/v1/exhibitions/featured
```

No query parameters required.

**Response: 200 OK**

```json
[
  {
    "id": "abc-123",
    "name": "Echoes in Glass",
    "venueName": "Saatchi Gallery",
    "city": "London",
    "region": "London",
    "openingDate": "2026-03-01",
    "closingDate": "2026-04-30",
    "isFeatured": true,
    "isEditorsPick": false,
    "latitude": 51.4879,
    "longitude": -0.1622,
    "description": "A survey of contemporary glass sculpture.",
    "coverImageUrl": "https://cdn.gallr.app/exhibitions/abc-123/cover.jpg"
  }
]
```

**Response: 200 OK (empty — no featured exhibitions)**

```json
[]
```

**Error responses**

| Status | When                              |
|--------|-----------------------------------|
| 500    | Server error                      |
| 503    | Service temporarily unavailable   |

---

### GET /api/v1/exhibitions

Returns exhibitions with optional server-side filtering. If the backend does not support
query-param filtering, the app falls back to client-side filtering using `FilterState`.

**Request**

```
GET /api/v1/exhibitions
  ?featured=true            (optional) — only featured exhibitions
  ?editors_pick=true        (optional) — only editors' picks
  ?region=London            (optional, repeatable) — match any listed region
  ?region=Paris
  ?opening_after=2026-03-18 (optional) — opening date >= this date
  ?opening_before=2026-03-25 (optional) — opening date <= this date
  ?closing_after=2026-03-18 (optional) — closing date >= this date
  ?closing_before=2026-03-25 (optional) — closing date <= this date
```

All parameters are optional. Omitting all parameters returns all exhibitions.

**Response: 200 OK**

Same schema as `/featured` — array of exhibition objects (see above).

**Error responses**

| Status | When                                      |
|--------|-------------------------------------------|
| 400    | Invalid query parameter value             |
| 500    | Server error                              |

---

## Exhibition Object Schema

| Field           | Type    | Nullable | Format             | Notes                              |
|-----------------|---------|----------|--------------------|------------------------------------|
| `id`            | string  | No       | Stable identifier  | Unique per exhibition              |
| `name`          | string  | No       |                    |                                    |
| `venueName`     | string  | No       |                    |                                    |
| `city`          | string  | No       |                    |                                    |
| `region`        | string  | No       |                    | Used for region filter matching    |
| `openingDate`   | string  | No       | `YYYY-MM-DD`       | ISO 8601 date                      |
| `closingDate`   | string  | No       | `YYYY-MM-DD`       | ISO 8601 date; ≥ `openingDate`     |
| `isFeatured`    | boolean | No       |                    |                                    |
| `isEditorsPick` | boolean | No       |                    |                                    |
| `latitude`      | number  | Yes      | WGS-84 decimal     | Null if no location available      |
| `longitude`     | number  | Yes      | WGS-84 decimal     | Null if `latitude` is null         |
| `description`   | string  | No       |                    | May be empty string `""`           |
| `coverImageUrl` | string  | Yes      | Absolute HTTPS URL | Null if no image available         |

**Invariants the app relies on**:
- `latitude` and `longitude` are both present or both null — never partial.
- `openingDate` ≤ `closingDate`.
- `id` values are stable and do not change between API calls for the same exhibition.
- Array order is not significant; the app sorts/groups as needed.

---

## Error Handling

The app treats all non-2xx responses as errors and maps them to a user-visible error
state with a retry action. The app does NOT attempt to parse error response bodies for
this feature.

Network timeouts and connectivity failures surface the same offline/error state.

---

## Client-Side Filtering Fallback

If the backend returns all exhibitions without server-side filtering support, the
`ExhibitionRepositoryImpl` applies `FilterState.matches(exhibition)` client-side after
fetching the full list from `GET /api/v1/exhibitions`. The interface contract (see
`data-model.md`) is agnostic to where filtering occurs.
