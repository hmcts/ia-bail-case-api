import { expect, test } from "../fixtures";
import { config } from "../utils";

test.use({ storageState: config.users.adminOfficer.sessionFile });

test.describe("Create case", () => {
  /**
   * The repo's functional tests drive CCD over the API with pipeline-issued tokens, so nothing
   * else checks that a signed-in admin officer is actually offered the Bail case type in XUI. A
   * user with no row in ccd-user-profile-api gets "404 No jurisdictions found" whatever its roles,
   * which leaves the jurisdiction dropdown empty, and a definition whose events are not authorised
   * for the role leaves the event dropdown empty. Either shows up here as a selectOption timeout
   * naming the missing option.
   */
  test(
    "offers the Bail case type and starts the application event for an admin officer",
    { tag: ["@smoke", "@pr"] },
    async ({ page, createCasePage, startApplicationPage }) => {
      await createCasePage.selectJurisdiction(config.bail.jurisdictionLabel);
      await expect(createCasePage.jurisdictionSelect).toHaveValue(
        config.bail.jurisdictionId
      );

      await createCasePage.selectCaseType(config.bail.caseTypeLabel);
      await expect(createCasePage.caseTypeSelect).toHaveValue(
        config.bail.caseTypeId
      );

      await createCasePage.selectEvent(config.bail.startApplicationEventLabel);
      await expect(createCasePage.eventSelect).toHaveValue(
        config.bail.startApplicationEventId
      );

      await createCasePage.start();

      await expect(page).toHaveURL(startApplicationPage.urlPattern);
      await expect(startApplicationPage.eventName).toBeVisible();
      await expect(startApplicationPage.continueBtn).toBeVisible();
    }
  );
});
