import { ExuiCaseDetailsComponent, ExuiSpinnerComponent } from "@hmcts/playwright-common";
import { Locator, Page, expect } from "@playwright/test";
import { config } from "../../utils/config.utils";
import { FIELD_ID_PREFIX } from "../../utils/tab-snapshot.utils";

/**
 * XUI appends ", Please use left and right arrow keys to navigate case tabs" to the selected
 * tab's accessible name, so a tab is matched by its name up to that suffix.
 */
export function caseTab(page: Page, name: string): Locator {
  return page.getByRole("tab", { name: new RegExp(`^${escapeRegExp(name)}(,|$)`) });
}

/**
 * Waits for a case page XUI has just loaded or returned to (from goto, a link, or an event's
 * "Close and Return to case details") to settle, so the next action is not undone by XUI:
 *
 * - its own follow-up navigation to /cases/case-details/<jurisdiction>/<case type>/<id>#<tab>;
 * - for roles with work allocation, the Tasks and Roles and access tabs it inserts in front of
 *   the case tabs once its lookups return. Inserting them shifts the selected tab and re-selects
 *   it with a navigation of its own: an event started before that lands is cancelled, and a tab
 *   opened before it can be swapped for its neighbour;
 * - the Overview tab's content. Its table is attached before CCD has applied the fields' show
 *   conditions; every role is shown exactly one progress field on Overview, so that field
 *   appearing means the tab has rendered.
 */
export async function waitForCaseViewToSettle(
  page: Page,
  caseReference: string,
  workAllocationTabs: string[]
): Promise<void> {
  const { jurisdictionId, caseTypeId } = config.bail;
  await expect(page).toHaveURL(
    new RegExp(`/cases/case-details/${jurisdictionId}/${caseTypeId}/${caseReference}#`),
    { timeout: 30_000 }
  );
  for (const name of workAllocationTabs) {
    await expect(page.getByRole("tab", { name, exact: true })).toBeVisible({ timeout: 30_000 });
  }
  // The re-selection after the insertion lands back on the tab the URL names.
  await expect(caseTab(page, "Overview")).toHaveAttribute("aria-selected", "true");
  await new ExuiSpinnerComponent(page).wait();
  await expect(new ExuiCaseDetailsComponent(page).caseHeader).toBeVisible();
  await expect(page.getByRole("tab").first()).toBeVisible();
  await expect(
    page
      .locator("mat-tab-body.mat-mdc-tab-body-active")
      .locator(`[id^="${FIELD_ID_PREFIX}progress_"]:visible`)
      .first()
  ).toBeVisible();
}

export function escapeRegExp(text: string): string {
  return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}
