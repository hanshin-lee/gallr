# Research: gallr Presentation Website

**Branch**: `003-gallr-presentation-web` | **Date**: 2026-03-18
**Phase**: 0 — Resolved all technical unknowns from plan.md

---

## Decision 1: Static Site Framework

**Decision**: Eleventy (11ty) 3.x

**Rationale**:
- HTML-first: page sections are written as plain HTML (or Nunjucks/Liquid templates) — no component syntax to learn; Kotlin/mobile engineers can read and edit the source directly without a JavaScript framework mental model
- Zero JavaScript shipped to browsers by default — satisfies FR-008 without any configuration
- Near-zero config to start: `npm install @11ty/eleventy` + `npx @11ty/eleventy --serve`; no bundler configuration required for a single static page
- Design tokens live in Eleventy `_data/` files (YAML/JSON); the build injects them as CSS custom properties at compile time — no runtime token resolution
- `eleventy --output=dist` produces a self-contained `dist/` directory deployable to any static host
- Eleventy 3.0 shipped October 2024 with full ESM support; actively maintained and growing in production usage

**Alternatives Considered**:

| Option | Static HTML? | DX for Kotlin/mobile team | Verdict |
|--------|-------------|---------------------------|---------|
| Astro 5.x | ✅ | Medium — `.astro` component syntax adds a new mental model; component islands architecture is overkill for a single static page | Rejected: well-suited for complex multi-page sites but overengineered for a single landing page; Eleventy's HTML-first model is lower friction for this team |
| Plain HTML + CSS | ✅ (no build) | Good familiarity but poor maintainability — no templating, no asset optimization, manual DRY management | Rejected: no design token compile step; duplication grows painful with iteration |
| Next.js static export | ✅ | Poor — React mental model, large bundle even with `output: 'export'`, complex configuration | Rejected: over-engineered for a single static page |
| Vite + vanilla HTML | ✅ | Medium — good asset pipeline but no site generator features (routing, data, templates) | Rejected: requires hand-rolling what Eleventy provides out of the box |

---

## Decision 2: Web Font Strategy

**Decision**: Self-host WOFF2 files in `/public/fonts/`; preload critical weight; `font-display: swap` with metric overrides

**Rationale**:
- Self-hosting eliminates the Google Fonts CDN DNS lookup (~200ms penalty on first visit) and avoids GDPR third-party data transfer concerns
- WOFF2 is universal in all target browsers (Chrome 120+, Firefox 120+, Safari 17+) — no fallback formats needed
- `font-display: swap` renders system fallback text immediately; custom font swaps in once loaded — prevents Flash of Invisible Text
- `size-adjust`, `ascent-override`, `descent-override` CSS descriptors match the custom font metrics to the system serif fallback (Georgia), minimising Cumulative Layout Shift
- Preloading only Playfair Display 700 (the above-the-fold headline weight) keeps the critical path minimal; other weights load non-blocking

**Font pairings from the KMP app design system**:

| Role | Web font | KMP equivalent | Weights |
|------|----------|---------------|---------|
| Display/Headline | Playfair Display | Playfair Display (bundled TTF) | 400, 700 |
| Body | Playfair Display | (same) | 400 |
| Metadata/Label | JetBrains Mono | Monospaced TTF (spec 002) | 400 |

**Font delivery**:
```css
@font-face {
  font-family: "Playfair Display";
  src: url("/fonts/playfair-display-700.woff2") format("woff2");
  font-weight: 700;
  font-display: swap;
  size-adjust: 102%;
  ascent-override: 90%;
  descent-override: 22%;
}
@font-face {
  font-family: "JetBrains Mono";
  src: url("/fonts/jetbrains-mono-400.woff2") format("woff2");
  font-weight: 400;
  font-display: swap;
}
```

**Preload strategy**: `<link rel="preload" as="font" type="font/woff2" href="/fonts/playfair-display-700.woff2" crossorigin>` in the `<head>` of the base template — ensures the dominant above-the-fold font is fetched at highest priority.

---

## Decision 3: CSS Design Token Architecture

**Decision**: Eleventy `_data/tokens.js` data file compiled into CSS custom properties in `_includes/tokens.css`

