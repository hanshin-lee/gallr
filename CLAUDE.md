# gallr Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-03-20

## Active Technologies
- Kotlin 2.1.x, Compose Multiplatform 1.8.0 + Compose Multiplatform, Material3, compose-resources (CMP 1.8.0 (002-monochrome-design-system)
- N/A — design tokens are compile-time Kotlin objects; font TTF files bundled (002-monochrome-design-system)
- HTML5 / CSS3, JavaScript ES2022 (build tooling only — no runtime JS shipped) + Astro 5.x (static site generator), `@fontsource/playfair-display`, `@fontsource/ibm-plex-mono` (self-hosted web fonts) (003-gallr-presentation-web)
- N/A — fully static (003-gallr-presentation-web)
- HTML5 / CSS3, JavaScript ES2022 (build tooling only — no runtime JS shipped) + Eleventy 3.x (static site generator), self-hosted WOFF2 fonts (Playfair Display, JetBrains Mono) (003-gallr-presentation-web)
- Kotlin 2.1.20 (KMP), Swift 5.9 (iOS entry point only) (004-naver-map-api)
- N/A — no new persistence (004-naver-map-api)
- Kotlin 2.1.20 (KMP), Swift 5.9 (iOS entry point — no changes needed) + Compose Multiplatform 1.8.0, compose-resources (font loading) — no new dependencies (005-reductionist-design-system)
- N/A — compile-time token objects only, no runtime persistence (005-reductionist-design-system)
- HTML5 / CSS3, JavaScript ES2022 (build scripts only — no runtime JS) + Eleventy 3.x (static site generator), `@fontsource/inter` (replace `@fontsource/playfair-display` + `@fontsource/jetbrains-mono`), pa11y 8.x (accessibility), Playwright 1.49 (visual acceptance tests) (006-web-reductionist-theme)

- Kotlin 2.0+ (2.3.0 recommended), Compose Multiplatform 1.8.0+ + Ktor 2.9+ (networking), DataStore Preferences 1.1+ (bookmarks), AndroidX ViewModel 2.8.0+, kotlinx.serialization 1.7+, kotlinx-datetime (001-exhibition-tabs)

## Project Structure

```text
src/
tests/
```

## Commands

# Add commands for Kotlin 2.0+ (2.3.0 recommended), Compose Multiplatform 1.8.0+

## Code Style

Kotlin 2.0+ (2.3.0 recommended), Compose Multiplatform 1.8.0+: Follow standard conventions

## Recent Changes
- 006-web-reductionist-theme: Added HTML5 / CSS3, JavaScript ES2022 (build scripts only — no runtime JS) + Eleventy 3.x (static site generator), `@fontsource/inter` (replace `@fontsource/playfair-display` + `@fontsource/jetbrains-mono`), pa11y 8.x (accessibility), Playwright 1.49 (visual acceptance tests)
- 005-reductionist-design-system: Added Kotlin 2.1.20 (KMP), Swift 5.9 (iOS entry point — no changes needed) + Compose Multiplatform 1.8.0, compose-resources (font loading) — no new dependencies
- 004-naver-map-api: Added Kotlin 2.1.20 (KMP), Swift 5.9 (iOS entry point only)


<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
