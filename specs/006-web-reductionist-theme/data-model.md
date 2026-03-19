# Data Model: Web Reductionist Design System (006)

**Feature Branch**: `006-web-reductionist-theme`
**Generated**: 2026-03-20

This feature introduces no new data models or entities. All changes are CSS design token mutations.
The token model below documents the before/after state of each affected token.

---

## CSS Design Tokens (web/_includes/../dist/styles/tokens.css)

### Color Tokens

| Token | Current Value | New Value | Notes |
|-------|--------------|-----------|-------|
| `--color-ink` | `#000000` | `#000000` | Unchanged |
| `--color-paper` | `#ffffff` | `#ffffff` | Unchanged |
| `--color-paper-alt` | `#f5f5f5` | `#f5f5f5` | Unchanged |
| `--color-ink-secondary` | `#525252` | `#525252` | Unchanged |
| `--color-accent` | *(absent)* | `#FF5400` | **NEW** — vivid orange; restricted to primary CTA and interaction feedback |

### Font Tokens

| Token | Current Value | New Value | Notes |
|-------|--------------|-----------|-------|
| `--font-display` | `"Playfair Display", Georgia, "Times New Roman", serif` | `"Inter", system-ui, -apple-system, Arial, sans-serif` | Replaces serif display font |
| `--font-meta` | `"JetBrains Mono", "Courier New", Courier, monospace` | `"Inter", system-ui, -apple-system, Arial, sans-serif` | Replaces monospaced meta font; converges with `--font-display` |

### Shape / Border Tokens (unchanged)

| Token | Value | Notes |
|-------|-------|-------|
| `--radius` | `0px` | Unchanged — sharp corners retained |
| `--border-ink` | `1px solid var(--color-ink)` | Unchanged — used on card mockup and CTA outline |
| `--border-section` | `4px solid var(--color-ink)` | Token retained but **no longer applied to any CSS selector** |
| `--border-hairline` | `1px solid var(--color-ink-secondary)` | Unchanged — used on header and footer |

### Spacing Tokens (unchanged)

| Token | Value |
|-------|-------|
| `--space-xs` | `4px` |
| `--space-sm` | `8px` |
| `--space-md` | `16px` |
| `--space-lg` | `32px` |
| `--space-xl` | `64px` |
| `--space-2xl` | `96px` |

---

## JS Token Mirror (_data/tokens.js)

| Property | Current Value | New Value |
|----------|--------------|-----------|
| `color.ink` | `"#000000"` | `"#000000"` |
| `color.paper` | `"#ffffff"` | `"#ffffff"` |
| `color.paperAlt` | `"#f5f5f5"` | `"#f5f5f5"` |
| `color.inkSecondary` | `"#525252"` | `"#525252"` |
| `color.accent` | *(absent)* | `"#FF5400"` |
| `font.display` | `"'Playfair Display', Georgia, 'Times New Roman', serif"` | `"'Inter', system-ui, -apple-system, Arial, sans-serif"` |
| `font.meta` | `"'JetBrains Mono', 'Courier New', Courier, monospace"` | `"'Inter', system-ui, -apple-system, Arial, sans-serif"` |

---

## Font Files (web/public/fonts/)

| Current File | Action | New File |
|--------------|--------|----------|
| `playfair-display-400.woff2` | Replace | `inter-400.woff2` |
| `playfair-display-700.woff2` | Replace | `inter-700.woff2` |
| `jetbrains-mono-400.woff2` | Replace | `inter-500.woff2` |

> Font files are copied from `@fontsource/inter` npm package by `scripts/copy-fonts.js` at build time.

---

## InteractionState (primary CTA)

| State | CSS Property | Value |
|-------|-------------|-------|
| Default | `background-color` | `var(--color-accent)` = `#FF5400` |
| Default | `color` | `var(--color-paper)` = `#ffffff` |
| Hover/Active | `background-color` | `color-mix(in srgb, var(--color-accent) 80%, black)` ≈ `#CC4400` |
| Hover/Active | `color` | `var(--color-paper)` = `#ffffff` (unchanged) |
| Focus | `outline` | `3px solid var(--color-accent)` |
| Transition | `transition` | `background-color 100ms ease` (existing, no change) |

---

## Component Impact Summary

| Component | File | Change |
|-----------|------|--------|
| Design tokens | `web/dist/styles/tokens.css` | Add `--color-accent`; update `--font-display` and `--font-meta` values |
| JS token mirror | `web/_data/tokens.js` | Add `color.accent`; update `font.display` and `font.meta` values |
| Main styles | `web/dist/styles/main.css` | Update `.btn--primary` colours; remove `border-top` from `#features`, `#downloads`, `#about` |
| Base template | `web/_includes/base.html` | Update `<link rel="preload">` from Playfair to Inter bold WOFF2 |
| Font copy script | `web/scripts/copy-fonts.js` | Replace Playfair Display + JetBrains Mono entries with Inter 400/500/700 entries |
| npm dependencies | `web/package.json` | Replace `@fontsource/playfair-display` + `@fontsource/jetbrains-mono` with `@fontsource/inter` |
