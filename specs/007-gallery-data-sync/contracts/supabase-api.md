# Contract: Supabase PostgREST API (KMP App → Supabase)

**Feature**: 007-gallery-data-sync
**Consumer**: gallr KMP app (Android + iOS)
**Provider**: Supabase PostgREST

---

## Base URL

```
https://<project-ref>.supabase.co/rest/v1/
```

## Required Headers (all requests)

```
apikey: <SUPABASE_ANON_KEY>
Authorization: Bearer <SUPABASE_ANON_KEY>
Content-Type: application/json
```

---

## Endpoint 1: Get All Exhibitions

**Used by**: `ExhibitionApiClient.fetchExhibitions(filter)`

```
GET /rest/v1/exhibitions?select=*
```

**Optional filters** (appended as query params):

| Filter | Query param | Example |
|--------|------------|---------|
| Featured only | `is_featured=eq.true` | `?select=*&is_featured=eq.true` |
| By region | `region=eq.{value}` | `?select=*&region=eq.Seoul` |
| Opening this week | `opening_date=gte.{date}&opening_date=lte.{date+7}` | — |
| Closing this week | `closing_date=gte.{date}&closing_date=lte.{date+7}` | — |

**Response**: `200 OK`

```json
[
  {
    "id": "a3f2b1c9",
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

**Error responses**:
- `401 Unauthorized` — invalid or missing `apikey` header
- `404 Not Found` — table does not exist (misconfiguration)

---

## Endpoint 2: Get Featured Exhibitions

**Used by**: `ExhibitionApiClient.fetchFeatured()`

```
GET /rest/v1/exhibitions?select=*&is_featured=eq.true
```

Response: same structure as Endpoint 1, filtered to `is_featured = true`.

---

## DTO Mapping

The KMP `ExhibitionDto` maps Supabase snake_case JSON to Kotlin camelCase via `@SerialName`.
The existing annotations use camelCase and **must be updated to snake_case** during implementation:

| JSON field (Supabase) | Kotlin field | Required `@SerialName` | Type |
|-----------------------|-------------|----------------------|------|
| `id` | `id` | (none needed) | `String` |
| `name` | `name` | (none needed) | `String` |
| `venue_name` | `venueName` | `@SerialName("venue_name")` | `String` |
| `city` | `city` | (none needed) | `String` |
| `region` | `region` | (none needed) | `String` |
| `opening_date` | `openingDate` | `@SerialName("opening_date")` | `String` (parsed to `LocalDate`) |
| `closing_date` | `closingDate` | `@SerialName("closing_date")` | `String` (parsed to `LocalDate`) |
| `is_featured` | `isFeatured` | `@SerialName("is_featured")` | `Boolean` |
| `is_editors_pick` | `isEditorsPick` | `@SerialName("is_editors_pick")` | `Boolean` |
| `latitude` | `latitude` | (none needed) | `Double?` |
| `longitude` | `longitude` | (none needed) | `Double?` |
| `description` | `description` | (none needed) | `String` |
| `cover_image_url` | `coverImageUrl` | `@SerialName("cover_image_url")` | `String?` |
