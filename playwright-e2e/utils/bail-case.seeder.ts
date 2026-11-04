import { randomInt, randomUUID } from "node:crypto";
import { CaseData, CcdApi } from "./ccd-api.utils";
import { config } from "./config.utils";

/** Who created the case, which decides its shape: an admin officer or a legal representative. */
export type BailCaseCreator = "adminOfficer" | "legalRep";

export interface AddressUK {
  AddressLine1: string;
  AddressLine2: string;
  AddressLine3: string;
  PostTown: string;
  County: string;
  PostCode: string;
  Country: string;
}

export interface SeededBailCase {
  caseId: string;
  createdBy: BailCaseCreator;
  /** The case data as CCD stored it once the application was submitted. */
  data: CaseData;
  applicant: { givenNames: string; familyName: string; address: AddressUK };
  supporter: { givenNames: string; familyNames: string; address: AddressUK };
  interpreter: { applicantSpokenLanguage: string; supporterSpokenLanguage: string };
  hearingCentre: string;
  homeOfficeReference: string;
  legalRep?: {
    name: string;
    familyName: string;
    phone: string;
    reference: string;
  };
}

interface DynamicListItem {
  code: string;
  label: string;
}

interface DynamicList {
  value?: DynamicListItem;
  list_items?: DynamicListItem[];
}

const START_APPLICATION = "startApplication";
const SUBMIT_APPLICATION = "submitApplication";
const APPLICANT_LANGUAGE = "French";
const SUPPORTER_LANGUAGE = "Polish";

/**
 * Seeds a Bail case at applicationSubmitted through CCD, as the role that would create it, in a
 * few seconds rather than through startApplication's 95 wizard pages.
 *
 * The data is based on src/functionalTest/resources/templates/minimal-application-started.json
 * without its five currentCaseStateVisibleTo* fields, which no user may create (CCD answers
 * "No field found"), and enriched with the shapes the SDK model has struggled to read back:
 *
 * - an applicant address and a financial condition supporter's address: AddressUK inside the
 *   @JsonUnwrapped(prefix) Applicant and Supporter groups;
 * - spoken-language interpreters for the applicant and the supporter: InterpreterSpokenLanguage
 *   complex values whose languageRefData is a DynamicList, picked from the reference-data list
 *   the event's about-to-start callback offers, as XUI would;
 * - for a legal representative's case, the legal rep's details and a ChangeOrganisationRequest
 *   in the shape RemoveRepresentationPreparer writes.
 *
 * Fields the case's own callbacks set are not supplied: startApplication's about-to-submit fills
 * in hearingCentreRefData and caseManagementCategory (DynamicLists), caseManagementLocation and
 * localAuthorityPolicy (OrganisationPolicy), and submitApplication generates the application
 * document, so the case carries what a real one does.
 *
 * Every value that identifies the case is unique per call, so tests never see each other's cases.
 */
export class BailCaseSeeder {
  constructor(private readonly api: CcdApi) {}

  public async seed(createdBy: BailCaseCreator): Promise<SeededBailCase> {
    const unique = uniqueToken();
    const applicant = {
      givenNames: `Pw${unique}`,
      familyName: `Applicant${unique}`,
      address: address(`${randomInt(1, 200)} ${unique} Road`, "London", "SW1A 1AA", "Greater London"),
    };
    const supporter = {
      givenNames: `Sup${unique}`,
      familyNames: `Supporter${unique}`,
      address: address(`${randomInt(1, 200)} ${unique} Street`, "Newcastle Upon Tyne", "NE2 1JX", ""),
    };
    const homeOfficeReference = String(randomInt(100_000_000, 999_999_999));
    const legalRep =
      createdBy === "legalRep"
        ? {
            name: `Lr${unique}`,
            familyName: `Representative${unique}`,
            phone: "01632960002",
            reference: `LR-${unique}`,
          }
        : undefined;

    const created = await this.api.createCase(
      config.bail.caseTypeId,
      START_APPLICATION,
      (prefilled) => ({
        ...roleFlags(prefilled),
        ...applicationData(createdBy),
        applicantGivenNames: applicant.givenNames,
        applicantFamilyName: applicant.familyName,
        applicantFullName: `${applicant.givenNames} ${applicant.familyName}`,
        applicantHasAddress: "Yes",
        applicantAddress: applicant.address,
        homeOfficeReferenceNumber: homeOfficeReference,
        interpreterYesNo: "Yes",
        applicantInterpreterLanguageCategory: ["spokenLanguageInterpreter"],
        applicantInterpreterSpokenLanguage: spokenLanguage(
          prefilled,
          "applicantInterpreterSpokenLanguage",
          APPLICANT_LANGUAGE
        ),
        hasFinancialCondSupporter: "Yes",
        hasFinancialCondSupporter2: "No",
        supporterGivenNames: supporter.givenNames,
        supporterFamilyNames: supporter.familyNames,
        supporterDOB: "1980-02-03",
        supporterAddressDetails: supporter.address,
        supporterContactDetails: ["telephone"],
        supporterTelephoneNumber1: "01632960001",
        supporterRelation: "Parent",
        supporterOccupation: "Teacher",
        supporterImmigration: "British citizen",
        supporterNationality: [{ id: randomUUID(), value: { code: "British" } }],
        supporterHasPassport: "No",
        financialAmountSupporterUndertakes1: "250",
        fcsInterpreterYesNo: "Yes",
        fcs1InterpreterLanguageCategory: ["spokenLanguageInterpreter"],
        fcs1InterpreterSpokenLanguage: spokenLanguage(
          prefilled,
          "fcs1InterpreterSpokenLanguage",
          SUPPORTER_LANGUAGE
        ),
        ...(legalRep && {
          legalRepName: legalRep.name,
          legalRepFamilyName: legalRep.familyName,
          legalRepPhone: legalRep.phone,
          legalRepReference: legalRep.reference,
          changeOrganisationRequestField: {
            CaseRoleId: dynamicList({
              code: "[LEGALREPRESENTATIVE]",
              label: "Legal Representative",
            }),
            RequestTimestamp: new Date().toISOString().replace("Z", ""),
            ApprovalStatus: "1",
          },
        }),
      })
    );

    // A legal representative has to make the declaration on submission; nobody else sees it.
    const submitted = await this.api.submitEvent(
      created.id,
      SUBMIT_APPLICATION,
      createdBy === "legalRep" ? { declarationOnSubmit: ["declarationOnSubmit"] } : {}
    );
    if (submitted.state !== "applicationSubmitted") {
      throw new Error(
        `Seeded case ${created.id} is in state ${submitted.state} after ${SUBMIT_APPLICATION}, expected applicationSubmitted.`
      );
    }

    const stored = await this.api.getCase(created.id);
    const hearingCentre = (stored.data.hearingCentreRefData as DynamicList | undefined)
      ?.value?.label;
    if (!hearingCentre) {
      throw new Error(
        `Seeded case ${created.id} has no hearingCentreRefData; startApplication's location reference-data lookup did not run.`
      );
    }

    return {
      caseId: created.id,
      createdBy,
      data: stored.data,
      applicant,
      supporter,
      interpreter: {
        applicantSpokenLanguage: APPLICANT_LANGUAGE,
        supporterSpokenLanguage: SUPPORTER_LANGUAGE,
      },
      hearingCentre,
      homeOfficeReference,
      legalRep,
    };
  }
}

