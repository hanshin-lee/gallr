# Feature Specification: Gallery Data Sync from Google Sheets

**Feature Branch**: `007-gallery-data-sync`
**Created**: 2026-03-20
**Status**: Draft
**Input**: User description: "Use an external Google Sheet on Google Drive as the managed data source for gallery/exhibition listings in gallr. Changes to the sheet are automatically reflected in the app. Supabase is the intended backend."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — App Displays Live Data from the Spreadsheet (Priority: P1)

A user opens gallr and browses the Featured, List, and Map tabs. All exhibition and venue data they see comes from the managed Google Sheet — not from hardcoded or mock data baked into the app. When the sheet has current, accurate information, the app faithfully presents it. The curator who maintains the spreadsheet is the sole person responsible for content quality; no developer involvement is needed to update what the app shows.

**Why this priority**: This is the core value of the feature. Without live data flowing from the spreadsheet into the app, nothing else works. All other stories build on top of this pipeline being established.

**Independent Test**: Add a test exhibition row to the Google Sheet with a distinctive title (e.g. "TEST GALLERY 001"). Open gallr — the exhibition should appear in the List and Featured tabs with the correct name, venue, dates, and location. Delete the row from the sheet — the exhibition should disappear from the app within 5 minutes.

**Acceptance Scenarios**:

1. **Given** the Google Sheet contains 10 exhibition rows, **When** a user opens the gallr List tab, **Then** all 10 exhibitions are displayed with their correct names, venues, dates, and locations as entered in the sheet.
2. **Given** the app is open, **When** a curator adds a new row to the sheet, **Then** the new exhibition appears in the app within 5 minutes — without any app release, code change, or developer action.
3. **Given** a row exists in the sheet and the app displays it, **When** the curator updates that row's closing date, **Then** the updated date is reflected in the app within 5 minutes.
4. **Given** the Google Sheet is the data source, **When** a user opens the Map tab, **Then** map pins reflect only exhibitions present in the sheet — no hardcoded or legacy data appears.

---

### User Story 2 — Curator Manages Listings Without Technical Assistance (Priority: P2)

A non-technical curator opens the Google Sheet on their computer, adds a new exhibition row, edits an existing venue name, or marks an exhibition as inactive by deleting the row. They do not need to contact a developer, run any tool, or perform any extra step — the sheet is the single source of truth and saving changes in the sheet is sufficient. The curator can use the familiar spreadsheet interface they already know.

**Why this priority**: The reason for using a spreadsheet as the data source is to empower a non-technical content owner. If the curator needs any technical step to publish their changes, the feature fails its purpose. This story validates the end-to-end editorial workflow.

**Independent Test**: Hand the Google Sheet link to someone with no technical background. Ask them to add one new exhibition row and update the venue name of an existing row. Within 5 minutes, both changes should be visible in the gallr app without any developer action.

**Acceptance Scenarios**:

1. **Given** the curator has edit access to the Google Sheet, **When** they add a new row with all required fields filled in, **Then** the new exhibition appears in the app automatically — no additional publish step required.
2. **Given** an exhibition row exists in the sheet, **When** the curator changes the exhibition title or venue, **Then** the app reflects the updated information within 5 minutes.
3. **Given** the curator deletes a row from the sheet, **When** the deletion is saved, **Then** the exhibition is removed from the app within 5 minutes.
4. **Given** the curator leaves a required field (e.g. exhibition name) blank in a row, **When** the sheet is synced, **Then** that incomplete row is silently skipped and the curator's other valid rows still appear in the app — no crash or data loss occurs.

---

### User Story 3 — App Remains Functional During Sync Failures (Priority: P3)

A user opens gallr while the data sync is temporarily unavailable — perhaps the spreadsheet service is down, the connection is lost, or a sync job has failed. The app continues to show the most recently successfully synced data. The user is not shown an empty list, a crash, or an error state. The content may be slightly stale but the app remains fully functional.

**Why this priority**: Resilience is a quality-of-life requirement. The app should degrade gracefully rather than break entirely when the data pipeline has a transient issue. This story is P3 because the app is still valuable with occasional staleness, but it is not acceptable to show users nothing.

**Independent Test**: Temporarily revoke the sync job's access to the Google Sheet. Open gallr — the last successfully synced listings should still be visible. Restore access and wait 5 minutes — fresh data should reappear without a user action.

**Acceptance Scenarios**:

1. **Given** the data sync has not run in the last hour due to an error, **When** a user opens the app, **Then** the last successfully synced exhibition data is displayed — the app does not show an empty list.
2. **Given** a sync failure occurs, **When** the issue is resolved and the sync next runs successfully, **Then** the app data updates to reflect the current sheet state without any user action.
3. **Given** the app is displaying cached data due to a sync failure, **When** a user opens and browses the app, **Then** no error message, crash, or empty state is shown — the experience is identical to a fully live state.

---

### Edge Cases

