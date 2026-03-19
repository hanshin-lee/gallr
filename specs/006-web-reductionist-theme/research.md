# Research: Web Reductionist Design System (006)

**Feature Branch**: `006-web-reductionist-theme`
**Generated**: 2026-03-20

---

## Decision 1: Inter font delivery mechanism

**Decision**: Use `@fontsource/inter` npm package, delivered through the existing `scripts/copy-fonts.js` build pipeline.

**Rationale**: The web project already self-hosts fonts via `copy-fonts.js`, which copies WOFF2 files from `@fontsource` packages into `public/fonts/`. Adding `@fontsource/inter` keeps the delivery mechanism identical to the current Playfair Display and JetBrains Mono setup — no new infrastructure needed. `@fontsource/inter` ships static weight WOFF2 files (400, 500, 700) which are exact equivalents of the TTF files used in the KMP app.

**Alternatives considered**:
- Google Fonts CDN: Rejected — introduces external dependency, privacy/GDPR concern, no offline support.
- Manual WOFF2 download: Rejected — would require manual updates; `@fontsource` keeps fonts versioned via npm.
- Variable font (Inter Variable): Rejected — CMP does not support variable fonts; keeping static weights maintains parity with the KMP side and avoids a web-only deviation.

---

## Decision 2: Font token update strategy — values vs. rename

**Decision**: Update the VALUES of `--font-display` and `--font-meta` tokens in `tokens.css` to both resolve to Inter (with a neutral sans-serif fallback stack), rather than renaming the tokens or introducing a new `--font-sans` token.

**Rationale**: Changing only the token values means zero changes to any CSS selector that already references `var(--font-display)` or `var(--font-meta)`. Both tokens converge on Inter — the semantic distinction (display vs. meta) dissolves in the reductionist system. This is the minimum viable diff per Principle III (Simplicity & YAGNI). `_data/tokens.js` (the JS data mirror) must be updated to match.

**Alternatives considered**:
- Introduce new `--font-sans` token and update all CSS references: Rejected — adds churn across `main.css` with no user-facing benefit; violates YAGNI.
- Keep `--font-display` as Playfair Display for headings and only change `--font-meta`: Rejected — partial swap creates the exact inconsistency the feature aims to eliminate.

---

## Decision 3: Accent colour token — scope and naming

**Decision**: Add a single new CSS custom property `--color-accent: #FF5400` to `:root` in `tokens.css`. Mirror it in `_data/tokens.js`. Apply it only to `.btn--primary` (default background and darkened-opacity hover state). Focus/active state uses the same token at reduced opacity.

**Rationale**: One token for one accent colour is the simplest implementation of FR-003. The token name `--color-accent` matches the semantic role defined in the spec (primary CTA, active state, interaction feedback) and is technology-agnostic. Using a CSS custom property means any future component that needs orange simply references the token — no hardcoded hex values in component CSS.

**Alternatives considered**:
- Three separate tokens (`--color-cta-primary`, `--color-active-indicator`, `--color-interaction-feedback`): Rejected — all three resolve to the same value (#FF5400); splitting is premature for a static marketing site with a single CTA type.
- Hardcode `#FF5400` directly in `.btn--primary`: Rejected — creates a maintenance divergence from the token system and violates the "all values use tokens" comment in `tokens.css`.

---

## Decision 4: Primary CTA hover state

**Decision**: `.btn--primary:hover` changes from the current inversion (white background, black text) to a darkened version of the accent: `background-color: color-mix(in srgb, var(--color-accent) 80%, black)` yielding approximately #CC4400 (the same orange darkened by 20%). The text remains white throughout.

**Rationale**: The spec requires state change "through a contrast or opacity shift" within 100ms, not the current inversion which reads as a different button entirely. Darkening the orange preserves the CTA identity while clearly communicating the active state. `color-mix()` is supported in all modern browsers (Chrome 111+, Safari 16.2+, Firefox 113+). The existing `transition: background-color 100ms ease` already satisfies the 100ms response requirement.

**Alternatives considered**:
- `opacity: 0.85` on the whole button: Rejected — reduces contrast against white background; may appear faded rather than active.
- Current inversion (white fill, black text): Rejected — spec explicitly requires colour/opacity shift, not a full identity swap that removes the orange signal.
- `filter: brightness(0.8)`: Rejected — CSS filter is heavier than background-color change; `color-mix()` is semantically cleaner.

---

## Decision 5: Section separator removal approach

**Decision**: Remove `border-top: var(--border-section)` from the `#features`, `#downloads`, and `#about` CSS rules in `main.css`. Retain the existing `padding: var(--space-xl) 0` on each section to preserve spacing. No additional top padding is required — the current padding is already sufficient as a whitespace separator.

**Rationale**: The 4px black border between sections was the web equivalent of the `HorizontalDivider` elements removed from the KMP app in 005. Removing only the border-top line (not the padding) maintains the current vertical rhythm while eliminating the decorative element. The `--border-section` token can remain defined in `tokens.css` for potential future use but will not be applied to any element.

**Alternatives considered**:
- Replace `--border-section` border-top with a larger `padding-top`: Rejected — existing padding is already `var(--space-xl)` (64px) which is generous; adding more creates excess whitespace.
- Remove `--border-section` token definition entirely: Rejected — the token is defined in `tokens.css` and removing it has no user-facing impact; avoids unnecessary churn.

---

## Decision 6: Font preload update in base.html

**Decision**: Update the `<link rel="preload">` in `web/_includes/base.html` from `playfair-display-700.woff2` to `inter-700.woff2` (the bold weight used for hero headline and card titles — the largest and most LCP-critical text).

**Rationale**: Preloading the bold Inter weight matches the current strategy of preloading the heaviest above-the-fold font. This eliminates the layout shift warning from the browser when the font file changes, and ensures LCP text renders in Inter without a FOIT delay.

**Alternatives considered**:
- Preload Inter Regular (400): Rejected — body text (Regular) is below-the-fold on most viewports; the bold headline is the LCP element.
- Preload both 400 and 700: Rejected — double preload adds a render-blocking hint with minimal LCP benefit; single preload is the current convention and should be preserved.
