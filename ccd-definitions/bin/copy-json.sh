#!/usr/bin/env bash

set -eu

definitions_dir=$(cd "$(dirname "${0}")/.." && pwd)
repo_dir=$(cd "${definitions_dir}/.." && pwd)
json_dir="${repo_dir}/build/ccd-definitions-json"
generated_dir="${repo_dir}/build/definitions/Bail"

rm -rf "${json_dir}"
mkdir -p "${json_dir}"

if [[ ! -d "${generated_dir}" ]]; then
  echo "Error: ${generated_dir} not found - run './gradlew generateCCDConfig' first"
  exit 1
fi

# The SDK splits the larger sheets into a directory of files; json2xlsx walks nested
# directories, so copy the whole tree rather than only the top-level files.
cp -R "${generated_dir}/." "${json_dir}/"
node "${definitions_dir}/bin/drop-empty-crud.js" "${json_dir}"

# UserProfile is not part of the generated definition, so it comes from the environment overlay.
cp "${definitions_dir}/env/${CCD_ENV:-dev}/"*.json "${json_dir}/"
