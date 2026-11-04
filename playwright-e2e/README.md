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
  and organisation the about-to-start callback sets, the declaration on submission, and a
  `ChangeOrganisationRequest` in the shape `RemoveRepresentationPreparer` writes.

A legal representative cannot create `hasLegalRep`, `sentByChecklist` or
`applicationSubmittedBy`, so those are only sent for an admin officer's case.

## What the tests assert

| Spec | Test | Assertion |
|---|---|---|
| `tests/add-case-note.spec.ts` | adds a note … on an admin officer's case | Snapshots six tabs as the admin officer, judge and Home Office; adds a note through Next step → page → Continue → check your answers → Add case note; asserts the confirmation, the note's subject, text, author and date on Case notes, and Add case note at the top of Case history; then every snapshotted tab, for every role, is unchanged except for the new note, and Overview shows exactly that role's one progress panel. |
| `tests/add-case-note.spec.ts` | adds a note … to a legal representative's case | The same on a case the legal rep created, viewed as the admin officer and the legal rep, so the legal rep's details are among what must survive. |
| `tests/interpreter-events.spec.ts` | updates the … booking statuses | "Update interpreter status", which still goes through Bail's own dispatcher: the dropdown labels name the applicant and supporter and their reference-data languages (the about-to-start reading them), the chosen statuses show on Hearing and appointment, and nothing else on any tab changes. |
| `tests/interpreter-events.spec.ts` | adds an interpreter's details | "Update interpreter details", likewise: the interpreter shows on Hearing and appointment and nothing else changes. |
| `tests/bail-case-view.spec.ts` | shows the `<role>` … | For the admin officer, judge, Home Office and (on their own case) legal rep: exactly the case-type tabs the definition grants the role, **exactly one** progress panel and the role's own one (`progress_*` field ID), the seeded applicant, addresses, supporter, interpreter languages, hearing centre and legal rep shown where the definition puts them, and, for the roles granted Case history, the events listed latest first. |
| `tests/create-case.spec.ts` (`@smoke`) | | Create case offers the jurisdiction "Immigration & Asylum" (`IA`), the Bail case type (`Bail`) and the event "Start the application" (`startApplication`) to an admin officer, and Start opens the event's first wizard page. |
| `tests/bail-case-view.a11y.spec.ts` (`@a11y`) | | axe audits of the Create case page and three tabs of a seeded case. Excluded from `yarn test`; run with `yarn test:a11y`. |

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

yarn test                       # everything except @a11y, on chromium
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
- `TEST_ADMINOFFICER_*`, `TEST_LAW_FIRM_A_*`, `TEST_HOMEOFFICE_BAIL_*`, `TEST_JUDGE_BAILS_*`:
  one user per role. The first three are the names both Jenkinsfiles already bind; neither binds
  a `TEST_JUDGE_BAILS_*` secret yet. Copy them into `.env` by hand; playwright-common's
  `get-secrets` only reads secrets tagged `e2e: <ENV_VAR_NAME>`, and the `ia-aat` secrets are
  not tagged.
- `BAIL_CASE_TYPE_LABEL`: defaults to `BailHEAD`. The definition names the case type
  `Bail${CCD_DEF_VERSION}` and `ccd-definitions/bin/generate-ccd-definition.sh` sets
  `CCD_DEF_VERSION` to the checked-out branch name, truncated to 16 characters. Jenkins builds
  from a detached HEAD, so preview and AAT both show `BailHEAD`; a definition you generate from
  a local branch shows `Bail` followed by your branch name.

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
- The four roles sign in one after another in `global.setup.ts`, every run: concurrent sign-ins
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
- `.sessions/` holds the stored sign-ins and is gitignored.
