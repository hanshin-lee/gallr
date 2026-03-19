# Feature Specification: Add Privacy Policy URL

**Feature Branch**: `009-privacy-policy-url`
**Created**: 2026-03-20
**Status**: Draft
**Input**: User description: "add privacy policy url for gallr. use gallrmap.com domain"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Access Privacy Policy from App (Priority: P1)

A user wants to review gallr's privacy policy. They find a "Privacy Policy" link within the app, tap it, and are taken to the privacy policy page on the gallrmap.com domain.

**Why this priority**: App stores (Apple App Store, Google Play) require a valid privacy policy URL for all apps. This is a compliance blocker — apps can be rejected or removed without it.

**Independent Test**: Open the app, locate the Privacy Policy link (in About or Settings), tap it — confirm `https://gallrmap.com/privacy` opens in the device browser.

**Acceptance Scenarios**:

1. **Given** the app is open on any main tab, **When** the user navigates to the Privacy Policy link, **Then** the device browser opens `https://gallrmap.com/privacy`
2. **Given** `https://gallrmap.com/privacy` is opened, **When** the page loads, **Then** it displays the gallr privacy policy content (HTTP 200, not a 404)
3. **Given** the app store listing for gallr, **When** a user views the privacy policy field, **Then** it shows `https://gallrmap.com/privacy`

---

### User Story 2 - Privacy Policy Reachable Without App (Priority: P2)

A prospective user or reviewer wants to read the privacy policy before installing the app. They visit `https://gallrmap.com/privacy` directly in a browser and read the policy.

**Why this priority**: App store reviewers and users who haven't installed the app need to access the privacy policy from the store listing. The page must work independently of the app.

**Independent Test**: Navigate to `https://gallrmap.com/privacy` in a browser without the app installed — confirm the page loads with privacy policy content.

**Acceptance Scenarios**:

1. **Given** a browser with no app installed, **When** the user navigates to `https://gallrmap.com/privacy`, **Then** the page loads with the full privacy policy
2. **Given** the privacy policy page, **When** it loads, **Then** it includes gallr's contact/data-controller information

---

### Edge Cases

- What if the device has no internet connection when the user taps the Privacy Policy link? The browser opens and shows a standard offline/network error — the app does not crash or hang.
- What if the `https://gallrmap.com/privacy` page is temporarily unavailable? The browser shows an error page — gallr app is unaffected.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The app MUST display a tappable "Privacy Policy" link accessible from within the app (About section or equivalent)
- **FR-002**: Tapping the Privacy Policy link MUST open `https://gallrmap.com/privacy` in the device's default browser
- **FR-003**: The page at `https://gallrmap.com/privacy` MUST be publicly accessible without authentication and return HTTP 200
- **FR-004**: App store listings for gallr on both Apple App Store and Google Play MUST reference `https://gallrmap.com/privacy` as the privacy policy URL
- **FR-005**: The Privacy Policy link MUST be reachable in 2 taps or fewer from any main tab of the app
- **FR-006**: The privacy policy page MUST display on mobile-sized screens without horizontal scrolling

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `https://gallrmap.com/privacy` returns HTTP 200 and displays privacy policy content within 3 seconds on a standard mobile connection
- **SC-002**: The Privacy Policy link is reachable from any main tab in 2 taps or fewer
- **SC-003**: App store submissions for both iOS and Android pass review with zero rejections due to missing or invalid privacy policy link
- **SC-004**: 100% of users who tap the Privacy Policy link are taken to `https://gallrmap.com/privacy` — no broken links or incorrect destinations

## Assumptions

- The gallr domain is `gallrmap.com` — privacy policy URL is `https://gallrmap.com/privacy`
- Privacy policy legal text is provided separately by the product owner; this feature covers the URL placement in-app, the hosted page, and store metadata
- The in-app link is placed in an About screen or similar non-primary navigation area
- Both iOS and Android apps require the link
- No authentication or login required to view the privacy policy
