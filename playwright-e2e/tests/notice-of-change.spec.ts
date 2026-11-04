import { expect, test } from "../fixtures";
import { BailCaseDetailsPage, NoticeOfChangePage } from "../page-objects";
import { ROLE_VIEWS, SeededBailCase } from "../utils";

/**
 * Notice of Change, ported from bails-notice-of-change.feature: "Notice of Change - incorrect
 * details" and "Notice of Change" (the old suite's "Legal Org User Rep C" taking over a case from
 * "Legal Org User Rep A Bails"). The two legal representatives are in different MyHMCTS
 * organisations.
 *
 * The case is seeded without the ChangeOrganisationRequest the seeder otherwise writes on a legal
 * representative's case to exercise the SDK model: aac-manage-case-assignment reads a request with
 * a case role as a notice of change already in progress and refuses a new one.
 */
const NO_PENDING_REQUEST = { overrides: { changeOrganisationRequestField: undefined } };

test.describe("Notice of change", () => {
  test(
    "rejects a notice of change whose first name, surname or date of birth does not match the case",
    async ({ seedCase, roleContext }) => {
      const seeded = await seedCase("legalRepBails", NO_PENDING_REQUEST);
      const answers = nocAnswers(seeded);
      const noc = new NoticeOfChangePage(await (await roleContext("otherOrgLegalRep")).newPage());
      await noc.goto();

      const wrongAnswers = [
        { ...answers, givenNames: `${answers.givenNames}x` },
        { ...answers, familyName: `${answers.familyName}x` },
        { ...answers, dateOfBirth: "1970-01-01" },
      ];
      for (const wrong of wrongAnswers) {
        await noc.enterCaseReference(seeded.caseId);
        await noc.answerQuestions(wrong);
        await expect(noc.detailsMismatchError).toBeVisible();
        await noc.backToCaseReference();
      }
    }
  );

  test(
    "moves the case to another organisation's legal representative and takes it away from the first",
    async ({ seedCase, roleContext, ccdApiAs }) => {
      const seeded = await seedCase("legalRepBails", NO_PENDING_REQUEST);
      const answers = nocAnswers(seeded);
      const firstRep = await ccdApiAs("legalRepBails");
      await expect
        .poll(() => firstRep.countSearchResults(seeded.caseId), {
          message: "the creating organisation finds its case before the notice of change",
        })
        .toBe(1);

      const noc = new NoticeOfChangePage(await (await roleContext("otherOrgLegalRep")).newPage());
      await noc.goto();
      await noc.enterCaseReference(seeded.caseId);
      await noc.answerQuestions(answers);
      await expect(noc.page.getByText(answers.givenNames, { exact: true })).toBeVisible();
      await expect(noc.page.getByText(answers.familyName, { exact: true })).toBeVisible();
      await expect(noc.page.getByText(answers.dateOfBirth, { exact: true })).toBeVisible();
      await noc.confirmAndSubmit();

      // The link reloads XUI on the case page, so the case is waited for as goto() would.
      await noc.viewCaseLink.click();
      const casePage = new BailCaseDetailsPage(noc.page);
      await casePage.expectCaseLoaded(seeded.caseId, ROLE_VIEWS.otherOrgLegalRep.workAllocationTabs);
      await casePage.expectOneProgressPanel(ROLE_VIEWS.otherOrgLegalRep.submittedProgressField);
      await expect(casePage.progressFields).toContainText("Current progress of the case");

      // The old scenario's check: the case is gone from the first organisation's case list.
      await expect
        .poll(() => firstRep.countSearchResults(seeded.caseId), {
          message: "the creating organisation finds the case after the notice of change",
        })
        .toBe(0);
      const newRep = await ccdApiAs("otherOrgLegalRep");
      await expect
        .poll(() => newRep.countSearchResults(seeded.caseId), {
          message: "the new organisation finds the case after the notice of change",
        })
        .toBe(1);
    }
  );
});

function nocAnswers(seeded: SeededBailCase) {
  return {
    givenNames: seeded.applicant.givenNames,
    familyName: seeded.applicant.familyName,
    dateOfBirth: seeded.applicant.dateOfBirth,
  };
}

