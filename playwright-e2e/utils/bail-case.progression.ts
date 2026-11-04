import { CaseData, CcdApi, CcdCase } from "./ccd-api.utils";
import type { UserRole } from "./config.utils";
import { pdfFile } from "./document.utils";

interface DynamicListItem {
  code: string;
  label: string;
}

/** A date as CCD stores a Date field (yyyy-mm-dd), `days` from today on the UK calendar. */
export function isoDateFromToday(days: number): string {
  return addDays(londonToday(), days).toISOString().slice(0, 10);
}

/** A date as CCD shows a Date field ("4 Nov 2026"), `days` from today on the UK calendar. */
export function shownDateFromToday(days: number): string {
  const date = addDays(londonToday(), days);
  // Built from parts because ICU abbreviates September as "Sept" in en-GB and CCD does not.
  const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
  return `${date.getUTCDate()} ${months[date.getUTCMonth()]} ${date.getUTCFullYear()}`;
}

function londonToday(): Date {
  const parts = new Intl.DateTimeFormat("en-GB", {
    day: "numeric",
    month: "numeric",
    year: "numeric",
    timeZone: "Europe/London",
  }).formatToParts(new Date());
  const part = (type: string) => Number(parts.find((p) => p.type === type)?.value);
  return new Date(Date.UTC(part("year"), part("month") - 1, part("day")));
}

function addDays(date: Date, days: number): Date {
  return new Date(date.getTime() + days * 86_400_000);
}

/**
 * Moves a seeded case through the events that come before the one a test drives in the UI, over
 * CCD as the role that would perform each one, so the case's own callbacks run and the case
 * carries what a real one does (documents generated, states set, notes and directions written).
 * The data each event submits is what XUI submitted for the same answers in the UI.
 *
 * Every step checks the state CCD answers with, so a precondition that did not take fails where
 * it happened rather than as a missing Next step option later.
 */
export class BailCaseProgression {
  constructor(
    private readonly caseId: string,
    private readonly ccdApiAs: (role: UserRole) => Promise<CcdApi>
  ) {}

  /** "Case listing" as the admin officer: an initial listing at Hatton Cross in two weeks. */
  public async listCase(): Promise<CcdCase> {
    return this.submit("adminOfficer", "caseListing", "bailSummaryUploaded", (prefilled) => ({
      listingEvent: "initialListing",
      refDataListingLocation: pickFromDynamicList(
        prefilled.refDataListingLocation,
        "Hatton Cross Tribunal Hearing Centre"
      ),
      isRemoteHearing: "No",
      listingHearingDuration: "100",
      listingHearingDate: `${isoDateFromToday(14)}T10:30:00.000`,
    }));
  }

  /** "Upload Bail Summary" as the Home Office. */
  public async uploadBailSummary(): Promise<CcdCase> {
    const api = await this.ccdApiAs("homeOffice");
    const document = await api.uploadDocument(pdfFile(`BailSummary-${this.caseId}`));
    return this.submit("homeOffice", "uploadBailSummary", "bailSummaryUploaded", {
      uploadBailSummaryDocs: [
        { id: null, value: { description: "This is the bail summary", document } },
      ],
    });
  }

  /**
   * "Record the decision" as the admin officer: the old suite's conditional grant with Secretary of
   * State consent (minded to grant, consent given, not released at once, activities and electronic
   * monitoring conditions, no financial condition, no transfer of bail management).
   */
  public async recordConditionalGrant(): Promise<CcdCase> {
    return this.submit("adminOfficer", "recordTheDecision", "unsignedDecision", (prefilled) => ({
      ...pick(prefilled, [
        "conditionsForBailAppearance",
        "conditionsForBailResidence",
        "conditionsForBailReporting",
        "bailTransferDirections",
      ]),
      judgeDetailsName: "Mr Judge Judgerson",
      secretaryOfStateConsentYesOrNo: "Yes",
      recordTheDecisionList: "mindedToGrant",
      reasonsJudgeIsMindedDetails: "This is a reason for minding to grant bail.",
      ssConsentDecision: "Yes",
      releaseStatusYesOrNo: "No",
      conditionsForBail: ["bailActivities", "bailElectronicMonitoring"],
      conditionsForBailActivities: "The applicant is not allowed to work.",
      conditionsForBailElectronicMonitoring: "The applicant is to be electronically monitored.",
      recordFinancialConditionYesOrNo: "No",
      bailTransferYesOrNo: "No",
      bailTransferOtherDetails: "Other relevant details",
    }));
  }

