# Research: Add Privacy Policy URL

**Branch**: `009-privacy-policy-url` | **Date**: 2026-03-20

---

## Finding 1: Cross-Platform URL Opening in Compose Multiplatform

**Decision**: Use `LocalUriHandler.current.openUri(url)` from `androidx.compose.ui.platform`.

**Rationale**: `LocalUriHandler` is a built-in Compose Multiplatform API that delegates to the platform's default browser — `Intent.ACTION_VIEW` on Android, `UIApplication.openURL` on iOS. No `expect/actual` declarations or platform-specific code needed. The URL constant can be a plain `private const val` in `App.kt`.

**Alternatives considered**:
- `expect/actual` platform URL opener — unnecessary; CMP provides this out of the box.
- Ktor or custom HTTP — not relevant; we are opening a URL in the system browser, not making an HTTP request from the app.

---

## Finding 2: In-App Link Placement

**Decision**: Add an `IconButton` (info/ⓘ icon) to the `actions` slot of the existing `TopAppBar` in `App.kt`. Tapping it opens `https://gallrmap.com/privacy` via `LocalUriHandler`.

**Rationale**: The app has exactly three tabs (FEATURED, LIST, MAP) and no existing About or Settings screen. Adding a 4th tab just for a privacy policy link violates Principle III (Simplicity & YAGNI). A TopAppBar action icon is the minimal, standard pattern — one tap from any tab satisfies FR-005 (≤ 2 taps). No new screen or navigation stack needed.

**Alternatives considered**:
- New "ABOUT" tab — would require a new tab entry in `GallrNavigationBar`, new screen composable, new `TabsViewModel` state. Disproportionate for a single link.
- Bottom sheet/dialog — adds state management overhead. Unnecessary when the URL can be opened directly on tap.
- Text link at bottom of each screen — would require changes to three screen composables instead of one entry point.

---

## Finding 3: Privacy Policy Web Page

**Decision**: Create `web/privacy.html` as a new Eleventy page using the existing `base.html` layout. The file uses the same front matter (`layout: base.html`) as `index.html`.

**Rationale**: The web project is already an Eleventy 3.x static site in `web/`, deployed to gallrmap.com via Vercel (root `vercel.json`). Adding a `privacy.html` file in `web/` is consistent with the existing structure and Eleventy will route it to `/privacy`. No new tooling or dependencies required. The `base.html` layout includes the site header, footer, and stylesheet — the privacy page inherits the correct design automatically.

**Alternatives considered**:
- Separate repo for privacy page — unnecessary complexity.
- New subdirectory `web/privacy/index.html` — works identically in Eleventy, but `web/privacy.html` is simpler.

---

## Finding 4: Privacy Policy Legal Content

**Decision**: Ship placeholder privacy policy content at launch; replace with legal text provided by the product owner before App Store submission.

**Rationale**: The spec explicitly notes: "Privacy policy legal text is provided separately by the product owner." The technical deliverable (a reachable, mobile-responsive page at `https://gallrmap.com/privacy`) is independent of the final legal wording.

**Alternatives considered**:
- Block implementation until legal text is ready — would block the in-app link and store metadata changes unnecessarily.

---

## Finding 5: Scope Boundary

**Decision**: Two changed artifacts only:
1. `composeApp/src/commonMain/kotlin/com/gallr/app/App.kt` — add info icon action
2. `web/privacy.html` — new static privacy policy page

No changes to `shared/`, `androidMain/`, `iosMain/`, or any other file.

**Rationale**: `LocalUriHandler` is cross-platform — no platform-specific code needed. The URL is a UI concern living in `composeApp/commonMain/`; no business logic involved, so `shared/` is untouched (Constitution Principle VI satisfied). The web page is a new static file in the existing web directory.
