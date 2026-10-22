#!/usr/bin/env bash

set -eu

definitions_dir=$(cd "$(dirname "${0}")/.." && pwd)
repo_dir=$(cd "${definitions_dir}/.." && pwd)
json_dir="${repo_dir}/build/ccd-definitions-json"
generated_dir="${repo_dir}/build/definitions/Bail"

rm -rf "${json_dir}"
mkdir -p "${json_dir}"

if [[ "${CCD_DEF_SOURCE:-json}" == "java" ]]; then
  if [[ ! -d "${generated_dir}" ]]; then
    echo "Error: ${generated_dir} not found - run './gradlew generateCCDConfig' first"
    exit 1
  fi
  # The SDK splits the larger sheets into a directory of files; json2xlsx walks nested
  # directories, so copy the whole tree rather than only the top-level files.
  cp -R "${generated_dir}/." "${json_dir}/"
else
  cp "${definitions_dir}/json/"*.json "${json_dir}/"
fi

# UserProfile is not part of the generated definition, so it comes from the environment
# overlay whichever source the rest of the definition came from.
cp "${definitions_dir}/env/${CCD_ENV:-dev}/"*.json "${json_dir}/"
