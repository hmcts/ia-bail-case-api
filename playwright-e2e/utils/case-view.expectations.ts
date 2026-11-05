import { expect } from "@playwright/test";
import type { BailTab } from "../page-objects/pages/bail-case-details.po";
import type { SeededBailCase } from "./bail-case.seeder";
import type { UserRole } from "./config.utils";
import type { CaseViewSnapshot } from "./tab-snapshot.utils";

interface RoleView {
  /** The case-type tabs the role is shown on a submitted case it can access. */
  tabs: BailTab[];
  /** The progress_* field the Overview tab shows the role at applicationSubmitted. */
  submittedProgressField: string;
  /**
   * XUI's own work-allocation tabs, which it inserts in front of the case tabs once its
   * work-allocation lookups return, for the roles that have them.
   */
  workAllocationTabs: string[];
}

const WORK_ALLOCATION_TABS = ["Tasks", "Roles and access"];

const LEGAL_REP_VIEW: RoleView = {
  tabs: ["Overview", "Bail application", "Documents", "Directions", "Hearing and appointment"],
  submittedProgressField: "progress_applicationSubmittedLegalRepOrHomeOffice",
  workAllocationTabs: [],
};

/**
 * What each role is shown on a case at applicationSubmitted, as the definition grants it
 * (BailTabs.java and the per-field access in the model). The case history and case notes tabs
 * are granted only to the admin officer and the judge (and the system user). A legal
 * representative only sees the cases their organisation created or was assigned.
 */
export const ROLE_VIEWS: Record<UserRole, RoleView> = {
  adminOfficer: {
    tabs: [
      "Overview",
      "Bail application",
      "Documents",
      "Directions",
      "Hearing and appointment",
      "Case notes",
      "Case history",
      "Linked Cases",
      "Case flags",
    ],
    submittedProgressField: "progress_applicationSubmittedAdminDetentionConfirmedNull",
    workAllocationTabs: WORK_ALLOCATION_TABS,
  },
  judge: {
    tabs: [
      "Overview",
      "Bail application",
      "Documents",
      "Directions",
      "Hearing and appointment",
      "Case notes",
      "Case history",
      "Linked Cases",
      "Case flags",
    ],
    submittedProgressField: "progress_applicationSubmittedJudge",
    workAllocationTabs: WORK_ALLOCATION_TABS,
  },
  homeOffice: {
    tabs: [
      "Overview",
      "Bail application",
      "Documents",
      "Directions",
      "Hearing and appointment",
      "Linked Cases",
    ],
    submittedProgressField: "progress_applicationSubmittedLegalRepOrHomeOffice",
    workAllocationTabs: [],
  },
  legalRep: LEGAL_REP_VIEW,
  legalRepBails: LEGAL_REP_VIEW,
  otherOrgLegalRep: LEGAL_REP_VIEW,
};

/** The tabs whose content an event that only adds something must leave exactly as it was. */
const CONTENT_TABS: BailTab[] = [
  "Overview",
  "Bail application",
  "Hearing and appointment",
  "Documents",
  "Directions",
  "Case notes",
];

export function contentTabsFor(role: UserRole): BailTab[] {
  return CONTENT_TABS.filter((tab) => ROLE_VIEWS[role].tabs.includes(tab));
}

/** The values the seeder put on the case, by the tab and field that show them. */
function seededValues(seeded: SeededBailCase): Partial<Record<BailTab, Record<string, string[]>>> {
  const { applicant, supporter, interpreter, legalRep } = seeded;
  const addressLines = (address: SeededBailCase["applicant"]["address"]) => [
    address.AddressLine1,
    address.PostTown,
    address.PostCode,
  ];
  const legalRepFields: Record<string, string[]> = legalRep
    ? {
        legalRepName: [legalRep.name],
        legalRepFamilyName: [legalRep.familyName],
        legalRepPhone: [legalRep.phone],
        legalRepReference: [legalRep.reference],
      }
    : {};
  return {
    Overview: {
      applicantFullName: [`${applicant.givenNames} ${applicant.familyName}`],
      hearingCentreRefData: [seeded.hearingCentre],
      homeOfficeReferenceNumber: [seeded.homeOfficeReference],
      ...legalRepFields,
    },
    "Bail application": {
      applicantGivenNames: [applicant.givenNames],
      applicantFamilyName: [applicant.familyName],
      homeOfficeReferenceNumber: [seeded.homeOfficeReference],
      applicantAddress: addressLines(applicant.address),
      supporterGivenNames: [supporter.givenNames],
      supporterFamilyNames: [supporter.familyNames],
      supporterAddressDetails: addressLines(supporter.address),
      applicantInterpreterSpokenLanguage: [interpreter.applicantSpokenLanguage],
      fcs1InterpreterSpokenLanguage: [interpreter.supporterSpokenLanguage],
      ...legalRepFields,
    },
    "Hearing and appointment": {
      applicantInterpreterSpokenLanguage: [interpreter.applicantSpokenLanguage],
      fcs1InterpreterSpokenLanguage: [interpreter.supporterSpokenLanguage],
    },
  };
}

/**
 * The seeded applicant, supporter, addresses, interpreter languages, hearing centre and legal
 * rep are shown where the definition puts them. Checked on the "before" snapshot, so that the
 * comparison after an event is between two views that actually contain the data at risk.
 */
export function expectSeededDataShown(
  snapshot: CaseViewSnapshot,
  seeded: SeededBailCase,
  viewer: string
): void {
  for (const [tab, fields] of Object.entries(seededValues(seeded))) {
    const tabSnapshot = snapshot[tab];
    if (!tabSnapshot) continue;
    for (const [fieldId, values] of Object.entries(fields)) {
      const shown = Object.entries(tabSnapshot)
        .filter(([key]) => key === fieldId || key.startsWith(`${fieldId} › `))
        .map(([, value]) => value);
      expect
        .soft(shown, `"${tab}" tab, as the ${viewer}, shows the seeded ${fieldId}`)
        .toEqual(expect.arrayContaining(values));
    }
  }
}
