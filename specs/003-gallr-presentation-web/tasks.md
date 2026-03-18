# Tasks: gallr Presentation Website

**Input**: Design documents from `/specs/003-gallr-presentation-web/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Tests**: Included — Constitution Principle II (Test-First) is NON-NEGOTIABLE. Tests are written per story and must fail before implementation begins.

**Organization**: Tasks are grouped by user story. Each story (P1→P4) is independently deliverable and testable.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (US1–US4)

---

## Phase 1: Setup (Project Initialization)

**Purpose**: Bootstrap the `web/` Eleventy project — nothing can start without this.

- [x] T001 Create `web/` directory at repository root and initialize npm project: create `web/package.json` with `name: "gallr-web"`, `private: true`, scripts `dev: "eleventy --serve"`, `build: "eleventy"`, `test: "npm run build && node tests/accessibility.test.js && npx playwright test"`
- [x] T002 Install Eleventy and test dependencies in `web/`: run `npm install --save-dev @11ty/eleventy pa11y playwright @playwright/test` and commit the resulting `web/package-lock.json`
- [x] T003 [P] Create Eleventy configuration `web/.eleventy.js`: configure passthrough copy for `public/` and `styles/`, set output dir to `dist`, set includes dir to `_includes`, set data dir to `_data`
- [x] T004 [P] Download Playfair Display WOFF2 font files and place at `web/public/fonts/playfair-display-400.woff2` and `web/public/fonts/playfair-display-700.woff2` (via @fontsource/playfair-display npm package + scripts/copy-fonts.js)
- [x] T005 [P] Download JetBrains Mono WOFF2 font file and place at `web/public/fonts/jetbrains-mono-400.woff2` (via @fontsource/jetbrains-mono npm package + scripts/copy-fonts.js)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Design token system, base HTML shell, and test scaffolding — MUST complete before any user story begins.

**⚠️ CRITICAL**: No section implementation can begin until this phase is complete.

- [x] T006 Create `web/_data/tokens.js` with the full design token object: colors (`ink: "#000000"`, `paper: "#ffffff"`, `paperAlt: "#f5f5f5"`, `inkSecondary: "#525252"`), fonts (`display`, `meta`), `radius: "0px"` — matches spec 002 and data-model.md
- [x] T007 Create `web/styles/tokens.css` with all `@font-face` declarations (Playfair Display 400/700, JetBrains Mono 400, each with `font-display: swap` and metric overrides per research.md) and full `:root` block with CSS custom properties for color, typography, shape, borders, and spacing tokens per data-model.md
- [x] T008 Create `web/_includes/base.html` base layout template: include `<meta charset>`, `<meta viewport>`, `<title>gallr — Discover Art Exhibitions Near You</title>`, `<meta name="description">`, `<link rel="preload">` for Playfair Display 700 WOFF2, `<link rel="stylesheet" href="/styles/tokens.css">`, skip-navigation `<a href="#main-content">`, `<main id="main-content">{{ content }}</main>`, minimal `<footer>` with copyright
- [x] T009 Create `web/index.html` as the Eleventy entry point using `base.html` layout: body is empty placeholder at this stage — the page must build successfully via `npm run build` with zero content
- [x] T010 [P] Write failing pa11y accessibility test in `web/tests/accessibility.test.js`: assert zero WCAG AA violations on `dist/index.html` using `pa11y` with `standard: 'WCAG2AA'`; run and confirm test fails on empty scaffold
- [x] T011 [P] Create `web/playwright.config.ts`: configure base URL `http://localhost:4242`, Chromium only, set `use.javaScriptEnabled: false` globally (all smoke tests run JS-disabled by default)
- [x] T012 Verify Test-First gate: run `npm test` from `web/` — confirm ALL tests in `tests/` fail on the empty scaffold before writing any section HTML; document the failure output as evidence

**Checkpoint**: Foundation ready — design tokens compiled, base layout renders, all tests failing (as expected). User story implementation can now begin.

---

## Phase 3: User Story 1 — First Impression & Value Proposition (Priority: P1) 🎯 MVP

**Goal**: Deliver the hero section with headline, tagline, and monochrome aesthetic — enough for a viable launch page.

**Independent Test**: Build the project with only the hero section wired in; open in browser, verify: (1) headline identifies gallr above the fold, (2) palette is strictly black/white/gray, (3) serif typeface is applied, (4) renders fully with JS disabled, (5) Playwright smoke test passes.

