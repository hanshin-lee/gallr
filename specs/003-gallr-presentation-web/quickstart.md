# Quickstart: gallr Presentation Website

**Branch**: `003-gallr-presentation-web` | **Date**: 2026-03-18

---

## Prerequisites

- Node.js 20+ (LTS) — check with `node --version`
- npm 10+ — check with `npm --version`

No other tooling is required. The KMP build tools (Gradle, JDK) are **not** needed for the web site.

---

## Initial Setup

```bash
# From the repository root
mkdir -p web && cd web

# Initialise package.json
npm init -y

# Install Eleventy
npm install --save-dev @11ty/eleventy

# Install dev tooling for testing
npm install --save-dev pa11y playwright @playwright/test

# Install Playwright browser (Chromium is sufficient)
npx playwright install chromium
```

Add scripts to `package.json`:

```json
{
  "scripts": {
    "dev":   "eleventy --serve",
    "build": "eleventy",
    "test":  "npm run build && npx pa11y ./dist/index.html --standard WCAG2AA && npx playwright test"
  }
}
```

---

## Font Setup

Download the following WOFF2 font files and place them in `web/public/fonts/`:

| File | Source |
|------|--------|
| `playfair-display-400.woff2` | [Fontsource — Playfair Display](https://fontsource.org/fonts/playfair-display) |
| `playfair-display-700.woff2` | same |
| `jetbrains-mono-400.woff2` | [JetBrains Mono releases](https://github.com/JetBrains/JetBrainsMono/releases) |

These files must be committed to the repository — they are not installed via npm.

---

## Development Server

```bash
cd web
npm run dev
# → http://localhost:8080
```

Live reload on file save. Fonts are served from `public/fonts/`.

---

## Build

```bash
cd web
npm run build
# → Output in web/dist/
# All files are static HTML/CSS — zero JS runtime
```

Verify zero runtime JS is shipped:

```bash
# Should output nothing meaningful (no app-bundle JS in dist)
find web/dist -name "*.js"
# Expected: only test files, not any app JS
```

---

## Run Tests

```bash
cd web
npm test
# 1. Builds dist/
# 2. Runs pa11y WCAG AA audit on dist/index.html
# 3. Runs Playwright smoke tests

# Expected on empty scaffold: ALL TESTS FAIL
# This is correct — Test-First requires failing tests before implementation
```

---

## Project Layout (post-setup)

```text
web/
├── _data/
│   └── tokens.js                # All design tokens as JS object
├── _includes/
│   ├── base.html                # HTML shell with <head>, font preloads
│   ├── hero.html                # Hero section template
│   ├── features.html            # Features section template
│   ├── downloads.html           # Download CTAs section template
│   └── about.html               # About section template
├── public/
│   ├── fonts/                   # Self-hosted WOFF2 files (committed)
│   │   ├── playfair-display-400.woff2
│   │   ├── playfair-display-700.woff2
│   │   └── jetbrains-mono-400.woff2
│   └── favicon.svg
├── styles/
│   └── tokens.css               # CSS custom properties (references token values)
├── index.html                   # Page entry point — uses {% include %} for sections
├── tests/
│   ├── accessibility.test.js    # pa11y WCAG AA checks
│   └── smoke.test.ts            # Playwright smoke tests (JS-disabled rendering)
├── .eleventy.js                 # Eleventy configuration
└── package.json
```

---

## Key Configuration

### `.eleventy.js`

```js
module.exports = function(eleventyConfig) {
  // Pass font files and CSS through to dist/ unchanged
  eleventyConfig.addPassthroughCopy("public");
  eleventyConfig.addPassthroughCopy("styles");

  return {
    dir: {
      output: "dist",
      includes: "_includes",
      data: "_data"
    }
  };
};
```

### `_data/tokens.js`

```js
module.exports = {
  color: {
    ink:          "#000000",
    paper:        "#ffffff",
    paperAlt:     "#f5f5f5",
    inkSecondary: "#525252"
  },
  font: {
    display: "'Playfair Display', Georgia, 'Times New Roman', serif",
    meta:    "'JetBrains Mono', 'Courier New', Courier, monospace"
  },
  radius: "0px"
};
```

### `styles/tokens.css`

```css
/* Self-hosted fonts */
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
  font-family: "Playfair Display";
  src: url("/fonts/playfair-display-400.woff2") format("woff2");
  font-weight: 400;
  font-display: swap;
}
@font-face {
  font-family: "JetBrains Mono";
  src: url("/fonts/jetbrains-mono-400.woff2") format("woff2");
  font-weight: 400;
  font-display: swap;
}

/* Design tokens */
:root {
  --color-ink:           #000000;
  --color-paper:         #ffffff;
  --color-paper-alt:     #f5f5f5;
  --color-ink-secondary: #525252;

  --font-display: 'Playfair Display', Georgia, 'Times New Roman', serif;
  --font-meta:    'JetBrains Mono', 'Courier New', Courier, monospace;

  --radius:          0px;
  --border-ink:      1px solid #000000;
  --border-section:  4px solid #000000;
  --border-hairline: 1px solid #525252;

  --space-xs:  4px;
  --space-sm:  8px;
  --space-md:  16px;
  --space-lg:  32px;
  --space-xl:  64px;
  --space-2xl: 96px;
}
```

### `_includes/base.html`

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>gallr — Discover Art Exhibitions Near You</title>
  <meta name="description" content="gallr helps you discover ongoing and upcoming art and cultural exhibitions in your city." />
  <link rel="preload" as="font" type="font/woff2"
        href="/fonts/playfair-display-700.woff2" crossorigin />
  <link rel="stylesheet" href="/styles/tokens.css" />
</head>
<body>
  <a href="#main-content" class="skip-link">Skip to content</a>
  <main id="main-content">
    {{ content }}
  </main>
  <footer>
    <p>&copy; 2026 gallr</p>
  </footer>
</body>
</html>
```

---

## Test-First Checklist (write these tests BEFORE implementing any section)

Per Constitution Principle II, every test must be written and verified to fail before the corresponding component is built:

- [ ] `tests/smoke.test.ts`: assert `<h1>` is visible with JS disabled — **must fail on empty scaffold**
- [ ] `tests/smoke.test.ts`: assert App Store `<a>` href is non-empty — **must fail on empty scaffold**
- [ ] `tests/smoke.test.ts`: assert Google Play `<a>` href is non-empty — **must fail on empty scaffold**
- [ ] `tests/smoke.test.ts`: assert 3 feature entries (`#discovery`, `#bookmarking`, `#filtering`) are present — **must fail on empty scaffold**
- [ ] `tests/accessibility.test.js`: assert zero WCAG AA violations on `dist/index.html` — **must fail on empty scaffold** (missing landmarks)

Run `npm test` and confirm all 5 tests fail before writing any HTML.

---

## Deploy

```bash
# Vercel (recommended — auto-detects Eleventy)
npm install -g vercel
cd web && vercel --prod
# Set output directory to "dist" in Vercel project settings

# GitHub Pages (alternative)
# Push web/dist/ to gh-pages branch via GitHub Actions
# Deployment workflow is out of scope for this feature
```
