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
import type { CaseEventDetails } from "./case-event.po";
import { caseTab, escapeRegExp, waitForCaseViewToSettle } from "./case-view-settled";
import { EventWizardPage } from "./event-wizard.po";

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

  /** A tab by name: a case-type tab, or one of XUI's own (Hearings, Tasks, ...). */
  public tab(name: BailTab | string): Locator {
    return caseTab(this.page, name);
  }

  /** Opens the case and waits for XUI to settle (see waitForCaseViewToSettle). */
  public async goto(caseReference: string, workAllocationTabs: string[] = []): Promise<void> {
    await this.page.goto(config.caseDetailsUrl(caseReference));
    await this.expectCaseLoaded(caseReference, workAllocationTabs);
  }

  /**
   * Waits for the case to have opened and XUI to have settled, after any navigation that loads
   * the case page (a link from another XUI page as well as goto()).
   */
  public async expectCaseLoaded(caseReference: string, workAllocationTabs: string[] = []): Promise<void> {
    await waitForCaseViewToSettle(this.page, caseReference, workAllocationTabs);
  }

  /** Opens a case-type tab and waits for its table. */
  public async openTab(name: BailTab): Promise<void> {
    await this.selectTab(name);
    // Attached rather than visible: a tab whose every field is hidden from the role (Directions,
    // for most roles) renders an empty, zero-height table.
    await expect(this.activeTabTable).toBeAttached();
  }

  /** Opens one of XUI's own tabs (Hearings, Tasks, ...), which the definition does not declare. */
  public async openXuiTab(name: string): Promise<void> {
    await this.selectTab(name);
  }

  /** Selects a tab and waits for its navigation to #<tab name> to complete. */
  private async selectTab(name: string): Promise<void> {
    const tab = this.tab(name);
    await expect(tab).toBeVisible();
    await tab.click();
    await expect(tab).toHaveAttribute("aria-selected", "true");
    await expect(this.page).toHaveURL(
      new RegExp(`#${escapeRegExp(encodeURIComponent(name))}$`)
    );
    await this.spinner.wait();
  }

  /** The case-type tabs this user is shown, leaving out XUI's own (Tasks, Hearings, ...). */
  public async visibleBailTabs(): Promise<string[]> {
    const names = (await this.allTabs.allInnerTexts()).map((name) => name.trim());
    return names.filter((name) => (BAIL_TABS as readonly string[]).includes(name));
  }

  /**
   * Opens a tab and reads it once it has finished rendering: CCD attaches the tab's table and then
   * applies its fields' show conditions, so the read is repeated until two in a row agree.
   */
  public async snapshotTab(name: BailTab): Promise<TabSnapshot> {
    await this.openTab(name);
    let previous: string | undefined;
    let snapshot: TabSnapshot = {};
    await expect
      .poll(
        async () => {
          snapshot = await this.readActiveTab();
          const current = JSON.stringify(snapshot);
          const settled = current === previous;
          previous = current;
          return settled;
        },
        { message: `"${name}" tab finished rendering`, intervals: [250] }
      )
      .toBe(true);
    return snapshot;
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

  /** Starts `event` from Next step and returns its wizard. */
  public async startWizard(event: CaseEventDetails): Promise<EventWizardPage> {
    await this.startEvent(event.name);
    return new EventWizardPage(this.page, event);
  }

  /** The active tab's text, for content CCD renders as a label rather than a field value. */
  public activeTabText(text: string | RegExp): Locator {
    return this.activeTabPanel.getByText(text);
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

