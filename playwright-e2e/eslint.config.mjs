import { LintingConfig } from "@hmcts/playwright-common";
import tseslint from "typescript-eslint";

const ignored = {
  ignores: [
    ...LintingConfig.ignored.ignores,
    "playwright-report/**",
    "test-results/**",
    ".sessions/**",
    "e2e-pr-report/**",
    "e2e-pr-results/**",
  ],
};

export default tseslint.config(
  LintingConfig.tseslintRecommended,
  ignored,
  LintingConfig.tseslintPlugin,
  LintingConfig.playwright
);
