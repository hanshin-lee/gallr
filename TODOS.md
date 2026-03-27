# TODOS

Last updated: 2026-03-27 (from /plan-ceo-review)

## P1 — Post-Launch

### Push Notifications
Weekly "N new exhibitions near you" push via FCM (Android) + APNs (iOS). Primary retention mechanism. Needs server-side trigger (Cloud Function or GAS extension). Depends on basic analytics being in place.
- Effort: M (human) → S (CC: ~1 day)
- Context: Design doc identifies retention as key initiative. Without a trigger, users forget to open the app.

## P2 — Quality of Life

### Open in Maps
Button on ExhibitionDetailScreen to open Apple Maps / Naver Map / Google Maps with exhibition coordinates. Completes the discover → save → navigate → visit loop.
- Effort: S (CC: ~30 min)
- Context: Latitude/longitude already in data model but unused on detail screen.

### Featured/Editor's Pick Badges
Show visual badges on detail screen and cards for featured / editor's pick exhibitions.
- Effort: S (CC: ~30 min)
- Context: `isFeatured` and `isEditorsPick` fields exist in data model.

### Bookmark Cloud Sync
Sync bookmarks across devices via Supabase (requires user identity).
- Effort: M (CC: ~1 day)
- Context: Currently DataStore-only (local). Multi-device users lose bookmarks.

## P3 — Technical Debt

### ViewModel Splitting
Split TabsViewModel (15+ StateFlows) into domain-specific ViewModels (ExhibitionViewModel, FilterViewModel, MapViewModel). Cleaner separation, easier testing.
- Effort: M (human) → S (CC: ~2 hours)
- Context: Single VM is manageable now but approaching god-object threshold.

### Proper Logging Framework
Replace println() calls with Napier or similar KMP logging library. Production crashes and errors are currently invisible.
- Effort: S (CC: ~1 hour)
- Context: TabsViewModel.kt:229,244 use println() for error logging.

### Full Analytics Dashboard
Expand basic 3-event logging to a proper analytics solution (Mixpanel, Amplitude, or Supabase dashboard).
- Effort: M (CC: ~1 day)
- Depends on: Basic analytics events being in place first.

### GAS Stale Record Cleanup
After switching to UPSERT, exhibitions deleted from the Google Sheet stay in Supabase. Add `last_synced_at` column and periodic cleanup of records not seen in recent syncs.
- Effort: S (CC: ~1 hour)
- Context: UPSERT fixes the destructive sync issue but introduces stale data risk.
