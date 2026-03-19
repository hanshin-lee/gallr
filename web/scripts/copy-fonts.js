#!/usr/bin/env node
// Copies required WOFF2 font files from @fontsource packages to public/fonts/
// Runs as part of the build step: node scripts/copy-fonts.js

const fs = require("fs");
const path = require("path");

const FONT_MAP = [
  {
    src: "node_modules/@fontsource/inter/files",
    patterns: [
      "inter-latin-400-normal.woff2",
      "inter-latin-500-normal.woff2",
      "inter-latin-700-normal.woff2",
    ],
    dest: [
      "public/fonts/inter-400.woff2",
      "public/fonts/inter-500.woff2",
      "public/fonts/inter-700.woff2",
    ],
  },
];

const destDir = path.join(__dirname, "..", "public", "fonts");
if (!fs.existsSync(destDir)) fs.mkdirSync(destDir, { recursive: true });

let allCopied = true;

for (const { src, patterns, dest } of FONT_MAP) {
  const srcDir = path.join(__dirname, "..", src);
  for (let i = 0; i < patterns.length; i++) {
    const srcFile = path.join(srcDir, patterns[i]);
    const destFile = path.join(__dirname, "..", dest[i]);
    if (fs.existsSync(srcFile)) {
      fs.copyFileSync(srcFile, destFile);
      console.log(`✓ Copied ${patterns[i]} → ${dest[i]}`);
    } else {
      // Fallback: search for any woff2 file matching weight hint
      const weight = patterns[i].includes("-400-") ? "400" : "700";
      const allFiles = fs.existsSync(srcDir) ? fs.readdirSync(srcDir) : [];
      const match = allFiles.find(
        (f) => f.endsWith(".woff2") && f.includes(`-latin-${weight}-`) && f.includes("normal")
      );
      if (match) {
        fs.copyFileSync(path.join(srcDir, match), destFile);
        console.log(`✓ Copied ${match} → ${dest[i]} (fallback match)`);
      } else {
        console.warn(`⚠ Font not found: ${srcFile} — run 'npm install' first`);
        allCopied = false;
      }
    }
  }
}

if (!allCopied) {
  console.error("Some fonts could not be copied. Run 'npm install' first.");
  process.exit(1);
}
console.log("✓ All fonts copied to public/fonts/");