  /** "Upload signed decision notice" for a conditional grant, as the admin officer. */
  public async uploadSignedConditionalGrantNotice(): Promise<CcdCase> {
    const api = await this.ccdApiAs("adminOfficer");
    const document = await api.uploadDocument(pdfFile(`SignedDecisionNotice-${this.caseId}`));
    return this.submit(
      "adminOfficer",
      "uploadSignedDecisionNoticeConditionalGrant",
      "decisionConditionalBail",
      { uploadSignedDecisionNoticeDocument: document }
    );
  }

  /** "Move application to decided" as the admin officer. */
  public async moveToDecided(): Promise<CcdCase> {
    return this.submit("adminOfficer", "moveApplicationToDecided", "decisionDecided", {});
  }

  /** "Add case note" as the admin officer. */
  public async addCaseNote(note: { subject: string; description: string }): Promise<CcdCase> {
    return this.submit("adminOfficer", "addCaseNote", undefined, {
      addCaseNoteSubject: note.subject,
      addCaseNoteDescription: note.description,
    });
  }

  /** "End the application" as the admin officer. */
  public async endApplication(reasons: string): Promise<CcdCase> {
    return this.submit("adminOfficer", "endApplication", "applicationEnded", {
      endApplicationOutcome: "Withdrawn",
      endApplicationReasons: reasons,
    });
  }

  /** "Send direction" as the admin officer. */
  public async sendDirection(direction: {
    description: string;
    to: "Legal Representative" | "Home Office";
    dueInDays: number;
  }): Promise<CcdCase> {
    return this.submit("adminOfficer", "sendBailDirection", undefined, {
      sendDirectionDescription: direction.description,
      sendDirectionList: direction.to,
      dateOfCompliance: isoDateFromToday(direction.dueInDays),
    });
  }

  /** "Upload documents" as `role`: one document with a description. */
  public async uploadDocument(role: UserRole, fileName: string, description: string): Promise<CcdCase> {
    const api = await this.ccdApiAs(role);
    const document = await api.uploadDocument(pdfFile(fileName));
    return this.submit(role, "uploadDocuments", undefined, (prefilled) => ({
      ...pick(prefilled, ["currentUser", "lastFileUploadedBy"]),
      uploadDocuments: [{ id: null, value: { description, document } }],
    }));
  }

  private async submit(
    role: UserRole,
    eventId: string,
    expectedState: string | undefined,
    data: CaseData | ((prefilled: CaseData) => CaseData)
  ): Promise<CcdCase> {
    const api = await this.ccdApiAs(role);
    const result = await api.submitEvent(this.caseId, eventId, data);
    if (expectedState && result.state !== expectedState) {
      throw new Error(
        `Case ${this.caseId} is in state ${result.state} after ${eventId}, expected ${expectedState}.`
      );
    }
    return result;
  }
}

function pickFromDynamicList(list: unknown, label: string): { value: DynamicListItem; list_items: DynamicListItem[] } {
  const items = (list as { list_items?: DynamicListItem[] } | undefined)?.list_items ?? [];
  const chosen = items.find((item) => item.label === label);
  if (!chosen) {
    throw new Error(`"${label}" is not among the ${items.length} options the event offered.`);
  }
  return { value: chosen, list_items: items };
}

function pick(data: CaseData, keys: string[]): CaseData {
  return Object.fromEntries(keys.filter((key) => key in data).map((key) => [key, data[key]]));
}
