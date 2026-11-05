# Bail case view tests (Playwright)

Browser tests for the Bail case type in XUI (Manage cases). They exist because the repo's
functional tests call the service's callback endpoints directly with fabricated case data and
never go through CCD or XUI. Every fault found in the generated definition and the SDK-served
callbacks so far was invisible to them:

- every progress panel on the Overview tab rendering at once (tab read access widened to every
  role);
- an empty Case history tab (a second `CaseHistoryViewer`);
- under hmcts/ia-bail-case-api#622, Add case note failing on every real case (a `DynamicList`
  the model cannot read), failing on any case with a legal representative (raw
  `OrganisationPolicy` / `ChangeOrganisationRequest`), or silently wiping the applicant's and
  supporters' addresses (nested objects inside `@JsonUnwrapped(prefix)` groups read back empty,
  and the whole case written back).

So the suite seeds a realistic case for each test through CCD, performs events **through the
XUI UI**, and after each one checks both that the event did what it should and that everything
already on the case is still there and shown the same way, for every role that can see it.

Written to the TCoE Playwright conventions: `@hmcts/playwright-common` for the shared config,
IDAM sign-in, ExUI components, `ApiClient` and axe; page objects under `page-objects/`;
fixtures merged in `fixtures.ts`; sign-in done once per role in a `setup` project whose
sessions the browser project reuses.

The suite also carries the Bail journeys of the old Protractor/Cucumber suite
(hmcts/ia-ccd-e2e-tests, the scenarios tagged `@bail-nightly-test` in `e2e/features/bails-*.feature`):
submission, editing the application, directions, documents, the decision, ending and making a new
application, case flags, Notice of Change and hearing requests.

### Where each old scenario went

| Old scenario (`e2e/features/`) | Test |
|---|---|
| bails-submit-bail-application-admin-officer: Submit bail application as Admin Officer | `submit-application.spec.ts` (also as the legal rep and the Home Office) |
| bails-edit-application-pre-submit: Legal Rep Edit application Before Submitting | `edit-application.spec.ts`, before submitting |
| bails-edit-application-post-submit: Edit application after submitting | `edit-application.spec.ts`, after submitting |
| bails-end-application: End Application after submitting | `end-application.spec.ts` |
| bails-send-direction: Send a direction to Applicant as an Admin Officer (it directs the legal rep) | `directions.spec.ts`, sends a direction |
| bails-change-direction-due-date: Change a direction due date as an Admin Officer | `directions.spec.ts`, changes a due date |
| bails-add-a-case-note: Add case note without document as Admin Officer | `add-case-note.spec.ts`, admin officer's case |
| bails-update-interpreter-details: Add Update interpreter details application | `interpreter-events.spec.ts`, interpreter's details |
| bails-update-interpreter-details-status: Add Update interpreter status application | `interpreter-events.spec.ts`, booking statuses |
| bails-upload-documents: Upload Documents after submitting | `documents.spec.ts`, uploads a document |
| bails-edit-documents: Delete, edit and add a document as a Judge (it runs as the admin officer) | `documents.spec.ts`, deletes, edits and adds |
| bails-make-new-application: as Admin Officer after ended application with case note; as Home Office after conditional grant decided | `make-new-application.spec.ts` |
| bails-add-a-case-flag: Create a case Flag; Create a case Flag and make Inactive | `case-flags.spec.ts` |
| bails-notice-of-change: Notice of Change - incorrect details; Notice of Change | `notice-of-change.spec.ts` |
| bails-snl-admin/ho/lr-request-a-hearing | `hearing-request.spec.ts` (without the party-level flags) |
| (the "record a conditional grant" precondition step) | `record-decision.spec.ts` |

## Two tiers: every PR, and the `enable_e2e_test` label

| Tier | Runs | When | How |
|---|---|---|---|
| Main journeys | the tests tagged `@pr` (19 with the six sign-ins, about 4 minutes at 4 workers) | every PR, once the functional tests have passed | `Jenkinsfile_CNP` `afterSuccess('functionalTest:preview')` → `./gradlew --rerun-tasks e2ePrTest` → `yarn test:ci:pr`. Report: **E2E PR Test Report** |
| Full suite | everything except `@a11y` | PRs labelled `enable_e2e_test` | the CNP library's "E2E Test - AKS preview" stage → `./gradlew --rerun-tasks e2eTest` → `yarn test:ci`. Report: **E2E Test Report** |

