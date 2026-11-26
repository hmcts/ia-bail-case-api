import type { CaseEventDetails } from "./case-event.po";

/**
 * The Bail events the suite drives through XUI, as the definition names them
 * (src/main/java/.../ccd/event) and as their submitted callbacks confirm them
 * (domain/handlers/postsubmit). Events whose callbacks return no confirmation header leave
 * `confirmationHeader` out: XUI returns to the case and says it was updated.
 */
export const BAIL_EVENTS = {
  submitApplication: {
    name: "Submit the application",
    pageHeading: "Submit the application",
    submitLabel: "Submit",
    confirmationHeader: "You have submitted this application",
  },
  editApplication: {
    name: "Edit the application",
    pageHeading: "Previous applications",
    submitLabel: "Save application",
    confirmationHeader: "Your application details have been updated",
  },
  endApplication: {
    name: "End the application",
    pageHeading: "Outcome and reasons",
    submitLabel: "End application",
    confirmationHeader: "You have ended the application",
  },
  sendDirection: {
    name: "Send direction",
    pageHeading: "Direction details",
    submitLabel: "Send the direction",
    confirmationHeader: "You have sent a direction",
  },
  changeDirectionDueDate: {
    name: "Change direction due date",
    pageHeading: "Select direction",
    submitLabel: "Change bail direction due date",
    confirmationHeader: "You have changed the direction due date",
  },
  uploadDocuments: {
    name: "Upload documents",
    pageHeading: "Upload",
    submitLabel: "Upload",
    confirmationHeader: "Your upload is complete",
  },
  editDocuments: {
    name: "Edit documents",
    pageHeading: "Edit or remove documents",
    submitLabel: "Submit",
  },
  recordDecision: {
    name: "Record the decision",
    pageHeading: "Judge details",
    submitLabel: "Record decision",
    confirmationHeader: "You have recorded the decision",
  },
  makeNewApplication: {
    name: "Make new application",
    pageHeading: "Before you start",
    submitLabel: "Submit",
    confirmationHeader: "You have submitted this application",
  },
  confirmDetentionLocation: {
    name: "Confirm detention location",
    pageHeading: "Confirm detention location",
    submitLabel: "Submit",
  },
} as const satisfies Record<string, CaseEventDetails>;
