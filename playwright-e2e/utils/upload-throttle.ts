import { createHash } from "node:crypto";
import fs from "node:fs/promises";
import path from "node:path";
import { setTimeout as delay } from "node:timers/promises";
import type { BrowserContext } from "@playwright/test";
import { config, sessionsDir } from "./config.utils";

/**
 * XUI throttles document uploads per XUI session (rpx-xui-webapp api/documents/index.ts): a
 * POST to /documentsv2 that starts within `documentUploadThrottle.initialMs` (5 s by default) of
 * the session's previous upload response is answered 429, and every rejected attempt doubles the
 * window, up to three minutes. Every test signed in as a role shares that role's stored session,
 * so two workers uploading as the admin officer, or one wizard uploading two files in a row,
 * would trip it, and retrying only lengthens the wait.
 *
 * So uploads are serialised per XUI session across all workers: a lock directory per session
 * (mkdir is atomic), and a file holding when that session's last upload got its response. An
 * upload waits for the lock, then for the throttle window to have passed since that time, then
 * runs, records its own response time and releases the lock. The waits are the server's rule,
 * not a guess at how long something takes.
 */
const THROTTLE_MS = 5_000;
/** Covers the gap between XUI recording the upload and the response reaching the browser. */
const MARGIN_MS = 750;
/** A lock older than this was left by a worker that died mid-upload. */
const STALE_LOCK_MS = 120_000;
const LOCK_POLL_MS = 200;

export async function throttledUpload<T>(
  context: BrowserContext,
  upload: () => Promise<T>
): Promise<T> {
  const key = await sessionKey(context);
  const lock = path.join(sessionsDir, `${key}.upload-lock`);
  const lastUpload = path.join(sessionsDir, `${key}.last-upload`);

  await acquire(lock);
  try {
    const last = Number(await fs.readFile(lastUpload, "utf-8").catch(() => "0"));
    const wait = last + THROTTLE_MS + MARGIN_MS - Date.now();
    if (wait > 0) {
      await delay(wait);
    }
    try {
      return await upload();
    } finally {
      await fs.writeFile(lastUpload, String(Date.now()));
    }
  } finally {
    await fs.rm(lock, { recursive: true, force: true });
  }
}

async function acquire(lock: string): Promise<void> {
  await fs.mkdir(path.dirname(lock), { recursive: true });
  for (;;) {
    try {
      await fs.mkdir(lock);
      return;
    } catch (error) {
      if ((error as NodeJS.ErrnoException).code !== "EEXIST") throw error;
      const stat = await fs.stat(lock).catch(() => undefined);
      if (stat && Date.now() - stat.mtimeMs > STALE_LOCK_MS) {
        await fs.rm(lock, { recursive: true, force: true });
        continue;
      }
      await delay(LOCK_POLL_MS);
    }
  }
}

/** The XUI session the context's requests belong to, hashed so it is safe as a file name. */
async function sessionKey(context: BrowserContext): Promise<string> {
  const cookies = await context.cookies(config.urls.xuiBaseUrl);
  const session = cookies.find((cookie) => cookie.name === "xui-webapp")?.value;
  if (!session) {
    throw new Error("The browser context holds no XUI session cookie; cannot upload documents.");
  }
  return createHash("sha256").update(session).digest("hex").slice(0, 16);
}