The `@pr` subset is one test per main journey: creating a case (`create-case`), submitting it as
each kind of applicant (`submit-application`), what each role is shown (`bail-case-view`), adding a
case note (`add-case-note`), sending a direction (`directions`), recording the decision
(`record-decision`) and ending the application (`end-application`). Everything else (editing the
application, changing a due date, documents, making a new application, flags, Notice of Change,
hearings) is in the full suite only. Tag a test `@pr` with `{ tag: "@pr" }` only if it is one of
those main journeys; `--grep` matches substrings, so no other tag may contain `@pr`.

The PR tier runs inside the functional stage, so its failure fails that stage, and it writes to
`e2e-pr-report/`, `e2e-pr-junit.xml` and `e2e-pr-results/` (`E2E_OUTPUT_NAME=e2e-pr`, set by the
gradle task) so its report sits beside a full run's. Retries are off in both tiers, on CI as well
(`playwright.config.ts` overrides CommonConfig's two retries on CI).

## How a test gets its case

`bailCase` and `legalRepBailCase` (`utils/case.fixtures.ts`) each give the test a case of its
own at `applicationSubmitted`, so tests are independent and run in parallel. Seeding takes a few
seconds rather than walking the 95 wizard pages of "Start the application".

`utils/bail-case.seeder.ts` creates the case as the role that would create it (the admin
officer, or the legal representative), through XUI's `/data` proxy to `ccd-data-store-api`,
from a browser context holding that role's session (`utils/ccd-api.utils.ts`):

1. `GET /data/case-types/Bail/event-triggers/startApplication`, which runs the event's
   about-to-start callback;
2. `POST /data/case-types/Bail/cases`, which runs its about-to-submit;
3. `GET /data/cases/{id}/event-triggers/submitApplication` and `POST /data/cases/{id}/events`.

The requests carry the context's cookies and the `XSRF-TOKEN` cookie as `X-XSRF-TOKEN`, with
the data store's v2 media types as the accept header (retrying once with whatever a 406 says it
accepts). Because the case's own callbacks run, the case carries what a real one does: the
location and language reference data, `caseManagementCategory` and `hearingCentreRefData`
(`DynamicList`s), `caseManagementLocation`, `localAuthorityPolicy` (`OrganisationPolicy`), the
generated application PDF and the bail reference number.

