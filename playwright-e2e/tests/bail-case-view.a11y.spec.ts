import { test } from "../fixtures";
import { config } from "../utils";

test.use({ storageState: config.users.adminOfficer.sessionFile });

/**
 * AxeUtils.audit asserts with expect.soft, so these tests report every violation on the page
 * rather than stopping at the first. They are excluded from `yarn test` and run on their own
 * with `yarn test:a11y`, because the pages are XUI's and most findings belong to XUI rather
 * than to this definition. Automated checks only find about 40% of issues.
 */
test.describe("Accessibility", () => {
  test(
    "reports no accessibility violations on the create case page",
    { tag: "@a11y" },
    async ({ createCasePage, axeUtils }) => {
      await createCasePage.selectJurisdiction(config.bail.jurisdictionLabel);
      await createCasePage.selectCaseType(config.bail.caseTypeLabel);
      await createCasePage.selectEvent(config.bail.startApplicationEventLabel);

      await axeUtils.audit();
    }
  );

  test(
    "reports no accessibility violations on the overview tab of a bail case",
    { tag: "@a11y" },
    async ({ bailCaseDetailsPage, axeUtils }) => {
      await bailCaseDetailsPage.openTab(bailCaseDetailsPage.tabs.overview);

      await axeUtils.audit();
    }
  );

  test(
    "reports no accessibility violations on the case history tab of a bail case",
    { tag: "@a11y" },
    async ({ bailCaseDetailsPage, axeUtils }) => {
      await bailCaseDetailsPage.openTab(bailCaseDetailsPage.tabs.caseHistory);

      await axeUtils.audit();
    }
  );
});
