import { IdamPage } from "@hmcts/playwright-common";
import {
  Fixtures,
  PlaywrightTestArgs,
  PlaywrightTestOptions,
} from "@playwright/test";
import { CreateCasePage } from "./create-case.po";
import { StartApplicationPage } from "./start-application.po";

export interface PageFixtures {
  idamPage: IdamPage;
  createCasePage: CreateCasePage;
  startApplicationPage: StartApplicationPage;
}

export const pageFixtures: Fixtures<
  PageFixtures,
  object,
  PlaywrightTestArgs & PlaywrightTestOptions
> = {
  idamPage: async ({ page }, use) => {
    await use(new IdamPage(page));
  },
  createCasePage: async ({ page }, use) => {
    const createCasePage = new CreateCasePage(page);
    await createCasePage.goto();
    await use(createCasePage);
  },
  startApplicationPage: async ({ page }, use) => {
    await use(new StartApplicationPage(page));
  },
};
