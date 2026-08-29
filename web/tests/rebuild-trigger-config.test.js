const assert = require("assert").strict;
const fs = require("fs");
const path = require("path");

// The public site bakes Supabase catalogue data into static pages at build time.
// Freshness therefore depends on rebuilds that carry NO source change at all:
//
//   1. `exhibition.published` -> outbox-delivery -> Vercel Deploy Hook
//   2. the 09:00 KST cron in .github/workflows/rebuild-web.yml
//
// Vercel exposes no environment variable identifying a Deploy Hook deployment,
// and a hook builds the branch tip -- the same commit that was already deployed.
// So a path-diff `ignoreCommand` (`git diff --quiet HEAD^ HEAD -- <paths>`)
// exits 0 for exactly those two triggers, and 0 means SKIP THE BUILD on Vercel.
//
// The observed production failure: an owner publishes, admin approves, the hook
// fires, Vercel skips the build, and the freshly published exhibition detail page
// is never generated -- the gallery portal's "view public page" link then serves
// a Vercel 404 NOT_FOUND.
//
// Guard the asymmetry: a needless rebuild costs one cheap Eleventy build, while a
// wrongly skipped rebuild is a customer-visible 404 on a published exhibition.

const repoRoot = path.resolve(__dirname, "../..");

const configs = [
  { label: "web/vercel.json", file: path.join(repoRoot, "web", "vercel.json") },
  { label: "root vercel.json", file: path.join(repoRoot, "vercel.json") },
];

for (const { label, file } of configs) {
  assert.equal(fs.existsSync(file), true, `${label} is missing`);

  const config = JSON.parse(fs.readFileSync(file, "utf8"));

  assert.equal(
    Object.prototype.hasOwnProperty.call(config, "ignoreCommand"),
    false,
    `${label} must not define ignoreCommand: a path diff exits 0 (skip) for ` +
      `Deploy Hook and cron rebuilds, which carry no source change, and those ` +
      `are the only triggers that refresh published exhibition pages`,
  );

  // `git.deploymentEnabled: false` would likewise suppress the rebuild path.
  const deploymentEnabled = config.git && config.git.deploymentEnabled;
  if (deploymentEnabled && typeof deploymentEnabled === "object") {
    for (const [branch, enabled] of Object.entries(deploymentEnabled)) {
      assert.notEqual(
        enabled,
        false,
        `${label} disables deployments for branch '${branch}', which would ` +
          `also block publish-triggered rebuilds`,
      );
    }
  }
}

console.log("[rebuild-trigger-config.test] all tests passed");
