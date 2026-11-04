import { expect, test as setup } from "./fixtures";
import { adminOfficerCredentials } from "./utils";

/**
 * Signs the IA admin officer in once per run and stores the session for the browser projects.
 *
 * IDAM's sign-in is two pages: an email page at /enter-email, then a password page at
 * /enter-password. IdamPage.login (playwright-common >= 1.1.6) handles that split.
 *
 * IDAM's cookie banner is deliberately left alone. Its own Accept/Reject buttons are
 * `type=button`, so the only visible submit button on each page is Continue and
 * IdamPage.login's submit locator stays unambiguous. Answer it, and IDAM replaces it with a
 * confirmation banner whose "Hide this message" button *is* a submit button, at which point
 * that locator matches two elements and login fails on a strict mode violation.
 *
 * The session is saved here rather than by passing `sessionFile` to login(), because login()
 * saves as soon as it clicks the final Continue, which can be before IDAM has redirected back
 * to XUI and XUI has set the session cookie the browser projects need.
 */
setup(
  "signs the admin officer in and stores a session the browser projects can reuse",
  async ({ page, config, idamPage, cookieUtils, SessionUtils }) => {
    const user = config.users.adminOfficer;
    // Reuse a session that still has more than two hours left rather than signing in again.
    // eslint-disable-next-line playwright/no-conditional-in-test
    if (SessionUtils.isSessionValid(user.sessionFile, user.cookieName)) {
      return;
    }

    await page.goto(config.urls.xuiBaseUrl);
    await expect(idamPage.usernameInput.first()).toBeVisible();
    await idamPage.login(adminOfficerCredentials());

    // Back on XUI holding a session cookie is the only reliable sign the sign-in finished.
    // Capped below the test timeout so that credentials IDAM rejects fail here, with the
    // "There is a problem" sign-in page in the trace, rather than at the end of the test.
    await page.waitForURL(`${config.urls.xuiBaseUrl}/**`, { timeout: 60_000 });
    await expect
      .poll(async () =>
        (await page.context().cookies(config.urls.xuiBaseUrl)).map(
          (cookie) => cookie.name
        )
      )
      .toContain(user.cookieName);

    await page.context().storageState({ path: user.sessionFile });
    cookieUtils.addAnalyticsCookie(user.sessionFile);
  }
);
