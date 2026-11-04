import { ExuiCaseDetailsComponent } from "@hmcts/playwright-common";
import { Locator, expect } from "@playwright/test";
import { config } from "../../utils/config.utils";
import {
  CaseViewSnapshot,
  FIELD_ID_PREFIX,
  TabSnapshot,
  diffSnapshots,
  readTabSnapshot,
} from "../../utils/tab-snapshot.utils";
import { Base } from "../base";

/** The case-type tabs the Bail definition declares (BailTabs.java), in display order. */
export const BAIL_TABS = [
  "Overview",
  "Bail application",
  "Documents",
  "Directions",
  "Notifications",
  "Hearing and appointment",
  "Case notes",
  "Case history",
  "Previous applications",
  "Linked Cases",
  "Case flags",
] as const;
export type BailTab = (typeof BAIL_TABS)[number];

/**
 * A Bail case in XUI (/cases/case-details/<reference>).
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
 * - Case tabs are mat-tab panels; only the selected one is `mat-mdc-tab-body-active`, and its
 *   fields are read by the `case-viewer-field-read--<fieldId>` ids CCD gives them.
 *
 * XUI routes in the background: opening a case, selecting each tab, and XUI inserting its
 * work-allocation tabs are Angular router navigations that complete after whatever started them.
 * One still in flight when Go is clicked on Next step cancels the event's navigation and leaves
 * XUI's loading spinner up for good, so goto() and openTab() wait for the state each one ends
 * in.
 */
export class BailCaseDetailsPage extends Base {
  readonly caseDetails = new ExuiCaseDetailsComponent(this.page);

  readonly allTabs = this.page.getByRole("tab");
  readonly activeTabPanel = this.page.locator("mat-tab-body.mat-mdc-tab-body-active");
  readonly activeTabTable = this.activeTabPanel.getByRole("table", {
    name: "case viewer table",
  });

  readonly overviewHeading = this.page.getByRole("heading", {
    name: "Case details",
  });
  readonly progressPanels = this.page.getByRole("img", {
    name: /^Progress map showing/,
  });
  /** The progress_* fields shown on the active tab; their IDs name the stage and audience. */
  readonly progressFields = this.activeTabPanel.locator(
    `[id^="${FIELD_ID_PREFIX}progress_"]:visible`
  );

  readonly historyTable = this.page.locator("ccd-event-log-table table");
  readonly historyRows = this.historyTable.locator("tr");
  /** Row 0 is the Event/Date/Author header row. */
  readonly firstEventRow = this.historyRows.nth(1);
  readonly firstEventName = this.firstEventRow.locator("td").first();
  readonly firstEventAuthor = this.firstEventRow.locator("td").nth(2);

  readonly nextStepSelect = this.page.getByLabel("Next step");
  readonly goBtn = this.page.getByRole("button", { name: "Go", exact: true });

  /**
   * XUI appends ", Please use left and right arrow keys to navigate case tabs" to the selected
   * tab's accessible name, so the name is matched up to that suffix.
   */
  public tab(name: BailTab): Locator {
    return this.page.getByRole("tab", { name: new RegExp(`^${escapeRegExp(name)}(,|$)`) });
  }

  /**
   * Opens the case and waits for XUI to settle: its own follow-up navigation, a few seconds
   * after the case renders, to /cases/case-details/<jurisdiction>/<case type>/<id>#<tab>; and,
   * for roles with work allocation, the Tasks and Roles and access tabs it inserts in front of
   * the case tabs once its work-allocation lookups return. Inserting them shifts the selected
   * tab, and the re-selection is a navigation of its own: an event started before it lands is
   * cancelled, and a tab opened before it can be swapped for its neighbour.
   */
  public async goto(caseReference: string, workAllocationTabs: string[] = []): Promise<void> {
    await this.page.goto(config.caseDetailsUrl(caseReference));
    const { jurisdictionId, caseTypeId } = config.bail;
    await expect(this.page).toHaveURL(
      new RegExp(`/cases/case-details/${jurisdictionId}/${caseTypeId}/${caseReference}#`),
      { timeout: 30_000 }
    );
    for (const name of workAllocationTabs) {
      await expect(this.page.getByRole("tab", { name, exact: true })).toBeVisible({
        timeout: 30_000,
      });
    }
    // The re-selection after the insertion lands back on the tab the URL names.
    await expect(this.tab("Overview")).toHaveAttribute("aria-selected", "true");
    await this.spinner.wait();
    await expect(this.caseDetails.caseHeader).toBeVisible();
    await expect(this.allTabs.first()).toBeVisible();
  }

