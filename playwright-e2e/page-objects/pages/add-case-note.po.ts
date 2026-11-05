import { Page } from "@playwright/test";
import { CaseEventPage } from "./case-event.po";

/** The "Add case note" event (AddCaseNote.java): one page, then check your answers. */
export class AddCaseNotePage extends CaseEventPage {
  static readonly EVENT = {
    name: "Add case note",
    pageHeading: "Case note details",
    submitLabel: "Add case note",
    confirmationHeader: "You have added a case note",
  };

  readonly subjectInput = this.page.getByLabel("Subject", { exact: true });
  readonly descriptionInput = this.page.getByLabel("Case note", { exact: true });

  constructor(page: Page) {
    super(page, AddCaseNotePage.EVENT);
  }

  public async fill(note: { subject: string; description: string }): Promise<void> {
    await this.subjectInput.fill(note.subject);
    await this.descriptionInput.fill(note.description);
  }
}