The data starts from `src/functionalTest/resources/templates/minimal-application-started.json`
without its `currentCaseStateVisibleTo*` fields (no user may create them; CCD answers "No field
found"), and adds, with values unique to the test:

- an applicant address and a financial condition supporter with an address (`AddressUK` inside
  the `@JsonUnwrapped(prefix)` Applicant and Supporter groups);
- spoken-language interpreters for the applicant and the supporter, whose `languageRefData`
  `DynamicList` is the reference-data list the about-to-start callback offered, with one item
  selected, as XUI submits it;
- on a legal representative's case, the legal rep's name, phone and reference, the firm email
  and organisation the about-to-start callback sets (`localAuthorityPolicy`, an
  `OrganisationPolicy`), and the declaration on submission. There is no
  `ChangeOrganisationRequest`, as on a real case at submission; aac-manage-case-assignment would
  read one with a case role as a notice of change already in progress.

A legal representative cannot create `hasLegalRep`, `sentByChecklist` or
`applicationSubmittedBy`, so those are only sent for an admin officer's case.

`seedCase(createdBy, options)` seeds other shapes: as the Home Office or as `legalRepBails`, left
unsubmitted (`submit: false`), with the applicant in prison (`detention: "prison"`), or with
extra or removed fields (`overrides`).

### Preconditions further along the case

`progress(caseId)` (`utils/bail-case.progression.ts`) drives the events before the one under
test over CCD, each as the role that performs it, with the data XUI submitted for the same
answers in the UI: Case listing, Upload Bail Summary, a conditional grant through Record the
decision, its signed notice, Move application to decided, Add case note, End the application,
Send direction and Upload documents. Each step checks the state CCD answers with. The event a
test is about is always driven through XUI.

Documents are uploaded to CDAM through XUI's `/documentsv2`, as the UI does, using PDFs built in
memory (`utils/document.utils.ts`) with names unique to the test.

## What the tests assert

| Spec | Test | Assertion |
|---|---|---|
| `tests/add-case-note.spec.ts` (`@pr`) | adds a note … on an admin officer's case | Snapshots six tabs as the admin officer, judge and Home Office; adds a note through Next step → page → Continue → check your answers → Add case note; asserts the confirmation, the note's subject, text, author and date on Case notes (for the judge too), and Add case note at the top of Case history; then every snapshotted tab, for every role, is unchanged except for the new note, and Overview shows exactly that role's one progress panel. |
| `tests/add-case-note.spec.ts` | adds a note … to a legal representative's case | The same on a case the legal rep created, viewed as the admin officer and the legal rep, so the legal rep's details are among what must survive. |
| `tests/interpreter-events.spec.ts` | updates the … booking statuses | "Update interpreter status", which still goes through Bail's own dispatcher: the dropdown labels name the applicant and supporter and their reference-data languages (the about-to-start reading them), the chosen statuses show on Hearing and appointment, and nothing else on any tab changes. |
| `tests/interpreter-events.spec.ts` | adds an interpreter's details | "Update interpreter details", likewise: the interpreter shows on Hearing and appointment and nothing else changes. |
| `tests/bail-case-view.spec.ts` | shows the `<role>` … | For the admin officer, judge, Home Office and (on their own case) legal rep: exactly the case-type tabs the definition grants the role, **exactly one** progress panel and the role's own one (`progress_*` field ID), the seeded applicant, addresses, supporter, interpreter languages, hearing centre and legal rep shown where the definition puts them, and, for the roles granted Case history, the events listed latest first. |
| `tests/create-case.spec.ts` (`@smoke`) | | Create case offers the jurisdiction "Immigration & Asylum" (`IA`), the Bail case type (`Bail`) and the event "Start the application" (`startApplication`) to an admin officer, and Start opens the event's first wizard page. |
| `tests/bail-case-view.a11y.spec.ts` (`@a11y`) | | axe audits of the Create case page and three tabs of a seeded case. Excluded from `yarn test`; run with `yarn test:a11y`. |
| `tests/submit-application.spec.ts` (`@pr`) | submits … as the `<role>` | Submit the application through Next step on an application the admin officer, the Home Office or a legal rep (with the declaration) saved; the confirmation, the role's one progress panel, and for the admin officer the prompt to confirm the detention location. |
| `tests/edit-application.spec.ts` | before submitting / after submitting | Edit the application as the legal rep before submission, and as the admin officer after: prison to Yarl's Wood, no supporter, financial condition or interpreter, new legal rep details; every changed value on Overview, Bail application and Hearing and appointment (including the reference-data hearing centre). |
| `tests/directions.spec.ts` | sends a direction (`@pr`) | Send direction to the legal representative: check your answers, the confirmation, and the direction's explanation, party and dates on the Directions tab. |
| `tests/directions.spec.ts` | changes a due date | Change direction due date on a direction sent over CCD: the new due date, and the old one under Previous dates. |
| `tests/end-application.spec.ts` (`@pr`) | ends … as withdrawn | End the application: check your answers, the confirmation, the outcome, reasons and date on Overview, and the ended-application notice among the tribunal documents. |
| `tests/record-decision.spec.ts` (`@pr`) | records a conditional grant | Record the decision after listing and the bail summary: the conditional-grant route through the wizard page by page, the event in Case history, and the unsigned decision notice among the decision documents. |
| `tests/documents.spec.ts` | uploads a document | Upload documents as the legal rep: the document, description and date among the applicant documents. |
| `tests/documents.spec.ts` | deletes, edits and adds | Edit documents as the admin officer: one document removed, one redescribed and one added with its supplier and date; and the case note that lists all three and the reason. |
| `tests/make-new-application.spec.ts` | after the first ended / after a conditional grant | Make new application as the admin officer (with a B1 form) and as the Home Office: who submitted it, what the event keeps and what it asks again, the new evidence; for the admin officer, the first application's case note is still there. |
| `tests/case-flags.spec.ts` | creates / makes inactive | Create Flag (case level, Complex Case) and Manage Flags: the active-flags banner and the flag's status in the Case level flags table. |
| `tests/notice-of-change.spec.ts` | rejects / moves the case | XUI's Notice of Change as a legal rep of another organisation: a wrong first name, surname or date of birth is refused; the right answers move the case: its `[LEGALREPRESENTATIVE]` organisation policy names the new rep's organisation, and the new rep finds the case in search and sees it. Whether the creating rep still finds it depends on the environment's data-store and Elasticsearch access setup, so that is not asserted. |
| `tests/hearing-request.spec.ts` | requests a bail hearing … | For applications made by each kind of applicant: Confirm detention location, an urgent-case flag, and a bail hearing request through the Hearings tab to HMC. Skipped where `config.hearingsAvailable` is false (preview has no HMC). |

### How "nothing else changed" is checked

`utils/tab-snapshot.utils.ts` reads a tab as `field path → rendered text`. Each top-level field
on a CCD tab is rendered in an element with the id `case-viewer-field-read--<fieldId>`, so keys
come from the definition, not the layout; complex fields are flattened one row per sub-field
(`applicantAddress › Address › Postcode/Zipcode`), and fields hidden by a show condition are
skipped. After the event each tab is read again and compared, allowing only the fields the
event is meant to change. A failure names every field that changed, per tab and role:

```
"Bail application" tab, as the judge, after "Add case note": fields that changed although the event should not have touched them
+   "supporterAddressDetails › Address › Postcode/Zipcode: was \"NE2 1JX\", now not shown",
```

The comparison is soft, so one run reports every loss rather than the first. The seeded values
are also checked on the "before" snapshot, so the comparison is between two views that actually
contain the data at risk. A refused submission fails at the submit step with CCD's status and
response body, e.g. a 502 `CallbackException` naming the callback URL.

Each test's case ID is attached to the report as a `Bail case` annotation, and traces,
screenshots and video are kept on failure for every role's browser context.

## Running them

```bash
cd playwright-e2e
yarn install
yarn setup                      # playwright install --with-deps chromium

cp .env.example .env            # then fill it in

yarn test                       # everything except @a11y, on chromium (the full tier)
yarn test:pr                    # the @pr main journeys (the every-PR tier)
yarn test:smoke                 # Create case only
yarn test:a11y                  # axe audits
yarn lint                       # tsc + eslint
```

All need the F5 VPN. `XUI_URL=<url> yarn test` runs against another environment without
editing `.env`.

### Environment

Every variable is listed in `.env.example`. The ones that matter:

- `XUI_URL`: the environment under test. `Jenkinsfile_CNP` already exports this for PR builds
  as `https://xui-ia-bail-case-api-pr-<PR>.preview.platform.hmcts.net`; nothing here hard-codes
  a PR number. AAT is `https://manage-case.aat.platform.hmcts.net`.
- `TEST_ADMINOFFICER_*`, `TEST_LAW_FIRM_A_*`, `TEST_HOMEOFFICE_BAIL_*`, `TEST_JUDGE_BAILS_*`,
  `TEST_LAW_FIRM_NOC_A_BAILS_*`, `TEST_LAW_FIRM_ORG_DELETED_*`: one user per role
  (`USER_ROLES` in `utils/config.utils.ts`), all bound in `Jenkinsfile_CNP` from the `ia-${env}`
  vault. The last two are the old suite's "Legal Org User Rep A Bails" and "Legal Org User Rep C",
  legal reps in two different MyHMCTS organisations, for the ported journeys and Notice of
  Change. Copy them into `.env` by hand; playwright-common's `get-secrets` only reads secrets
  tagged `e2e: <ENV_VAR_NAME>`, and the `ia-aat` secrets are not tagged.
- `BAIL_CASE_TYPE_LABEL`: defaults to `BailHEAD`. The definition names the case type
  `Bail${CCD_DEF_VERSION}` and `ccd-definitions/bin/generate-ccd-definition.sh` sets
  `CCD_DEF_VERSION` to the checked-out branch name, truncated to 16 characters. Jenkins builds
  from a detached HEAD, so preview and AAT both show `BailHEAD`; a definition you generate from
  a local branch shows `Bail` followed by your branch name.
- `HEARINGS_AVAILABLE`: whether to run the hearing-request tests. Defaults to true unless
  `XUI_URL` is a preview namespace, which has no HMC.

Each signed-in user needs a row in `ccd-user-profile-api`, or CCD answers
`404 No jurisdictions found` whatever roles the user holds. For preview that means an entry in
`ccd-definitions/env/preview/UserProfile.json`.

## Notes for whoever changes this next

- IDAM's sign-in is two pages (`/enter-email`, then `/enter-password`). `IdamPage.login` from
  playwright-common 1.1.6 and later handles the split.
- Leave IDAM's cookie banner alone. Its Accept/Reject buttons are `type=button`, so the only
  visible submit button is Continue. Click Reject and IDAM swaps the banner for a confirmation
  whose "Hide this message" button *is* a submit button, which makes `IdamPage`'s submit locator
  match two elements and the sign-in fails on a strict mode violation.
- XUI's cookie banner is answered once in the stored session (`utils/cookie.utils.ts`) rather
  than clicked, because clicking either of its buttons reloads the page.
- The roles sign in one after another in `global.setup.ts`, every run: concurrent sign-ins
  can crash XUI's node-lib, and XUI's cookies are session cookies (`expires: -1`), so
  `SessionUtils.isSessionValid` can never report a stored session as reusable.
- XUI routes in the background. Opening a case is followed a few seconds later by XUI's own
  navigation to `/cases/case-details/IA/Bail/<id>#<tab>`, and each tab click is a navigation to
  `#<tab>` that completes after the click. One still queued when Go is clicked cancels the
  event's navigation and leaves the loading spinner up indefinitely, so `goto()` and `openTab()`
  wait for those URLs before returning. Don't remove those waits.
- The selected tab's accessible name has ", Please use left and right arrow keys to navigate
  case tabs" appended, so tabs are matched by a prefix.
- Each role's Case history shows only the events it has access to: the judge is not shown
  "Start the application".
- XUI's `/api/user/details` names the user as `given_name` / `family_name` on AAT and as
  `forename` / `surname` on the preview; `CcdApi.signedInUserName` accepts both.
- `.sessions/` holds the stored sign-ins and the upload throttle's state, and is gitignored.
- Long wizards (the application events, Record the decision) are driven by
  `EventWizardPage.completeToCheckYourAnswers`, with answers keyed by wizard page ID (the URL's
  last segment, after the event ID) and inputs located by the IDs CCD derives from the field IDs.
  It waits for each page change by URL and fails on a page CCD refuses, naming CCD's error, or
  on an answered page that never appeared.
- XUI throttles document uploads per XUI session (`documentUploadThrottle` in
  rpx-xui-webapp: an upload starting within 5 s of the session's previous one is answered 429,
  and each 429 doubles the window). All tests signed in as a role share its session, so every
  upload, in the UI or over the API, goes through `utils/upload-throttle.ts`, which serialises
  uploads per session across workers and keeps the gap.
- A CCD date field writes its value on each input's change event, which fires on blur. A
  Continue click that does the blurring is ignored without an error, so the wizard blurs each
  date input as it fills it.
- When all of an event's wizard pages are hidden (Submit the application for the admin officer
  and the Home Office), ccd-case-ui-toolkit opens a "Page refreshed" dialog over the summary page,
  because it never recorded the event's URL. The wizard acknowledges it.
- The Remove button of a collection item sits in the item's header and is named after the item;
  a date inside a collection item has the same un-prefixed ID in every item. The wizard's
  `removeItem` and `withinItem` answers handle both.
