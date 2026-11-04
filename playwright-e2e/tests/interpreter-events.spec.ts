import { randomInt } from "node:crypto";
import { expect, test } from "../fixtures";
import {
  UpdateInterpreterBookingStatusPage,
  UpdateInterpreterDetailsPage,
} from "../page-objects";
import { ROLE_VIEWS, config, contentTabsFor, expectSeededDataShown } from "../utils";

/**
 * Two admin-officer events that still go through Bail's own callback dispatcher
 * (/bail/ccdAboutToStart, /bail/ccdAboutToSubmit, /bail/ccdSubmitted), chosen because they work
 * on the data the SDK model has struggled with (the applicant's and supporter's interpreter
 * language reference data) and call nothing outside Bail: no notifications, no reference-data
 * writes. They guard the old dispatcher the same way the add-case-note tests guard the SDK path,
 * and will keep guarding these events when they move.
 */
test.describe("Interpreter events", () => {
  const viewer = config.users.adminOfficer.label;

  test(
    "updates the applicant's and supporter's interpreter booking statuses and changes nothing else",
    async ({ bailCase, openCaseAs, ccdApiAs }) => {
      const author = await (await ccdApiAs("adminOfficer")).signedInUserName();
      const casePage = await openCaseAs("adminOfficer", bailCase.caseId);
      const before = await casePage.snapshotTabs(contentTabsFor("adminOfficer"));
      expectSeededDataShown(before, bailCase, viewer);

      const applicantName = `${bailCase.applicant.givenNames} ${bailCase.applicant.familyName}`;
      const supporterName = `${bailCase.supporter.givenNames} ${bailCase.supporter.familyNames}`;
      const applicantBooking = `${applicantName} - Applicant - ${bailCase.interpreter.applicantSpokenLanguage}`;
      const supporterBooking = `${supporterName} - FCS - ${bailCase.interpreter.supporterSpokenLanguage}`;

      await test.step("set the statuses through Next step", async () => {
        await casePage.startEvent(UpdateInterpreterBookingStatusPage.EVENT.name);
        const event = new UpdateInterpreterBookingStatusPage(casePage.page);
        await event.expectOnFirstPage();
        // The about-to-start callback names each booking from the case's names and language
        // reference data, so these labels are the callback reading that data correctly.
        await expect(
          event.labelFor("applicantInterpreterSpokenLanguageBookingStatus")
        ).toHaveText(applicantBooking);
        await expect(event.labelFor("fcsInterpreterSpokenLanguageBookingStatus1")).toHaveText(
          supporterBooking
        );
        await event.applicantSpokenStatus.selectOption({ label: "Booked" });
        await event.supporterSpokenStatus.selectOption({ label: "Requested" });
        await event.continueToCheckYourAnswers();
        await event.submit();
        await event.closeAndReturnToCase();
      });

      await test.step("the hearing tab shows the statuses, and nothing else changed", async () => {
        const hearing = await casePage.snapshotTab("Hearing and appointment");
        expect(hearing).toMatchObject({
          applicantInterpreterSpokenLanguageBookingStatus: "Booked",
          fcsInterpreterSpokenLanguageBookingStatus1: "Requested",
        });
        await casePage.expectTabsUnchanged(before, {
          viewer,
          event: UpdateInterpreterBookingStatusPage.EVENT.name,
          expected: {
            "Hearing and appointment": [
              "interpreterBookingStatusTitle",
              "applicantInterpreterSpokenLanguageBookingStatus",
              "fcsInterpreterSpokenLanguageBookingStatus1",
            ],
          },
        });
        await casePage.expectOneProgressPanel(ROLE_VIEWS.adminOfficer.submittedProgressField);
        await casePage.expectLatestEvent(UpdateInterpreterBookingStatusPage.EVENT.name, author);
      });
    }
  );

  test(
    "adds an interpreter's details and changes nothing else",
    async ({ bailCase, openCaseAs, ccdApiAs }) => {
      const author = await (await ccdApiAs("adminOfficer")).signedInUserName();
      const casePage = await openCaseAs("adminOfficer", bailCase.caseId);
      const before = await casePage.snapshotTabs(contentTabsFor("adminOfficer"));
      expectSeededDataShown(before, bailCase, viewer);

      const interpreter = {
        bookingRef: `PW-${randomInt(100_000, 999_999)}`,
        givenNames: `Interp${bailCase.applicant.givenNames}`,
        familyName: "Interpreter",
        phone: "01632960003",
        email: "interpreter@example.com",
        note: "Booked for the bail hearing",
      };

      await test.step("add the interpreter through Next step", async () => {
        await casePage.startEvent(UpdateInterpreterDetailsPage.EVENT.name);
        const event = new UpdateInterpreterDetailsPage(casePage.page);
        await event.expectOnFirstPage();
        await event.addInterpreter(interpreter);
        await event.continueToCheckYourAnswers();
        // bails-update-interpreter-details.feature checked these on check your answers.
        await expect(event.page.getByText(interpreter.bookingRef, { exact: true })).toBeVisible();
        await expect(event.page.getByText(interpreter.givenNames, { exact: true })).toBeVisible();
        await event.submit();
        await event.closeAndReturnToCase();
      });

      await test.step("the hearing tab lists the interpreter, and nothing else changed", async () => {
        const hearing = await casePage.snapshotTab("Hearing and appointment");
        const shown = Object.entries(hearing)
          .filter(([key]) => key.startsWith("interpreterDetails › "))
          .map(([, value]) => value);
        expect(shown).toEqual(
          expect.arrayContaining([
            interpreter.bookingRef,
            interpreter.givenNames,
            interpreter.familyName,
            interpreter.phone,
            interpreter.email,
            interpreter.note,
          ])
        );
        await casePage.expectTabsUnchanged(before, {
          viewer,
          event: UpdateInterpreterDetailsPage.EVENT.name,
          expected: {
            "Hearing and appointment": ["interpreterDetailsTitle", "interpreterDetails"],
          },
        });
        await casePage.expectOneProgressPanel(ROLE_VIEWS.adminOfficer.submittedProgressField);
        await casePage.expectLatestEvent(UpdateInterpreterDetailsPage.EVENT.name, author);
      });
    }
  );
});
