# Feature Specification: Reductionist Design System

**Feature Branch**: `005-reductionist-design-system`
**Created**: 2026-03-19
**Status**: Draft
**Input**: User description: "Design a digital interface with a strictly utilitarian, reductionist design philosophy — monochrome base with #FF5400 accent, neo-grotesque typography, grid-driven layout, state-driven interactions."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Immediate Editorial Identity on Launch (Priority: P1)

A user opens gallr for the first time and immediately perceives a purposeful, editorial aesthetic: a strict black-and-white palette, clean sans-serif typography, sharp-edged cards, and generous whitespace that frames content as the sole focus. Every screen — Featured, List, and Map — feels like a single controlled publication. Nothing competes with the content for attention.

**Why this priority**: The visual identity is the foundation that all other stories build on. Without it, interaction states and layout refinements are incoherent. This story must be independently complete before any other design work delivers value.

**Independent Test**: Launch the app from scratch; all three tabs show only black, white, and neutral grays — no decorative color, no rounded corners, no shadows — using a neo-grotesque sans-serif throughout. The orange (#FF5400) is absent unless an active state or CTA is visible.

**Acceptance Scenarios**:

1. **Given** the app is open on any tab, **When** a user scans the screen, **Then** all text uses a neo-grotesque sans-serif typeface with no stylized weights or expressive treatments.
2. **Given** any tab is visible, **When** a user inspects card and container edges, **Then** all corners are sharp (zero radius) and no drop shadows or gradients are present.
3. **Given** any tab is visible with no active state and no CTA in focus, **When** a user inspects the color palette, **Then** only black (#000000), white (#FFFFFF), and neutral grays are present — #FF5400 does not appear.
4. **Given** any section of the app is displayed, **When** a user inspects spacing, **Then** functional zones are separated by whitespace rather than decorative dividers, borders, or ornamental elements.

---

### User Story 2 — Unambiguous Active State and Navigation Feedback (Priority: P2)

A user navigates between tabs and activates filters. The currently active tab and selected filter chip are immediately distinguishable from inactive states. The orange accent (#FF5400) appears precisely at the active indicator and nowhere else on the navigation bar or filter row, making the user's current context unmistakable without visual noise.

**Why this priority**: Navigation clarity is a functional requirement — users must know where they are and what is selected. The orange accent earns its presence here because color shift is the most legible state signal without resorting to animation.

**Independent Test**: Tap each tab in sequence; the active tab indicator turns #FF5400 while all other tabs remain in the neutral color system. Activate a filter chip; the chip's active state uses #FF5400 or a high-contrast fill derived from it, while inactive chips remain monochrome. No other orange elements are visible.

**Acceptance Scenarios**:

1. **Given** the bottom navigation bar is visible, **When** a user switches tabs, **Then** the active tab indicator immediately displays in #FF5400 and all other tab indicators are monochrome.
2. **Given** filter chips are visible on the List tab, **When** a user activates a filter, **Then** the selected chip immediately shifts to its active state using #FF5400 as the differentiating signal.
3. **Given** a filter chip is active, **When** a user inspects other filter chips, **Then** inactive chips remain strictly monochrome (black outline / white fill or inverse).
4. **Given** any interactive element is in hover or focused state (web/desktop), **When** the user inspects the element, **Then** state change is communicated via contrast or opacity shift — not animation or movement.

---

### User Story 3 — Primary Action and CTA Clarity (Priority: P2)

A user encounters a primary call-to-action — a bookmark button, a confirmation action, or a primary navigation trigger. The CTA is immediately distinguishable from secondary and tertiary controls through its use of #FF5400. The user does not need to search for the primary action; functional hierarchy makes it self-evident.

**Why this priority**: CTAs represent decision points. If they are not immediately legible within the monochrome hierarchy, the user experience degrades into ambiguity. Orange is reserved precisely for this signal — using it here justifies its presence in the system.

**Independent Test**: Open the exhibition detail view (or the bookmark action). Confirm the primary action is the only orange-colored element visible on screen. All secondary actions and controls remain monochrome. Tap the primary action; the active state responds immediately via a contrast shift using #FF5400.

**Acceptance Scenarios**:

1. **Given** a screen with both primary and secondary actions, **When** a user scans the controls, **Then** the primary CTA is the sole orange-colored element and secondary actions are monochrome.
2. **Given** a primary CTA button is displayed in its default state, **When** a user taps or clicks it, **Then** the button's active state responds immediately (under 100ms) through a contrast or opacity shift — no animation.
3. **Given** a primary CTA is in disabled state, **When** a user inspects it, **Then** it is visually subdued (reduced opacity or gray fill) and clearly non-interactive — not orange.
4. **Given** a page with no primary CTA, **When** a user inspects the full screen, **Then** #FF5400 does not appear — the palette is entirely monochrome.

---

### User Story 4 — Grid-Driven Exhibition Layout (Priority: P3)

A user browses the exhibition list or featured content. Cards are arranged in a consistent grid with predictable, generous spacing. Typographic hierarchy — type size and weight — communicates the importance of each piece of information without color or decoration. Content is the dominant visual element; the interface recedes.

**Why this priority**: Layout consistency is a quality-of-life requirement. The functional states (P1, P2) must be correct first; the grid refinement elevates an already-correct system.

**Independent Test**: View the Featured or List tab with content loaded; cards are aligned to a consistent grid, spacing between cards is uniform and generous, and no decorative background fills or image overlays compete with text content.

**Acceptance Scenarios**:

1. **Given** the List or Featured tab is displaying content, **When** a user scans the layout, **Then** all cards align to a consistent column grid with equal gutters.
2. **Given** multiple exhibition cards are visible, **When** a user inspects spacing, **Then** the vertical and horizontal gaps between cards are equal and match the internal card padding proportionally.
3. **Given** an exhibition card is displayed, **When** a user reads it, **Then** typographic scale alone (size, weight) establishes hierarchy — no color, icon badges, or decorative lines differentiate importance levels.
4. **Given** a section header is visible above a content list, **When** a user inspects it, **Then** the header is separated from content by whitespace only — no ornamental dividers, rules, or colored bands.

---

### Edge Cases

- What happens when #FF5400 appears on a white background and must meet contrast requirements? The orange on white combination (≈ 3.1:1) does not meet WCAG AA for small text (4.5:1); orange use must therefore be limited to large interactive targets or paired with sufficient surrounding contrast to communicate state without relying solely on color.
- What happens when multiple active states are simultaneously visible (e.g., active tab + active filter chip)? Both may display orange independently — this is intentional and acceptable provided no tertiary elements also use orange.
- What if the device is in dark mode? The design operates in light mode only (strict white base); dark mode support is out of scope for this feature.
- What happens when an exhibition title is very long? Typography must truncate or wrap gracefully without breaking grid alignment.
- What if a screen has no available primary CTA? The screen renders entirely in the monochrome palette; #FF5400 must not appear as a decorative fallback.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: All UI elements MUST use only black (#000000), white (#FFFFFF), and neutral grays as base colors; #FF5400 MUST NOT appear outside the three permitted use cases (primary CTA, active state, key interaction feedback).
- **FR-002**: All typographic elements MUST use a neo-grotesque sans-serif typeface; no serif, monospaced, or expressive display typefaces are permitted in this design update.
- **FR-003**: All corners on cards, buttons, chips, inputs, and containers MUST have zero border radius (sharp corners).
- **FR-004**: No drop shadows, elevation shadows, gradients, or background blurs MUST be applied to any UI element.
- **FR-005**: The active tab in the bottom navigation bar MUST be indicated solely by an #FF5400 indicator; inactive tabs MUST be monochrome.
- **FR-006**: Selected filter chips MUST display their active state using #FF5400 as the primary differentiating signal; unselected chips MUST be monochrome.
- **FR-007**: Primary CTA buttons MUST be the only orange-colored elements on any given screen; their resting state uses #FF5400 and their pressed/active state uses a contrast or opacity shift of #FF5400.
- **FR-008**: Disabled states for all interactive elements MUST be communicated through reduced opacity or gray fill — not through color or animation.
- **FR-009**: All interactive elements MUST respond to interaction within 100ms through a contrast or opacity shift — no motion or positional animation is permitted for feedback.
- **FR-010**: Layout MUST use a consistent grid system with uniform column widths and gutters; all content cards MUST align to this grid.
- **FR-011**: Spacing between functional zones MUST use whitespace only — no ornamental dividers, decorative rules, or background fills as zone separators.
- **FR-012**: Typographic hierarchy MUST rely solely on type size and weight; color MUST NOT be used to convey information hierarchy within body content.
- **FR-013**: All touch/click targets MUST have a minimum tappable area of 44×44 points.
- **FR-014**: #FF5400 MUST NOT be used for backgrounds, large surface fills, illustrations, or any purely decorative purpose.

### Key Entities

- **DesignToken**: A named value (color, spacing, type scale) that is the single source of truth for all styling; tokens define the three color roles (base-monochrome, accent-primary, state-disabled) and enforce the constraint that #FF5400 is only assignable to accent-primary role.
- **InteractionState**: The defined visual representation of an element across its possible states (default, hover, active, disabled); each state maps to an explicit token combination with no ambiguity.
- **GridSpec**: The column count, gutter width, and margin values that govern all layout composition; all content placement decisions reference the grid spec.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: #FF5400 appears on no more than 3 distinct element types per screen (tab indicator, active filter, primary CTA) — verifiable by visual audit of each screen in the app.
- **SC-002**: 100% of UI elements with interactive states respond visibly within 100ms — verifiable by manual interaction testing on both Android and iOS.
- **SC-003**: 100% of card, button, chip, and container elements have zero border radius — verifiable by visual inspection across all three tabs.
- **SC-004**: No gradients, shadows, or decorative fills are present on any screen — verifiable by pixel-level screenshot audit.
- **SC-005**: The app passes WCAG AA contrast (4.5:1) for all body text against its background — verifiable with a contrast checker; orange-only elements are sized as large interactive targets (≥18pt or bold ≥14pt) where orange-on-white is used.
- **SC-006**: 3 of 3 independent reviewers can identify the app's primary CTA on any given screen within 3 seconds — demonstrating functional hierarchy without color explanation.
- **SC-007**: All three tabs display a consistent typeface, palette, and spacing system — verifiable by side-by-side screenshot comparison showing no per-screen typographic or color deviations.

---

## Assumptions

- This spec updates and supersedes the visual language defined in `002-monochrome-design-system` for the mobile app; the serif/monospaced typeface combination from 002 is replaced by the neo-grotesque system defined here.
- The marketing website (003) has its own separate visual treatment and is not in scope for this update.
- A single neo-grotesque typeface family will be selected (e.g., Inter, Helvetica Neue, or equivalent open-license alternative) and bundled as an app asset; system fallbacks are acceptable only if the system default is a neo-grotesque sans-serif.
- The app operates in light mode only; dark mode support is deferred.
- #FF5400 orange on white at small text sizes does not meet WCAG AA (4.5:1) for text contrast; orange-colored text will not be used — orange is restricted to fills, borders, and underline indicators on sufficiently large targets.
- The Map tab placeholder retains its existing treatment; the design system update applies to the map screen's header, mode toggle, and info sheet only.

## Out of Scope

- Dark mode or system-theme-responsive theming.
- Typography for right-to-left languages or CJK character sets.
- Haptic feedback.
- Illustration, iconography, or imagery systems.
- Marketing website redesign (003 is a separate artifact).
- Staggered content reveal animations (eliminated by this spec's motion constraints).
- Any use of #FF5400 beyond the three designated roles (primary CTA, active state, key interaction feedback).
