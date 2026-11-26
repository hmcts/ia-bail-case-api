import { expect, test } from "../fixtures";
import {
  CaseFixtures,
  ROLE_VIEWS,
  SeededBailCase,
  UserRole,
  config,
  contentTabsFor,
  expectSeededDataShown,
} from "../utils";

/**
 * What each role is shown on a submitted case, read from a case of the test's own.
 *
 * - The case-type tabs. The case history and case notes tabs are only granted to the admin
 *   officer and the judge; XUI's own tabs (Tasks, Roles and access, Hearings) are left out.
 * - The Overview tab renders exactly one progress panel, and it is the role's own. The 41
 *   progress_* label fields on that tab (BailTabs.java) have show conditions that are not
 *   mutually exclusive, so field-level read access is what selects the panel for the role: a role
 *   that can read other roles' progress fields is shown several panels at once. Hence a count,
 *   and the ID.
 * - The seeded applicant, addresses, supporter, interpreter languages, hearing centre and (on a
 *   legal representative's case) the legal rep's details are shown where the definition puts
 *   them.
 * - For the roles granted it, the case history tab lists the case's events, latest first (each
 *   role sees only the events it has access to: the judge is not shown startApplication).
 *   ccd-data-store-api hydrates only the first CaseHistoryViewer field in a case type, so the tab
 *   is only filled if it shows that field.
 */
test.describe("Bail case view", () => {
  for (const role of ["adminOfficer", "judge", "homeOffice"] as const) {
    test(
      `shows the ${config.users[role].label} their tabs, their one progress panel and the case's data`,
      { tag: "@pr" },
      async ({ bailCase, openCaseAs, ccdApiAs }) => {
        await expectRoleView(role, bailCase, { openCaseAs, ccdApiAs });
      }
    );
  }

  test(
    "shows the legal representative their tabs, their one progress panel and their own case's data",
    { tag: "@pr" },
    async ({ legalRepBailCase, openCaseAs, ccdApiAs }) => {
      await expectRoleView("legalRep", legalRepBailCase, { openCaseAs, ccdApiAs });
    }
  );
});

async function expectRoleView(
  role: UserRole,
  seeded: SeededBailCase,
  { openCaseAs, ccdApiAs }: Pick<CaseFixtures, "openCaseAs" | "ccdApiAs">
): Promise<void> {
  const viewer = config.users[role].label;
  const view = ROLE_VIEWS[role];
  const casePage = await openCaseAs(role, seeded.caseId);

  expect(await casePage.visibleBailTabs(), `case tabs shown to the ${viewer}`).toEqual(
    view.tabs
  );
  await casePage.expectOneProgressPanel(view.submittedProgressField);

  const snapshot = await casePage.snapshotTabs(contentTabsFor(role));
  expectSeededDataShown(snapshot, seeded, viewer);

  if (view.tabs.includes("Case history")) {
    const creator = await (await ccdApiAs(seeded.createdBy)).signedInUserName();
    await casePage.expectLatestEvent("Submit the application", creator);
  }
}
