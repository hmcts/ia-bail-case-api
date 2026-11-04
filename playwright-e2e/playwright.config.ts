import { CommonConfig, ProjectsConfig } from "@hmcts/playwright-common";
import { defineConfig } from "@playwright/test";

/**
 * Only chromium is configured. These tests run against a preview namespace with a single
 * replica of each CCD service, so the suite stays deliberately small: one browser, and the
 * worker count CommonConfig derives from FUNCTIONAL_TESTS_WORKERS on CI.
 */
export default defineConfig({
  testDir: "./tests",
  ...CommonConfig.recommended,
  reporter: process.env.CI
    ? [["list"], ["html", { open: "never" }], ["junit", { outputFile: "playwright-junit.xml" }]]
    : [["list"]],
  projects: [
    {
      name: "setup",
      testMatch: /global\.setup\.ts/,
      testDir: ".",
    },
    {
      ...ProjectsConfig.chromium,
      dependencies: ["setup"],
    },
  ],
});
