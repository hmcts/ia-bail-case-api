import { randomInt } from "node:crypto";
import type { UploadFile, WizardAnswers } from "../page-objects/pages/event-wizard.po";
import { uniqueToken } from "./bail-case.seeder";
import { pdfFile } from "./document.utils";

/**
 * An edited application: the applicant in an immigration removal centre, with no financial
 * condition supporter and new legal rep details. Names and references are unique to the test so
 * a failure can never be satisfied by another case.
 */
export interface EditedApplication {
  givenNames: string;
  familyName: string;
  dateOfBirth: { day: string; month: string; year: string };
  /** How CCD shows dateOfBirth. */
  dateOfBirthShown: string;
  homeOfficeReference: string;
  ircName: string;
  groundsForBail: string;
  legalRep: { company: string; name: string; familyName: string; email: string; reference: string; phone: string };
}

export function editedApplication(): EditedApplication {
  const unique = uniqueToken(6);
  return {
    givenNames: `Harry${unique}`,
    familyName: `Pughes${unique}`,
    dateOfBirth: { day: "25", month: "12", year: "1999" },
    dateOfBirthShown: "25 Dec 1999",
    homeOfficeReference: String(randomInt(10_000_000, 99_999_999)),
    ircName: "Yarlswood",
    groundsForBail: `Edited grounds for bail ${unique}`,
    legalRep: {
      company: `Edited Legal Representative Company ${unique}`,
      name: `Edited${unique}`,
      familyName: `Representative${unique}`,
      email: `edited.legal.rep.${unique.toLowerCase()}@example.com`,
      reference: `Edited reference ${unique}`,
      phone: "07292929292",
    },
  };
}

/**
 * The answers for "Edit the application" (before or after submission) that move the applicant to
 * Yarl's Wood, take away the financial condition and its supporter and the interpreter, and
 * change the legal representative's details. Pages not listed keep the values already on the
 * case; the seeded case starts in prison with one supporter and two interpreters, so every page
 * that changes is listed.
 */
export function editApplicationAnswers(edit: EditedApplication): WizardAnswers {
  return {
    hasPreviousBailApplication: [{ choose: "hasPreviousBailApplication-No" }],
    applicantNameDetails: [
      { fill: "applicantGivenNames", value: edit.givenNames },
      { fill: "applicantFamilyName", value: edit.familyName },
    ],
    applicantDateOfBirthDetails: [{ date: "applicantDateOfBirth", value: edit.dateOfBirth }],
    applicantNationalityDetails: [{ choose: "applicantNationality-STATELESS" }],
    homeOfficeReferenceNumber: [{ fill: "homeOfficeReferenceNumber", value: edit.homeOfficeReference }],
    applicantDetainedDetails: [{ choose: "applicantDetainedLoc-immigrationRemovalCentre" }],
    ircDetails: [{ select: "ircName", label: edit.ircName }],
    applicantMobileDetails: [{ choose: "applicantHasMobile_No" }],
    hasAppealHearingPending: [{ choose: "hasAppealHearingPending-No" }],
    applicantHasAddress: [{ choose: "applicantHasAddress_No" }],
    hasProbationOffenderManager: [{ choose: "hasProbationOffenderManager_No" }],
    financialConditionAgree: [{ choose: "agreesToBoundByFinancialCond_No" }],
    financialConditionSupporter: [{ choose: "hasFinancialCondSupporter_No" }],
    groundsForBailReasons: [{ fill: "groundsForBailReasons", value: edit.groundsForBail }],
    groundsForBailProvideEvidence: [{ choose: "groundsForBailProvideEvidenceOption_No" }],
    transferBailManagement: [{ choose: "transferBailManagementObjectionYesOrNo_No" }],
    interpreter: [{ choose: "interpreterYesNo_No" }],
    disabilityDetails: [{ choose: "applicantDisability1_No" }],
    applicantVideoHearingDetails: [{ choose: "videoHearing1_Yes" }],
    legalRepresentativeDetails: [
      { fill: "legalRepCompany", value: edit.legalRep.company },
      { fill: "legalRepName", value: edit.legalRep.name },
      { fill: "legalRepFamilyName", value: edit.legalRep.familyName },
      { fill: "legalRepEmail", value: edit.legalRep.email },
      { fill: "legalRepReference", value: edit.legalRep.reference },
      { fill: "legalRepPhone", value: edit.legalRep.phone },
    ],
  };
}

