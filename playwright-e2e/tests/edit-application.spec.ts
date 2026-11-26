import { expect, test } from "../fixtures";
import { BAIL_EVENTS, BailCaseDetailsPage, WizardAnswers } from "../page-objects";
import {
  EditedApplication,
  ROLE_VIEWS,
  SeededBailCase,
  UserRole,
  editApplicationAnswers,
  editedApplication,
} from "../utils";

/**
 * "Edit the application", before submission as the legal representative and after it as the
 * admin officer.
 *
 * Both start from a legal representative's application for an applicant in prison with a
 * financial condition supporter and interpreters, and move the applicant to Yarl's Wood with no
 * supporter, financial condition or interpreter, and new legal rep details. The two events share a
 * name and their pages, so one set of answers drives both; after submission only the admin officer
 * may edit, and is asked who sent the application in.
 */
test.describe("Edit the application", () => {
  test(
    "lets the legal representative edit a saved application before submitting it and shows the edits",
    async ({ seedCase, openCaseAs }) => {
      const saved = await seedCase("legalRepBails", { submit: false, detention: "prison" });
      const edit = editedApplication();

      await editApplication("legalRepBails", saved, edit, openCaseAs, {});
      const after = await openCaseAs("legalRepBails", saved.caseId);
      await expectEditsShown(after, edit, "Legal Representative");
    }
  );

  test(
    "lets the admin officer edit a submitted application and shows the edits",
    async ({ seedCase, openCaseAs }) => {
      const submitted = await seedCase("legalRepBails", { detention: "prison" });
      const edit = editedApplication();

      await editApplication("adminOfficer", submitted, edit, openCaseAs, {
        adminSentByPage: [{ choose: "sentByChecklist-Applicant" }],
        hasLegalRepresentative: [{ choose: "hasLegalRep_Yes" }],
      });
      const after = await openCaseAs("adminOfficer", submitted.caseId);
      await expectEditsShown(after, edit, "Applicant");
    }
  );
});

async function editApplication(
  editor: UserRole,
  seeded: SeededBailCase,
  edit: EditedApplication,
  openCaseAs: (role: UserRole, caseId: string) => Promise<BailCaseDetailsPage>,
  roleAnswers: WizardAnswers
): Promise<void> {
  const casePage = await openCaseAs(editor, seeded.caseId);
  const wizard = await casePage.startWizard(BAIL_EVENTS.editApplication);
  await wizard.completeToCheckYourAnswers({ ...editApplicationAnswers(edit), ...roleAnswers });
  await wizard.submit();
  await wizard.closeAndReturnToCase(seeded.caseId, ROLE_VIEWS[editor].workAllocationTabs);
}

/** Every edited value, on the fields that show it. */
async function expectEditsShown(
  casePage: BailCaseDetailsPage,
  edit: EditedApplication,
  submittedBy: string
): Promise<void> {
  const legalRep = {
    legalRepCompany: edit.legalRep.company,
    legalRepName: edit.legalRep.name,
    legalRepEmail: edit.legalRep.email,
    legalRepReference: edit.legalRep.reference,
  };

  expect.soft(await casePage.snapshotTab("Overview"), "Overview tab").toMatchObject({
    applicantFullName: `${edit.givenNames} ${edit.familyName}`,
    ircName: edit.ircName,
    hearingCentreRefData: "Yarls Wood Immigration And Asylum Hearing Centre",
    homeOfficeReferenceNumber: edit.homeOfficeReference,
    ...legalRep,
  });
  expect.soft(await casePage.snapshotTab("Bail application"), "Bail application tab").toMatchObject({
    applicationSubmittedBy: submittedBy,
    applicantGivenNames: edit.givenNames,
    applicantFamilyName: edit.familyName,
    applicantDateOfBirth: edit.dateOfBirthShown,
    applicantGender: "Male",
    applicantDetainedLoc: "Immigration removal centre",
    ircName: edit.ircName,
    applicantHasMobile: "No",
    hasAppealHearingPending: "No",
    applicantHasAddress: "No",
    hasPreviousBailApplication: "No",
    agreesToBoundByFinancialCond: "No",
    hasFinancialCondSupporter: "No",
    groundsForBailReasons: edit.groundsForBail,
    transferBailManagementObjectionYesOrNo: "No",
    groundsForBailProvideEvidenceOption: "No",
    interpreterYesNo: "No",
    applicantDisability1: "No",
    videoHearing1: "Yes",
    ...legalRep,
  });
  expect.soft(await casePage.snapshotTab("Hearing and appointment"), "Hearing and appointment tab").toMatchObject({
    interpreterYesNo: "No",
    applicantDisability1: "No",
    videoHearing1: "Yes",
  });
}
