/**
 * SyncExhibitions.gs
 * Google Apps Script — Sync Google Sheet → Supabase exhibitions table
 *
 * SETUP REQUIRED (one-time):
 * 1. Open Apps Script → Project Settings → Script Properties
 *    Add the following properties:
 *      SUPABASE_URL        = https://<project-ref>.supabase.co
 *      SUPABASE_SERVICE_ROLE_KEY = <your-service-role-key>  ← NEVER share this
 *
 * 2. Install triggers (Triggers menu in Apps Script editor):
 *    a) onEdit trigger:  Function = syncToSupabase, Event source = From spreadsheet,
 *                        Event type = On edit
 *    b) Time-driven trigger: Function = syncToSupabase, Time-based = Minutes timer,
 *                            Every 5 minutes
 *
 * GOOGLE SHEET COLUMN ORDER (Row 1 = header, data from Row 2):
 *   A: name (required)         B: venue_name (required)
 *   C: city (required)         D: region (required)
 *   E: opening_date (required) F: closing_date (required)
 *   G: is_featured             H: is_editors_pick
 *   I: latitude                J: longitude
 *   K: description             L: cover_image_url
 */

// ---------------------------------------------------------------------------
// Main entry point — called by both triggers
// ---------------------------------------------------------------------------

function syncToSupabase() {
  const props = PropertiesService.getScriptProperties();
  const supabaseUrl = props.getProperty('SUPABASE_URL');
  const serviceKey = props.getProperty('SUPABASE_SERVICE_ROLE_KEY');
  const timestamp = new Date().toISOString();

  if (!supabaseUrl || !serviceKey) {
    Logger.log(JSON.stringify({
      timestamp,
      status: 'FAILURE',
      error: 'SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY not set in Script Properties',
      rows_read: 0,
      rows_inserted: 0,
      rows_skipped: 0,
    }));
    return;
  }

  const sheet = SpreadsheetApp.getActiveSpreadsheet().getSheets()[0];
  const data = sheet.getDataRange().getValues();
  const dataRows = data.slice(1); // skip header row
  const rowsRead = dataRows.length;

  const validRows = [];
  const skippedReasons = [];

  dataRows.forEach(function(row, index) {
    const rowNum = index + 2; // 1-based, +1 for header
    const result = validateRow(row, rowNum);
    if (result.valid) {
      validRows.push(buildRecord(row));
    } else {
      skippedReasons.push(result.reason);
    }
  });

  // Deduplicate by id — rows with the same name+venue_name+opening_date generate the same id.
  // Inserting two rows with the same id in one batch causes a Postgres error even with ON CONFLICT.
  const seenIds = new Set();
  const uniqueRows = [];
  validRows.forEach(function(row) {
    if (!seenIds.has(row.id)) {
      seenIds.add(row.id);
      uniqueRows.push(row);
    } else {
      skippedReasons.push('Duplicate id ' + row.id + ' (same name+venue+opening_date): ' + row.name);
    }
  });

  try {
    deleteAllExhibitions(supabaseUrl, serviceKey);
    insertExhibitions(uniqueRows, supabaseUrl, serviceKey);

    Logger.log(JSON.stringify({
      timestamp,
      status: 'SUCCESS',
      rows_read: rowsRead,
      rows_inserted: uniqueRows.length,
      rows_skipped: skippedReasons.length,
      skipped_details: skippedReasons,
    }));
  } catch (e) {
    Logger.log(JSON.stringify({
      timestamp,
      status: 'FAILURE',
      error: e.message,
      rows_read: rowsRead,
      rows_inserted: 0,
      rows_skipped: skippedReasons.length,
    }));
  }
}

// ---------------------------------------------------------------------------
// Row validation
// ---------------------------------------------------------------------------

/**
 * Returns { valid: true } or { valid: false, reason: String }
 */
function validateRow(row, rowNum) {
  const name = String(row[0] || '').trim();
  const venueName = String(row[1] || '').trim();
  const city = String(row[2] || '').trim();
  const region = String(row[3] || '').trim();
  const openingRaw = row[4];
  const closingRaw = row[5];

  if (!name)      return { valid: false, reason: 'Row ' + rowNum + ': name (A) is empty' };
  if (!venueName) return { valid: false, reason: 'Row ' + rowNum + ': venue_name (B) is empty' };
  if (!city)      return { valid: false, reason: 'Row ' + rowNum + ': city (C) is empty' };
  if (!region)    return { valid: false, reason: 'Row ' + rowNum + ': region (D) is empty' };

  const openingDate = parseDate(openingRaw);
  if (!openingDate) return { valid: false, reason: 'Row ' + rowNum + ': opening_date (E) is not a valid date: ' + openingRaw };

  const closingDate = parseDate(closingRaw);
  if (!closingDate) return { valid: false, reason: 'Row ' + rowNum + ': closing_date (F) is not a valid date: ' + closingRaw };

  const lat = row[8];
  const lon = row[9];
  if (lat !== '' && lat !== null && lat !== undefined && isNaN(Number(lat))) {
    return { valid: false, reason: 'Row ' + rowNum + ': latitude (I) is not numeric: ' + lat };
  }
  if (lon !== '' && lon !== null && lon !== undefined && isNaN(Number(lon))) {
    return { valid: false, reason: 'Row ' + rowNum + ': longitude (J) is not numeric: ' + lon };
  }

  return { valid: true };
}

