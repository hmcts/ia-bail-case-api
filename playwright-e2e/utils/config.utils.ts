import { ConfigUtils } from "@hmcts/playwright-common";
import dotenv from "dotenv";
import path from "node:path";
import { fileURLToPath } from "node:url";

dotenv.config({ quiet: true });

/** Stored sign-ins and the upload throttle's per-session state. Gitignored. */
export const sessionsDir = path.join(
  path.dirname(fileURLToPath(import.meta.url)),
  "..",
  ".sessions"
);

export const USER_ROLES = [
  "adminOfficer",
  "legalRep",
  "homeOffice",
  "judge",
  "legalRepBails",
  "otherOrgLegalRep",
] as const;
export type UserRole = (typeof USER_ROLES)[number];

export interface SessionUser {
  /** How the role is named in test titles and failure messages. */
  label: string;
  sessionFile: string;
  cookieName: string;
  /** Prefix of the <PREFIX>_USERNAME / <PREFIX>_PASSWORD variables in .env. */
  credentialsEnvPrefix: string;
}

export interface UserCredentials {
  username: string;
  password: string;
}

export interface Config {
  users: Record<UserRole, SessionUser>;
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
  /**
   * Whether the environment can take a hearing request: HMC and ia-hearings-api, which AAT has and
   * a PR's preview namespace does not (its CCD is its own, so HMC on AAT cannot see its cases).
   * HEARINGS_AVAILABLE overrides the default, which is "not a preview URL".
   */
  hearingsAvailable: boolean;
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

const sessionUser = (
  label: string,
  fileName: string,
  credentialsEnvPrefix: string
): SessionUser => ({
  label,
  sessionFile: path.join(sessionsDir, `${fileName}.json`),
  cookieName: "xui-webapp",
  credentialsEnvPrefix,
});

export const config: Config = {
  users: {
    adminOfficer: sessionUser("admin officer", "admin-officer", "TEST_ADMINOFFICER"),
    legalRep: sessionUser("legal representative", "legal-rep", "TEST_LAW_FIRM_A"),
    homeOffice: sessionUser("Home Office", "home-office", "TEST_HOMEOFFICE_BAIL"),
    judge: sessionUser("judge", "judge", "TEST_JUDGE_BAILS"),
    // The old suite's "Legal Org User Rep A Bails": a solicitor and case-access administrator in
    // a MyHMCTS organisation, who creates the cases another organisation takes over by NoC.
    legalRepBails: sessionUser(
      "legal representative of the creating organisation",
      "legal-rep-bails",
      "TEST_LAW_FIRM_NOC_A_BAILS"
    ),
    // The old suite's "Legal Org User Rep C": a solicitor in a different organisation.
    otherOrgLegalRep: sessionUser(
      "legal representative of another organisation",
      "other-org-legal-rep",
      "TEST_LAW_FIRM_ORG_DELETED"
    ),
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
  hearingsAvailable: process.env.HEARINGS_AVAILABLE
    ? process.env.HEARINGS_AVAILABLE.trim() === "true"
    : !/\.preview\.platform\.hmcts\.net$/.test(new URL(xuiBaseUrl).hostname),
  caseDetailsUrl: (caseReference: string) =>
    `${xuiBaseUrl}/cases/case-details/${caseReference}`,
};

/**
 * Credentials for a role. Read on demand, because global.setup.ts is the only place that signs
 * in: the specs run from the sessions it stores.
 */
export function credentialsFor(role: UserRole): UserCredentials {
  const prefix = config.users[role].credentialsEnvPrefix;
  return {
    username: requireEnvVar(`${prefix}_USERNAME`),
    password: requireEnvVar(`${prefix}_PASSWORD`),
  };
}
