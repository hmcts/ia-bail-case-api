import { ApiClient, ApiClientError } from "@hmcts/playwright-common";
import { BrowserContext } from "@playwright/test";
import { config } from "./config.utils";

export type CaseData = Record<string, unknown>;

export interface CcdCase {
  id: string;
  state: string;
  data: CaseData;
}

interface StartTriggerResponse {
  token: string;
  case_details?: { case_data?: CaseData; data?: CaseData };
}

interface CaseResponse {
  id: string | number;
  state: string;
  data?: CaseData;
  case_data?: CaseData;
}

const MEDIA_TYPE = "application/vnd.uk.gov.hmcts.ccd-data-store-api";
const ACCEPT = {
  startCaseTrigger: `${MEDIA_TYPE}.start-case-trigger.v2+json;charset=UTF-8`,
  createCase: `${MEDIA_TYPE}.create-case.v2+json;charset=UTF-8`,
  startEventTrigger: `${MEDIA_TYPE}.start-event-trigger.v2+json;charset=UTF-8`,
  createEvent: `${MEDIA_TYPE}.create-event.v2+json;charset=UTF-8`,
  case: `${MEDIA_TYPE}.case.v2+json;charset=UTF-8`,
} as const;

/**
 * Drives ccd-data-store-api as a signed-in XUI user, through XUI's /data proxy.
 *
 * The requests ride on the browser context's own cookie jar, so they carry the XUI session of
 * whichever role the context was opened with, and CCD applies that role's access exactly as it
 * does for the UI. XUI rejects a state-changing request whose X-XSRF-TOKEN header does not match
 * its XSRF-TOKEN cookie, so the header is read from the jar on every call.
 *
 * The case's own callbacks run as they do for a case created in the UI: startApplication's
 * about-to-start and about-to-submit fill in the location and language reference data, the
 * case management category and the organisation policy, which are the shapes a seeded case
 * needs to carry.
 */
export class CcdApi {
  private readonly client: ApiClient;

  constructor(private readonly context: BrowserContext) {
    this.client = new ApiClient({
      name: "ccd-via-xui",
      baseUrl: config.urls.xuiBaseUrl,
      requestFactory: async () => this.context.request,
      // Successful calls would otherwise log every case body; failures still log.
      loggerOptions: { level: "warn" },
      defaultHeaders: { "content-type": "application/json", experimental: "true" },
    });
  }

  private async startCase(caseTypeId: string, eventId: string): Promise<StartTriggerResponse> {
    return this.send<StartTriggerResponse>(
      "GET",
      `/data/case-types/${caseTypeId}/event-triggers/${eventId}?ignore-warning=false`,
      ACCEPT.startCaseTrigger
    );
  }

  /**
   * Creates a case. `buildData` receives the data the event's about-to-start callback prepared,
   * which is what XUI pre-fills the wizard with, and returns the data to submit.
   */
  public async createCase(
    caseTypeId: string,
    eventId: string,
    buildData: (prefilled: CaseData) => CaseData
  ): Promise<CcdCase> {
    const trigger = await this.startCase(caseTypeId, eventId);
    const created = await this.send<CaseResponse>(
      "POST",
      `/data/case-types/${caseTypeId}/cases?ignore-warning=false`,
      ACCEPT.createCase,
      {
        data: buildData(triggerData(trigger)),
        event: { id: eventId, summary: "", description: "" },
        event_token: trigger.token,
        ignore_warning: false,
      }
    );
    return toCase(created);
  }

  public async submitEvent(
    caseId: string,
    eventId: string,
    data: CaseData = {}
  ): Promise<CcdCase> {
    const trigger = await this.send<StartTriggerResponse>(
      "GET",
      `/data/cases/${caseId}/event-triggers/${eventId}?ignore-warning=false`,
      ACCEPT.startEventTrigger
    );
    const submitted = await this.send<CaseResponse>(
      "POST",
      `/data/cases/${caseId}/events`,
      ACCEPT.createEvent,
      {
        data,
        event: { id: eventId, summary: "", description: "" },
        event_token: trigger.token,
        ignore_warning: false,
      }
    );
    return toCase(submitted);
  }

  /**
   * The signed-in user's name as IDAM holds it, from XUI's own /api/user/details. Bail's
   * handlers record "<forename> <surname>" as the author of what the user adds. XUI signed in
   * through OIDC (AAT) returns given_name / family_name; the preview's XUI returns forename /
   * surname.
   */
  public async signedInUserName(): Promise<string> {
    const response = await this.client.get<{
      userInfo?: {
        given_name?: string;
        family_name?: string;
        forename?: string;
        surname?: string;
      };
    }>("/api/user/details", { headers: { accept: "application/json" } });
    const user = response.data?.userInfo ?? {};
    const forename = user.given_name ?? user.forename;
    const surname = user.family_name ?? user.surname;
    if (!forename || !surname) {
      throw new Error("XUI's /api/user/details returned no forename and surname for the user.");
    }
    return `${forename} ${surname}`;
  }

  public async getCase(caseId: string): Promise<CcdCase> {
    return toCase(
      await this.send<CaseResponse>("GET", `/data/cases/${caseId}`, ACCEPT.case)
    );
  }

  /**
   * XUI's proxy answers 406 when the accept header names a media-type version the data store
   * does not offer for that endpoint, and lists the ones it does. Retrying once with the first
   * of those keeps the helper working across data-store releases.
   */
  private async send<T>(
    method: "GET" | "POST",
    path: string,
    accept: string,
    data?: unknown
  ): Promise<T> {
    const attempt = async (acceptHeader: string) => {
      const headers = { accept: acceptHeader, ...(await this.xsrfHeader()) };
      return method === "GET"
        ? this.client.get<T>(path, { headers, throwOnError: false })
        : this.client.post<T>(path, { headers, data, throwOnError: false });
    };

    let response = await attempt(accept);
    if (response.status === 406) {
      const offered = /Acceptable representations: \[([^\]]+)\]/.exec(
        JSON.stringify(response.data)
      )?.[1];
      if (offered) {
        response = await attempt(offered.split(",")[0].trim());
      }
    }
    if (!response.ok) {
      throw new ApiClientError(
        `${method} ${path} failed with ${response.status}: ${preview(response.data)}`,
        response.status,
        response.logEntry
      );
    }
    return response.data;
  }

  private async xsrfHeader(): Promise<Record<string, string>> {
    const token = (await this.context.cookies(config.urls.xuiBaseUrl)).find(
      (cookie) => cookie.name === "XSRF-TOKEN"
    )?.value;
    return token ? { "X-XSRF-TOKEN": decodeURIComponent(token) } : {};
  }
}

function triggerData(trigger: StartTriggerResponse): CaseData {
  return trigger.case_details?.case_data ?? trigger.case_details?.data ?? {};
}

function toCase(response: CaseResponse): CcdCase {
  return {
    id: String(response.id),
    state: response.state,
    data: response.data ?? response.case_data ?? {},
  };
}

function preview(body: unknown): string {
  const text = typeof body === "string" ? body : JSON.stringify(body);
  return (text ?? "").slice(0, 800);
}
