import { expect, test } from "../fixtures";
import { BailCaseDetailsPage, NoticeOfChangePage } from "../page-objects";
import { CaseData, ROLE_VIEWS, SeededBailCase } from "../utils";

/**
 * Notice of Change through XUI: answers that do not match the case are refused, and a legal
 * representative of another MyHMCTS organisation takes over a case the first organisation's legal
 * representative created.
 */

test.describe("Notice of change", () => {
  test(
    "rejects a notice of change whose first name, surname or date of birth does not match the case",
    async ({ seedCase, roleContext }) => {
      const seeded = await seedCase("legalRepBails");
      const answers = nocAnswers(seeded);
      const noc = new NoticeOfChangePage(await (await roleContext("otherOrgLegalRep")).newPage());

      const wrongAnswers = [
        { ...answers, givenNames: `${answers.givenNames}x` },
        { ...answers, familyName: `${answers.familyName}x` },
        { ...answers, dateOfBirth: "1970-01-01" },
      ];
      // Each attempt starts a fresh journey: XUI's Back link is in-page state that can re-render
      // the client details page after briefly showing the case reference.
      for (const wrong of wrongAnswers) {
        await noc.goto();
        await noc.enterCaseReference(seeded.caseId);
        await noc.answerQuestions(wrong);
        await expect(noc.detailsMismatchError).toBeVisible();
      }
    }
  );

  test(
    "moves the case's legal representative organisation to the new representative, who then finds and sees the case",
    async ({ seedCase, roleContext, ccdApiAs }) => {
      const seeded = await seedCase("legalRepBails");
      const answers = nocAnswers(seeded);
      const adminOfficer = await ccdApiAs("adminOfficer");
      const newRep = await ccdApiAs("otherOrgLegalRep");
      const firstOrganisation = await (await ccdApiAs("legalRepBails")).organisationId();
      const newOrganisation = await newRep.organisationId();
      expect(newOrganisation, "the two legal reps are in different organisations").not.toBe(firstOrganisation);
      expect(representingOrganisation((await adminOfficer.getCase(seeded.caseId)).data)).toBe(firstOrganisation);

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

      // aac applies the decision by rewriting the organisation policy for [LEGALREPRESENTATIVE].
      await expect
        .poll(async () => representingOrganisation((await adminOfficer.getCase(seeded.caseId)).data), {
          message: "the organisation representing the applicant after the notice of change",
        })
        .toBe(newOrganisation);
      // The creator's own search is not asserted: whether it still finds the case depends on the
      // environment's data-store and Elasticsearch access setup, not on Bail.
      await expect
        .poll(() => newRep.countSearchResults(seeded.caseId), {
          message: "the new organisation finds the case after the notice of change",
        })
        .toBe(1);
    }
  );
});

/** The organisation in the case's [LEGALREPRESENTATIVE] organisation policy. */
function representingOrganisation(data: CaseData): string | undefined {
  return (data.localAuthorityPolicy as { Organisation?: { OrganisationID?: string } } | undefined)
    ?.Organisation?.OrganisationID;
}

function nocAnswers(seeded: SeededBailCase) {
  return {
    givenNames: seeded.applicant.givenNames,
    familyName: seeded.applicant.familyName,
    dateOfBirth: seeded.applicant.dateOfBirth,
  };
}

