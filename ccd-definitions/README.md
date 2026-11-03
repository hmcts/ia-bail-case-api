# Bail CCD definition

Builds the spreadsheet the CCD definition store imports for the `Bail` case type
(jurisdiction `IA`) from the [ccd-config-generator](https://github.com/hmcts/dtsse-ccd-config-generator)
Java in `src/main/java/uk/gov/hmcts/reform/bailcaseapi/ccd`.

```
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

Each run calls `./gradlew generateCCDConfig` first, which needs IDAM reachable — the
pipeline points it at AAT, and locally that means the rse-idam-simulator or AAT. The
per-environment `env/<env>/UserProfile.json` is added on top, because the SDK does not
generate work-basket defaults.

To change the definition, change the Java. Every environment — preview, dev, demo, ithc,
perftest, aat and prod — imports what it generates.

Callback URLs the Java does not own are written as `${CCD_DEF_BAIL_URL}` and
`${CCD_DEF_AAC_URL}`; `bin/generate-ccd-definition.sh` resolves them per environment from
`default.env`. The `CCD_DEF` prefix is required — `ccd-definition-processor` only
substitutes environment variables whose names start with it.

## Validating

```bash
yarn test     # duplicate rows, missing system-user grants, Publish flag mismatches
```

These read the generated definition in `../build/ccd-definitions-json`, so run a
`yarn generate-<env>` first. `bin/generate-ccd-definition.sh` runs them itself once it has
built the spreadsheet.

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
