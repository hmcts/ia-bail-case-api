import { Page, expect } from "@playwright/test";
import { CaseEventDetails, CaseEventPage } from "./case-event.po";

/**
 * Create Flag and Manage Flags. Both events are one CCD page holding ccd-case-ui-toolkit's case
 * flag component, which steps through its own questions on that page (the URL does not change)
 * and then shows CCD's summary page. Each step is waited for by its heading before it is answered.
 * Neither event's callbacks return a confirmation, so XUI goes back to the case and says it was
 * updated.
 */
export class CaseFlagsPage extends CaseEventPage {
  static readonly CREATE: CaseEventDetails = {
    name: "Create Flag",
    pageHeading: "Create Flag",
    submitLabel: "Create Flag",
  };
  static readonly MANAGE: CaseEventDetails = {
    name: "Manage Flags",
    pageHeading: "Manage Flags",
    submitLabel: "Manage Flags",
  };

  readonly locationQuestion = this.page.getByRole("heading", { name: "Where should this flag be added?" });
  readonly flagTypeQuestion = this.page.getByRole("heading", { name: "Select flag type" });
  readonly commentsQuestion = this.page.getByRole("heading", { name: "Add comments for this flag (optional)" });
  readonly reviewHeading = this.page.getByRole("heading", { name: "Review flag details" });
  readonly manageQuestion = this.page.getByRole("heading", { name: "Manage case flags" });
  readonly makeInactiveBtn = this.page.getByRole("button", { name: "Make inactive", exact: true });
  readonly commentsInput = this.page.locator("#flagComments");
  readonly updateCommentInput = this.page.locator("#flagComment");

  constructor(page: Page, event: CaseEventDetails) {
    super(page, event);
  }

  /** Creates a case-level flag of `flagType`, with a comment, and submits it. */
  public async createCaseLevelFlag(flagType: string, comment: string): Promise<void> {
    await expect(this.locationQuestion.first()).toBeVisible();
    await this.page.getByRole("radio", { name: "Case level", exact: true }).check();
    await this.next(this.flagTypeQuestion);
    await this.page.getByRole("radio", { name: flagType, exact: true }).check();
    await this.next(this.commentsQuestion);
    await this.commentsInput.fill(comment);
    await this.next(this.reviewHeading);
    await expect(this.page.getByText(flagType, { exact: true }).first()).toBeVisible();
    await this.submit();
  }

  /** Makes the case-level flag of `flagType` inactive and submits it. */
  public async makeCaseLevelFlagInactive(flagType: string, comment: string): Promise<void> {
    await expect(this.manageQuestion.first()).toBeVisible();
    // The option is named "Case level - <type> (<comment>)".
    await this.page
      .getByRole("radio", { name: new RegExp(`^Case level - ${flagType}( \\(|$)`) })
      .check();
    const updateQuestion = this.page.getByRole("heading", { name: `Update flag "${flagType}"` });
    await this.next(updateQuestion);
    await this.makeInactiveBtn.click();
    await this.updateCommentInput.fill(comment);
    await this.next(this.reviewHeading);
    await this.submit();
  }

  /** The case flags tab's table of case-level flags, by its caption. */
  public static caseLevelFlags(panel: ReturnType<Page["locator"]>) {
    return panel.getByRole("table", { name: "Case level flags" });
  }

  private async next(question: ReturnType<Page["getByRole"]>): Promise<void> {
    await this.continueBtn.click();
    await this.spinner.wait();
    await expect(question.first()).toBeVisible();
  }
}
