# Bail CCD definition

The `Bail` case type definition (jurisdiction `IA`), authored as JSON and converted to the
spreadsheet the CCD definition store imports. It moved here from
[`hmcts/ia-ccd-definitions`](https://github.com/hmcts/ia-ccd-definitions), which keeps the
`Asylum` definition.

```
json/                       the definition, one file per definition-store sheet
env/<env>/UserProfile.json  per-environment work-basket defaults
bin/                        generation and validation scripts
ccd-definition-processor/   git submodule: the JSON -> xlsx converter
```

## Setup

The converter is a submodule, so clone with `--recursive` or run:

```bash
yarn install
yarn setup
```

## Generating the spreadsheet

```bash
yarn generate-aat        # or -dev, -demo, -ithc, -perftest, -prod
PR_NUMBER=1234 yarn generate-preview
```

Output lands in `../build/ccd-config/ccd-bail-config-<env>.xlsx`.

### Building from the Java config instead

The same case type is also expressed as [ccd-config-generator](https://github.com/hmcts/dtsse-ccd-config-generator)
Java under `src/main/java/uk/gov/hmcts/reform/bailcaseapi/ccd`. Passing `--source java`
(or setting `CCD_DEF_SOURCE=java`) runs `./gradlew generateCCDConfig` and builds the
spreadsheet from its output instead of `json/`:

```bash
CCD_DEF_SOURCE=java PR_NUMBER=1234 yarn generate-preview
```

Either way the per-environment `env/<env>/UserProfile.json` is added on top, because the
SDK does not generate it.

`Jenkinsfile_CNP` sets `CCD_DEF_SOURCE=java` for preview builds, so a PR's preview
environment imports the definition built from the Java config. Every other environment
— dev, demo, ithc, perftest, aat, prod — is built from `json/`, which stays the source of
truth, and `yarn test` always validates `json/`.

Callback URLs in `json/CaseEvent.json` are written as `${CCD_DEF_BAIL_URL}` and
`${CCD_DEF_AAC_URL}`; `bin/generate-ccd-definition.sh` resolves them per environment from
`default.env`. The `CCD_DEF` prefix is required — `ccd-definition-processor` only
substitutes environment variables whose names start with it.

## Validating

```bash
yarn test     # JSON syntax, duplicate rows, Publish flag mismatches
```

`Jenkinsfile_CNP` runs this before the build, so a broken definition fails the PR early.

## Importing

CI imports the definition through the pipeline's High Level Data Setup stage —
`./gradlew highLevelDataSetup --args=<environment>`, implemented by
`src/ccdDataSetup/java/uk/gov/hmcts/reform/bailcaseapi/ccd/HighLevelDataSetupApp.java`.
It imports `build/ccd-config/ccd-bail-config-<environment>.xlsx`, so generate before
importing.

For a local or personal environment:

```bash
yarn upload-local                         # localhost:4451
PR_NUMBER=1234 yarn upload-preview        # a preview definition store
```

The AAT-authenticated uploads read `ccd-importer-username` / `ccd-importer-password` from
the `ia-aat` Key Vault and need `az login` plus the VPN.
