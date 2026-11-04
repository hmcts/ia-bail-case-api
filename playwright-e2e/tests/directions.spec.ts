import type { Locator } from "@playwright/test";
import { expect, test } from "../fixtures";
import { BAIL_EVENTS, dateParts } from "../page-objects";
import { isoDateFromToday, shownDateFromToday } from "../utils";

/**
 * Directions, as the admin officer, on a legal representative's submitted case.
 *
 * - "Send direction" is ported from bails-send-direction.feature, "Send a direction to Applicant
 *   as an Admin Officer". Despite its name that scenario directs the legal representative: the
 *   definition offers only "Legal representative" and "Home Office" (DirectionToList).
 * - "Change direction due date" is ported from bails-change-direction-due-date.feature. Its
 *   precondition, a direction due in two days, is sent over CCD.
 */
test.describe("Directions", () => {
  test(
    "sends a direction to the legal representative and lists it on the directions tab with its dates",
    { tag: "@pr" },
    async ({ seedCase, openCaseAs }) => {
      const seeded = await seedCase("legalRepBails");
      const explanation = `This is a test direction to the legal representative on case ${seeded.caseId}`;

      const casePage = await openCaseAs("adminOfficer", seeded.caseId);
      const wizard = await casePage.startWizard(BAIL_EVENTS.sendDirection);
      await wizard.completeToCheckYourAnswers({
        sendDirection: [
          { fill: "sendDirectionDescription", value: explanation },
          { select: "sendDirectionList", label: "Legal representative" },
          { date: "dateOfCompliance", value: dateParts(new Date(isoDateFromToday(8))) },
        ],
      });
      await expect(wizard.answer("Explain the direction you are issuing")).toHaveText(explanation);
      await expect(wizard.answer("Who are you giving direction to?")).toHaveText("Legal representative");
      await expect(wizard.answer("By what date must they comply?")).toHaveText(shownDateFromToday(8));

      await wizard.submit();
      await expect(wizard.confirmation("You can see the status of the direction in the directions tab.")).toBeVisible();
      await wizard.closeAndReturnToCase();

      const after = await openCaseAs("adminOfficer", seeded.caseId);
      expect(await after.snapshotTab("Directions")).toMatchObject({
        "directions › Directions 1 › Explanation": explanation,
        "directions › Directions 1 › Party": "Legal representative",
        "directions › Directions 1 › Date due": shownDateFromToday(8),
        "directions › Directions 1 › Date sent": shownDateFromToday(0),
      });
    }
  );

  test(
    "changes a direction's due date and keeps the old date under previous dates",
    async ({ seedCase, progress, openCaseAs }) => {
      const seeded = await seedCase("legalRepBails");
      await progress(seeded.caseId).sendDirection({
        description: `Direction whose due date changes, on case ${seeded.caseId}`,
        to: "Legal Representative",
        dueInDays: 2,
      });

      const casePage = await openCaseAs("adminOfficer", seeded.caseId);
      const wizard = await casePage.startWizard(BAIL_EVENTS.changeDirectionDueDate);
      await wizard.completeToCheckYourAnswers({
        changeBailDirection: [{ select: "bailDirectionList", label: "Direction 1" }],
        changeBailDirectionDueDate: [
          { date: "bailDirectionEditDateDue", value: dateParts(new Date(isoDateFromToday(4))) },
        ],
      });
      await expect(wizard.answer("Direction")).toHaveText("Direction 1");
      await expect(wizard.answer("Date Sent")).toHaveText(shownDateFromToday(0));
      await expect(wizard.answer("Date Due")).toHaveText(shownDateFromToday(4));

      await wizard.submit();
      await expect(wizard.confirmation("All parties will be notified that the direction due date has changed.")).toBeVisible();
      await wizard.closeAndReturnToCase();

      const after = await openCaseAs("adminOfficer", seeded.caseId);
      expect(await after.snapshotTab("Directions")).toMatchObject({
        "directions › Directions 1 › Date due": shownDateFromToday(4),
        "directions › Directions 1 › Date sent": shownDateFromToday(0),
      });
      // Previous dates is a collection inside each direction, rendered as a nested table the tab
      // snapshot does not flatten, so it is read as the cell beside its row header, and its own
      // rows likewise.
      const beside = (container: Locator, label: string) =>
        container
          .getByRole("rowheader", { name: label, exact: true })
          .locator("xpath=following-sibling::td[1]");
      const previousDates = beside(after.activeTabPanel, "Previous dates");
      await expect(beside(previousDates, "Date due")).toHaveText(shownDateFromToday(2));
      await expect(beside(previousDates, "Date sent")).toHaveText(shownDateFromToday(0));
    }
  );
});
