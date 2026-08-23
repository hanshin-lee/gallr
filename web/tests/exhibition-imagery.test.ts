import { test, expect, type Page } from "@playwright/test";

// Exhibition imagery reads in full colour so the artwork carries the page.
// Monochrome is reserved as the signal for a run that has ended.
//
// Fixture rows (see tests/fixtures/exhibitions.json):
//   fx-002-closing-soon → line-and-form-fx-0      (running)
//   fx-004-closed       → monochrome-studies-fx-0 (ended)

// 1x1 transparent PNG — the fixture cover URLs are unresolvable stubs, so
// serve real bytes to keep the <img> loaded instead of falling back.
const PIXEL = Buffer.from(
  "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==",
  "base64",
);

// --duration-med is 500ms. Waiting past it before re-reading a computed style
// keeps a hover regression from slipping through mid-transition.
const TRANSITION_SETTLE_MS = 700;

async function serveCoverImages(page: Page) {
  await page.route("https://stub/**", (route) =>
    route.fulfill({ status: 200, contentType: "image/png", body: PIXEL }),
  );
}

function cardImage(page: Page, status: string) {
  const card = page.locator(`.exhibition-card[data-status="${status}"]`).first();
  return { card, image: card.locator(".exhibition-card__image") };
}

test.describe("Exhibition card imagery", () => {
  test("a running exhibition renders in colour and lifts on hover", async ({ page }) => {
    await serveCoverImages(page);
    await page.goto("/exhibitions/");

    const { card, image } = cardImage(page, "closing_soon");

    await expect(image).toBeVisible();
    await expect(image).toHaveCSS("filter", "none");
    await expect(image).toHaveCSS("transform", "none");

    await card.hover();
    await expect(image).toHaveCSS("transform", "matrix(1.03, 0, 0, 1.03, 0, 0)");
  });

  test("the hover lift is suppressed under reduced motion", async ({ page }) => {
    await serveCoverImages(page);
    await page.emulateMedia({ reducedMotion: "reduce" });
    await page.goto("/exhibitions/");

    const { card, image } = cardImage(page, "closing_soon");

    await expect(image).toBeVisible();
    await card.hover();
    await page.waitForTimeout(TRANSITION_SETTLE_MS);
    await expect(image).toHaveCSS("transform", "none");
    // Colour is a static treatment, not motion — it must survive the opt-out.
    await expect(image).toHaveCSS("filter", "none");
  });

  test("an ended exhibition stays monochrome, hovered or not", async ({ page }) => {
    await serveCoverImages(page);
    await page.goto("/exhibitions/");

    const { card, image } = cardImage(page, "closed");

    await expect(image).toBeVisible();
    await expect(image).toHaveCSS("filter", "grayscale(1)");

    await card.hover();
    await page.waitForTimeout(TRANSITION_SETTLE_MS);
    await expect(image).toHaveCSS("filter", "grayscale(1)");
  });
});

test.describe("Exhibition detail hero imagery", () => {
  test("a running exhibition's hero renders in colour", async ({ page }) => {
    await serveCoverImages(page);
    await page.goto("/exhibitions/line-and-form-fx-0/");

    await expect(page.locator(".detail-page__hero-image")).toHaveCSS("filter", "none");
  });

  test("an ended exhibition's hero renders monochrome", async ({ page }) => {
    await serveCoverImages(page);
    await page.goto("/exhibitions/monochrome-studies-fx-0/");

    const hero = page.locator(".detail-page__hero");
    await expect(hero).toHaveAttribute("data-status", "closed");
    await expect(hero.locator(".detail-page__hero-image")).toHaveCSS("filter", "grayscale(1)");
  });
});
