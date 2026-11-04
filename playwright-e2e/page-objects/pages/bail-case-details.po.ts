import { ExuiCaseDetailsComponent } from "@hmcts/playwright-common";
import { Locator, expect } from "@playwright/test";
import { config } from "../../utils/config.utils";
import { Base } from "../base";

/**
 * A Bail case in XUI (/cases/case-details/<reference>).
 *
 * Two locators here carry the weight of this suite:
 *
 * - `progressPanels` counts the progress panel the Overview tab is meant to show one of. Each
 *   of the 41 progress_* label fields on the tab (BailTabs.java) renders a markdown image whose
 *   alt text starts "Progress map showing that the application is now at stage"; their show
 *   conditions are not mutually exclusive, so when every role can read every field the tab
 *   renders one panel per field. getByRole only matches elements in the accessibility tree, so
 *   the fields CCD leaves in the DOM with a `hidden` attribute are not counted.
 * - `historyTable` is the table ccd-event-log-table renders for the caseHistory
 *   CaseHistoryViewer field, with Event, Date and Author columns. It is located by that
 *   component rather than by role: the tab is laid out with a `table.caseHistoryTab` wrapping
 *   both the event log and the Details panel, and that wrapper repeats an "Event" header of its
 *   own, so a role-and-column filter cannot tell the two apart. The `th` cells carry no scope
 *   either, so they are not reliably columnheaders in the accessibility tree.
 */
export class BailCaseDetailsPage extends Base {
  readonly caseDetails = new ExuiCaseDetailsComponent(this.page);

  readonly tabs = {
    overview: this.page.getByRole("tab", { name: "Overview" }),
    caseHistory: this.page.getByRole("tab", { name: "Case history" }),
  } as const;

  readonly overviewHeading = this.page.getByRole("heading", {
    name: "Case details",
  });
  readonly progressPanels = this.page.getByRole("img", {
    name: /^Progress map showing/,
  });

  readonly historyTable = this.page.locator("ccd-event-log-table table");
  readonly historyRows = this.historyTable.locator("tr");
  /** Row 0 is the Event/Date/Author header row. */
  readonly firstEventRow = this.historyRows.nth(1);
  readonly firstEventName = this.firstEventRow.locator("td").first();

  public async goto(caseReference: string): Promise<void> {
    await this.page.goto(config.caseDetailsUrl(caseReference));
    await this.spinner.wait();
    await expect(this.caseDetails.caseHeader).toBeVisible();
  }

  public async openTab(tab: Locator): Promise<void> {
    await expect(tab).toBeVisible();
    await tab.click();
    await this.spinner.wait();
  }
}