/**
 * Parses YYYY-MM-DD or YYYY.MM.DD into an ISO date string.
 * Returns null if the value is not parseable.
 */
function parseDate(value) {
  if (!value) return null;

  // If Google Sheets already parsed it as a Date object
  if (value instanceof Date) {
    const y = value.getFullYear();
    const m = String(value.getMonth() + 1).padStart(2, '0');
    const d = String(value.getDate()).padStart(2, '0');
    return y + '-' + m + '-' + d;
  }

  // String: normalize YYYY.MM.DD → YYYY-MM-DD
  const normalized = String(value).replace(/\./g, '-').trim();
  if (/^\d{4}-\d{2}-\d{2}$/.test(normalized)) return normalized;

  return null;
}

// ---------------------------------------------------------------------------
// Record construction and ID generation
// ---------------------------------------------------------------------------

/**
 * Builds a Supabase-ready record object from a sheet row.
 */
function buildRecord(row) {
  const name = String(row[0] || '').trim();
  const venueName = String(row[1] || '').trim();
  const city = String(row[2] || '').trim();
  const region = String(row[3] || '').trim();
  const openingDate = parseDate(row[4]);
  const closingDate = parseDate(row[5]);
  const isFeatured = parseBool(row[6]);
  const isEditorsPick = parseBool(row[7]);
  const lat = row[8];
  const lon = row[9];
  const description = String(row[10] || '').trim();
  const coverImageUrl = String(row[11] || '').trim() || null;

  return {
    id: generateId(name, venueName, openingDate),
    name: name,
    venue_name: venueName,
    city: city,
    region: region,
    opening_date: openingDate,
    closing_date: closingDate,
    is_featured: isFeatured,
    is_editors_pick: isEditorsPick,
    latitude: (lat !== '' && lat !== null && lat !== undefined) ? Number(lat) : null,
    longitude: (lon !== '' && lon !== null && lon !== undefined) ? Number(lon) : null,
    description: description,
    cover_image_url: coverImageUrl,
    updated_at: new Date().toISOString(),
  };
}

/**
 * Generates a stable 16-char hex ID from name + venue_name + opening_date.
 * Deterministic: same inputs always produce the same ID across sync runs.
 */
function generateId(name, venueName, openingDate) {
  const raw = (name + '|' + venueName + '|' + openingDate).toLowerCase().trim();
  const digest = Utilities.computeDigest(Utilities.DigestAlgorithm.SHA_256, raw);
  return digest.slice(0, 8).map(function(b) {
    return (b & 0xff).toString(16).padStart(2, '0');
  }).join('');
}

/**
 * Interprets TRUE/FALSE, 1/0, "yes"/"no", or blank (→ false).
 */
function parseBool(value) {
  if (value === true || value === 1) return true;
  if (typeof value === 'string') {
    const v = value.trim().toLowerCase();
    return v === 'true' || v === '1' || v === 'yes';
  }
  return false;
}

// ---------------------------------------------------------------------------
// Supabase API calls
// ---------------------------------------------------------------------------

/**
 * Deletes all rows from the exhibitions table.
 * Uses id=neq.IMPOSSIBLE_VALUE to satisfy PostgREST's filter requirement.
 */
function deleteAllExhibitions(supabaseUrl, serviceKey) {
  const url = supabaseUrl + '/rest/v1/exhibitions?id=neq.IMPOSSIBLE_VALUE';
  const response = UrlFetchApp.fetch(url, {
    method: 'delete',
    headers: {
      'apikey': serviceKey,
      'Authorization': 'Bearer ' + serviceKey,
    },
    muteHttpExceptions: true,
  });

  const code = response.getResponseCode();
  if (code < 200 || code >= 300) {
    throw new Error('DELETE failed with HTTP ' + code + ': ' + response.getContentText());
  }
}

/**
 * Inserts all valid rows into the exhibitions table.
 * Uses Prefer: resolution=merge-duplicates for upsert semantics.
 */
function insertExhibitions(rows, supabaseUrl, serviceKey) {
  if (rows.length === 0) return;

  const url = supabaseUrl + '/rest/v1/exhibitions';
  const response = UrlFetchApp.fetch(url, {
    method: 'post',
    contentType: 'application/json',
    headers: {
      'apikey': serviceKey,
      'Authorization': 'Bearer ' + serviceKey,
      'Prefer': 'resolution=merge-duplicates,return=minimal',
    },
    payload: JSON.stringify(rows),
    muteHttpExceptions: true,
  });

  const code = response.getResponseCode();
  if (code < 200 || code >= 300) {
    throw new Error('INSERT failed with HTTP ' + code + ': ' + response.getContentText());
  }
}
