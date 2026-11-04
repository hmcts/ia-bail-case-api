import { expect, test } from "../fixtures";
import { BAIL_EVENTS, WizardAnswers } from "../page-objects";
import { BailCaseCreator, ROLE_VIEWS, config } from "../utils";

/**
 * "Submit the application", driven through Next step on an application the role saved earlier.
 *
 * Ported from bails-submit-bail-application-admin-officer.feature (@bail-nightly-test) and from
 * the "I submit my bail application as a <role>" step every other old Bail scenario starts with.
 * The old scenario walked startApplication's wizard in the UI first; the case here is saved over
 * CCD by the same role instead (create-case.spec.ts checks the wizard opens), so the test is about
 * the submission.
 *
 * A legal representative is asked to make the declaration on submission; the admin officer and
 * the Home Office are not, so their wizard is only the submit page.
 */
const CONFIRM_DETENTION_PROMPT =
  "Please Confirm location where Applicant is detained is correct, and they are held on immigration matters, before listing the case for hearing in List Assist.";

const SUBMITTERS: {
  role: BailCaseCreator;
  answers: WizardAnswers;
  progressField: string;
  /** What the role's Overview tells them to do next. */
  overviewText: string[];
}[] = [
  {
    role: "adminOfficer",
    answers: {},
    progressField: ROLE_VIEWS.adminOfficer.submittedProgressField,
    overviewText: [CONFIRM_DETENTION_PROMPT],
  },
  {
    role: "legalRepBails",
    answers: { declarationOnSubmit: [{ choose: "declarationOnSubmit-declarationOnSubmit" }] },
    progressField: ROLE_VIEWS.legalRepBails.submittedProgressField,
    overviewText: [],
  },
  {
    role: "homeOffice",
    answers: {},
    progressField: ROLE_VIEWS.homeOffice.submittedProgressField,
    overviewText: [],
  },
];

test.describe("Submit the application", () => {
  for (const { role, answers, progressField, overviewText } of SUBMITTERS) {
    test(
      `submits a saved application through Next step as the ${config.users[role].label} and moves the case to submitted`,
      { tag: "@pr" },
      async ({ seedCase, openCaseAs }) => {
        const saved = await seedCase(role, { submit: false });

        const casePage = await openCaseAs(role, saved.caseId);
        const wizard = await casePage.startWizard(BAIL_EVENTS.submitApplication);
        await wizard.completeToCheckYourAnswers(answers);
        await wizard.submit();
        await expect(wizard.confirmation("All parties will be notified that the application has been submitted.")).toBeVisible();
        await wizard.closeAndReturnToCase();

        const submitted = await openCaseAs(role, saved.caseId);
        await submitted.expectOneProgressPanel(progressField);
        for (const text of overviewText) {
          await expect(submitted.activeTabText(text)).toBeVisible();
        }
      }
    );
  }
});
