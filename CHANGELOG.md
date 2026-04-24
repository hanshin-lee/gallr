# Changelog

All notable changes to gallr will be documented in this file.

## [1.3.0] - 2026-04-24

### Added
- **City-wide art event support (Phase 1).** A new Featured-tab promoted card and dedicated Event Detail screen surface active city-wide events (launch event: Loop Lab Busan 2025). Participating galleries and linked exhibitions are discoverable from a single entry point. Backed by a new `events` table, an `exhibitions.event_id` foreign key, and a new `gas/SyncEvents.gs` sync pipeline.
- **Hero image on the Featured event card (Phase 2a).** Event cards now render a cover image with a dark scrim and overlaid text, using a new `events.cover_image_url` column. Falls back gracefully to the flat brand color when no image is present.
- **List-tab surface treatments (Phase 2b).** Three new surfaces on the List tab appear automatically when a city-wide event is active:
  - A slim pinned banner above the tab row showing the event name and "NOW ON" label; tap opens Event Detail.
  - A brand-colored filter chip leading the flags row that filters the list to event-linked exhibitions.
  - A 3dp left edge and small corner label on event-linked exhibition cards for at-a-glance identification.
- All three surfaces collapse to zero footprint when no event is active, and auto-reset if an active event expires mid-session.

### Changed
- Events sync switched from delete-all-then-insert to upsert + diff-delete, eliminating the FK orphan window that previously caused linked exhibitions to briefly appear unlinked after each events-sheet edit.

## [0.0.4.0] - 2026-04-16

### Added
- Profile photo crop & resize screen with pan/pinch-to-zoom and circle overlay. Users can frame their photo before uploading.
- Skeleton placeholders on Profile tab while data loads, eliminating flash of default username/avatar.
- Keyboard dismiss on tap outside text fields in Edit Profile screen.

### Changed
- Image picker now returns raw bytes; compression happens after cropping for better quality.
- Crop overlay renders at app level with proper z-ordering on both iOS and Android.

## [0.0.3.0] - 2026-04-15

### Changed
- App now defaults to Korean on first launch, regardless of device locale. Existing users with a saved language preference are unaffected.
- Profile photo change button ("사진 변경") is now the sole tap target for the photo picker. The profile photo circle is display-only.

### Fixed
- Removed camera emoji overlay from profile photo circle, consistent with the Reductionist design system.
- Photo change button now uses Material3 TextButton with proper ripple, touch target, and disabled state dimming.

## [0.0.2.0] - 2026-04-09

### Added
- City filter chips now sorted by exhibition count (most exhibitions first). Each chip shows the count, e.g. "Seoul (42)".
- Region sub-filter chips appear below city chips when a city is selected. Multi-select support lets you combine regions (e.g. Gangnam-gu + Jongno-gu). Includes "All" chip for quick region reset.
- `CityWithCount` and `RegionWithCount` data classes for type-safe city/region filter data.
- 8 unit tests covering city sort-by-count, region grouping, active-only counting, and edge cases.

### Changed
- City filter counts only active (non-ended) exhibitions, so the displayed count matches visible results.
- Switching cities or tapping "All" automatically clears region selection.
- `GallrFilterChip` now supports a `small` variant for compact region chips.

## [0.0.1.0] - 2026-04-08

### Added
- Opening time display on exhibition detail page. When a reception has a time recorded (e.g., "5 PM"), the label now reads "Opening today, 5 PM" instead of just "Opening today". Works across all label states: today, tomorrow, weekday, and past dates. Both Korean and English locales supported.
- New `opening_time` column in the exhibitions database (nullable text, free-form entry).
- Sync pipeline support for opening time from Google Sheet to app.
- 21 unit tests covering all label states with and without opening time, both locales, and edge cases.

### Changed
- Extracted `receptionDateLabel()` from ExhibitionDetailScreen to shared module for testability. Injectable `today` parameter enables deterministic testing.
