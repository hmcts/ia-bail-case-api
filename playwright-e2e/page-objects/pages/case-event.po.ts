import { Locator, Page, Response, expect } from "@playwright/test";
import { Base } from "../base";

export interface CaseEventDetails {
  /** The event's name, as offered in Next step and shown above check your answers. */
  name: string;
  /** The first wizard page's label, which ccd-case-ui-toolkit shows as that page's heading. */
  pageHeading: string;
  /** The label on the check-your-answers submit button (the event's endButtonLabel). */
  submitLabel: string;
  /** The submitted callback's confirmation header, without its leading "# ". */
  confirmationHeader: string;
}

/**
 * A CCD event wizard in XUI: the event's pages, its check-your-answers page and the confirmation
 * page its submitted callback produces.
 */
export class CaseEventPage extends Base {
  readonly heading: Locator;
  readonly submitBtn: Locator;
  readonly confirmationHeading: Locator;
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
      name: event.confirmationHeader,
    });
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
    await expect(this.confirmationHeading).toBeVisible();
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
