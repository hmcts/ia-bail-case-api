import { ConfigUtils } from "@hmcts/playwright-common";
import dotenv from "dotenv";
import path from "node:path";
import { fileURLToPath } from "node:url";

dotenv.config({ quiet: true });

const sessionsDir = path.join(
  path.dirname(fileURLToPath(import.meta.url)),
  "..",
  ".sessions"
);

export interface SessionUser {
  sessionFile: string;
  cookieName: string;
}

export interface UserCredentials {
  username: string;
  password: string;
}

export interface Config {
  users: {
    adminOfficer: SessionUser;
  };
  urls: {
    xuiBaseUrl: string;
    createCaseUrl: string;
  };
  bail: {
    jurisdictionId: string;
    jurisdictionLabel: string;
    caseTypeId: string;
    caseTypeLabel: string;
    startApplicationEventId: string;
    startApplicationEventLabel: string;
  };
  caseDetailsUrl: (caseReference: string) => string;
}

/**
 * Reads a variable the whole suite depends on. Throws rather than returning a default, so a
 * missing environment variable fails the run with its name instead of a timeout somewhere later.
 */
export function requireEnvVar(name: string): string {
  const value = ConfigUtils.getEnvVar(name).trim();
  if (!value) {
    throw new Error(
      `Missing required environment variable: ${name}. See playwright-e2e/.env.example.`
    );
  }
  return value;
}

const xuiBaseUrl = requireEnvVar("XUI_URL").replace(/\/+$/, "");

export const config: Config = {
  users: {
    adminOfficer: {
      sessionFile: path.join(sessionsDir, "admin-officer.json"),
      cookieName: "xui-webapp",
    },
  },
  urls: {
    xuiBaseUrl,
    createCaseUrl: `${xuiBaseUrl}/cases/case-filter`,
  },
  bail: {
    jurisdictionId: "IA",
    jurisdictionLabel: "Immigration & Asylum",
    caseTypeId: "Bail",
    caseTypeLabel: process.env.BAIL_CASE_TYPE_LABEL?.trim() || "BailHEAD",
    startApplicationEventId: "startApplication",
    startApplicationEventLabel: "Start the application",
  },
  caseDetailsUrl: (caseReference: string) =>
    `${xuiBaseUrl}/cases/case-details/${caseReference}`,
};

/**
 * Credentials for the IA admin officer. Read on demand, because global.setup.ts is the only
 * place that signs in: the specs run from the session it stores.
 */
export function adminOfficerCredentials(): UserCredentials {
  return {
    username: requireEnvVar("TEST_ADMINOFFICER_USERNAME"),
    password: requireEnvVar("TEST_ADMINOFFICER_PASSWORD"),
  };
}

/**
 * The case view tests read an existing Bail case rather than creating one through the 95-page
 * "Start the application" event, so the reference has to be supplied. Fail loudly when it is
 * not: a skipped case view test looks the same as a passing one in a Jenkins summary.
 */
export function requireBailCaseReference(): string {
  const reference = process.env.BAIL_CASE_REFERENCE?.trim().replace(/\s|-/g, "");
  if (!reference) {
    throw new Error(
      "BAIL_CASE_REFERENCE is not set. Set it to the 16-digit reference of a Bail case that " +
        "exists in the environment under test (see playwright-e2e/README.md)."
    );
  }
  if (!/^\d{16}$/.test(reference)) {
    throw new Error(
      `BAIL_CASE_REFERENCE must be a 16-digit case reference, got "${reference}".`
    );
  }
  return reference;
}
