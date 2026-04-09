# Changelog

All notable changes to gallr will be documented in this file.

## [0.0.1.0] - 2026-04-08

### Added
- Opening time display on exhibition detail page. When a reception has a time recorded (e.g., "5 PM"), the label now reads "Opening today, 5 PM" instead of just "Opening today". Works across all label states: today, tomorrow, weekday, and past dates. Both Korean and English locales supported.
- New `opening_time` column in the exhibitions database (nullable text, free-form entry).
- Sync pipeline support for opening time from Google Sheet to app.
- 21 unit tests covering all label states with and without opening time, both locales, and edge cases.

### Changed
- Extracted `receptionDateLabel()` from ExhibitionDetailScreen to shared module for testability. Injectable `today` parameter enables deterministic testing.