/**
 * The fields startApplication's about-to-start callback sets from the signed-in user's role:
 * isAdmin / isLegalRep / isHomeOffice, which decide the state the case starts in and who it is
 * recorded as submitted by, and for a legal representative the firm's email and organisation.
 */
function roleFlags(prefilled: CaseData): CaseData {
  const keep = [
    "isAdmin",
    "isLegalRep",
    "isHomeOffice",
    "legalRepEmail",
    "legalRepCompany",
    "localAuthorityPolicy",
  ];
  return Object.fromEntries(
    Object.entries(prefilled).filter(([key]) => keep.includes(key))
  );
}

/**
 * minimal-application-started.json, less the currentCaseStateVisibleTo* fields and the bail
 * reference number (submitApplication generates it from the case ID). An admin officer records
 * who sent the application in; a legal representative cannot create hasLegalRep or
 * sentByChecklist, which only apply to applications made on someone's behalf.
 */
function applicationData(createdBy: BailCaseCreator): CaseData {
  return {
    ircName: "Colnbrook",
    detentionFacility: "Colnbrook",
    applicantDetainedLoc: "immigrationRemovalCentre",
    hearingCentre: "hattonCross",
    videoHearing1: "Yes",
    applicantGender: "Male",
    applicantHasMobile: "No",
    applicantDateOfBirth: "1990-01-01",
    applicantDisability1: "No",
    applicantNationality: "HAS_NATIONALITY",
    applicantNationalities: [{ id: randomUUID(), value: { code: "English" } }],
    applicantArrivalInUKDate: "2015-01-01",
    interpreterLanguages: [],
    groundsForBailReasons: "The applicant has a fixed address and a supporter.",
    hasAppealHearingPending: "No",
    hasPreviousBailApplication: "No",
    agreesToBoundByFinancialCond: "No",
    previousApplicationDoneViaCcd: "No",
    previousApplicationDoneViaAria: "No",
    transferBailManagementObjectionYesOrNo: "No",
    groundsForBailProvideEvidenceOption: "No",
    ...(createdBy === "adminOfficer"
      ? {
          hasLegalRep: "No",
          sentByChecklist: "Applicant",
          applicationSubmittedBy: "Applicant",
        }
      : {}),
  };
}

/**
 * An InterpreterSpokenLanguage value whose languageRefData DynamicList is the reference-data list
 * the about-to-start callback offered, with `label` selected: exactly what XUI submits.
 */
function spokenLanguage(prefilled: CaseData, field: string, label: string): CaseData {
  const offered = (prefilled[field] as { languageRefData?: DynamicList } | undefined)
    ?.languageRefData?.list_items;
  if (!offered?.length) {
    throw new Error(
      `startApplication's about-to-start returned no language reference data for ${field}.`
    );
  }
  const chosen = offered.find((item) => item.label === label);
  if (!chosen) {
    throw new Error(
      `"${label}" is not in the language reference data offered for ${field}; offered e.g. ${offered
        .slice(0, 5)
        .map((item) => item.label)
        .join(", ")}.`
    );
  }
  return {
    languageManualEntry: "No",
    languageRefData: { value: chosen, list_items: offered },
  };
}

function dynamicList(item: DynamicListItem): DynamicList {
  return { value: item, list_items: [item] };
}

function address(line1: string, town: string, postCode: string, county: string): AddressUK {
  return {
    AddressLine1: line1,
    AddressLine2: "",
    AddressLine3: "",
    PostTown: town,
    County: county,
    PostCode: postCode,
    Country: "United Kingdom",
  };
}

/** Eight letters, so the value is valid in name fields that reject digits. */
function uniqueToken(): string {
  const letters = "abcdefghijklmnopqrstuvwxyz";
  const token = Array.from({ length: 8 }, () => letters[randomInt(letters.length)]).join("");
  return token[0].toUpperCase() + token.slice(1);
}
