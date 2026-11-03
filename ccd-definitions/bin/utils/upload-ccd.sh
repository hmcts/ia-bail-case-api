#!/usr/bin/env bash

set -eu

# Generates the Bail CCD definition and uploads it to a definition store.
# Only for local / personal environments — CI imports via the pipeline's
# High Level Data Setup stage (./gradlew highLevelDataSetup).

ENV="dev"
PR_NUMBER=""
USERNAME=$(whoami)
DRY_RUN=false

while [[ $# -gt 0 ]]; do
  key="$1"
  case $key in
    -e|--env)
      ENV="$2"
      shift
      shift
      ;;
    -p|--pr)
      PR_NUMBER="$2"
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
      echo "  -e, --env ENV           Environment (dev, preview, mirrord)"
      echo "  -p, --pr PR_NUMBER      PR number for preview environment (required when '-e preview' is specified)"
      echo "  -d, --dry-run           Show what would be done without actually uploading"
      echo "  -h, --help              Show this help message"
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

dir=$(dirname "${0}")
definitions_dir=$(cd "${dir}/../.." && pwd)

if [[ "${ENV}" == "preview" && -z "${PR_NUMBER}" ]]; then
  echo "Error: PR number (-p, --pr) is required for preview environment"
  echo "Usage example: $0 -e preview -p 1234"
  exit 1
fi

case $ENV in
  dev)
    FILENAME="ccd-bail-config-dev.xlsx"
    CCD_URL="http://localhost:4451"
    GENERATE_ARGS="-e dev"
    TOKEN_ENV="local"
    ;;
  mirrord)
    FILENAME="ccd-bail-config-mirrord.xlsx"
    CCD_URL="https://ccd-definition-store-ia-bail-case-api-${USERNAME}-pr-1.preview.platform.hmcts.net"
    GENERATE_ARGS="-e mirrord"
    TOKEN_ENV="aat"
    ;;
  preview)
    FILENAME="ccd-bail-config-preview.xlsx"
    CCD_URL="https://ccd-definition-store-ia-bail-case-api-pr-${PR_NUMBER}.preview.platform.hmcts.net"
    GENERATE_ARGS="-e preview -p ${PR_NUMBER}"
    TOKEN_ENV="aat"
    ;;
  *)
    echo "Error: Unsupported environment: ${ENV}"
    echo "Supported environments: dev, mirrord, preview"
    exit 1
    ;;
esac

echo "Environment: ${ENV}"
echo "File to upload: ${FILENAME}"
echo "CCD Definition Store URL: ${CCD_URL}"
echo "Token Environment: ${TOKEN_ENV}"

if [[ "${DRY_RUN}" == "true" ]]; then
  echo "Dry run - exiting without uploading"
  exit 0
fi

echo "Generating definition file for ${ENV} environment..."
"${definitions_dir}/bin/generate-ccd-definition.sh" ${GENERATE_ARGS}

echo "Uploading definition file to ${CCD_URL}..."
"${definitions_dir}/bin/utils/ccd-import-definition.sh" -f "${FILENAME}" -u "${CCD_URL}" -e "${TOKEN_ENV}"
