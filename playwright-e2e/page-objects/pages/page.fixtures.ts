import { IdamPage } from "@hmcts/playwright-common";
import {
  Fixtures,
  PlaywrightTestArgs,
  PlaywrightTestOptions,
} from "@playwright/test";
import { requireBailCaseReference } from "../../utils/config.utils";
import { BailCaseDetailsPage } from "./bail-case-details.po";
import { CreateCasePage } from "./create-case.po";
import { StartApplicationPage } from "./start-application.po";

export interface PageFixtures {
  idamPage: IdamPage;
  createCasePage: CreateCasePage;
  startApplicationPage: StartApplicationPage;
  bailCaseDetailsPage: BailCaseDetailsPage;
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
  /**
   * Opens the Bail case named by BAIL_CASE_REFERENCE. requireBailCaseReference throws when it is
   * unset, so a test that needs a case fails here instead of passing against nothing.
   */
  bailCaseDetailsPage: async ({ page }, use) => {
    const bailCaseDetailsPage = new BailCaseDetailsPage(page);
    await bailCaseDetailsPage.goto(requireBailCaseReference());
    await use(bailCaseDetailsPage);
  },
};