  /** Selects a tab and waits for its navigation to #<tab name> to complete. */
  public async openTab(name: BailTab): Promise<void> {
    const tab = this.tab(name);
    await expect(tab).toBeVisible();
    await tab.click();
    await expect(tab).toHaveAttribute("aria-selected", "true");
    await expect(this.page).toHaveURL(
      new RegExp(`#${escapeRegExp(encodeURIComponent(name))}$`)
    );
    await this.spinner.wait();
    // Attached rather than visible: a tab whose every field is hidden from the role (Directions,
    // for most roles) renders an empty, zero-height table.
    await expect(this.activeTabTable).toBeAttached();
  }

  /** The case-type tabs this user is shown, leaving out XUI's own (Tasks, Hearings, ...). */
  public async visibleBailTabs(): Promise<string[]> {
    const names = (await this.allTabs.allInnerTexts()).map((name) => name.trim());
    return names.filter((name) => (BAIL_TABS as readonly string[]).includes(name));
  }

  public async snapshotTab(name: BailTab): Promise<TabSnapshot> {
    await this.openTab(name);
    return this.readActiveTab();
  }

  public async snapshotTabs(names: readonly BailTab[]): Promise<CaseViewSnapshot> {
    const snapshot: CaseViewSnapshot = {};
    for (const name of names) {
      snapshot[name] = await this.snapshotTab(name);
    }
    return snapshot;
  }

  /** Reads the active tab without re-opening it, so it can be polled while it re-renders. */
  public async readActiveTab(): Promise<TabSnapshot> {
    return this.activeTabTable.evaluate(readTabSnapshot, FIELD_ID_PREFIX);
  }

  /**
   * Starts an event from the Next step dropdown. selectOption waits for the option to be
   * offered, so an event the role is not granted, or one not allowed in the case's state, fails
   * here naming it.
   */
  public async startEvent(eventName: string): Promise<void> {
    await expect(this.nextStepSelect).toBeVisible();
    await this.nextStepSelect.selectOption({ label: eventName });
    await expect(this.goBtn).toBeEnabled();
    await this.goBtn.click();
    await expect(this.page).toHaveURL(/\/trigger\/[^/]+\//, { timeout: 30_000 });
    await this.spinner.wait();
  }

  /**
   * Re-opens each tab in `before` and checks it still shows exactly what it showed then, apart
   * from the fields the event is expected to change. Soft, so one run reports every field lost
   * on every tab for every role, each as "<field>: was ..., now ...".
   */
  public async expectTabsUnchanged(
    before: CaseViewSnapshot,
    options: { viewer: string; event: string; expected?: Partial<Record<BailTab, string[]>> }
  ): Promise<void> {
    for (const [name, beforeTab] of Object.entries(before) as [BailTab, TabSnapshot][]) {
      await this.openTab(name);
      await expect
        .configure({ soft: true })
        .poll(
          async () =>
            diffSnapshots(beforeTab, await this.readActiveTab(), {
              expected: options.expected?.[name],
            }),
          {
            message: `"${name}" tab, as the ${options.viewer}, after "${options.event}": fields that changed although the event should not have touched them`,
            timeout: 5_000,
          }
        )
        .toEqual([]);
    }
  }

  /** The Overview tab shows one progress panel, and it is the one for this role and stage. */
  public async expectOneProgressPanel(progressFieldId: string): Promise<void> {
    await this.openTab("Overview");
    await expect(this.overviewHeading).toBeVisible();
    await expect(this.progressPanels).toHaveCount(1);
    await expect(this.progressFields).toHaveCount(1);
    await expect(this.progressFields).toHaveAttribute(
      "id",
      `${FIELD_ID_PREFIX}${progressFieldId}`
    );
  }

  /** The case history tab lists `eventName` as the latest event, by `author`. */
  public async expectLatestEvent(eventName: string, author: string): Promise<void> {
    await this.openTab("Case history");
    await expect(this.historyTable).toBeVisible();
    await expect(this.firstEventName).toHaveText(eventName);
    // The event log shows the surname in capitals.
    await expect(this.firstEventAuthor).toHaveText(
      new RegExp(`^\\s*${escapeRegExp(author)}\\s*$`, "i")
    );
  }
}

function escapeRegExp(text: string): string {
  return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}
