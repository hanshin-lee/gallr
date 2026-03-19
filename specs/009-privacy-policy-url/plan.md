# Implementation Plan: Add Privacy Policy URL

**Branch**: `009-privacy-policy-url` | **Date**: 2026-03-20 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/009-privacy-policy-url/spec.md`

## Summary

gallr has no privacy policy link — required for App Store and Google Play compliance. The fix adds two artifacts: (1) an info icon in the app's existing TopAppBar that opens `https://gallrmap.com/privacy` in the device browser using Compose Multiplatform's `LocalUriHandler`, and (2) a new static `privacy.html` page in the existing Eleventy web project that deploys to gallrmap.com via Vercel.

## Technical Context

**Language/Version**: Kotlin 2.1.20 (KMP composeApp module); HTML5/CSS3 (web/privacy.html)
**Primary Dependencies**: Compose Multiplatform 1.8.0 (`LocalUriHandler` from `androidx.compose.ui.platform`); Eleventy 3.x (web static site, existing)
**Storage**: N/A — static URL constant; no persistence
**Testing**: Manual visual verification (platform UI layer exempt per Constitution Principle II). Acceptance: info icon opens browser to `https://gallrmap.com/privacy`.
**Target Platform**: Android + iOS (KMP app); gallrmap.com (web via Vercel/Eleventy)
**Project Type**: KMP mobile app + static web page (existing Eleventy site)
**Performance Goals**: Privacy policy page loads within 3 seconds (SC-001)
**Constraints**: In-app change isolated to `composeApp/commonMain/App.kt` only; no `shared/`, no platform modules, no new dependencies
**Scale/Scope**: Two files — one modified (`App.kt`), one created (`web/privacy.html`)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Spec-First | ✅ Pass | `spec.md` written and validated before implementation |
| II. Test-First | ✅ Pass (exempt) | UI-only change (tappable link) + static HTML page. No business logic. Manual acceptance via quickstart.md. |
| III. Simplicity & YAGNI | ✅ Pass | Minimal change: one `IconButton` in existing `TopAppBar`, one `privacy.html` page. No new screens, tabs, abstractions, or dependencies. |
| IV. Incremental Delivery | ✅ Pass | US1 (in-app link) and US2 (web page) are independently deliverable and testable. |
| V. Observability | ✅ Pass | No new silent failure paths. `LocalUriHandler.openUri` failure surfaces through the platform's standard browser error handling. |
| VI. Shared-First | ✅ Pass | URL is a UI constant in `composeApp/commonMain`. No business logic. `shared/` untouched. |

No violations. Complexity Tracking table not required.

## Project Structure

### Documentation (this feature)

```text
specs/009-privacy-policy-url/
├── plan.md          ← this file
├── spec.md          ← feature specification
├── research.md      ← Phase 0: findings on URL handling + web structure
├── quickstart.md    ← Phase 1: acceptance verification steps
└── checklists/
    └── requirements.md
```

### Source Code (affected files only)

```text
composeApp/
└── src/
    └── commonMain/
        └── kotlin/
            └── com/gallr/app/
                └── App.kt           ← add info IconButton to TopAppBar actions

web/
└── privacy.html                     ← new static privacy policy page
```

No changes to:
- `shared/` (no business logic)
- `androidMain/` or `iosMain/` (LocalUriHandler handles both platforms)
- `GallrNavigationBar.kt` (no new tab)
- Any existing screen composables

**Structure Decision**: `composeApp/commonMain` for the URL constant and the `IconButton` addition — this is UI presentation code. `web/privacy.html` extends the existing Eleventy site at gallrmap.com.

## Implementation

### What changes in `App.kt`

Add an `actions` slot to the existing `TopAppBar` with an info `IconButton`. Tapping it calls `LocalUriHandler.current.openUri(PRIVACY_POLICY_URL)`.

```kotlin
private const val PRIVACY_POLICY_URL = "https://gallrmap.com/privacy"

// In TopAppBar:
actions = {
    val uriHandler = LocalUriHandler.current
    IconButton(onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) }) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Privacy Policy",
        )
    }
}
```

### What changes in `web/privacy.html`

New Eleventy page at `web/privacy.html` using the existing `base.html` layout. Eleventy routes this to `https://gallrmap.com/privacy`.

```html
---
layout: base.html
---

<section class="privacy">
  <div class="privacy__inner">
    <h1>Privacy Policy</h1>
    <!-- Legal text to be provided by product owner -->
    <p>Last updated: [DATE]</p>
    <p>gallr ("we", "our", "us") operates the gallr mobile application...</p>
    <!-- ... full legal text ... -->
  </div>
</section>
```

### App Store Metadata (manual step)

After both changes are deployed:
1. **Apple App Store Connect**: Enter `https://gallrmap.com/privacy` in the Privacy Policy URL field for the gallr app.
2. **Google Play Console**: Enter `https://gallrmap.com/privacy` in the Store Listing → Privacy Policy URL field.

## Phase 0 Findings Summary

See [`research.md`](research.md) for full details.

- URL handling: `LocalUriHandler.current.openUri(url)` — built into CMP, no `expect/actual` needed
- In-app placement: `IconButton` in `TopAppBar` `actions` — one tap from any tab, zero new screens
- Web: new `web/privacy.html` using existing `base.html` layout — Eleventy routes to `/privacy`
- Scope: 2 files only (`App.kt` + `web/privacy.html`); no `shared/`, no platform modules

## Acceptance Verification

See [`quickstart.md`](quickstart.md) for step-by-step verification of all success criteria.
