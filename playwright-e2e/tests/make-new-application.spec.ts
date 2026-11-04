import { expect, test } from "../fixtures";
import { BAIL_EVENTS } from "../page-objects";
import {
  NewApplication,
  SeededBailCase,
  newApplication,
  newApplicationAnswers,
  shownDateFromToday,
} from "../utils";
import type { CaseFixtures } from "../utils";

/**
 * "Make new application" on a case whose application has finished, ported from
 * bails-make-new-application.feature:
 *
 * - "Make new application as Admin Officer after ended application with case note": the case note
 *   from the first application must still be there afterwards;
 * - "Make new application as Home Office after conditional grant decided".
 *
 * The finished applications are driven over CCD as the roles that would finish them: a case note
 * and End the application by the admin officer, and listing, the bail summary, a conditional grant,
 * its signed notice and Move application to decided for the other.
 */
test.describe("Make new application", () => {
  test(
    "makes a new application as the admin officer after the first one ended and keeps the first application's case note",
    async ({ seedCase, progress, openCaseAs }) => {
      const seeded = await seedCase("legalRepBails", { detention: "prison" });
      const note = {
        subject: `Case note subject from application 1 of ${seeded.caseId}`,
        description: `Case note text. This is from application 1 of ${seeded.caseId}`,
      };
      const steps = progress(seeded.caseId);
      await steps.addCaseNote(note);
      await steps.endApplication("Ended so that a new application can be made.");

      const application = newApplication(seeded.caseId);
      await makeNewApplication("adminOfficer", seeded, application, openCaseAs);

      const after = await openCaseAs("adminOfficer", seeded.caseId);
      await expectNewApplicationShown(after, seeded, application, "Applicant");
      await expect(after.activeTabText(application.b1Form.description)).toBeVisible();
      expect(await after.snapshotTab("Case notes")).toMatchObject({
        "caseNotes › Case notes 1 › Subject": note.subject,
        "caseNotes › Case notes 1 › Case note": note.description,
        "caseNotes › Case notes 1 › Date added": shownDateFromToday(0),
      });
    }
  );

  test(
    "makes a new application as the Home Office after a conditional grant was decided",
    async ({ seedCase, progress, openCaseAs }) => {
      const seeded = await seedCase("legalRepBails", { detention: "prison" });
      const steps = progress(seeded.caseId);
      await steps.listCase();
      await steps.uploadBailSummary();
      await steps.recordConditionalGrant();
      await steps.uploadSignedConditionalGrantNotice();
      await steps.moveToDecided();

      const application = newApplication(seeded.caseId);
      await makeNewApplication("homeOffice", seeded, application, openCaseAs);

      const after = await openCaseAs("homeOffice", seeded.caseId);
      await expectNewApplicationShown(after, seeded, application, "Home Office");
    }
  );
});

async function makeNewApplication(
  by: "adminOfficer" | "homeOffice",
  seeded: SeededBailCase,
  application: NewApplication,
  openCaseAs: CaseFixtures["openCaseAs"]
): Promise<void> {
  const casePage = await openCaseAs(by, seeded.caseId);
  const wizard = await casePage.startWizard(BAIL_EVENTS.makeNewApplication);
  await wizard.completeToCheckYourAnswers(newApplicationAnswers(application, by));
  await wizard.submit();
  await wizard.closeAndReturnToCase();
}

/** What the old scenarios checked on the Bail application and Documents tabs. */
async function expectNewApplicationShown(
  casePage: Awaited<ReturnType<CaseFixtures["openCaseAs"]>>,
  seeded: SeededBailCase,
  application: NewApplication,
  submittedBy: string
): Promise<void> {
  expect(await casePage.snapshotTab("Bail application"), "Bail application tab").toMatchObject({
    applicationSubmittedBy: submittedBy,
    applicantGivenNames: seeded.applicant.givenNames,
    applicantHasMobile: "No",
    homeOfficeReferenceNumber: seeded.homeOfficeReference,
    applicantHasAddress: "No",
    agreesToBoundByFinancialCond: "No",
    hasFinancialCondSupporter: "No",
    groundsForBailReasons: application.groundsForBail,
  });
  await casePage.openTab("Documents");
  await expect(casePage.activeTabText(application.evidence.description)).toBeVisible();
}
