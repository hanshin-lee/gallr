# Feature Specification: Web Reductionist Design System

**Feature Branch**: `006-web-reductionist-theme`
**Created**: 2026-03-20
**Status**: Draft
**Input**: User description: "apply similar ui theme changes to web applied in 005-reductionist-design-system for kmp app"

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Editorial Identity Through Neo-Grotesque Typography (Priority: P1)

A visitor lands on the gallr web presentation site and immediately perceives the same editorial, utilitarian aesthetic they see in the mobile app. Body copy, headings, labels, and footer text all share the same neo-grotesque sans-serif typeface. There is no visual discontinuity between the mobile app identity (005) and the web presence — both feel like a single, controlled publication.

**Why this priority**: Typography is the most pervasive single-token change. It underlies every section of the site. Until it is applied, the #FF5400 accent and spacing updates exist in an inconsistent visual context. This must ship before or alongside any other story.

**Independent Test**: Open the web homepage in a browser. Scan every visible text element — headings, body paragraphs, labels, metadata, navigation wordmark, footer copy. Every text node must render in the same neo-grotesque sans-serif typeface. No serif or monospaced characters should be visible anywhere on the page.

**Acceptance Scenarios**:

1. **Given** the homepage is loaded, **When** a visitor reads the hero headline, tagline, and feature card headings, **Then** all text renders in a neutral neo-grotesque sans-serif typeface with no stylized or expressive weights.
2. **Given** the homepage is loaded, **When** a visitor inspects the section labels (e.g. "FEATURES", "ABOUT"), button labels, and footer copy, **Then** these elements also use the same neo-grotesque typeface — not a monospaced or slab alternative.
3. **Given** the web site and the mobile app are viewed side-by-side, **When** a visitor compares the two, **Then** both products share the same typographic identity — they are visually recognisable as the same design system.
4. **Given** the site loads on a slow connection, **When** the custom font has not yet loaded, **Then** a neutral sans-serif system font is used as a fallback, preserving layout without visible text reflow or content shift.

---

### User Story 2 — #FF5400 Accent on Primary CTAs (Priority: P2)

