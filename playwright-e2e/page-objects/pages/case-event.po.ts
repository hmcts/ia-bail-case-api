import { Locator, Page, Response, expect } from "@playwright/test";
import { Base } from "../base";

export interface CaseEventDetails {
  /** The event's name, as offered in Next step and shown above check your answers. */
  name: string;
  /** The first wizard page's label, which ccd-case-ui-toolkit shows as that page's heading. */
  pageHeading: string;
  /** The label on the check-your-answers submit button (the event's endButtonLabel). */
  submitLabel: string;
  /**
   * The submitted callback's confirmation header, without its leading "# ". Absent for an event
   * with no confirmation page: XUI goes straight back to the case and says it was updated.
   */
  confirmationHeader?: string;
}

/**
 * A CCD event wizard in XUI: the event's pages, its check-your-answers page and the confirmation
 * page its submitted callback produces.
 */
export class CaseEventPage extends Base {
  readonly heading: Locator;
  readonly submitBtn: Locator;
  readonly confirmationHeading: Locator;
  /** XUI's banner on the case after an event with no confirmation page. */
  readonly updatedAlert: Locator;
  readonly continueBtn = this.page.getByRole("button", { name: "Continue", exact: true });
  readonly checkYourAnswersHeading = this.page.getByRole("heading", {
    name: "Check your answers",
  });
  readonly closeAndReturnBtn = this.page.getByRole("button", {
    name: "Close and Return to case details",
  });

  constructor(
    page: Page,
    public readonly event: CaseEventDetails
  ) {
    super(page);
    this.heading = this.page.getByRole("heading", { name: event.pageHeading, level: 1 });
    this.submitBtn = this.page.getByRole("button", { name: event.submitLabel, exact: true });
    this.confirmationHeading = this.page.getByRole("heading", {
      name: event.confirmationHeader ?? event.name,
    });
    this.updatedAlert = this.page
      .getByText(`has been updated with event: ${event.name}`)
      .first();
  }

  public async expectOnFirstPage(): Promise<void> {
    await expect(this.heading).toBeVisible();
    await expect(this.continueBtn).toBeVisible();
  }

  /** Moves from the event's last page to check your answers. */
  public async continueToCheckYourAnswers(): Promise<void> {
    await this.continueBtn.click();
    await this.spinner.wait();
    await expect(this.checkYourAnswersHeading).toBeVisible();
  }

  /**
   * The value check your answers shows for the question labelled `label`. CYA is a table of
   * label and value rows, so the row is found by its header cell's exact text.
   */
  public answer(label: string): Locator {
    return this.page
      .getByRole("table", { name: "check your answers table" })
      .getByRole("row")
      .filter({ has: this.page.getByRole("rowheader", { name: label, exact: true }) })
      .getByRole("cell")
      .first();
  }

  /** Text in the confirmation page's body, which the submitted callback writes as markdown. */
  public confirmation(text: string): Locator {
    return this.page.getByText(text, { exact: false });
  }

  /**
   * Submits the event and waits for CCD's answer. A refused submission (a callback error, or a
   * callback service that cannot read the case) fails here with CCD's status and response body
   * rather than as a missing confirmation heading, because the body is what says why.
   */
  public async submit(): Promise<void> {
    const submission = this.page.waitForResponse(isEventSubmission, { timeout: 60_000 });
    await this.submitBtn.click();
    const response = await submission;
    expect(
      response.status(),
      `CCD refused the "${this.event.name}" submission: ${await summarise(response)}`
    ).toBe(201);
    await this.spinner.wait();
    if (this.event.confirmationHeader) {
      await expect(this.confirmationHeading).toBeVisible();
    } else {
      await expect(this.page).toHaveURL(/\/cases\/case-details\//);
      await expect(this.updatedAlert).toBeVisible();
    }
  }

  public async closeAndReturnToCase(): Promise<void> {
    await this.closeAndReturnBtn.click();
    await this.spinner.wait();
    await expect(this.page).toHaveURL(/\/cases\/case-details\//);
  }
}

function isEventSubmission(response: Response): boolean {
  return (
    response.request().method() === "POST" &&
    /\/data\/cases\/\d{16}\/events$/.test(new URL(response.url()).pathname)
  );
}

async function summarise(response: Response): Promise<string> {
  const body = await response.text().catch(() => "(no body)");
  return `${response.status()} ${body.slice(0, 1500)}`;
}
