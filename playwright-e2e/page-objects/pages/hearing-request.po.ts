import { Locator, Response, expect } from "@playwright/test";
import { Base } from "../base";
import type { BailCaseDetailsPage } from "./bail-case-details.po";

/**
 * XUI's "Request a hearing" journey (/hearings/request/...), which sends the request to HMC.
 * Its pages are XUI's own, not CCD's, and each has its own route, so every step waits for the
 * route and the page's heading before answering it.
 */
export class HearingRequestPage extends Base {
  readonly requestHearingBtn = this.page.getByRole("button", { name: "Request a hearing" });
  readonly continueBtn = this.page.getByRole("button", { name: "Continue", exact: true });
  readonly submitRequestBtn = this.page.getByRole("button", { name: "Submit request", exact: true });
  readonly venueSearch = this.page.getByRole("combobox", { name: "Search for a location by name" });
  readonly addLocationLink = this.page.getByRole("link", { name: "Add location", exact: true });
  readonly partyChannels = this.page.locator("select[id^=partyChannel]");
  readonly submittedHeading = this.page.getByRole("heading", { name: "Hearing request submitted" });

  /**
   * Opens the case's Hearings tab and starts a request from it.
   *
   * Opening the tab loads the case's service hearing values (POST
   * /api/hearings/loadServiceHearingValues). The request journey's guard reads them
   * (rpx-xui-webapp HearingsGuard.hasMatchedPermissions) and, if they have not arrived yet, sends
   * the user back to the case instead. So the response is waited for, from before the tab opens,
   * and Request a hearing is clicked only once it has arrived.
   */
  public async startFrom(casePage: BailCaseDetailsPage): Promise<void> {
    const serviceHearingValues = this.page.waitForResponse(isServiceHearingValues, { timeout: 30_000 });
    await casePage.openXuiTab("Hearings");
    const response = await serviceHearingValues;
    expect(response.status(), "XUI could not load the case's service hearing values").toBeLessThan(300);
    await expect(this.page.getByText("Retrieving hearing data...")).toHaveCount(0);
    await this.requestHearingBtn.click();
    await this.onStep("hearing-requirements", "Hearing requirements");
    await expect(
      this.page.getByText("Any reasonable adjustments on this page will be included in the hearing request.")
    ).toBeVisible();
  }

  /**
   * A request with no additional facilities, a bail hearing, every participant in person, at the
   * given venue, and the defaults for judge, length, date, priority and instructions. Returns how
   * many participants the attendance page listed.
   */
  public async requestBailHearingInPerson(venue: string): Promise<number> {
    await this.next("hearing-facilities", "Do you require any additional facilities?");
    await this.next("hearing-stage", "What stage is this hearing at?");
    await this.page.getByRole("radio", { name: "Bail", exact: true }).check();

    await this.next("hearing-attendance", "Participant attendance");
    await this.page.locator("#paperHearingNo").check();
    await this.page.getByRole("checkbox", { name: "In Person", exact: true }).check();
    const participants = await this.partyChannels.count();
    for (let index = 0; index < participants; index++) {
      await this.partyChannels.nth(index).selectOption({ label: "In Person" });
    }
    await this.page.locator("#attendance-number").fill(String(participants));

    await this.next("hearing-venue", "What are the hearing venue details?");
    await this.venueSearch.pressSequentially(venue.split(" ")[0]);
    await this.page.getByRole("option", { name: venue }).click();
    await this.addLocationLink.click();
    // Added venues are listed as removable chips: the name and a remove cross.
    await expect(this.page.getByText(venue).first()).toBeVisible();

    await this.next("hearing-judge", "Do you want a specific judge?");
    await this.next("hearing-timing", "Length, date and priority level of hearing");
    await this.next("hearing-additional-instructions", "Enter any additional instructions for the hearing");
    await this.next("hearing-create-edit-summary", "Check your answers before sending your request");
    return participants;
  }

  public async submit(): Promise<void> {
    await this.submitRequestBtn.click();
    await this.spinner.wait();
    await expect(this.submittedHeading).toBeVisible({ timeout: 30_000 });
  }

  private async next(route: string, heading: string): Promise<void> {
    await this.continueBtn.click();
    await this.onStep(route, heading);
  }

  private async onStep(route: string, heading: string): Promise<void> {
    await expect(this.page).toHaveURL(new RegExp(`/hearings/request/${route}$`));
    await this.spinner.wait();
    await expect(this.headingNamed(heading)).toBeVisible();
  }

  private headingNamed(name: string): Locator {
    return this.page.getByRole("heading", { name }).first();
  }
}

function isServiceHearingValues(response: Response): boolean {
  return (
    response.request().method() === "POST" &&
    new URL(response.url()).pathname === "/api/hearings/loadServiceHearingValues"
  );
}
