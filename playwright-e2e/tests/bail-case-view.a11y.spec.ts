import { test } from "../fixtures";
import { BailCaseDetailsPage } from "../page-objects";
import { ROLE_VIEWS, config } from "../utils";

test.use({ storageState: config.users.adminOfficer.sessionFile });

/**
 * AxeUtils.audit asserts with expect.soft, so these tests report every violation on the page
 * rather than stopping at the first. They are excluded from `yarn test` and run on their own
 * with `yarn test:a11y`, because the pages are XUI's and most findings belong to XUI rather
 * than to this definition. Automated checks only find about 40% of issues.
 *
 * The case tabs are audited on the default page, which axeUtils is bound to, signed in as the
 * admin officer by test.use above.
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

  for (const tab of ["Overview", "Bail application", "Case history"] as const) {
    test(
      `reports no accessibility violations on the ${tab.toLowerCase()} tab of a bail case`,
      { tag: "@a11y" },
      async ({ page, bailCase, axeUtils }) => {
        const casePage = new BailCaseDetailsPage(page);
        await casePage.goto(bailCase.caseId, ROLE_VIEWS.adminOfficer.workAllocationTabs);
        await casePage.openTab(tab);

        await axeUtils.audit();
      }
    );
  }
});
