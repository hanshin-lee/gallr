# Implementation Plan: gallr Presentation Website

**Branch**: `003-gallr-presentation-web` | **Date**: 2026-03-18 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/003-gallr-presentation-web/spec.md`

## Summary

Build a single-page static marketing website for gallr that mirrors the Minimalist Monochrome design system defined in spec 002. The site presents gallr's value proposition (exhibitions discovery, bookmarking, filtering), showcases the app's key features, and provides App Store / Google Play download CTAs. The site is a self-contained static artifact in `web/` — independent of the KMP codebase — built with Astro and delivered as pre-rendered HTML/CSS with zero runtime JavaScript.

## Technical Context

**Language/Version**: HTML5 / CSS3, JavaScript ES2022 (build tooling only — no runtime JS shipped)
**Primary Dependencies**: Eleventy 3.x (static site generator), self-hosted WOFF2 fonts (Playfair Display, JetBrains Mono)
**Storage**: N/A — fully static
**Testing**: Playwright (smoke tests, JS-disabled validation), axe-core / pa11y (accessibility audit)
**Target Platform**: Modern web browsers (Chrome 120+, Firefox 120+, Safari 17+, Edge 120+); mobile browsers (iOS Safari, Chrome Android); viewport range 320px–1440px
**Project Type**: Static marketing website / landing page
**Performance Goals**: Above-the-fold content fully painted in < 3 seconds on 10 Mbps (Lighthouse Performance score ≥ 90)
**Constraints**: All content renders with JavaScript disabled; WCAG AA contrast on all text; no JS runtime shipped to the browser; all corners 0px radius; palette restricted to 4 tokens
**Scale/Scope**: Single page, 4 sections (Hero, Features, Downloads, About), ~5 Astro components, zero backend

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I — Spec-First | ✅ PASS | `spec.md` completed and all checklist items pass |
| II — Test-First | ✅ PASS (conditional) | Playwright smoke tests (JS-disabled render) and `pa11y` accessibility tests must be written and verified to fail before implementation of each section |
| III — Simplicity & YAGNI | ✅ PASS | Eleventy chosen as simplest viable static site generator for a non-web-specialist team; HTML-first with no component syntax to learn. Next.js rejected (over-engineered), Astro rejected (component mental model unnecessary for a single page), plain HTML rejected (no token compile step, poor maintainability). See research.md. |
| IV — Incremental Delivery | ✅ PASS | Each Astro component (Hero, Features, Downloads, About) maps 1:1 to a prioritized user story and can be built, deployed, and reviewed independently |
| V — Observability | ✅ PASS (adapted) | No server-side code; Lighthouse CI monitors performance and accessibility regressions on every deploy. No crash reporting needed for static content. |
| VI — Shared-First | ✅ EXEMPT | Constitution explicitly states: "The marketing website is a separate, independent artifact with no shared-module dependency." No KMP shared module code in `web/`. |

**Post-Phase 1 re-check**: No violations found. Data model is UI-only (no shared types, no backend). Component contracts define HTML structure only. EXEMPT status for Principle VI confirmed.

## Project Structure

### Documentation (this feature)

```text
specs/003-gallr-presentation-web/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── ui-sections.md
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created here)
```

### Source Code (repository root)

```text
web/
├── _data/
│   └── tokens.js                # Design tokens: color, typography, spacing, borders
├── _includes/
│   ├── base.html                # HTML shell: font preloads, meta, tokens.css import
│   ├── hero.html                # US1: Headline, tagline, primary download CTAs
│   ├── features.html            # US2: Feature showcase (3 FeatureEntry items)
│   ├── downloads.html           # US3: App Store + Google Play CTA block
│   └── about.html               # US4: Mission and target audience
├── public/
│   ├── fonts/
│   │   ├── playfair-display-400.woff2
│   │   ├── playfair-display-700.woff2
│   │   └── jetbrains-mono-400.woff2
│   └── favicon.svg
├── styles/
│   └── tokens.css               # CSS custom properties (compiled from _data/tokens.js)
├── index.html                   # Single page entry point (Eleventy processes this)
├── tests/
│   ├── accessibility.test.js    # pa11y WCAG AA checks
│   └── smoke.test.ts            # Playwright JS-disabled render checks
├── .eleventy.js                 # Eleventy config: passthrough copy, output dir
└── package.json
```

**Structure Decision**: Single-page Eleventy project in `web/` at the repository root. Eleventy's `_data/` + `_includes/` conventions are HTML-native — no component syntax required. This keeps the web artifact completely separate from the KMP modules (`shared/`, `composeApp/`, `iosApp/`) as required by the constitution. `cd web && npm run build` is sufficient.

## Complexity Tracking

> No Constitution Check violations. Table intentionally empty.
