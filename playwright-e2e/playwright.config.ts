import { CommonConfig, ProjectsConfig } from "@hmcts/playwright-common";
import { defineConfig } from "@playwright/test";

/**
 * Where a run writes its HTML report, junit file and failure artefacts. The PR subset
 * (`yarn test:ci:pr`) and the full suite (`yarn test:ci`) can run in the same Jenkins workspace,
 * so each publishes from its own place: E2E_OUTPUT_NAME=e2e-pr gives e2e-pr-report/,
 * e2e-pr-junit.xml and e2e-pr-results/.
 */
const outputName = process.env.E2E_OUTPUT_NAME?.trim();
const output = outputName
  ? {
      report: `${outputName}-report`,
      junit: `${outputName}-junit.xml`,
      results: `${outputName}-results`,
    }
  : { report: "playwright-report", junit: "playwright-junit.xml", results: "test-results" };

/**
 * Only chromium is configured. These tests run against a preview namespace with a single
 * replica of each CCD service, so they use one browser and the worker count CommonConfig
 * derives from FUNCTIONAL_TESTS_WORKERS on CI (4 locally). Every test seeds its own case, so
 * they run in parallel; the sign-ins in the setup project do not.
 *
 * Retries are off on CI as well as locally (CommonConfig turns on two when CI is set): a test
 * that only passes on a second attempt is reported as the failure it is.
 *
 * globalTimeout bounds a whole run. On preview the @pr tier takes about 2 minutes and the full
 * suite about 3; 15 minutes leaves room for a slow preview without letting a stuck run hold the
 * pipeline.
 */
export default defineConfig({
  testDir: "./tests",
  ...CommonConfig.recommended,
  retries: 0,
  globalTimeout: 15 * 60_000,
  outputDir: output.results,
  reporter: process.env.CI
    ? [
        ["list"],
        ["html", { open: "never", outputFolder: output.report }],
        ["junit", { outputFile: output.junit }],
      ]
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