- What happens when the Google Sheet is completely empty (no data rows)? The app should show an empty state message rather than crashing or showing stale data.
- What happens when a row has a malformed date (e.g. text in a date field)? That row should be skipped and logged; other valid rows must still appear.
- What happens when the same exhibition appears twice (duplicate rows)? Both rows are treated as separate exhibitions unless they share a unique identifier column.
- What happens when the sheet has more rows than the app can display efficiently? The system should support at least 500 exhibition rows without performance degradation.
- What happens when a curator accidentally deletes all rows? The app shows an empty state; the data is not irreversibly lost since Google Sheets maintains version history.
- What happens when the sync job runs and the sheet data is identical to what is already stored? The app data is unchanged; no spurious update events are triggered.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The app MUST display exhibition and venue data sourced from the managed spreadsheet, not from hardcoded or bundled data files.
- **FR-002**: Changes made to the spreadsheet (add, edit, delete rows) MUST be reflected in the app within 5 minutes, without any developer action or app release.
- **FR-003**: The data pipeline MUST run automatically in response to spreadsheet changes or on a regular schedule — no manual trigger required from a curator or developer.
- **FR-004**: The system MUST validate incoming rows before storing them; rows missing required fields (exhibition name, venue name, opening date, closing date, latitude, longitude) MUST be skipped, not crash the pipeline.
- **FR-005**: The system MUST retain the most recently valid dataset so the app can display content even when the sync pipeline is temporarily unavailable.
- **FR-006**: The curator MUST be able to manage all exhibition data exclusively through the spreadsheet interface — no other tool, admin panel, or technical access is required for content updates.
- **FR-007**: The spreadsheet MUST serve as the single source of truth; the system MUST NOT merge spreadsheet data with any other independently maintained data source.
- **FR-008**: The system MUST support adding, updating, and removing exhibitions by editing the spreadsheet rows — create, update, and delete operations MUST all be reflected in the app.
- **FR-009**: Each exhibition row in the spreadsheet MUST map to the same fields currently supported by the app: exhibition name, venue name, city/region, opening date, closing date, geographic coordinates, and a category or type label.
- **FR-010**: The system MUST log sync failures with enough detail (timestamp, error description, affected row count) to allow a developer to diagnose the problem without accessing the spreadsheet directly.

### Key Entities

- **Exhibition**: A single cultural event or display; the primary content unit in gallr. Fields: name, venue name, city/region, opening date, closing date, latitude, longitude, category. Sourced from one row in the spreadsheet.
- **SyncJob**: A scheduled or event-triggered process that reads the spreadsheet, validates rows, and writes the result to the persistent data store. Has a status (success/failure), a timestamp, and a count of rows imported.
- **SyncLog**: A record of each sync run; captures timestamp, status, number of rows processed, number of rows skipped (with reasons), and any error messages. Used for monitoring and debugging.
- **SpreadsheetSource**: A reference to the external spreadsheet (identifier, last-known-modified timestamp). The system tracks this to detect when the sheet has changed and a sync is warranted.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A change made to the spreadsheet (add, edit, or delete a row) is visible in the gallr app within 5 minutes — verifiable by timing the end-to-end update from sheet save to app display.
- **SC-002**: A non-technical curator can add 5 new exhibition listings to the spreadsheet, without developer assistance, and see all 5 appear in the app within 5 minutes — verifiable by observation.
- **SC-003**: 100% of valid spreadsheet rows (all required fields present and correctly formatted) appear in the app after a successful sync — verifiable by comparing row count in sheet vs. listing count in app.
- **SC-004**: Invalid rows (missing required fields or malformed dates) are skipped silently; 0 app crashes or empty-state errors occur due to bad data — verifiable by intentionally introducing a bad row and observing app behaviour.
- **SC-005**: When the sync pipeline is down for up to 1 hour, the app continues to display the last successfully synced data — verifiable by disabling the sync and confirming the app still shows content.
- **SC-006**: The system supports at least 500 exhibition rows in the spreadsheet without visible performance degradation in the app's list or map views — verifiable by load testing with a 500-row sheet.

---

## Assumptions

- The Google Sheet structure will be flat (one row per exhibition, one sheet/tab); hierarchical or multi-tab layouts are out of scope for this feature.
- The curator has a Google account and can be granted edit access to the shared Google Sheet.
- Geographic coordinates (latitude and longitude) are provided directly in the spreadsheet by the curator; reverse geocoding from an address is out of scope.
- Image URLs for exhibitions, if needed, will be hosted externally (e.g. Google Drive public link or an image hosting service) and stored as a URL column in the sheet; in-app image upload is out of scope.
- The sync pipeline will be server-side (not run on a user's device); a cloud function or server process reads the sheet and updates the backend data store on behalf of all users.
- The existing mock/hardcoded exhibition data in the app will be fully replaced by this feature; no hybrid of real and mock data will remain after successful implementation.
- A Supabase project will be used as the persistent data store and the data layer the app queries; this is the intended backend technology.
- The Google Sheet will be the **only** way to manage exhibition content; no in-app admin panel or CMS is in scope for this feature.

## Out of Scope

- An in-app admin panel or CMS for managing exhibition data.
- User authentication or role-based access control within gallr (the spreadsheet's own sharing permissions control who can edit).
- Real-time push notifications to app users when new exhibitions are added.
- Reverse geocoding (converting text addresses to coordinates); curators must enter lat/lng directly.
- Multi-source data merging (e.g. combining spreadsheet data with a separate ticketing API).
- Offline-first sync for users who have no network connection; the app requires connectivity to display up-to-date data.
- Image storage or upload; image URLs are managed externally by the curator.
- Historical sync audit trail visible to the curator; logs are developer-facing only.
