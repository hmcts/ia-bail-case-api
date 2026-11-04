import { LintingConfig } from "@hmcts/playwright-common";
import tseslint from "typescript-eslint";

const ignored = {
  ignores: [
    ...LintingConfig.ignored.ignores,
    "playwright-report/**",
    "test-results/**",
    ".sessions/**",
  ],
};

export default tseslint.config(
  LintingConfig.tseslintRecommended,
  ignored,
  LintingConfig.tseslintPlugin,
  LintingConfig.playwright
);
