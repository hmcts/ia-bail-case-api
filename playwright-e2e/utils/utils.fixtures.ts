import { AxeUtils } from "@hmcts/playwright-common";
import {
  Fixtures,
  PlaywrightTestArgs,
  PlaywrightTestOptions,
} from "@playwright/test";
import { Config, config } from "./config.utils";
import { CookieUtils } from "./cookie.utils";

export interface UtilsFixtures {
  config: Config;
  axeUtils: AxeUtils;
  cookieUtils: CookieUtils;
}

export const utilsFixtures: Fixtures<
  UtilsFixtures,
  object,
  PlaywrightTestArgs & PlaywrightTestOptions
> = {
  config: async ({}, use) => {
    await use(config);
  },
  cookieUtils: async ({}, use) => {
    await use(new CookieUtils());
  },
  /**
   * Accessibility audits. generateReport has to run after the test body, otherwise the
   * consolidated report is never attached to the Playwright report.
   */
  axeUtils: async ({ page }, use, testInfo) => {
    const axeUtils = new AxeUtils(page);
    await use(axeUtils);
    await axeUtils.generateReport(testInfo);
  },
};
