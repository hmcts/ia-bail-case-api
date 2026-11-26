import { expect } from "@playwright/test";
import { config } from "../../utils/config.utils";
import { Base } from "../base";

/**
 * XUI's Create case page (/cases/case-filter).
 *
 * The three dropdowns each repopulate the next one from a fresh request, so nothing here waits
 * on a timer: selectOption waits for the option it is asked for to appear, and the assertions
 * that follow check the option that got selected was the expected case type and event rather
 * than whatever happened to be first.
 */
export class CreateCasePage extends Base {
  readonly heading = this.page.getByRole("heading", {
    name: "Create Case",
    level: 1,
  });
  readonly jurisdictionSelect = this.page.getByLabel("Jurisdiction", {
    exact: true,
  });
  readonly caseTypeSelect = this.page.getByLabel("Case type", { exact: true });
  readonly eventSelect = this.page.getByLabel("Event", { exact: true });
  readonly startBtn = this.page.getByRole("button", {
    name: "Start",
    exact: true,
  });

  public async goto(): Promise<void> {
    await this.page.goto(config.urls.createCaseUrl);
    await expect(this.heading).toBeVisible();
    await expect(this.jurisdictionSelect).toBeVisible();
  }

  public async selectJurisdiction(label: string): Promise<void> {
    await this.jurisdictionSelect.selectOption({ label });
  }

  public async selectCaseType(label: string): Promise<void> {
    await this.caseTypeSelect.selectOption({ label });
  }

  public async selectEvent(label: string): Promise<void> {
    await this.eventSelect.selectOption({ label });
  }

  public async start(): Promise<void> {
    await this.startBtn.click();
    await this.spinner.wait();
  }
}
