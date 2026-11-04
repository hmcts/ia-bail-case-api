import { expect, test } from "../fixtures";
import { BAIL_EVENTS, WizardAnswers } from "../page-objects";

/**
 * "Record the decision" as the admin officer, through the UI.
 *
 * The old suite only ever recorded a decision as a precondition ("I record a conditional grant
 * decision with SS consent Yes with no financial condition supporters", in the make-new-application,
 * end-application, upload-documents and edit-application features), so this is that step as a test
 * of its own: the decision is the main judicial outcome of a bail application, and its about-to-
 * submit callback generates the unsigned decision notice.
 *
 * Its preconditions, listing and the Home Office's bail summary, are driven over CCD.
 */
const CONDITIONAL_GRANT: WizardAnswers = {
  recordDecisionJudgeDetails: [{ fill: "judgeDetailsName", value: "Mr Judge Judgerson" }],
  recordDecisionConsentDetails: [{ choose: "secretaryOfStateConsentYesOrNo_Yes" }],
  tribunalDecision: [{ choose: "recordTheDecisionList-mindedToGrant" }],
  reasonJudgeIsMinded: [
    { fill: "reasonsJudgeIsMindedDetails", value: "This is a reason for minding to grant bail." },
  ],
  ssConsentDecision: [{ choose: "ssConsentDecision_Yes" }],
  releaseStatus: [{ choose: "releaseStatusYesOrNo_No" }],
  conditionDetails: [
    { choose: "conditionsForBail-bailActivities" },
    { fill: "conditionsForBailActivities", value: "The applicant is not allowed to work." },
    { choose: "conditionsForBail-bailElectronicMonitoring" },
    {
      fill: "conditionsForBailElectronicMonitoring",
      value: "The applicant is to be electronically monitored.",
    },
  ],
  recordFinancialCondition: [{ choose: "recordFinancialConditionYesOrNo_No" }],
  recordTheDecisionBailTransfer: [
    { choose: "bailTransferYesOrNo_No" },
    { fill: "bailTransferOtherDetails", value: "Other relevant details" },
  ],
};

test.describe("Record the decision", () => {
  test(
    "records a conditional grant with Secretary of State consent and generates the unsigned decision notice",
    { tag: "@pr" },
    async ({ seedCase, progress, openCaseAs, ccdApiAs }) => {
      const seeded = await seedCase("legalRepBails");
      const steps = progress(seeded.caseId);
      await steps.listCase();
      await steps.uploadBailSummary();
      const author = await (await ccdApiAs("adminOfficer")).signedInUserName();

      const casePage = await openCaseAs("adminOfficer", seeded.caseId);
      const wizard = await casePage.startWizard(BAIL_EVENTS.recordDecision);
      const pages = await wizard.completeToCheckYourAnswers(CONDITIONAL_GRANT);
      expect(pages, "the conditional-grant route through the decision wizard").toEqual(
        Object.keys(CONDITIONAL_GRANT)
      );
      await expect(wizard.answer("Judge name")).toHaveText("Mr Judge Judgerson");
      await wizard.submit();
      await wizard.closeAndReturnToCase();

      const decided = await openCaseAs("adminOfficer", seeded.caseId);
      await decided.expectLatestEvent(BAIL_EVENTS.recordDecision.name, author);
      expect(await decided.snapshotTab("Documents")).toMatchObject({
        "unsgnDecisionDocumentWithMetadata › Decision documents 1 › Document": `${seeded.applicant.familyName}-decision-notice-UNSIGNED.docx`,
      });
    }
  );
});
