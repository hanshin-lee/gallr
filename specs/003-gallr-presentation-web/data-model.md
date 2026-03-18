# Data Model: gallr Presentation Website

**Branch**: `003-gallr-presentation-web` | **Date**: 2026-03-18
**Phase**: 1 — Entities from spec.md

---

> This is a purely static presentation site with no persistent data storage. "Data model" here describes the **content model** — the structured data that each page section requires to render — and the **design token model** that governs all visual styling. There are no database schemas, no API payloads, and no user-generated data.

---

## Content Entities

### HeroSection

The above-the-fold entry point of the page.

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `headline` | string | ✅ | Primary display text; max 60 characters to fit a single serif line at desktop size |
| `tagline` | string | ✅ | Secondary clarifying sentence; max 100 characters |
| `downloadCTAs` | `DownloadCTA[]` | ✅ | Exactly 2 items: one iOS, one Android |

**Validation rules**:
- `headline` must be non-empty and fit within the display type scale (headline truncation is a failure state, not a design choice)
- `downloadCTAs` must contain exactly one `ios` entry and one `android` entry

---

### FeatureEntry

A single feature presentation unit within the Features section.

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `id` | string | ✅ | Unique slug; used as HTML `id` for anchor linking |
| `headline` | string | ✅ | Short capability name; max 40 characters |
| `description` | string | ✅ | Supporting text; max 200 characters; plain text only |
| `visual` | `VisualRepresentation \| null` | ❌ | Optional styled card mockup; absence is acceptable (spec assumption: no screenshots available) |

**Validation rules**:
- Minimum 3 `FeatureEntry` items required in the page (FR-005)
- Required feature IDs: `discovery`, `bookmarking`, `filtering`

---

### DownloadCTA

A call-to-action element linking to an app store.

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `store` | `'ios' \| 'android'` | ✅ | Determines label text and accessible name |
| `href` | string | ✅ | URL to app store listing; placeholder `/coming-soon` acceptable during development |
| `label` | string | ✅ | Human-readable button label; e.g. "Download on the App Store" |

**Validation rules**:
- `href` must be a valid URL or the string `/coming-soon` (no empty strings, no `#` anchors)
- `label` must include the store name for screen reader clarity

---

### VisualRepresentation

A styled mockup that mirrors the app's monochrome card design, replacing an actual device screenshot.

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `type` | `'card-mockup'` | ✅ | Only `card-mockup` type is in scope for this feature |
| `title` | string | ✅ | Sample exhibition title rendered in the mockup; max 50 characters |
| `venue` | string | ✅ | Sample venue name; max 40 characters |
| `dateRange` | string | ✅ | Formatted date range string; e.g. "15 Jan — 28 Feb 2026" |

**Notes**: Mockups are rendered using HTML + CSS only, not images. They must apply the same 0px corner radius, serif/mono typography, and hairline border treatment as the app's card design.

---

### Section

A named content block in the page, establishing structure and visual separation.

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `id` | string | ✅ | HTML `id` attribute for in-page navigation |
| `label` | string | ✅ | Section heading or screen-reader label |
| `separatorStyle` | `'major' \| 'hairline' \| 'none'` | ✅ | `major`: 4px solid black rule; `hairline`: 1px gray rule; `none`: for the first/hero section |

**Sections in page order**:

| Order | id | label | separatorStyle |
|-------|----|-------|---------------|
| 1 | `hero` | (none — top of page) | `none` |
| 2 | `features` | Features | `major` |
| 3 | `downloads` | Download | `major` |
| 4 | `about` | About | `major` |

---

## Design Token Model

The `tokens.css` file is the canonical source of truth for all visual constants. No hardcoded color, font, or spacing values are permitted outside this file.

### Color Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `--color-ink` | `#000000` | Primary text, borders, filled button backgrounds |
| `--color-paper` | `#ffffff` | Page background, filled button text |
| `--color-paper-alt` | `#f5f5f5` | Section background alternation (optional subtle differentiation) |
| `--color-ink-secondary` | `#525252` | Secondary text, hairline borders, captions |

### Typography Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `--font-display` | `'Playfair Display', Georgia, serif` | Headlines, exhibition titles, section headings |
| `--font-meta` | `'IBM Plex Mono', 'Courier New', monospace` | Dates, venue labels, metadata, button labels |

### Shape & Border Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `--radius` | `0px` | All corners everywhere — no exceptions |
| `--border-ink` | `1px solid #000000` | Card outlines, secondary button borders |
| `--border-section` | `4px solid #000000` | Major section separators (FR-011) |
| `--border-hairline` | `1px solid #525252` | Minor dividers within cards and sections |

### Spacing Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `--space-xs` | `4px` | Tight internal gaps |
| `--space-sm` | `8px` | Component internal padding |
| `--space-md` | `16px` | Standard padding |
| `--space-lg` | `32px` | Section internal spacing |
| `--space-xl` | `64px` | Section-to-section gap |
| `--space-2xl` | `96px` | Hero vertical padding |

---

## State Model (Interactive Elements)

### Button / DownloadCTA States

| State | Background | Text | Border |
|-------|-----------|------|--------|
| Default (primary) | `--color-ink` | `--color-paper` | none |
| Hover (primary) | `--color-paper` | `--color-ink` | `--border-ink` |
| Default (secondary/outline) | `--color-paper` | `--color-ink` | `--border-ink` |
| Hover (secondary) | `--color-ink` | `--color-paper` | none |
| Focus | any | any | `3px solid --color-ink` at `3px offset` |

**Notes**: No pressed/active state animation is specified for the web (this was defined for the native KMP app). `hover` state replaces the native `pressed` inversion. `focus` ring is required for keyboard accessibility.
