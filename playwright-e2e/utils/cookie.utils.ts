import fs from "node:fs";
import { Cookie } from "playwright-core";
import { config } from "./config.utils";

interface StorageState {
  cookies: Cookie[];
}

/**
 * XUI's cookie banner reloads the page when either of its buttons is clicked
 * (rpx-xui-common-lib cookie-banner.component.ts calls window.location.reload), which cancels
 * whatever the test was in the middle of. Answering it once in the stored session instead means
 * the banner never renders during a test.
 */
export class CookieUtils {
  public static readonly ANALYTICS_REJECTED = "false";

  /**
   * Writes the XUI "cookies answered" cookie into a saved session so the banner stays hidden.
   * The name is per user: hmcts-exui-cookies-${userId}-mc-accepted, where userId comes from the
   * __userid__ cookie XUI sets at sign-in (see rpx-xui-webapp app.component.ts).
   */
  public addAnalyticsCookie(sessionFile: string): void {
    const state = JSON.parse(
      fs.readFileSync(sessionFile, "utf-8")
    ) as StorageState;

    const userId = state.cookies.find(
      (cookie) => cookie.name === "__userid__"
    )?.value;
    if (!userId) {
      throw new Error(
        `No __userid__ cookie in ${sessionFile}; the sign-in did not complete.`
      );
    }

    state.cookies.push({
      name: `hmcts-exui-cookies-${userId}-mc-accepted`,
      value: CookieUtils.ANALYTICS_REJECTED,
      domain: new URL(config.urls.xuiBaseUrl).hostname,
      path: "/",
      expires: -1,
      httpOnly: false,
      secure: false,
      sameSite: "Lax",
    });

    fs.writeFileSync(sessionFile, JSON.stringify(state, null, 2));
  }
}
