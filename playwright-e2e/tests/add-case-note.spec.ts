import { randomUUID } from "node:crypto";
import { expect, test } from "../fixtures";
import { AddCaseNotePage } from "../page-objects";
import {
  CaseViewSnapshot,
  ROLE_VIEWS,
  SeededBailCase,
  UserRole,
  config,
  contentTabsFor,
  expectSeededDataShown,
} from "../utils";
import type { CaseFixtures } from "../utils";

/**
 * Add case note, driven through XUI's Next step dropdown on a case of the test's own.
 *
 * hmcts/ia-bail-case-api#622 serves this event's callbacks through ccd-config-generator, which
 * reads the whole case into the CaseData model and writes the whole case back. That is where
 * every fault so far has been: data the model cannot read fails the event on every real case,
 * and data it reads back empty is silently written over. So each test snapshots every content
 * tab, as every role that can see the case, before the event; adds the note as the admin
 * officer; and then checks that the note is there and that nothing else on any of those tabs
 * changed for any of those roles.
 *
 * The first test also covers bails-add-a-case-note.feature's "Add case note without document as
 * Admin Officer": the confirmation, and the note on the case notes tab for the judge as well as
 * the admin officer.
 */
test.describe("Add case note", () => {
  // Each test reads every content tab as two or three roles, twice, through XUI: about two
  // minutes of real work against a shared environment, close to the three-minute default.
  test.slow();

  test(
    "adds a note through Next step and leaves every field on an admin officer's case as each role saw it",
    { tag: "@pr" },
    async ({ bailCase, openCaseAs, ccdApiAs }) => {
      await addCaseNoteAndCheck({
        seeded: bailCase,
        viewers: ["adminOfficer", "judge", "homeOffice"],
        openCaseAs,
        ccdApiAs,
      });
    }
  );

  test(
    "adds a note as the admin officer to a legal representative's case and keeps the legal rep's details",
    { tag: "@pr" },
    async ({ legalRepBailCase, openCaseAs, ccdApiAs }) => {
      await addCaseNoteAndCheck({
        seeded: legalRepBailCase,
        viewers: ["adminOfficer", "legalRep"],
        openCaseAs,
        ccdApiAs,
      });
    }
  );
});

async function addCaseNoteAndCheck({
  seeded,
  viewers,
  openCaseAs,
  ccdApiAs,
}: {
  seeded: SeededBailCase;
  viewers: UserRole[];
  openCaseAs: CaseFixtures["openCaseAs"];
  ccdApiAs: CaseFixtures["ccdApiAs"];
}): Promise<void> {
  const note = {
    subject: `Playwright note ${randomUUID().slice(0, 8)}`,
    description: `Added through XUI to case ${seeded.caseId} to check nothing else on the case changes.`,
  };
  const author = await (await ccdApiAs("adminOfficer")).signedInUserName();

  const before: Partial<Record<UserRole, CaseViewSnapshot>> = {};

  await test.step("snapshot the case as every role that can see it", async () => {
    for (const role of viewers) {
      const casePage = await openCaseAs(role, seeded.caseId);
      before[role] = await casePage.snapshotTabs(contentTabsFor(role));
      expectSeededDataShown(before[role], seeded, config.users[role].label);
    }
  });

  await test.step("add the note through Next step as the admin officer", async () => {
    const casePage = await openCaseAs("adminOfficer", seeded.caseId);
    await casePage.startEvent(AddCaseNotePage.EVENT.name);
    const addCaseNote = new AddCaseNotePage(casePage.page);
    await addCaseNote.expectOnFirstPage();
    await addCaseNote.fill(note);
    await addCaseNote.continueToCheckYourAnswers();
    await expect(addCaseNote.page.getByText(note.subject, { exact: true })).toBeVisible();
    await addCaseNote.submit();
    await expect(addCaseNote.confirmation("You can review this note in the case notes tab.")).toBeVisible();
    await addCaseNote.closeAndReturnToCase();
  });

  await test.step("the note is on the case notes tab with its subject, text and author", async () => {
    const casePage = await openCaseAs("adminOfficer", seeded.caseId);
    const notes = await casePage.snapshotTab("Case notes");
    expect(notes).toMatchObject({
      "caseNotes › Case notes 1 › Subject": note.subject,
      "caseNotes › Case notes 1 › Case note": note.description,
      "caseNotes › Case notes 1 › User": author,
      "caseNotes › Case notes 1 › Date added": todayInLondon(),
    });
    await casePage.expectLatestEvent(AddCaseNotePage.EVENT.name, author);
  });

  for (const role of viewers.filter((viewer) => ROLE_VIEWS[viewer].tabs.includes("Case notes"))) {
    await test.step(`the note is on the case notes tab for the ${config.users[role].label}`, async () => {
      const casePage = await openCaseAs(role, seeded.caseId);
      expect(await casePage.snapshotTab("Case notes")).toMatchObject({
        "caseNotes › Case notes 1 › Subject": note.subject,
        "caseNotes › Case notes 1 › Case note": note.description,
        "caseNotes › Case notes 1 › Date added": todayInLondon(),
      });
    });
  }

  for (const role of viewers) {
    await test.step(`nothing else changed for the ${config.users[role].label}`, async () => {
      const casePage = await openCaseAs(role, seeded.caseId);
      await casePage.expectTabsUnchanged(before[role] ?? {}, {
        viewer: config.users[role].label,
        event: AddCaseNotePage.EVENT.name,
        expected: { "Case notes": ["caseNotes"] },
      });
      await casePage.expectOneProgressPanel(ROLE_VIEWS[role].submittedProgressField);
    });
  }
}

/**
 * Today as CCD's date field shows it ("4 Nov 2026"), on the UK calendar Bail's handlers stamp.
 * Built from parts because ICU abbreviates September as "Sept" in en-GB and CCD does not.
 */
function todayInLondon(): string {
  const parts = new Intl.DateTimeFormat("en-GB", {
    day: "numeric",
    month: "numeric",
    year: "numeric",
    timeZone: "Europe/London",
  }).formatToParts(new Date());
  const part = (type: string) => Number(parts.find((p) => p.type === type)?.value);
  const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
  return `${part("day")} ${months[part("month") - 1]} ${part("year")}`;
}
