import { expect, test } from "../fixtures";
import { BAIL_EVENTS, CaseFlagsPage, HearingRequestPage } from "../page-objects";
import { BailCaseCreator, config } from "../utils";

/**
 * A hearing request through XUI's Hearings tab, as the admin officer, on applications made by each
 * kind of applicant. Ported from bails-snl-admin-request-a-hearing.feature,
 * bails-snl-ho-request-a-hearing.feature and bails-snl-lr-request-a-hearing.feature.
 *
 * Each confirms the detention location first (the admin officer is asked to before listing) and
 * flags the case as urgent, then requests a bail hearing with every participant in person at
 * Hatton Cross.
 *
 * The old scenarios also added language-interpreter and reasonable-adjustment flags for the
 * applicant and a supporter, through the flags component's language search; case-flags.spec.ts
 * covers the flags journey, and those party flags change only the hearing requirements page, so
 * they are not repeated here.
 *
 * Needs HMC, which a PR's preview namespace does not have (config.hearingsAvailable).
 */
const APPLICATIONS: { createdBy: BailCaseCreator; participants: number }[] = [
  { createdBy: "adminOfficer", participants: 2 },
  { createdBy: "homeOffice", participants: 2 },
  { createdBy: "legalRepBails", participants: 3 },
];

test.describe("Request a hearing", () => {
  // eslint-disable-next-line playwright/no-skipped-test -- skipped only where HMC is not deployed
  test.skip(!config.hearingsAvailable, "HMC is not deployed to this environment");

  for (const { createdBy, participants } of APPLICATIONS) {
    test(
      `requests a bail hearing for an application made by the ${config.users[createdBy].label}`,
      async ({ seedCase, openCaseAs }) => {
        const seeded = await seedCase(createdBy);
        const casePage = await openCaseAs("adminOfficer", seeded.caseId);
        await expect(
          casePage.activeTabText(
            "Please Confirm location where Applicant is detained is correct, and they are held on immigration matters, before listing the case for hearing in List Assist."
          )
        ).toBeVisible();

        const confirm = await casePage.startWizard(BAIL_EVENTS.confirmDetentionLocation);
        await confirm.completeToCheckYourAnswers({
          confirmDetentionLocation: [{ choose: "isDetentionLocationCorrect_Yes" }],
        });
        await expect(
          confirm.answer("Have you verified if the Applicant's place of detention is correct?")
        ).toHaveText("Yes");
        await confirm.submit();
        await expect(
          casePage.page.getByText("You must now add any case flags for interpreter or hearing requirement.")
        ).toBeVisible();

        const flagged = await openCaseAs("adminOfficer", seeded.caseId);
        await flagged.startEvent(CaseFlagsPage.CREATE.name);
        await new CaseFlagsPage(flagged.page, CaseFlagsPage.CREATE).createCaseLevelFlag(
          "Urgent case",
          `Urgent bail hearing for case ${seeded.caseId}`
        );

        const ready = await openCaseAs("adminOfficer", seeded.caseId);
        await ready.openXuiTab("Hearings");
        const hearing = new HearingRequestPage(ready.page);
        await hearing.start();
        expect(await hearing.requestBailHearingInPerson("Hatton Cross Tribunal Hearing Centre")).toBe(
          participants
        );
        await hearing.submit();
      }
    );
  }
});
