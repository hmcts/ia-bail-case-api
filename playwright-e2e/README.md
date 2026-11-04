# Bail case view tests (Playwright)

Browser tests for the Bail case type in XUI (Manage cases). They exist because the repo's
functional tests drive CCD over the API with pipeline-issued tokens, so nothing exercised the
things that only show up in a browser: what the Create case dropdowns offer a signed-in user,
and what the case view renders from the definition's tabs and field-level read access.

Written to the TCoE Playwright conventions: `@hmcts/playwright-common` for the shared config,
IDAM sign-in, ExUI components and axe, page objects under `page-objects/`, fixtures merged in
`fixtures.ts`, and sign-in done once in a `setup` project whose session the browser project
reuses.

## What the tests assert

| Spec | Assertion |
|---|---|
| `tests/create-case.spec.ts` (`@smoke`) | Create case offers the jurisdiction "Immigration & Asylum" (`IA`), the Bail case type (`Bail`) and the event "Start the application" (`startApplication`) to an IA admin officer, and Start opens the event's first wizard page. |
| `tests/bail-case-view.spec.ts` | The Overview tab of a Bail case renders **exactly one** progress panel. The 41 `progress_*` label fields on that tab (`BailTabs.java`) have show conditions that are not mutually exclusive, so field-level read access is what selects the panel for the signed-in role; when every role could read every field, every panel rendered at once. The assertion is therefore a count, not a presence check. |
| `tests/bail-case-view.spec.ts` | The Case history tab lists events. `ccd-data-store-api` hydrates the first `CaseHistoryViewer` field in a case type and returns, so a second one is left empty and the tab renders an empty field. Only the admin officer, the judge and the system user are granted read on `caseHistory`, so this is not asserted for a legal representative. |
| `tests/bail-case-view.a11y.spec.ts` (`@a11y`) | axe audits of the Create case page and both tabs. Excluded from `yarn test`; run with `yarn test:a11y`. |

The case view tests read an existing case named by `BAIL_CASE_REFERENCE`; they do not create one.
Creating a Bail case through the UI means walking the "Start the application" event, which has
95 wizard pages in the definition (`CaseEventToFields.json`), several of them conditional, with
an address lookup, a document upload and date triplets along the way. A page-by-page journey of
that size is its own project and is not something to hang two tab assertions off. When
`BAIL_CASE_REFERENCE` is unset these tests **fail** with that message rather than skipping, so
they can never pass against a case that was never created.

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

Both need the F5 VPN.

### Environment

Every variable is listed in `.env.example`. The ones that matter:

- `XUI_URL` — the environment under test. `Jenkinsfile_CNP` already exports this for PR builds
  as `https://xui-ia-bail-case-api-pr-<PR>.preview.platform.hmcts.net`; nothing here hard-codes
  a PR number.
- `TEST_ADMINOFFICER_USERNAME` / `TEST_ADMINOFFICER_PASSWORD` — the `ia-${env}` Key Vault
  secrets `test-adminofficer-username` / `test-adminofficer-password`, under the names both
  Jenkinsfiles already bind them to. Copy them into `.env` by hand; playwright-common's
  `get-secrets` only reads secrets tagged `e2e: <ENV_VAR_NAME>`, and the `ia-aat` secrets are
  not tagged.
- `BAIL_CASE_REFERENCE` — 16-digit reference of a Bail case in that environment.
- `BAIL_CASE_TYPE_LABEL` — defaults to `BailHEAD`. The definition names the case type
  `Bail${CCD_DEF_VERSION}` and `ccd-definitions/bin/generate-ccd-definition.sh` sets
  `CCD_DEF_VERSION` to the checked-out branch name, truncated to 16 characters. Jenkins builds
  from a detached HEAD, so preview and AAT both show `BailHEAD`; a definition you generate from
  a local branch shows `Bail` followed by your branch name.

The signed-in user needs a row in `ccd-user-profile-api`, or CCD answers
`404 No jurisdictions found` whatever roles the user holds and the jurisdiction dropdown comes
back empty. For preview that means an entry in `ccd-definitions/env/preview/UserProfile.json`.

## Notes for whoever changes this next

- IDAM's sign-in is two pages (`/enter-email`, then `/enter-password`). `IdamPage.login` from
  playwright-common 1.1.6 and later handles the split.
- Leave IDAM's cookie banner alone. Its Accept/Reject buttons are `type=button`, so the only
  visible submit button is Continue. Click Reject and IDAM swaps the banner for a confirmation
  whose "Hide this message" button *is* a submit button, which makes `IdamPage`'s submit locator
  match two elements and the sign-in fails on a strict mode violation.
- XUI's cookie banner is answered once in the stored session (`utils/cookie.utils.ts`) rather
  than clicked, because clicking either of its buttons reloads the page.
- `.sessions/` holds the stored sign-in and is gitignored.
