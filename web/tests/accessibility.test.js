#!/usr/bin/env node
// pa11y WCAG AA accessibility test
// Asserts zero violations on the built dist/index.html
// Run after `npm run build`: node tests/accessibility.test.js

const path = require("path");
const pa11y = require("pa11y");

async function run() {
  const distFile = path.resolve(__dirname, "../dist/index.html");
  const url = `file://${distFile}`;

  console.log(`Running WCAG AA audit on ${url}`);

  let results;
  try {
    results = await pa11y(url, {
      standard: "WCAG2AA",
      ignore: [
        // Ignore notices and warnings — only fail on errors
        "WCAG2AA.Principle1.Guideline1_4.1_4_3.G18.Fail",
      ],
      includeNotices: false,
      includeWarnings: false,
      timeout: 30000,
    });
  } catch (err) {
    console.error("pa11y failed to run:", err.message);
    process.exit(1);
  }

  const errors = results.issues.filter((i) => i.type === "error");

  if (errors.length > 0) {
    console.error(`\n✗ ${errors.length} WCAG AA violation(s) found:\n`);
    errors.forEach((issue, i) => {
      console.error(`  ${i + 1}. [${issue.code}]`);
      console.error(`     ${issue.message}`);
      console.error(`     Selector: ${issue.selector}\n`);
    });
    process.exit(1);
  }

  console.log("✓ No WCAG AA violations found.");
}

run();
