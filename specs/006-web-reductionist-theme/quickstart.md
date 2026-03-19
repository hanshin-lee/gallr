# Quickstart & Acceptance Test Guide: Web Reductionist Design System (006)

**Feature Branch**: `006-web-reductionist-theme`
**Generated**: 2026-03-20

---

## Setup

```bash
cd web
npm install          # installs @fontsource/inter (replaces playfair + jetbrains-mono)
npm run build        # runs copy-fonts.js then eleventy; outputs to dist/
npm run preview      # serves dist/ at localhost:8080
```

---

## Acceptance Scenarios

### SC-001 — All text uses Inter typeface

**Steps**:
1. Open `http://localhost:8080` in Chrome.
2. Open DevTools → Elements.
3. Click on the hero headline, the tagline, a feature card heading, a section label ("FEATURES"), a button label, and the footer copy.
4. In the Computed styles panel, check `font-family` for each element.

**Pass**: Every element shows `Inter` (or the system-ui fallback) as the resolved font. No element shows `Playfair Display`, `Georgia`, `JetBrains Mono`, or `Courier`.

---

### SC-002 — Primary CTA is the only orange element

**Steps**:
1. Open `http://localhost:8080`.
2. Visually scan the full page (Hero, Features, Downloads, About).
3. Identify every button and interactive control.

**Pass**: Only the primary download/action button (`btn--primary`) renders with an orange (#FF5400) background. All other controls (outline buttons, links) are monochrome (black/white). No other page element uses orange.

---

### SC-003 — Primary CTA hover responds within 100ms via colour shift

**Steps**:
1. Open `http://localhost:8080`.
2. Hover over the primary CTA button.

**Pass**: The button background shifts to a darker orange (approximately #CC4400) immediately on hover. The text remains white. No movement, animation, or scale change occurs. The transition is imperceptible in duration (≤100ms per CSS transition value).

---

### SC-004 — No decorative section borders

**Steps**:
1. Open `http://localhost:8080`.
2. Scroll slowly from the Hero section through Features, Downloads, and About.
3. Inspect the top edge of each section visually.

**Pass**: No thick black horizontal line appears between any two sections. Section transitions are marked only by whitespace. Open DevTools and confirm `#features`, `#downloads`, `#about` have no `border-top` style applied.

---

### SC-005 — Section boundaries are still legible

**Steps**:
1. Open `http://localhost:8080`.
2. Scroll through the page without DevTools open.

**Pass**: Despite no border rules, each section is clearly distinguishable by its section heading typographic contrast and the vertical gap above it. A first-time visitor can identify 4 distinct sections without confusion.

---

### SC-006 — Fallback font is neutral sans-serif

**Steps**:
1. Open `http://localhost:8080` with DevTools → Network → throttle to Slow 3G.
2. Hard-reload the page (`Cmd+Shift+R` / `Ctrl+Shift+R`) to bypass cache.
3. Observe text before the Inter WOFF2 loads.

**Pass**: The page renders with a neutral sans-serif system font (system-ui / -apple-system / Arial) as fallback — no serif or monospaced text is visible during font load. Content layout does not reflow when Inter swaps in.

---

### SC-007 — WCAG AA contrast

**Steps**:
1. Open `http://localhost:8080`.
2. Run `npx pa11y http://localhost:8080` (or open the accessibility test: `node tests/accessibility.test.js`).

**Pass**: No WCAG AA contrast failures reported for body text (4.5:1 minimum). Primary CTA orange-on-white passes the 3:1 minimum for large interactive components (the button is ≥44px height and bold text).

---

### SC-008 — Visual parity with KMP app (005)

**Steps**:
1. Open `http://localhost:8080` on a desktop browser.
2. Open the gallr KMP app on an Android or iOS simulator.
3. View both side-by-side.

**Pass**: Both products use the same typeface character (Inter), the same accent colour role (orange CTA only), and the same absence of decorative dividers between content zones. A reviewer identifies them as sharing the same design system.

---

## Build Verification

After `npm run build`, confirm:

```
dist/
├── fonts/
│   ├── inter-400.woff2    ✓ (was playfair-display-400.woff2)
│   ├── inter-500.woff2    ✓ (was jetbrains-mono-400.woff2)
│   └── inter-700.woff2    ✓ (was playfair-display-700.woff2)
├── styles/
│   ├── tokens.css         ✓ (contains --color-accent: #FF5400)
│   └── main.css           ✓ (btn--primary uses orange; no border-top on sections)
└── index.html             ✓ (preload link points to inter-700.woff2)
```