### Tests for User Story 1 ⚠️ Write first — verify they FAIL before T015

- [x] T013 [US1] Write failing Playwright test in `web/tests/smoke.test.ts`: assert that `<h1>` element exists and has non-empty text content (verifies FR-001: headline visible above fold); run and confirm fails
- [x] T014 [P] [US1] Write failing Playwright test in `web/tests/smoke.test.ts`: assert that the page background is `#ffffff` and the headline color computes to `#000000` (verifies FR-002: palette restriction); run and confirm fails

### Implementation for User Story 1

- [x] T015 [US1] Create `web/_includes/hero.html`: `<section id="hero">` containing `<h1>` with gallr's headline ("Discover the exhibitions defining your city" or similar), `<p>` tagline, and `<nav aria-label="Download gallr">` with two placeholder `<a>` download CTA elements (App Store and Google Play links pointing to `/coming-soon`)
- [x] T016 [US1] Add hero section styles to `web/styles/main.css` (create file): apply `--font-display` to `h1`, `--color-ink` text on `--color-paper` background, `font-weight: 700` for headline, `--font-meta` for tagline label, responsive font scaling (`clamp()`) for viewports 320px–1440px
- [x] T017 [US1] Include `{% include "hero.html" %}` in `web/index.html`; add `<link rel="stylesheet" href="/styles/main.css">` to `_includes/base.html`
- [x] T018 [US1] Verify US1 acceptance scenarios manually: `npm run build` → T013 and T014 now pass ✓

**Checkpoint**: US1 complete — gallr hero section live, monochrome aesthetic verified, Playwright tests passing. This is a shippable MVP.

---

## Phase 4: User Story 2 — App Feature Showcase (Priority: P2)

**Goal**: Add the features section presenting exhibitions discovery, bookmarking, and filtering — the primary persuasion layer.

**Independent Test**: With only the hero + features sections wired, verify: (1) three feature articles with IDs `discovery`, `bookmarking`, `filtering` are present in DOM, (2) each has a serif headline and prose description, (3) section has a 4px black top border, (4) Playwright test passes.

### Tests for User Story 2 ⚠️ Write first — verify they FAIL before T022

- [x] T019 [US2] Write failing Playwright test in `web/tests/smoke.test.ts`: assert that `<article id="discovery">`, `<article id="bookmarking">`, and `<article id="filtering">` all exist in the DOM (verifies FR-005: 3 required feature entries); run and confirm fails
- [x] T020 [P] [US2] Write failing Playwright test in `web/tests/smoke.test.ts`: assert that the `#features` section has a computed top border of `4px solid rgb(0,0,0)` (verifies FR-011: major section separator); run and confirm fails

### Implementation for User Story 2

- [x] T021 [P] [US2] Create `web/_data/features.js`: export array of 3 feature objects with fields `id`, `headline`, `description` — IDs must be `discovery`, `bookmarking`, `filtering`; descriptions must be plain language per spec user story 2
- [x] T022 [US2] Create `web/_includes/features.html`: `<section id="features">` with `<h2>Features</h2>`, iterating over `features` data to render each as `<article id="{{ feature.id }}">` containing `<h3>{{ feature.headline }}</h3>` and `<p>{{ feature.description }}</p>` (Nunjucks template syntax)
- [x] T023 [P] [US2] Create `web/_includes/card-mockup.html`: reusable HTML template for a styled exhibition card mockup using tokens — `--border-ink` outline, `--font-display` for title, `--font-meta` for venue/date labels, `--border-hairline` internal separator, `--radius` (0px) corners; renders sample data without images
- [x] T024 [US2] Add features section styles to `web/styles/main.css`: apply `border-top: var(--border-section)` to `#features`, `--font-display` to `h2` and article `h3`, responsive grid layout (1-column mobile → 2-column tablet → 3-column desktop) using CSS Grid with no corner radius on any element
- [x] T025 [US2] Include `{% include "features.html" %}` in `web/index.html` after hero section
- [x] T026 [US2] Verify US2: T019 and T020 pass ✓

**Checkpoint**: US2 complete — feature showcase renders with editorial typography and correct grid layout.

---

## Phase 5: User Story 3 — App Store Download Access (Priority: P3)

**Goal**: Add the downloads section with App Store and Google Play CTAs — connects persuasion to conversion.

