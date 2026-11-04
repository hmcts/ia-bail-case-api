import { expect, test } from "../fixtures";
import { CaseFlagsPage } from "../page-objects";

/**
 * Case flags as the admin officer, on a legal representative's submitted case, ported from
 * bails-add-a-case-flag.feature: "Create a case Flag", and "Create a case Flag and make Inactive"
 * (tagged @fix-test in the old suite). The flag is created through the UI in both, because
 * creating it is what the first scenario tests and the second's starting point.
 */
test.describe("Case flags", () => {
  test(
    "creates a case-level flag and shows it in the banner and on the case flags tab",
    async ({ seedCase, openCaseAs }) => {
      const seeded = await seedCase("legalRepBails");
      const comment = `Complex case flag on ${seeded.caseId}`;

      const casePage = await openCaseAs("adminOfficer", seeded.caseId);
      await casePage.startEvent(CaseFlagsPage.CREATE.name);
      await new CaseFlagsPage(casePage.page, CaseFlagsPage.CREATE).createCaseLevelFlag("Complex Case", comment);

      const flagged = await openCaseAs("adminOfficer", seeded.caseId);
      await expect(flagged.page.getByText("There is 1 active flag on this case.")).toBeVisible();
      await flagged.openTab("Case flags");
      const flagRow = CaseFlagsPage.caseLevelFlags(flagged.activeTabPanel)
        .getByRole("row")
        .filter({ has: flagged.page.getByRole("cell", { name: "Complex Case", exact: true }) });
      await expect(flagRow).toContainText(comment);
      await expect(flagRow.getByRole("cell", { name: "Active", exact: true })).toBeVisible();
    }
  );

  test(
    "makes a case-level flag inactive and takes it out of the active-flags banner",
    async ({ seedCase, openCaseAs }) => {
      const seeded = await seedCase("legalRepBails");

      const casePage = await openCaseAs("adminOfficer", seeded.caseId);
      await casePage.startEvent(CaseFlagsPage.CREATE.name);
      await new CaseFlagsPage(casePage.page, CaseFlagsPage.CREATE).createCaseLevelFlag(
        "Complex Case",
        `Flag to make inactive on ${seeded.caseId}`
      );

      const flagged = await openCaseAs("adminOfficer", seeded.caseId);
      await expect(flagged.page.getByText("There is 1 active flag on this case.")).toBeVisible();
      await flagged.startEvent(CaseFlagsPage.MANAGE.name);
      await new CaseFlagsPage(flagged.page, CaseFlagsPage.MANAGE).makeCaseLevelFlagInactive(
        "Complex Case",
        `No longer complex, on ${seeded.caseId}`
      );

      const after = await openCaseAs("adminOfficer", seeded.caseId);
      await after.openTab("Case flags");
      const flagRow = CaseFlagsPage.caseLevelFlags(after.activeTabPanel)
        .getByRole("row")
        .filter({ has: after.page.getByRole("cell", { name: "Complex Case", exact: true }) });
      await expect(flagRow.getByRole("cell", { name: "Inactive", exact: true })).toBeVisible();
      await expect(after.page.getByText(/There (is|are) \d+ active flags? on this case\./)).toHaveCount(0);
    }
  );
});
