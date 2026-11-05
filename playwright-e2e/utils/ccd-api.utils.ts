import { ApiClient, ApiClientError } from "@hmcts/playwright-common";
import { APIRequestContext, BrowserContext } from "@playwright/test";
import { config } from "./config.utils";
import { throttledUpload } from "./upload-throttle";

export type CaseData = Record<string, unknown>;

/** A Document field value as CCD stores it once CDAM has accepted the file. */
export interface CcdDocument {
  document_url: string;
  document_binary_url: string;
  document_filename: string;
  document_hash?: string;
}

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
  private readonly request: APIRequestContext;

  constructor(private readonly context: BrowserContext) {
    this.request = withoutCredentials(context.request);
    this.client = new ApiClient({
      name: "ccd-via-xui",
      baseUrl: config.urls.xuiBaseUrl,
      requestFactory: async () => this.request,
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

  /**
   * Submits an event. `data` is either the data to submit or a function of the data the event's
   * about-to-start callback prepared (what XUI pre-fills the wizard with).
   */
  public async submitEvent(
    caseId: string,
    eventId: string,
    data: CaseData | ((prefilled: CaseData) => CaseData) = {}
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
        data: typeof data === "function" ? data(triggerData(trigger)) : data,
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

  /**
   * Uploads a file to CDAM through XUI's /documentsv2, exactly as a document field in the UI does,
   * and returns the value to put in a Document field. CDAM ties the document to the case type and
   * hands back a hash token that ccd-data-store-api checks when the document is attached to a case,
   * so the hash goes into the field. Uploads are throttled per XUI session; see upload-throttle.ts.
   */
  public async uploadDocument(file: { name: string; mimeType: string; buffer: Buffer }): Promise<CcdDocument> {
    const headers = { accept: "application/json", ...(await this.xsrfHeader()) };
    const response = await throttledUpload(this.context, () =>
      this.request.post(`${config.urls.xuiBaseUrl}/documentsv2`, {
        headers,
        multipart: {
          files: file,
          classification: "PUBLIC",
          caseTypeId: config.bail.caseTypeId,
          jurisdictionId: config.bail.jurisdictionId,
        },
        timeout: 60_000,
      })
    );
    if (!response.ok()) {
      throw new Error(`Uploading ${file.name} to CDAM failed with ${response.status()}: ${preview(await response.text())}`);
    }
    const body = (await response.json()) as {
      documents?: {
        originalDocumentName: string;
        hashToken?: string;
        _links: { self: { href: string }; binary: { href: string } };
      }[];
    };
    const document = body.documents?.[0];
    if (!document) {
      throw new Error(`CDAM accepted ${file.name} but returned no document: ${preview(body)}`);
    }
    return {
      document_url: document._links.self.href,
      document_binary_url: document._links.binary.href,
      document_filename: document.originalDocumentName,
      ...(document.hashToken && { document_hash: document.hashToken }),
    };
  }

  /**
   * The ID of the signed-in professional user's MyHMCTS organisation, from XUI's /api/organisation
   * (rd-professional-api). Only users with a PUI role may read it.
   */
  public async organisationId(): Promise<string> {
    const response = await this.client.get<{ organisationIdentifier?: string }>("/api/organisation", {
      headers: { accept: "application/json" },
    });
    const id = response.data?.organisationIdentifier;
    if (!id) {
      throw new Error("XUI's /api/organisation returned no organisationIdentifier for the user.");
    }
    return id;
  }

  /**
   * How many Bail cases with this reference the signed-in user finds through CCD's Elasticsearch
   * search: what the case list shows them. A case reaches the index a moment after it is written,
   * so callers poll.
   */
  public async countSearchResults(caseId: string): Promise<number> {
    const headers = { accept: "application/json", ...(await this.xsrfHeader()) };
    const response = await this.client.post<{ total?: number }>(
      `/data/searchCases?ctid=${config.bail.caseTypeId}`,
      { headers, data: { query: { term: { reference: caseId } }, _source: ["reference"] } }
    );
    return response.data?.total ?? 0;
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

const REQUEST_METHODS = new Set(["fetch", "get", "post", "put", "patch", "delete", "head"]);
/** A header line in Playwright's call log, which colours each line with ANSI escapes. */
const CREDENTIAL_HEADER =
  /^((?:\u001b\[[0-9;]*m)?\s*-?\s*(?:cookie|x-xsrf-token|authorization|serviceauthorization):\s*)[^\u001b\n]*/gim;

/**
 * The context's request API, with credentials taken out of its errors. Playwright's request
 * errors (DNS failures, resets, timeouts) carry a call log listing the request's headers, and
 * these requests carry the XUI session: the cookie jar with the IDAM token, and the XSRF token.
 * ApiClient logs a failed request's error message before rethrowing it, so the message is
 * redacted here, before either sees it, and nothing secret reaches a CI log.
 */
function withoutCredentials(request: APIRequestContext): APIRequestContext {
  const redact = (text: string) => text.replace(CREDENTIAL_HEADER, "$1[REDACTED]");
  return new Proxy(request, {
    get(target, property, receiver) {
      const value = Reflect.get(target, property, receiver);
      if (typeof value !== "function") return value;
      if (typeof property !== "string" || !REQUEST_METHODS.has(property)) return value.bind(target);
      return async (...args: unknown[]) => {
        try {
          return await value.apply(target, args);
        } catch (error) {
          if (error instanceof Error) {
            error.message = redact(error.message);
            if (error.stack) error.stack = redact(error.stack);
          }
          throw error;
        }
      };
    },
  });
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
