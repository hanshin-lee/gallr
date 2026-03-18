import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
  testDir: "./tests",
  testMatch: "**/*.test.ts",
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: 0,
  workers: 1,

  use: {
    // All smoke tests run with JavaScript disabled (FR-008)
    javaScriptEnabled: false,
    baseURL: "http://localhost:4242",
  },

  // Auto-start a static file server against the built dist/
  webServer: {
    command: "npx serve dist -l 4242 --no-clipboard",
    url: "http://localhost:4242",
    // Always start a fresh server — never reuse a stale one on the port
    reuseExistingServer: false,
    timeout: 30000,
  },

  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
});
