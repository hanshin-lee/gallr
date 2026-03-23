# Data Model: Bilingual Data Pipeline (012)

**Feature Branch**: `012-bilingual-data-pipeline`
**Generated**: 2026-03-23

---

## Supabase Postgres Schema

### Table: `exhibitions` (migrated)

| Column | Type | Nullable | Default | Notes |
|--------|------|----------|---------|-------|
| `id` | `TEXT` | NOT NULL | — | Primary key; hash of name_ko+venue_name_ko+opening_date |
| `name_ko` | `TEXT` | NOT NULL | — | Exhibition title (Korean) — renamed from `name` |
| `name_en` | `TEXT` | NOT NULL | `''` | Exhibition title (English) |
| `venue_name_ko` | `TEXT` | NOT NULL | — | Gallery/venue name (Korean) — renamed from `venue_name` |
| `venue_name_en` | `TEXT` | NOT NULL | `''` | Gallery/venue name (English) |
| `city_ko` | `TEXT` | NOT NULL | — | City (Korean) — renamed from `city` |
| `city_en` | `TEXT` | NOT NULL | `''` | City (English) |
| `region_ko` | `TEXT` | NOT NULL | — | District/region (Korean) — renamed from `region` |
| `region_en` | `TEXT` | NOT NULL | `''` | District/region (English) |
| `description_ko` | `TEXT` | NOT NULL | `''` | Description (Korean) — renamed from `description` |
| `description_en` | `TEXT` | NOT NULL | `''` | Description (English) |
| `opening_date` | `DATE` | NOT NULL | — | Opening date (no bilingual variant) |
| `closing_date` | `DATE` | NOT NULL | — | Closing date (no bilingual variant) |
| `is_featured` | `BOOLEAN` | NOT NULL | `false` | Featured flag |
| `is_editors_pick` | `BOOLEAN` | NOT NULL | `false` | Editor's pick flag |
| `latitude` | `DOUBLE PRECISION` | NULL | `null` | Geographic latitude |
| `longitude` | `DOUBLE PRECISION` | NULL | `null` | Geographic longitude |
| `cover_image_url` | `TEXT` | NULL | `null` | HTTPS URL to cover image |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | `now()` | Set by sync script on each upsert |

**Primary key**: `id`
**Row Level Security**: Unchanged (public SELECT for anon, service_role for writes)

### Migration SQL

```sql
-- 002_bilingual_columns.sql
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

## KMP Shared Module — Updated Models

### Exhibition.kt (updated)

```kotlin
data class Exhibition(
    val id: String,
    val nameKo: String,
    val nameEn: String,
    val venueNameKo: String,
    val venueNameEn: String,
    val cityKo: String,
    val cityEn: String,
    val regionKo: String,
    val regionEn: String,
    val openingDate: LocalDate,
    val closingDate: LocalDate,
    val isFeatured: Boolean,
    val isEditorsPick: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val descriptionKo: String,
    val descriptionEn: String,
    val coverImageUrl: String?,
) {
    /** Returns the localized name, falling back to Korean if English is empty. */
    fun localizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> nameEn.ifEmpty { nameKo }
        AppLanguage.KO -> nameKo
    }

    fun localizedVenueName(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> venueNameEn.ifEmpty { venueNameKo }
        AppLanguage.KO -> venueNameKo
    }

    fun localizedCity(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> cityEn.ifEmpty { cityKo }
        AppLanguage.KO -> cityKo
    }

    fun localizedRegion(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> regionEn.ifEmpty { regionKo }
        AppLanguage.KO -> regionKo
    }

    fun localizedDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> descriptionEn.ifEmpty { descriptionKo }
        AppLanguage.KO -> descriptionKo
    }
}
```

### AppLanguage.kt (new)

```kotlin
enum class AppLanguage { KO, EN }
```

### ExhibitionDto.kt (updated)

```kotlin
@Serializable
data class ExhibitionDto(
    val id: String,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("name_en") val nameEn: String = "",
    @SerialName("venue_name_ko") val venueNameKo: String,
    @SerialName("venue_name_en") val venueNameEn: String = "",
    @SerialName("city_ko") val cityKo: String,
    @SerialName("city_en") val cityEn: String = "",
    @SerialName("region_ko") val regionKo: String,
    @SerialName("region_en") val regionEn: String = "",
    @SerialName("opening_date") val openingDate: String,
    @SerialName("closing_date") val closingDate: String,
    @SerialName("is_featured") val isFeatured: Boolean,
    @SerialName("is_editors_pick") val isEditorsPick: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("description_ko") val descriptionKo: String = "",
    @SerialName("description_en") val descriptionEn: String = "",
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
) {
    fun toDomain(): Exhibition = Exhibition(
        id = id,
        nameKo = nameKo,
        nameEn = nameEn,
        venueNameKo = venueNameKo,
        venueNameEn = venueNameEn,
        cityKo = cityKo,
        cityEn = cityEn,
        regionKo = regionKo,
        regionEn = regionEn,
        openingDate = LocalDate.parse(openingDate),
        closingDate = LocalDate.parse(closingDate),
        isFeatured = isFeatured,
        isEditorsPick = isEditorsPick,
        latitude = latitude,
        longitude = longitude,
        descriptionKo = descriptionKo,
        descriptionEn = descriptionEn,
        coverImageUrl = coverImageUrl,
    )
}
```

---

## KMP Shared Module — Language Repository (new)

### LanguageRepository.kt

```kotlin
interface LanguageRepository {
    fun observeLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
}
```

### LanguageRepositoryImpl.kt

```kotlin
class LanguageRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : LanguageRepository {

    private val LANGUAGE_KEY = stringPreferencesKey("app_language")

    override fun observeLanguage(): Flow<AppLanguage> =
        dataStore.data.map { prefs ->
            when (prefs[LANGUAGE_KEY]) {
                "en" -> AppLanguage.EN
                "ko" -> AppLanguage.KO
                else -> detectDeviceLanguage()
            }
        }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = when (language) {
                AppLanguage.KO -> "ko"
                AppLanguage.EN -> "en"
            }
        }
    }

    private fun detectDeviceLanguage(): AppLanguage {
        // Platform-specific locale detection via expect/actual
        return getSystemLanguage()
    }
}
```

---

## Google Sheet Column Layout (updated)

```
A: name_ko (required)        B: name_en
C: venue_name_ko (required)  D: venue_name_en
E: city_ko (required)        F: city_en
G: region_ko (required)      H: region_en
I: opening_date (required)   J: closing_date (required)
K: is_featured               L: is_editors_pick
M: latitude                  N: longitude
O: description_ko            P: description_en
Q: cover_image_url
```

**Note**: Column order is no longer significant — headers drive mapping.

---

## Sync Pipeline — Updated Validation Rules

### Required headers (sync aborts if missing)
- `name_ko`, `venue_name_ko`, `city_ko`, `region_ko`, `opening_date`, `closing_date`

### Required per-row values (row skipped if empty)
- `name_ko`, `venue_name_ko`, `city_ko`, `region_ko`, `opening_date`, `closing_date`

### Optional columns (empty values accepted)
- All `_en` columns, `is_featured`, `is_editors_pick`, `latitude`, `longitude`, `description_ko`, `description_en`, `cover_image_url`

### ID generation (unchanged logic, updated field reference)
```javascript
function generateId(nameKo, venueNameKo, openingDate) {
    const raw = (nameKo + '|' + venueNameKo + '|' + openingDate).toLowerCase().trim();
    // SHA-256 hash → 16-char hex ID
}
```