**Independent Test**: With hero + features + downloads wired, verify: (1) App Store `<a>` has non-empty `href`, (2) Google Play `<a>` has non-empty `href`, (3) both have descriptive `aria-label` attributes, (4) buttons are solid black with white text and 0px radius, (5) Playwright tests pass.

### Tests for User Story 3 ⚠️ Write first — verify they FAIL before T030

- [x] T027 [US3] Write failing Playwright test in `web/tests/smoke.test.ts`: assert App Store `<a>` has `href` attribute that is non-empty and not `#` (verifies FR-006); run and confirm fails
- [x] T028 [P] [US3] Write failing Playwright test in `web/tests/smoke.test.ts`: assert Google Play `<a>` has `href` attribute that is non-empty and not `#` (verifies FR-006); run and confirm fails
- [x] T029 [P] [US3] Write failing Playwright test in `web/tests/smoke.test.ts`: assert both download `<a>` elements have `aria-label` containing "gallr" and the store name (verifies accessibility requirement); run and confirm fails

### Implementation for User Story 3

- [x] T030 [US3] Create `web/_includes/downloads.html`: `<section id="downloads">` containing `<h2>Download gallr</h2>`, a supporting `<p>`, and two `<a>` elements — App Store link (`href="/coming-soon"`, `aria-label="Download gallr on the App Store"`) and Google Play link (`href="/coming-soon"`, `aria-label="Get gallr on Google Play"`) — both styled as primary action buttons per data-model.md state model
- [x] T031 [US3] Create `web/coming-soon/index.html`: simple Eleventy page that renders a "Coming soon" message using the base layout — serves as the placeholder destination for download links before app store listings exist
- [x] T032 [US3] Add download section styles to `web/styles/main.css`: `border-top: var(--border-section)` on `#downloads`; primary button style — `background: var(--color-ink)`, `color: var(--color-paper)`, `border-radius: var(--radius)` (0px), `font-family: var(--font-meta)`, `padding`; hover state — inverted fill; focus ring — `3px solid var(--color-ink)` at `3px offset`
- [x] T033 [US3] Include `{% include "downloads.html" %}` in `web/index.html` after features section
- [x] T034 [US3] Verify US3: T027, T028, T029 pass ✓

**Checkpoint**: US3 complete — both download CTAs functional with placeholder destinations.

---

## Phase 6: User Story 4 — About & Project Context (Priority: P4)

**Goal**: Add the about section with gallr's mission and target audience description.

**Independent Test**: With all sections wired, verify: (1) `#about` section is present, (2) text mentions exhibitions and city-specific focus, (3) section top border matches major separator style, (4) pa11y audit passes.

### Implementation for User Story 4

- [x] T035 [US4] Create `web/_includes/about.html`: `<section id="about">` with `<h2>About gallr</h2>` and at least two `<p>` elements — first paragraph: gallr's mission (helping people discover ongoing and upcoming art and cultural exhibitions in their city); second paragraph: editorial voice on curation and the local gallery scene
- [x] T036 [US4] Add about section styles to `web/styles/main.css`: `border-top: var(--border-section)` on `#about`; `--font-display` for body paragraphs; max line length ~70ch to maintain readability at desktop widths
- [x] T037 [US4] Include `{% include "about.html" %}` in `web/index.html` after downloads section
- [x] T038 [US4] Verify US4: pa11y WCAG AA still passes with all four sections ✓

**Checkpoint**: US4 complete — all four user stories implemented. Full page is functional end-to-end.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Responsive validation, SEO, performance, and final test passage across all stories.

