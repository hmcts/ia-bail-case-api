import { expect, test as setup } from "./fixtures";
import { USER_ROLES, credentialsFor } from "./utils";

/**
 * Signs each role in once per run and stores a session for the browser projects.
 *
 * The sign-ins run one after another in a single worker, because XUI's node-lib OAuth handling
 * can crash under concurrent sign-ins. They run on every run: XUI's cookies are session cookies
 * (expires -1), so SessionUtils.isSessionValid would never accept a stored session.
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
setup.describe.configure({ mode: "default" });

for (const role of USER_ROLES) {
  setup(
    `signs the ${role} in and stores a session the browser projects can reuse`,
    async ({ page, config, idamPage, cookieUtils }) => {
      const user = config.users[role];
      await page.goto(config.urls.xuiBaseUrl);
      await expect(idamPage.usernameInput.first()).toBeVisible();
      await idamPage.login(credentialsFor(role));

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
}
