#!/usr/bin/env bash

# Script to generate the Bail CCD definition spreadsheet for a given environment.
# Usage: ./bin/generate-ccd-definition.sh [options]
# Options:
#   -e, --env ENV           Environment (dev, preview, demo, ithc, perftest, aat, prod, mirrord)
#   -s, --source SOURCE     Definition source: json (hand-written) or java (ccd-config-generator);
#                           defaults to $CCD_DEF_SOURCE, or json
#   -p, --pr PR_NUMBER      PR number for preview environment (required when `-e preview` is specified)
#   -u, --user USERNAME     Username for mirrord environment (defaults to current user)
#   -d, --dry-run           Show what would be done without actually generating files
#   -h, --help              Show this help message

set -eu

ENV="dev"
SOURCE="${CCD_DEF_SOURCE:-json}"
PR_NUMBER=""
USERNAME=""
DRY_RUN=false

while [[ $# -gt 0 ]]; do
  key="$1"
  case $key in
    -e|--env)
      ENV="$2"
      shift
      shift
      ;;
    -s|--source)
      SOURCE="$2"
      shift
      shift
      ;;
    -p|--pr)
      PR_NUMBER="$2"
      shift
      shift
      ;;
    -u|--user)
      USERNAME="$2"
      shift
      shift
      ;;
    -d|--dry-run)
      DRY_RUN=true
      shift
      ;;
    -h|--help)
      echo "Usage: $0 [options]"
      echo "Options:"
      echo "  -e, --env ENV           Environment (dev, preview, demo, ithc, perftest, aat, prod, mirrord)"
      echo "  -s, --source SOURCE     Definition source: json (the hand-written json/ directory, the"
      echo "                          default) or java (generated from the ccd-config-generator config"
      echo "                          by './gradlew generateCCDConfig'). Defaults to \$CCD_DEF_SOURCE"
      echo "  -p, --pr PR_NUMBER      PR number for preview environment (required when '-e preview' is specified)"
      echo "  -u, --user USERNAME     Username for mirrord environment (defaults to current user)"
      echo "  -d, --dry-run           Show what would be done without actually generating files"
      echo "  -h, --help              Show this help message"
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

definitions_dir=$(cd "$(dirname "${0}")/.." && pwd)
repo_dir=$(cd "${definitions_dir}/.." && pwd)

if [[ "${ENV}" == "preview" && -z "${PR_NUMBER}" ]]; then
  echo "Error: PR number (-p, --pr) is required for preview environment"
  echo "Usage example: $0 -e preview -p 1234"
  exit 1
fi

if [[ "${SOURCE}" != "json" && "${SOURCE}" != "java" ]]; then
  echo "Error: unknown definition source '${SOURCE}' (expected 'json' or 'java')"
  exit 1
fi

source "${definitions_dir}/default.env"

# mirrord is a personal preview, and has no env/ overlay of its own
USER_PROFILE_ENV="${ENV}"
if [[ "${ENV}" == "mirrord" ]]; then
  USER_PROFILE_ENV="preview"
fi

if [[ "${ENV}" == "prod" ]]; then
  CCD_DEF_VERSION=""
else
  CCD_DEF_VERSION=$(git -C "${repo_dir}" rev-parse --abbrev-ref HEAD | cut -c -16)
fi

if [[ "${ENV}" == "mirrord" && -z "${USERNAME}" ]]; then
  USERNAME=$(whoami)
  echo "No username provided, using current user: ${USERNAME}"
fi

if [[ "${ENV}" == "preview" ]]; then
  CCD_DEF_BAIL_URL="http://ia-bail-case-api-pr-${PR_NUMBER}-java"
  CCD_DEF_AAC_URL="${preview_aacUrl}"
elif [[ "${ENV}" == "mirrord" ]]; then
  CCD_DEF_BAIL_URL="http://ia-bail-case-api-${USERNAME}-java"
  CCD_DEF_AAC_URL="${preview_aacUrl}"
else
  BAIL_URL_VAR="${ENV}_iaBailCaseUrl"
  AAC_URL_VAR="${ENV}_aacUrl"
  CCD_DEF_BAIL_URL="${!BAIL_URL_VAR}"
  CCD_DEF_AAC_URL="${!AAC_URL_VAR}"
fi

# For dev, allow an override from the developer's environment
if [[ "${ENV}" == "dev" ]]; then
  CCD_DEF_BAIL_URL="${IA_BAIL_CASE_URL:-$CCD_DEF_BAIL_URL}"
  CCD_DEF_AAC_URL="${AAC_URL:-$CCD_DEF_AAC_URL}"
fi

echo "Generating Bail CCD definition for environment: ${ENV}"
if [[ "${SOURCE}" == "java" ]]; then
  echo "Definition source: java (ccd-config-generator config, via ./gradlew generateCCDConfig)"
else
  echo "Definition source: json (the hand-written json/ directory)"
fi
echo "Service URL for ia-bail-case-api: ${CCD_DEF_BAIL_URL}"
echo "AAC URL: ${CCD_DEF_AAC_URL}"

if [[ "${DRY_RUN}" == "true" ]]; then
  echo "Dry run - exiting without generating files"
  exit 0
fi

# One file per environment — HighLevelDataSetupApp derives this name from the
# environment it is given, so the PR number must not appear in it.
OUTPUT_FILE="${repo_dir}/build/ccd-config/ccd-bail-config-${ENV}.xlsx"

if [[ "${SOURCE}" == "java" ]]; then
  # Writes the per-sheet JSON copy-json.sh picks up to build/definitions/Bail.
  "${repo_dir}/gradlew" -p "${repo_dir}" generateCCDConfig
fi

CCD_ENV="${USER_PROFILE_ENV}" CCD_DEF_SOURCE="${SOURCE}" "${definitions_dir}/bin/copy-json.sh"

CCD_DEF_VERSION="${CCD_DEF_VERSION}" \
CCD_DEF_BAIL_URL="${CCD_DEF_BAIL_URL}" \
CCD_DEF_AAC_URL="${CCD_DEF_AAC_URL}" \
"${definitions_dir}/bin/generate-excel.sh" "${OUTPUT_FILE}"

echo "Bail CCD definition generated successfully: ${OUTPUT_FILE}"

node "${definitions_dir}/bin/run-duplicate-checks.js"
node "${definitions_dir}/bin/check-missing-system-user-fields.js"
node "${definitions_dir}/bin/check-publish-mismatches.js"