export interface NewApplication {
  groundsForBail: string;
  evidence: { file: UploadFile; description: string };
  /** Only an admin officer uploads a B1 form. */
  b1Form: { file: UploadFile; description: string };
  nomsNumber: string;
}

export function newApplication(caseId: string): NewApplication {
  const unique = uniqueToken(6);
  return {
    groundsForBail: `New grounds for bail ${unique}`,
    evidence: {
      file: pdfFile(`GroundsForBailSupportingEvidence-${caseId}`),
      description: `This is a different supporting evidence ${unique}`,
    },
    b1Form: { file: pdfFile(`B1Form-${caseId}`), description: `This is a new B1 form ${unique}` },
    nomsNumber: String(randomInt(100_000_000, 999_999_999)),
  };
}

/**
 * The answers for "Make new application": the applicant in prison, with no financial condition
 * supporter and no legal representative. The event keeps the
 * applicant's name, date of birth, nationality and Home Office reference and asks again for
 * everything else. An admin officer also says who sent the application and uploads a B1 form.
 */
export function newApplicationAnswers(application: NewApplication, by: "adminOfficer" | "homeOffice"): WizardAnswers {
  return {
    ...(by === "adminOfficer" && { adminSentByPage: [{ choose: "sentByChecklist-Applicant" }] }),
    applicantDetainedDetails: [
      { choose: "applicantDetainedLoc-prison" },
      { fill: "applicantPrisonDetails", value: application.nomsNumber },
    ],
    applicantPrisonDetails: [{ select: "prisonName", label: "HM Prison Blundeston" }],
    applicantMobileDetails: [{ choose: "applicantHasMobile_No" }],
    hasAppealHearingPending: [{ choose: "hasAppealHearingPending-No" }],
    applicantHasAddress: [{ choose: "applicantHasAddress_No" }],
    hasProbationOffenderManager: [{ choose: "hasProbationOffenderManager_No" }],
    financialConditionAgree: [{ choose: "agreesToBoundByFinancialCond_No" }],
    financialConditionSupporter: [{ choose: "hasFinancialCondSupporter_No" }],
    groundsForBailReasons: [{ fill: "groundsForBailReasons", value: application.groundsForBail }],
    groundsForBailProvideEvidence: [{ choose: "groundsForBailProvideEvidenceOption_Yes" }],
    groundsForBailEvidenceDocumentUpload: [
      { addNew: "uploadTheBailEvidenceDocs" },
      { upload: "uploadTheBailEvidenceDocs_0_document", file: application.evidence.file },
      { fill: "uploadTheBailEvidenceDocs_0_description", value: application.evidence.description },
    ],
    transferBailManagement: [{ choose: "transferBailManagementObjectionYesOrNo_No" }],
    interpreter: [{ choose: "interpreterYesNo_No" }],
    disabilityDetails: [{ choose: "applicantDisability1_No" }],
    applicantVideoHearingDetails: [{ choose: "videoHearing1_Yes" }],
    hasLegalRepresentative: [{ choose: "hasLegalRep_No" }],
    ...(by === "adminOfficer" && {
      uploadB1Form: [
        { addNew: "uploadB1FormDocs" },
        { upload: "uploadB1FormDocs_0_document", file: application.b1Form.file },
        { fill: "uploadB1FormDocs_0_description", value: application.b1Form.description },
      ],
    }),
  };
}
