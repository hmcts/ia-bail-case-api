import { config } from "../../utils/config.utils";
import { Base } from "../base";

/**
 * The first wizard page of the Bail "Start the application" event.
 *
 * ccd-case-ui-toolkit renders the event name as a caption above the wizard page's own heading
 * (case-edit-page.html), so the event name is matched as text rather than as a heading.
 */
export class StartApplicationPage extends Base {
  readonly eventName = this.page.getByText(
    config.bail.startApplicationEventLabel,
    { exact: true }
  );
  readonly continueBtn = this.page.getByRole("button", {
    name: "Continue",
    exact: true,
  });

  /** URL XUI navigates to when the Create case form is submitted. */
  readonly urlPattern = new RegExp(
    `/cases/case-create/${config.bail.jurisdictionId}/${config.bail.caseTypeId}/${config.bail.startApplicationEventId}`
  );
}
