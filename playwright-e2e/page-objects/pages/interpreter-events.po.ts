import { Page } from "@playwright/test";
import { CaseEventPage } from "./case-event.po";

export type BookingStatus = "Not requested" | "Requested" | "Booked" | "Cancelled";

/**
 * "Update interpreter status" (UpdateInterpreterBookingStatus.java). Its about-to-start callback
 * builds one status dropdown per interpreter language on the case, labelled
 * "<name> - <Applicant|FCS> - <language>", from the applicant's and supporters' language
 * reference data. The labels are substituted at runtime, so the dropdowns are located by their
 * field IDs.
 */
export class UpdateInterpreterBookingStatusPage extends CaseEventPage {
  static readonly EVENT = {
    name: "Update interpreter status",
    pageHeading: "Update interpreter booking status",
    submitLabel: "Update",
    confirmationHeader: "Booking statuses have been updated",
  };

  readonly applicantSpokenStatus = this.page.locator(
    "#applicantInterpreterSpokenLanguageBookingStatus"
  );
  readonly supporterSpokenStatus = this.page.locator(
    "#fcsInterpreterSpokenLanguageBookingStatus1"
  );

  constructor(page: Page) {
    super(page, UpdateInterpreterBookingStatusPage.EVENT);
  }

  public labelFor(fieldId: string) {
    return this.page.locator(`label[for="${fieldId}"]`);
  }
}

/**
 * "Update interpreter details" (UpdateInterpreterDetails.java): a collection of interpreters,
 * each added with "Add new". Items are numbered from 0 in their field IDs.
 */
export class UpdateInterpreterDetailsPage extends CaseEventPage {
  static readonly EVENT = {
    name: "Update interpreter details",
    pageHeading: "Update interpreter details",
    submitLabel: "Update",
    confirmationHeader: "Interpreter details have been updated",
  };

  readonly addNewBtn = this.page.getByRole("button", { name: "Add new" }).first();

  constructor(page: Page) {
    super(page, UpdateInterpreterDetailsPage.EVENT);
  }

  public async addInterpreter(interpreter: {
    bookingRef: string;
    givenNames: string;
    familyName: string;
    phone: string;
    email: string;
    note: string;
  }): Promise<void> {
    await this.addNewBtn.click();
    const item = (field: string) => this.page.locator(`#interpreterDetails_0_${field}`);
    await item("interpreterBookingRef").fill(interpreter.bookingRef);
    await item("interpreterGivenNames").fill(interpreter.givenNames);
    await item("interpreterFamilyName").fill(interpreter.familyName);
    await item("interpreterPhoneNumber").fill(interpreter.phone);
    await item("interpreterEmail").fill(interpreter.email);
    await item("interpreterNote").fill(interpreter.note);
  }
}