**Rationale**:
- Eleventy's data cascade allows token values to be defined once in a JS/JSON/YAML data file and injected into templates at build time — no runtime resolution
- CSS custom properties in a single `tokens.css` (imported in the base template) are the output form; the Eleventy data layer is the authoring form
- All template styles reference `var(--token-name)` — changing a token in the data file cascades everywhere without touching HTML or CSS directly
- No preprocessor (Sass/Less) needed; CSS variables are sufficient for this scale
- Mirrors the KMP app's `DesignToken` concept from spec 002: a single source of truth for all visual decisions

**Token set** (maps directly to spec 002 palette and typography):

```css
/* tokens.css */
:root {
  /* Color */
  --color-ink:          #000000;
  --color-paper:        #ffffff;
  --color-paper-alt:    #f5f5f5;
  --color-ink-secondary: #525252;

  /* Typography */
  --font-display:  'Playfair Display', Georgia, 'Times New Roman', serif;
  --font-meta:     'JetBrains Mono', 'Courier New', Courier, monospace;

  /* Shape */
  --radius: 0px;

  /* Borders */
  --border-ink:      1px solid var(--color-ink);
  --border-section:  4px solid var(--color-ink);
  --border-hairline: 1px solid var(--color-ink-secondary);

  /* Spacing scale */
  --space-xs:  4px;
  --space-sm:  8px;
  --space-md:  16px;
  --space-lg:  32px;
  --space-xl:  64px;
  --space-2xl: 96px;
}
```

---

## Decision 4: Responsive Layout Strategy

**Decision**: CSS Grid and Flexbox with mobile-first breakpoints; no CSS framework (no Tailwind, Bootstrap)

**Rationale**:
- The monochrome design is intentionally minimal — a CSS utility framework would add dead code and complicate the strict design token constraint
- CSS Grid handles the features section layout (1-column on mobile → 2-3 column on desktop) with minimal code
- Breakpoints: 320px (base), 768px (tablet), 1024px (desktop), 1440px (max-width container)
- Max content width: 960px centered, with symmetric horizontal padding — prevents excessive line lengths on wide displays

---

## Decision 5: Accessibility Testing Tooling

**Decision**: `pa11y` (CLI) for automated WCAG AA checks; Playwright for JS-disabled smoke tests

**Rationale**:
- `pa11y` runs WCAG 2.1 AA rules against built HTML output — satisfies Principle II (Test-First) for accessibility: a test that verifies FR-013 (contrast) can be written against a target before the section is styled
- Playwright's `page.setJavaScriptEnabled(false)` tests FR-008 (JS-disabled content rendering) precisely
- Both tools integrate with CI (GitHub Actions) for regression prevention post-deploy

**Test-First application**:
1. Write a `pa11y` test asserting no WCAG AA violations on the `dist/index.html` → initially fails (page is empty)
2. Write a Playwright test asserting visible headline text with JS disabled → initially fails (page is empty)
3. Implement the Hero component → both tests pass for the hero
4. Repeat for each subsequent component

---

## Decision 6: Deployment Target

**Decision**: Deferred to implementation; Vercel recommended as default

**Rationale**:
- `vercel --prod` (or GitHub Actions deploy action) deploys `web/dist/` in under 1 minute
- Zero-config for Astro static output: Vercel auto-detects Astro and runs `npm run build`
- Custom domain setup is a one-time DNS configuration, not a build concern
- GitHub Pages is an acceptable alternative; the choice does not affect any code in `web/`

---

## Resolved Unknowns

All NEEDS CLARIFICATION items from Technical Context are resolved:

| Unknown | Resolution |
|---------|-----------|
| Framework/Language | Eleventy 3.x + HTML5/CSS3, zero runtime JS |
| Font delivery | Self-hosted WOFF2 files in `/public/fonts/`; preload critical weight + `font-display: swap` + metric overrides |
| CSS architecture | CSS custom properties compiled from Eleventy `_data/tokens.js` into `_includes/tokens.css` |
| Testing approach | Playwright (smoke + JS-disabled), pa11y (accessibility) |
| Deployment | Vercel (recommended default); GitHub Pages viable alternative |
