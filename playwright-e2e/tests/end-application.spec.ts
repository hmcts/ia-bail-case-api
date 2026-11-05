import { expect, test } from "../fixtures";
import { BAIL_EVENTS } from "../page-objects";
import { ROLE_VIEWS, shownDateFromToday } from "../utils";

/**
 * "End the application" as the admin officer, on a legal representative's submitted case.
 *
 * The submitted callback generates an ended-application notice, named after the applicant's
 * family name, into the tribunal documents; that part depends on ia-case-documents-api.
 */
test.describe("End the application", () => {
  test(
    "ends a submitted application as withdrawn and shows the outcome, the reasons and the ended-application notice",
    { tag: "@pr" },
    async ({ seedCase, openCaseAs }) => {
      const seeded = await seedCase("legalRepBails");
      const reasons = `Withdrawn because it was needed for the test of case ${seeded.caseId}.`;

      const casePage = await openCaseAs("adminOfficer", seeded.caseId);
      const wizard = await casePage.startWizard(BAIL_EVENTS.endApplication);
      await wizard.completeToCheckYourAnswers({
        endApplication: [
          { choose: "endApplicationOutcome-Withdrawn" },
          { fill: "endApplicationReasons", value: reasons },
        ],
      });
      await expect(wizard.answer("Outcome")).toHaveText("Withdrawn");
      await expect(wizard.answer("Reasons")).toHaveText(reasons);

      await wizard.submit();
      await expect(wizard.confirmation("A notification has been sent to all parties. No further action is required.")).toBeVisible();
      await wizard.closeAndReturnToCase(seeded.caseId, ROLE_VIEWS.adminOfficer.workAllocationTabs);

      const ended = await openCaseAs("adminOfficer", seeded.caseId);
      await ended.openTab("Overview");
      await expect(ended.activeTabText("No further action is required on this application.")).toBeVisible();
      expect(await ended.readActiveTab()).toMatchObject({
        endApplicationOutcome: "Withdrawn",
        endApplicationReasons: reasons,
        endApplicationDate: shownDateFromToday(0),
      });

      const documents = await ended.snapshotTab("Documents");
      expect(
        Object.entries(documents).filter(([key]) => key.startsWith("tribunalDocumentsWithMetadata")),
        "tribunal documents after the application was ended"
      ).toContainEqual([
        expect.stringMatching(/^tribunalDocumentsWithMetadata › .* › Document$/),
        `${seeded.applicant.familyName}-ended-application-notice.PDF`,
      ]);
    }
  );
});
