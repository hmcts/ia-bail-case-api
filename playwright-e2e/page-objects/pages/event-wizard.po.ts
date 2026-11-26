import { Locator, Page, Response, expect } from "@playwright/test";
import { throttledUpload } from "../../utils/upload-throttle";
import { CaseEventDetails, CaseEventPage } from "./case-event.po";

export interface UploadFile {
  name: string;
  mimeType: string;
  buffer: Buffer;
}

export interface DateParts {
  day: string;
  month: string;
  year: string;
  /** For a DateTime field. */
  hour?: string;
  minute?: string;
  second?: string;
}

/**
 * One answer on a wizard page, addressed by the ID ccd-case-ui-toolkit gives the input.
 *
 * - `choose`: a radio or checkbox, by its element ID (`<fieldId>-<code>` or `<fieldId>_<code>`).
 * - `fill`: a text, number, textarea or email input, by element ID.
 * - `date`: a CCD Date or DateTime field, by field ID; fills each part it is given. A date
 *   inside a collection item is rendered with the same un-prefixed IDs in every item, so
 *   `withinItem` names the item (`<collectionId>_<n>`) to look in.
 * - `select`: a fixed-list or dynamic-list dropdown, by element ID and option label.
 * - `upload`: a document field, by field ID; waits for CDAM to accept the file.
 * - `addNew`: adds an item to a collection field, by the collection's field ID.
 * - `removeItem`: removes a collection item (`<collectionId>_<n>`) and confirms the removal. The
 *   items after it are renumbered.
 */
export type WizardAnswer =
  | { choose: string }
  | { fill: string; value: string }
  | { date: string; value: DateParts; withinItem?: string }
  | { select: string; label: string }
  | { upload: string; file: UploadFile }
  | { addNew: string }
  | { removeItem: string };

/** Answers keyed by wizard page ID: the part of the URL after the event ID. */
export type WizardAnswers = Record<string, WizardAnswer[]>;

const CHECK_YOUR_ANSWERS = "submit";
const MAX_PAGES = 100;

/**
 * A CCD event wizard whose pages are decided by show conditions: the Bail application events
 * have up to 95 pages, most hidden depending on the role and on earlier answers.
 *
 * The driver does not assume an order. It reads the page ID from the URL
 * (`/trigger/<eventId>/<eventId><pageId>`), applies the answers given for that page, and clicks
 * Continue; a page it has no answers for keeps whatever is already on the case. After each
 * Continue it waits for the URL to name a different page (or check your answers) and for the
 * spinner, so it never acts on a page that is still being replaced. A validation error fails at
 * once with CCD's own message. It fails too if an answered page never appeared, so a changed show
 * condition is reported rather than an answer silently not given.
 *
 * Inputs are located by the IDs ccd-case-ui-toolkit derives from the definition's field IDs.
 * Labels repeat across these pages ("Given names" for the applicant and four supporters, "Yes"
 * and "No" on most questions), and the field IDs are what the definition is written in.
 */
export class EventWizardPage extends CaseEventPage {
  readonly errorSummary = this.page.locator(".govuk-error-summary");
  /**
   * ccd-case-ui-toolkit's "Page refreshed" dialog (case-edit.component.ts checkPageRefresh). It
   * also opens when an event's only wizard pages are all hidden by show conditions: XUI then
   * routes straight to the summary page without passing through next(), never records the
   * event's URL, and treats the summary as a deep link. Submit the application for an admin
   * officer or the Home Office is such an event (its one page is the legal rep's declaration).
   */
  readonly refreshDialog = this.page.getByRole("heading", { name: "Page refreshed" });
  readonly refreshDialogOkBtn = this.page.getByRole("button", { name: "Ok", exact: true });

  constructor(page: Page, event: CaseEventDetails) {
    super(page, event);
  }

  /** The current wizard page ID, or "submit" on check your answers. */
  public currentPageId(): string {
    const path = new URL(this.page.url()).pathname;
    const match = /\/trigger\/([^/]+)\/([^/]+)$/.exec(path);
    if (!match) {
      throw new Error(`Not on a page of the "${this.event.name}" wizard: ${path}`);
    }
    const [, eventId, pageId] = match;
    return pageId.startsWith(eventId) && pageId !== eventId ? pageId.slice(eventId.length) : pageId;
  }

