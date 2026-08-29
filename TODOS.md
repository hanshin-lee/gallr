# TODOS

Last updated: 2026-08-29. Revalidate external service and release status before
acting on older operational entries.

## P1 — Post-Launch

### Push Notifications
Weekly "N new exhibitions near you" push via FCM (Android) + APNs (iOS). Primary retention mechanism. Needs a reviewed server-side scheduler and delivery worker; do not revive the retired Apps Script pipeline. Depends on basic analytics being in place.
- Effort: M (human) → S (CC: ~1 day)
- Context: Design doc identifies retention as key initiative. Without a trigger, users forget to open the app.

## P3 — Technical Debt

### Full Analytics Dashboard
Expand basic 3-event logging to a proper analytics solution (Mixpanel, Amplitude, or Supabase dashboard).
- Effort: M (CC: ~1 day)
- Depends on: Basic analytics events being in place first.

### Debounce public-site rebuilds triggered by the outbox
`outbox-delivery` POSTs the Vercel deploy hook once per `exhibition.published`,
`exhibition.archived`, and `exhibition.restored` event. Now that those builds
actually run, a heavy staff editing session queues one full Eleventy build plus
Supabase fetch per event. Steady state is fine; bursts are wasteful.
- Effort: S (CC: ~1h)
- Options: coalesce events in the function over a short window, or move the hook
  POST behind a scheduled drain instead of a per-event fire.
- Noticed on: shin/gallr-gallery-publish-error-464c65 while investigating the
  cancelled rebuild that made newly published exhibitions 404 (fixed in #228).
