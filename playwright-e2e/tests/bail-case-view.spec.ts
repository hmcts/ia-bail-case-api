import { expect, test } from "../fixtures";
import { config } from "../utils";

test.use({ storageState: config.users.adminOfficer.sessionFile });

/**
 * Reads an existing Bail case named by BAIL_CASE_REFERENCE; see playwright-e2e/README.md for why
 * these tests do not create their own case. The case is only read, never edited, so the two
 * tests here are independent of each other and of anything else in the suite.
 */
test.describe("Bail case view", () => {
  /**
   * The overview tab's 41 progress_* label fields have show conditions that are not mutually
   * exclusive, so field-level read access is what picks the panel for the signed-in role. When
   * every role could read every field, the tab rendered every panel at once, which is why this
   * counts panels instead of checking that one is present.
   */
  test("renders exactly one progress panel on the overview tab of a bail case", async ({
    bailCaseDetailsPage,
  }) => {
    await bailCaseDetailsPage.openTab(bailCaseDetailsPage.tabs.overview);
    await expect(bailCaseDetailsPage.overviewHeading).toBeVisible();

    await expect(bailCaseDetailsPage.progressPanels).toHaveCount(1);
  });

  /**
   * ccd-data-store-api hydrates the first CaseHistoryViewer field in a case type and returns, so
   * a second one is left with no value and the case history tab renders an empty field. Only the
   * admin officer, the judge and the system user are granted read on caseHistory, so this is not
   * asserted for a legal representative.
   */
  test("lists the case's events on the case history tab for an admin officer", async ({
    bailCaseDetailsPage,
  }) => {
    await bailCaseDetailsPage.openTab(bailCaseDetailsPage.tabs.caseHistory);

    // The event log table rendered at all, then: it has a row below the Event/Date/Author
    // header, and that row names an event. An unhydrated CaseHistoryViewer fails the first of
    // these, and a hydrated but empty one the second.
    await expect(bailCaseDetailsPage.historyTable).toBeVisible();
    await expect(bailCaseDetailsPage.firstEventRow).toBeVisible();
    await expect(bailCaseDetailsPage.firstEventName).not.toBeEmpty();
  });
});
