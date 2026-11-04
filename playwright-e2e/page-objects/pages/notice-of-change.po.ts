import { expect } from "@playwright/test";
import { config } from "../../utils/config.utils";
import { Base } from "../base";

export interface NocAnswers {
  givenNames: string;
  familyName: string;
  /** yyyy-mm-dd */
  dateOfBirth: string;
}

/**
 * XUI's Notice of Change journey (/noc), backed by aac-manage-case-assignment: the case
 * reference, the challenge questions the definition declares (BailNoticeOfChange.java: the
 * client's first name, surname and date of birth), check and submit, and the outcome.
 *
 * The date-of-birth inputs are found by their Day, Month and Year labels: XUI renders their IDs
 * from an unevaluated function ("id(){return this.idPrefix+...}-day").
 */
export class NoticeOfChangePage extends Base {
  readonly heading = this.page.getByRole("heading", { name: "Notice of change", exact: true });
  readonly caseReferenceInput = this.page.getByLabel("Online case reference number");
  readonly continueBtn = this.page.getByRole("button", { name: "Continue", exact: true });
  readonly clientDetailsHeading = this.page.getByRole("heading", { name: "Enter your client's details" });
  readonly firstNameInput = this.page.getByLabel("Client first name");
  readonly surnameInput = this.page.getByLabel("Client surname");
  readonly dayInput = this.page.getByLabel("Day", { exact: true });
  readonly monthInput = this.page.getByLabel("Month", { exact: true });
  readonly yearInput = this.page.getByLabel("Year", { exact: true });
  readonly detailsMismatchError = this.page
    .getByText("Enter the client details exactly as they're written on the case, including any mistakes")
    .first();
  readonly backLink = this.page.getByRole("link", { name: "Back", exact: true });
  readonly checkAndSubmitHeading = this.page.getByRole("heading", { name: "Check and submit" });
  readonly affirmationCheckbox = this.page.locator("#affirmation");
  readonly notifyEveryPartyCheckbox = this.page.locator("#notifyEveryParty");
  readonly submitBtn = this.page.getByRole("button", { name: "Submit", exact: true });
  readonly successHeading = this.page.getByRole("heading", { name: /Notice of change successful/ });
  readonly viewCaseLink = this.page.getByRole("link", { name: "View this case" });

  public async goto(): Promise<void> {
    await this.page.goto(`${config.urls.xuiBaseUrl}/noc`);
    await expect(this.heading).toBeVisible();
  }

  public async enterCaseReference(caseId: string): Promise<void> {
    await expect(this.caseReferenceInput).toBeVisible();
    await this.caseReferenceInput.fill(caseId);
    await this.continueBtn.click();
    await expect(this.clientDetailsHeading).toBeVisible();
  }

  public async answerQuestions(answers: NocAnswers): Promise<void> {
    const [year, month, day] = answers.dateOfBirth.split("-");
    await this.firstNameInput.fill(answers.givenNames);
    await this.surnameInput.fill(answers.familyName);
    await this.dayInput.fill(day);
    await this.monthInput.fill(month);
    await this.yearInput.fill(year);
    await this.continueBtn.click();
  }

  /** Goes back from the client details page to the case reference page. */
  public async backToCaseReference(): Promise<void> {
    await this.backLink.click();
    await expect(this.caseReferenceInput).toBeVisible();
  }

  public async confirmAndSubmit(): Promise<void> {
    await expect(this.checkAndSubmitHeading).toBeVisible();
    await this.affirmationCheckbox.check();
    await this.notifyEveryPartyCheckbox.check();
    await this.submitBtn.click();
    // aac answers once it has raised the NoC request and applied the decision: two CCD events,
    // each with callbacks to the Bail service.
    await expect(this.successHeading).toBeVisible({ timeout: 30_000 });
  }
}