  /**
   * Walks the wizard to the point where it can be submitted, applying `answers`, and returns the
   * pages seen. That is the summary page (check your answers) or, for an event without a summary,
   * its last page, whose button is the event's submit button rather than Continue.
   */
  public async completeToCheckYourAnswers(answers: WizardAnswers): Promise<string[]> {
    const seen: string[] = [];
    await this.waitForWizardPage();
    for (let step = 0; step < MAX_PAGES; step++) {
      const pageId = this.currentPageId();
      if (pageId === CHECK_YOUR_ANSWERS) {
        return this.expectAllAnswered(answers, seen);
      }
      if (seen.includes(pageId)) {
        throw new Error(`"${this.event.name}" wizard showed page ${pageId} twice: ${seen.join(", ")}`);
      }
      seen.push(pageId);
      for (const answer of answers[pageId] ?? []) {
        await this.apply(answer);
      }
      if (!(await this.continueBtn.isVisible())) {
        // waitForWizardPage saw Continue or the submit button, so this is the last page.
        return this.expectAllAnswered(answers, seen);
      }
      await this.continueFrom(pageId);
    }
    throw new Error(`"${this.event.name}" wizard did not reach check your answers in ${MAX_PAGES} pages.`);
  }

  private expectAllAnswered(answers: WizardAnswers, seen: string[]): string[] {
    const unanswered = Object.keys(answers).filter((id) => !seen.includes(id));
    expect(
      unanswered,
      `"${this.event.name}" wizard pages that were given answers but never shown (pages shown: ${seen.join(", ")})`
    ).toEqual([]);
    return seen;
  }

  /**
   * Waits for a wizard page to be ready: Continue on a question page, the event's submit button
   * on the summary page (which has no "Check your answers" heading when there is nothing to check)
   * or on the last page of an event without a summary. The page-refreshed dialog renders with the
   * page, in the same change-detection pass, so once the button is there the dialog either is too
   * or will not appear; when it is, it is acknowledged.
   */
  private async waitForWizardPage(): Promise<void> {
    await expect(this.page).toHaveURL(/\/trigger\/[^/]+\/[^/]+$/, { timeout: 30_000 });
    await this.spinner.wait();
    await expect(this.continueBtn.or(this.submitBtn).first()).toBeVisible();
    if (await this.refreshDialog.isVisible()) {
      await this.refreshDialogOkBtn.click();
      await expect(this.refreshDialog).toBeHidden();
      await this.spinner.wait();
      await expect(this.continueBtn.or(this.submitBtn).first()).toBeVisible();
    }
  }

  /**
   * Continues to the next page. CCD validates each page (POST /data/case-types/<type>/validate,
   * which runs any mid-event callback); a refusal there is not always shown as an error summary,
   * so a failed validate response ends the wait at once with CCD's status and body.
   */
  private async continueFrom(pageId: string): Promise<void> {
    const refusals: string[] = [];
    const onResponse = async (response: Response) => {
      const request = response.request();
      if (
        request.method() === "POST" &&
        /\/validate$/.test(new URL(response.url()).pathname) &&
        response.status() >= 400
      ) {
        const body = await response.text().catch(() => "(no body)");
        refusals.push(`${response.status()} ${body.slice(0, 800)}`);
      }
    };
    this.page.on("response", onResponse);
    try {
      await this.continueAndWait(pageId, refusals);
    } finally {
      this.page.off("response", onResponse);
    }
  }

  private async continueAndWait(pageId: string, refusals: string[]): Promise<void> {
    await expect(this.continueBtn).toBeEnabled();
    await this.continueBtn.click();
    await expect
      .poll(
        async () => {
          if (refusals.length) {
            return `refused: ${refusals[0]}`;
          }
          if (await this.errorSummary.isVisible()) {
            return `error: ${(await this.errorSummary.innerText()).replace(/\s+/g, " ")}`;
          }
          return this.currentPageId();
        },
        {
          message: `"${this.event.name}" wizard did not move on from page ${pageId}`,
          timeout: 30_000,
        }
      )
      .not.toMatch(new RegExp(`^${pageId}$`));
    if (refusals.length) {
      throw new Error(`CCD refused page ${pageId} of the "${this.event.name}" wizard: ${refusals[0]}`);
    }
    if (await this.errorSummary.isVisible()) {
      throw new Error(
        `"${this.event.name}" wizard page ${pageId} refused to continue: ${(await this.errorSummary.innerText()).replace(/\s+/g, " ")}`
      );
    }
    await this.waitForWizardPage();
  }

