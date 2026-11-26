import { ExuiSpinnerComponent } from "@hmcts/playwright-common";
import { Page } from "@playwright/test";

/** Base page: holds the components every page in this suite shares. */
export abstract class Base {
  readonly spinner: ExuiSpinnerComponent;

  constructor(public readonly page: Page) {
    this.spinner = new ExuiSpinnerComponent(this.page);
  }
}
