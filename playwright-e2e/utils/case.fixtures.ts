import {
  BrowserContext,
  Fixtures,
  Page,
  PlaywrightTestArgs,
  PlaywrightTestOptions,
  PlaywrightWorkerArgs,
  PlaywrightWorkerOptions,
} from "@playwright/test";
import { BailCaseDetailsPage } from "../page-objects/pages/bail-case-details.po";
import { BailCaseCreator, BailCaseSeeder, SeededBailCase } from "./bail-case.seeder";
import { ROLE_VIEWS } from "./case-view.expectations";
import { CcdApi } from "./ccd-api.utils";
import { UserRole, config } from "./config.utils";

export interface CaseFixtures {
  /** A browser context signed in as the role, opened on first use and closed after the test. */
  roleContext: (role: UserRole) => Promise<BrowserContext>;
  /** CCD, as the role. */
  ccdApiAs: (role: UserRole) => Promise<CcdApi>;
  /** Opens the case in the role's own page (one per role per test) and returns it. */
  openCaseAs: (role: UserRole, caseId: string) => Promise<BailCaseDetailsPage>;
  /** A case of this test's own, created and submitted by an admin officer. */
  bailCase: SeededBailCase;
  /** A case of this test's own, created and submitted by the legal representative. */
  legalRepBailCase: SeededBailCase;
}

const seedCase = async (
  ccdApiAs: CaseFixtures["ccdApiAs"],
  createdBy: BailCaseCreator,
  annotate: (description: string) => void
): Promise<SeededBailCase> => {
  const seeded = await new BailCaseSeeder(await ccdApiAs(createdBy)).seed(createdBy);
  annotate(`${seeded.caseId} (created by the ${config.users[createdBy].label})`);
  return seeded;
};

export const caseFixtures: Fixtures<
  CaseFixtures,
  object,
  PlaywrightTestArgs & PlaywrightTestOptions,
  PlaywrightWorkerArgs & PlaywrightWorkerOptions
> = {
  /**
   * Each role gets its own context from its stored session, with the project's viewport, so a
   * test can look at the same case as several roles at once. Contexts made from the `browser`
   * fixture are traced like the default one.
   */
  roleContext: async ({ browser, viewport }, use) => {
    const contexts = new Map<UserRole, Promise<BrowserContext>>();
    await use((role) => {
      let context = contexts.get(role);
      if (!context) {
        context = browser.newContext({
          storageState: config.users[role].sessionFile,
          viewport,
        });
        contexts.set(role, context);
      }
      return context;
    });
    for (const context of contexts.values()) {
      await (await context).close();
    }
  },
  ccdApiAs: async ({ roleContext }, use) => {
    await use(async (role) => new CcdApi(await roleContext(role)));
  },
  openCaseAs: async ({ roleContext }, use) => {
    const pages = new Map<UserRole, Promise<Page>>();
    await use(async (role, caseId) => {
      let page = pages.get(role);
      if (!page) {
        page = roleContext(role).then((context) => context.newPage());
        pages.set(role, page);
      }
      const casePage = new BailCaseDetailsPage(await page);
      await casePage.goto(caseId, ROLE_VIEWS[role].workAllocationTabs);
      return casePage;
    });
  },
  bailCase: async ({ ccdApiAs }, use, testInfo) => {
    await use(
      await seedCase(ccdApiAs, "adminOfficer", (description) =>
        testInfo.annotations.push({ type: "Bail case", description })
      )
    );
  },
  legalRepBailCase: async ({ ccdApiAs }, use, testInfo) => {
    await use(
      await seedCase(ccdApiAs, "legalRep", (description) =>
        testInfo.annotations.push({ type: "Bail case", description })
      )
    );
  },
};
