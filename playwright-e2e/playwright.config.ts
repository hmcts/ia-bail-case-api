import { CommonConfig, ProjectsConfig } from "@hmcts/playwright-common";
import { defineConfig } from "@playwright/test";

/**
 * Only chromium is configured. These tests run against a preview namespace with a single
 * replica of each CCD service, so they use one browser and the worker count CommonConfig
 * derives from FUNCTIONAL_TESTS_WORKERS on CI (4 locally). Every test seeds its own case, so
 * they run in parallel; the four sign-ins in the setup project do not.
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
