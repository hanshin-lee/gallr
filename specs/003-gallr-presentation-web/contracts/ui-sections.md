# UI Section Contracts: gallr Presentation Website

**Branch**: `003-gallr-presentation-web` | **Date**: 2026-03-18
**Phase**: 1 — Component interface contracts

---

> These contracts define the **required HTML structure and content obligations** for each Astro component. They are the interface boundary between the spec (what the page must communicate) and the implementation (how Astro renders it). Implementations that satisfy these contracts are valid regardless of internal CSS details.

---

## Contract: Hero Section (`_includes/hero.html`)

**Maps to**: User Story 1 (P1)
**Required output**: Above-the-fold section with headline, tagline, and download CTAs

### Required HTML Elements

| Element | Role | Token/Style Requirements |
|---------|------|--------------------------|
| `<section id="hero">` | Section container | Background: `--color-paper`; no separator rule |
| `<h1>` | Primary headline | Font: `--font-display`; color: `--color-ink`; font-weight: 700 |
| `<p>` (tagline) | One-sentence description | Font: `--font-display` or `--font-meta`; color: `--color-ink-secondary` |
| `<nav aria-label="Download gallr">` | CTA group | Contains exactly 2 `<a>` download links |
| `<a>` (App Store) | Primary CTA | Applies primary button style; `href` must be non-empty |
| `<a>` (Google Play) | Primary CTA | Applies primary button style; `href` must be non-empty |

### Content Obligations

- Headline must identify gallr as an exhibitions discovery app
- Tagline must be a complete sentence (ends with punctuation)
- Both CTA links must have descriptive `aria-label` attributes naming the store

### Validation

- Section renders fully with JavaScript disabled: ✅ (pure HTML)
- Headline visible above fold at 320px viewport: ✅ (requires responsive font scaling)
- Both CTA links present and have non-empty `href`: ✅

---

## Contract: Features Section (`_includes/features.html`)

**Maps to**: User Story 2 (P2)
**Required output**: Section presenting at least 3 core app capabilities

### Required HTML Elements

| Element | Role | Token/Style Requirements |
|---------|------|--------------------------|
| `<section id="features">` | Section container | Top border: `--border-section` |
| `<h2>` | Section heading | Font: `--font-display`; color: `--color-ink` |
| `<article>` × ≥3 | Feature entry | Each article contains required child elements below |
| `<h3>` (in article) | Feature headline | Font: `--font-display`; font-weight: 400 or 700 |
| `<p>` (in article) | Feature description | Font: `--font-display`; color: `--color-ink` |
| `<div class="card-mockup">` (optional) | Visual representation | 0px radius; `--border-ink`; serif/mono content |

### Required Feature IDs

The following feature IDs must be represented (as `<article id="...">` or `data-feature="..."`):

1. `discovery` — exhibition discovery capability
2. `bookmarking` — save exhibitions capability
3. `filtering` — filter by category/location/timing capability

### Content Obligations

- All feature descriptions must be written in plain language accessible to a non-technical visitor
- If a card mockup is present, it must use only `--color-ink`, `--color-paper`, `--color-ink-secondary` — no other colors

### Validation

- Section contains ≥ 3 feature articles: ✅
- All 3 required feature IDs present: ✅
- Section top border is `4px solid black`: ✅

---

## Contract: Downloads Section (`_includes/downloads.html`)

**Maps to**: User Story 3 (P3)
**Required output**: Standalone CTA block with App Store and Google Play links

### Required HTML Elements

| Element | Role | Token/Style Requirements |
|---------|------|--------------------------|
| `<section id="downloads">` | Section container | Top border: `--border-section` |
| `<h2>` | Section heading | Font: `--font-display`; color: `--color-ink` |
| `<p>` | Supporting text | Brief invitation to download |
| `<a>` (App Store) | Primary CTA | Primary button style: `--color-ink` background, `--color-paper` text, 0px radius |
| `<a>` (Google Play) | Primary CTA | Same button style as App Store |

### Content Obligations

- Each `<a>` must have `aria-label` that includes the store name and "gallr" (e.g. "Download gallr on the App Store")
- `href` must resolve to a valid URL or `/coming-soon` — empty string or `#` are not permitted
- Section must be independently navigable (appears in page landmark structure)

### Validation

- Both links present with valid `href`: ✅
- Both links have descriptive `aria-label`: ✅
- Section top border is `4px solid black`: ✅

---

## Contract: About Section (`_includes/about.html`)

**Maps to**: User Story 4 (P4)
**Required output**: Short project description with mission and target audience

### Required HTML Elements

| Element | Role | Token/Style Requirements |
|---------|------|--------------------------|
| `<section id="about">` | Section container | Top border: `--border-section` |
| `<h2>` | Section heading | Font: `--font-display`; color: `--color-ink` |
| `<p>` × ≥1 | About text | Must contain mission statement and target audience reference |

### Content Obligations

- The about text must mention: what gallr does, who it is for (art exhibition-goers), and the city/local focus
- Paragraphs must be in `--font-display` at body text scale
- No images, logos, or external links required

### Validation

- Section contains at least one paragraph with gallr's mission: ✅
- Top border is `4px solid black`: ✅
- All text meets WCAG AA contrast on `--color-paper` background: ✅ (black on white = 21:1)

---

## Contract: Base Layout (`_includes/base.html`)

**Maps to**: All user stories (global shell)
**Required output**: Semantic HTML document shell with required head metadata and font loading

### Required `<head>` Elements

| Element | Purpose |
|---------|---------|
| `<meta charset="UTF-8">` | Character encoding |
| `<meta name="viewport" content="width=device-width, initial-scale=1">` | Mobile responsiveness |
| `<title>gallr — Discover Art Exhibitions Near You</title>` | Page title |
| `<meta name="description" content="...">` | SEO description |
| `<link rel="preload" as="font" type="font/woff2" href="...playfair-display-700...">` | Critical font preload |
| `<link rel="stylesheet" href="/styles/tokens.css">` | Design token import |

### Required `<body>` Structure

```
<body>
  <header>          <!-- Skip-navigation link for accessibility -->
  <main id="main-content">
    {{ content }}   <!-- index.html sections included via {% include %} -->
  </main>
  <footer>          <!-- Minimal: copyright, no complex layout -->
</body>
```

### Validation

- Page passes HTML5 semantic validation (single `<h1>`, landmark structure): ✅
- `<main id="main-content">` allows skip-navigation for accessibility: ✅
- `tokens.css` is loaded before body paint: ✅