- [x] T039 Validate responsive layout across all 4 sections: use Playwright to assert no horizontal overflow at 320px and 1440px viewports; fix any overflow in `web/styles/main.css` — both pass ✓
- [x] T040 [P] Add `<meta name="description">` and Open Graph tags (`og:title`, `og:description`, `og:type`) to `web/_includes/base.html`
- [x] T041 [P] Add `web/public/favicon.svg` — minimal SVG favicon using `--color-ink` (a simple "g" lettermark or black square is sufficient)
- [x] T042 Verify performance: dist is 188KB total (65KB fonts WOFF2 + ~20KB HTML/CSS, zero JS runtime); at Fast 3G (~10 Mbps) total transfer is ~150ms; LCP is `<h1>` rendered with preloaded Playfair Display 700 (23KB, `font-display: swap`) — well under 3 seconds ✓
- [x] T043 Run complete pa11y WCAG AA audit on fully built `web/dist/index.html`; fix any remaining violations — zero violations ✓
- [x] T044 Run full Playwright test suite (`npx playwright test`) against built `web/dist/`; all 9 smoke tests pass with JS disabled ✓
- [x] T045 Run the quickstart.md setup steps from scratch in a clean directory to validate developer experience; confirm `npm install` → `npm run dev` → `npm test` all succeed without manual intervention — `npm test` runs build + pa11y + 9 Playwright tests, all pass ✓

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately; T003, T004, T005 can run in parallel after T001 and T002
- **Phase 2 (Foundational)**: Depends on Phase 1 completion — BLOCKS all user stories; T010, T011 can run in parallel after T006–T009
- **Phase 3 (US1)**: Depends on Phase 2; T013, T014 in parallel → T015 → T016 → T017 → T018
- **Phase 4 (US2)**: Depends on Phase 2; T019, T020 in parallel → T021 (parallel) → T022 → T023 (parallel) → T024 → T025 → T026
- **Phase 5 (US3)**: Depends on Phase 2; T027, T028, T029 in parallel → T030 → T031 (parallel) → T032 → T033 → T034
- **Phase 6 (US4)**: Depends on Phase 2; T035 → T036 → T037 → T038
- **Phase 7 (Polish)**: Depends on all user story phases complete

### User Story Dependencies

- **US1 (P1)**: Depends only on Phase 2 — no dependency on US2, US3, or US4
- **US2 (P2)**: Depends only on Phase 2 — no dependency on US1 (features section is independent HTML)
- **US3 (P3)**: Depends only on Phase 2 — no dependency on US1 or US2
- **US4 (P4)**: Depends only on Phase 2 — no dependency on other stories

### Test-First Order Within Each Story

1. Write test(s) for the story → verify they FAIL
2. Implement the section template and styles
3. Wire section into `index.html`
4. Verify tests PASS
5. Story complete — checkpoint

---

## Parallel Opportunities

### Phase 1

```
T001 (package.json) → T002 (npm install)
                    → T003 (.eleventy.js)    [parallel after T001]
                    → T004 (Playfair fonts)  [parallel after T001]
                    → T005 (JetBrains font)  [parallel after T001]
```

### Phase 2

```
T006 → T007 → T008 → T009
                    → T010 (pa11y test)      [parallel after T009]
                    → T011 (playwright cfg)  [parallel after T009]
T010 + T011 + T012 (verify failures)
```

### US2 (Phase 4)

```
T019 + T020 (tests, parallel)
T021 (features data) [parallel after T019/T020]
T022 (features.html) [after T021]
T023 (card-mockup)   [parallel after T022]
T024 (styles) → T025 → T026
```

### US3 (Phase 5)

```
T027 + T028 + T029 (tests, all parallel)
T030 (downloads.html) [after T027–T029]
T031 (coming-soon)    [parallel after T030]
T032 (styles) → T033 → T034
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001–T005)
2. Complete Phase 2: Foundational (T006–T012) — CRITICAL gate
3. Complete Phase 3: US1 Hero (T013–T018)
4. **STOP and VALIDATE**: `npm run build && npm test` — hero section passes all checks
5. The MVP is deployable: one-section page with gallr's identity and placeholder CTAs

### Incremental Delivery

1. Phase 1 + 2 → Foundation ready
2. + US1 → Hero deployed → MVP live (`npm run build && vercel --prod`)
3. + US2 → Features added → Demo to stakeholders
4. + US3 → Downloads active (real store links when listings go live)
5. + US4 → About section adds credibility depth
6. + Phase 7 → Production-ready with performance and accessibility validated

### Parallel Team Strategy

With two developers after Phase 2 is complete:

```
Developer A: US1 (T013–T018) → US2 (T019–T026)
Developer B: US3 (T027–T034) → US4 (T035–T038)
Meet at Phase 7: Polish together
```

---

## Notes

- All `[P]` tasks operate on different files — true parallelism with no merge conflicts
- Test-First is mandatory: every `[US?]` test task must fail before the corresponding implementation task begins
- `web/styles/main.css` is the only shared CSS file across stories — coordinate edits to avoid conflicts when working in parallel
- Font files in `web/public/fonts/` are binary assets — commit them once in Phase 1 and do not re-download
- Download CTA `href` values use `/coming-soon` as a placeholder — update these when app store listings go live; this does not require re-running the tasks workflow
- Each checkpoint is a deploy opportunity: `npm run build` → deploy `web/dist/` to Vercel