A visitor lands on the page and immediately identifies the download CTA or the primary action button as the key interactive element. The button is rendered in vivid orange (#FF5400) against a white background, making it the sole colour-differentiated control on the page. All other interactive elements remain monochrome. The visitor understands immediately what to act on without colour explanation.

**Why this priority**: The primary CTA is the conversion goal of the presentation site. Without clear visual hierarchy through the accent colour, the page fails its core purpose. This is the direct parallel to the bookmark/action CTA update in 005.

**Independent Test**: Open the homepage. Identify all buttons and interactive controls on the page. Confirm: exactly one category of button uses #FF5400 (the primary/download CTA). All other buttons (outline style, secondary actions) are monochrome. Click or tap the primary CTA — the active/pressed state shifts via opacity or contrast within 100ms, with no animation or movement.

**Acceptance Scenarios**:

1. **Given** the homepage displays download or primary action buttons, **When** a visitor scans the page, **Then** the primary CTA is the sole orange-coloured element and all other interactive controls are monochrome.
2. **Given** a primary CTA button is in its default state, **When** a visitor hovers over or taps it, **Then** the button's interactive state is communicated immediately through a contrast or opacity shift of #FF5400 — not through movement, animation, or scale change.
3. **Given** the page is displayed with no primary CTA visible in the viewport, **When** a visitor inspects the visible content, **Then** #FF5400 does not appear — the palette is entirely monochrome.
4. **Given** a visitor uses keyboard focus to reach the primary CTA, **When** the element receives focus, **Then** a visible focus ring appears that is consistent with the primary CTA's orange identity.

---

### User Story 3 — Whitespace as the Sole Section Separator (Priority: P3)

A visitor scrolls through the homepage sections — Hero, Features, Downloads, About. The transition between sections is communicated through generous whitespace alone. There are no thick decorative border rules, coloured bands, or ornamental dividers between sections. The content fills the full visual hierarchy without structural ornamentation interrupting the reading flow.

**Why this priority**: The thick black section border currently on the site acts as structural decoration — it is the web equivalent of the `HorizontalDivider` elements removed from the KMP app in 005. Removing it completes the parity. It is P3 because the site remains fully functional without this change.

**Independent Test**: Open the homepage and scroll from Hero to Features to Downloads to About. Confirm: no thick horizontal rules or coloured lines divide sections. Each section transition uses only top padding/margin as the visual separator. The content hierarchy is communicated through typography and whitespace alone.

**Acceptance Scenarios**:

1. **Given** the homepage is displayed, **When** a visitor scrolls between any two sections, **Then** the transition is marked by whitespace only — no decorative border, divider, or coloured band is visible.
2. **Given** no section borders are present, **When** a visitor scans the page, **Then** section boundaries are still clearly legible through heading typographic contrast and vertical spacing.
3. **Given** a section with a heading and body content, **When** a visitor inspects the space above the heading, **Then** the gap clearly indicates a new section without requiring a decorative line.

---

### Edge Cases

- What happens when the Inter font fails to load entirely? The visitor must still see readable text — the font-stack fallback must specify a neutral sans-serif system font (e.g. system-ui, -apple-system, Arial) and not fall back to a serif or monospaced typeface.
- What if #FF5400 orange on white does not meet WCAG AA contrast for text? Orange must be used only for button fills and interactive targets with sufficient size (minimum 44×44px touch target), not for body text or small labels — contrast requirement for interactive components (3:1) is met at that size.
- What if both a primary and a secondary CTA appear side by side? Only the primary CTA uses #FF5400; the secondary (outline) CTA remains monochrome (black border, white fill), ensuring clear hierarchy between the two.
- What if the site gains additional pages in future? The design token additions (accent colour, font family variable) must be defined in `tokens.css` so all future pages inherit the system without per-page overrides.
- What happens when sections are added or reordered? Since separators are whitespace-only (spacing tokens), reordering sections requires no structural separator adjustments.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: All typographic elements (headings, body, labels, meta, footer, buttons, wordmark) MUST use a neo-grotesque sans-serif typeface; no serif or monospaced typefaces are permitted in the updated design.
- **FR-002**: A single design token for the neo-grotesque font family MUST be defined and used consistently across all typographic rules — no individual elements may reference the serif or monospace font variables after this update.
- **FR-003**: The system MUST define an accent colour token for #FF5400 that is used exclusively for: primary CTA button fill, and interactive state feedback (hover/active/focus on primary CTA).
- **FR-004**: Primary CTA buttons MUST use the #FF5400 accent token as their default background fill with white text; their hover/active state MUST communicate the state change through a contrast or opacity shift within 100ms — not animation or movement.
- **FR-005**: All non-primary interactive controls (outline buttons, text links, secondary actions) MUST remain strictly monochrome; the #FF5400 accent MUST NOT appear on these elements.
- **FR-006**: Decorative section-separator borders between major page sections MUST be removed; section transitions MUST be communicated through vertical spacing (whitespace) alone.
- **FR-007**: Spacing used for section separation MUST reference existing design tokens (spacing scale) rather than hardcoded pixel values.
- **FR-008**: All primary CTA interactive targets MUST maintain a minimum tappable/clickable area of 44×44 pixels.
- **FR-009**: Font loading MUST specify a neutral sans-serif fallback font stack so the layout remains readable and visually consistent before the web font loads.
- **FR-010**: The accent colour (#FF5400) MUST NOT be used for backgrounds, decorative fills, text on small targets, or any purpose beyond the three designated roles (primary CTA, active state, interaction feedback).

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of visible text on the homepage renders in the neo-grotesque sans-serif typeface — verifiable by visual audit in all major browsers (Chrome, Safari, Firefox) with the web font loaded.
- **SC-002**: #FF5400 appears on no more than one button category per page (primary CTA) — verifiable by visual inspection of the homepage.
- **SC-003**: 3 of 3 independent reviewers identify the primary download/action CTA as the highest-priority interactive element within 3 seconds of page load, without colour explanation.
- **SC-004**: No thick decorative horizontal rules or coloured section-separator borders are visible when scrolling the full homepage — verifiable by visual inspection.
- **SC-005**: The primary CTA button interactive state change (hover/press) is observable within 100ms — verifiable by browser DevTools or slow-motion device testing.
- **SC-006**: The web site and mobile app (005) are assessed by a reviewer as sharing the same visual identity — same typeface character, same accent colour role, same absence of decorative elements.
- **SC-007**: The page passes WCAG AA contrast (4.5:1) for all body text; the primary CTA passes WCAG AA for large interactive components (3:1) — verifiable with a contrast audit tool.

---

## Assumptions

- This spec applies the same design principles from `005-reductionist-design-system` (KMP app) to the `003-gallr-presentation-web` site, ensuring visual parity between the two products.
- The web presentation site is a static marketing/landing page; all style changes are in CSS token and component files — no backend or JavaScript logic is required.
- The same Inter typeface used in the KMP app will be self-hosted as WOFF2 for the web, replacing the current Playfair Display and JetBrains Mono WOFF2 files.
- The existing spacing token scale in `tokens.css` is already 8pt-based and does not need restructuring; only the section separator approach changes.
- Dark mode is out of scope; the site operates in light mode only.
- The `--color-paper-alt` background used in the Downloads section is a functional zone marker, not decoration, and is retained.
- Existing border tokens used for card outlines (`--border-ink`) and header/footer hairline rules (`--border-hairline`) are structural and are retained; only `--border-section` (the thick decorative section-top rule) is removed from active use.

## Out of Scope

- Dark mode or system-theme-responsive theming.
- Any new page layouts, sections, or content changes.
- Changes to SVG logo or icon assets.
- Navigation or routing changes.
- Mobile app (KMP) — covered by feature `005-reductionist-design-system`.
- Changes to `--color-paper-alt` background on the Downloads section.
- Any animation or scroll-triggered effects.
- Any use of #FF5400 beyond primary CTA and its hover/focus states.