  private async apply(answer: WizardAnswer): Promise<void> {
    if ("choose" in answer) {
      const input = this.byId(answer.choose);
      await input.check();
      await expect(input).toBeChecked();
    } else if ("fill" in answer) {
      const input = this.byId(answer.fill);
      await input.fill(answer.value);
      await expect(input).toHaveValue(answer.value);
    } else if ("date" in answer) {
      // ccd-case-ui-toolkit's date field writes its form control on each part's change event,
      // which fires on blur. A Continue click that does the blurring is handled before the
      // control is valid and is silently ignored, so each part is blurred as it is filled.
      for (const part of ["day", "month", "year", "hour", "minute", "second"] as const) {
        const value = answer.value[part];
        if (value === undefined) continue;
        const input = (answer.withinItem ? this.collectionItem(answer.withinItem) : this.page)
          .locator(`[id="${answer.date}-${part}"]`);
        await input.fill(value);
        await input.blur();
        await expect(input).toHaveValue(value);
      }
    } else if ("select" in answer) {
      const select = this.byId(answer.select);
      await select.selectOption({ label: answer.label });
      await expect(select.locator("option:checked")).toHaveText(answer.label);
    } else if ("upload" in answer) {
      await this.upload(answer.upload, answer.file);
    } else if ("removeItem" in answer) {
      // The Remove button is in the item's header, beside its fields rather than among them, and
      // is named after the item ("Remove Applicant documents 2"), so the item is the innermost
      // element holding both the item's inputs and a Remove button.
      const removeBtn = this.page.getByRole("button", { name: /^Remove\b/ });
      const item = this.page
        .locator("div")
        .filter({ has: this.page.locator(`[id^="${answer.removeItem}_"]`) })
        .filter({ has: removeBtn })
        .last();
      const collection = answer.removeItem.replace(/_\d+$/, "");
      const before = await this.collectionItemCount(collection);
      await item.getByRole("button", { name: /^Remove\b/ }).first().click();
      const dialog = this.page.getByRole("dialog");
      await dialog.getByRole("button", { name: "Remove", exact: true }).click();
      await expect(dialog).toBeHidden();
      await expect.poll(() => this.collectionItemCount(collection)).toBe(before - 1);
    } else {
      const before = await this.collectionItemCount(answer.addNew);
      await this.page
        .locator(`ccd-write-collection-field:has([id="${answer.addNew}"]), ccd-write-collection-field:has(#${answer.addNew})`)
        .or(this.page.locator(`#${answer.addNew}`))
        .first()
        .getByRole("button", { name: "Add new" })
        .first()
        .click();
      await expect.poll(() => this.collectionItemCount(answer.addNew)).toBe(before + 1);
    }
  }

  /**
   * Uploads through the field's file input. XUI posts the file to CDAM as soon as it is chosen
   * and keeps the field's "Cancel upload" button enabled until CDAM has answered and the field
   * holds the document; Continue treats a document still uploading as missing. So this waits for
   * CDAM's response and then for that button to be disabled. XUI throttles uploads per session,
   * so the upload waits its turn (utils/upload-throttle.ts).
   */
  private async upload(fieldId: string, file: UploadFile): Promise<void> {
    const response = await throttledUpload(this.page.context(), async () => {
      const uploaded = this.page.waitForResponse(
        (candidate) =>
          candidate.request().method() === "POST" &&
          /\/documentsv2$/.test(new URL(candidate.url()).pathname),
        { timeout: 60_000 }
      );
      await this.byId(fieldId).setInputFiles(file);
      return uploaded;
    });
    expect(response.status(), `CDAM refused the upload of ${file.name}`).toBeLessThan(300);
    const field = this.page.locator("ccd-write-document-field").filter({ has: this.byId(fieldId) });
    await expect(field.getByRole("button", { name: "Cancel upload" })).toBeDisabled({ timeout: 30_000 });
  }

  /**
   * One item of a collection of complex values: the innermost complex field holding an input whose
   * ID starts with the item's prefix (`<collectionId>_<n>_`).
   */
  private collectionItem(itemId: string): Locator {
    return this.page
      .locator("ccd-write-complex-type-field")
      .filter({ has: this.page.locator(`[id^="${itemId}_"]`) })
      .last();
  }

  /**
   * How many items a collection field shows. Items are numbered from 0 and each item's inputs
   * have IDs <collection>_<n>_<field>, whatever the item's fields are, so the items are the
   * distinct numbers among those IDs.
   */
  private async collectionItemCount(collection: string): Promise<number> {
    return this.page
      .locator(`[id^="${collection}_"]`)
      .evaluateAll((inputs, prefix) => {
        const pattern = new RegExp(`^${prefix}_(\\d+)_`);
        const numbers = inputs.map((input) => pattern.exec(input.id)?.[1]).filter(Boolean);
        return new Set(numbers).size;
      }, collection);
  }

  private byId(id: string): Locator {
    return this.page.locator(`[id="${id}"]`);
  }
}

/** A day, month and year for a CCD date field. */
export function dateParts(date: Date): DateParts {
  return {
    day: String(date.getUTCDate()).padStart(2, "0"),
    month: String(date.getUTCMonth() + 1).padStart(2, "0"),
    year: String(date.getUTCFullYear()),
  };
}
