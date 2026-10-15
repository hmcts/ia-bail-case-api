#!/usr/bin/env bash

set -eu

definitions_dir=$(cd "$(dirname "${0}")/.." && pwd)
repo_dir=$(cd "${definitions_dir}/.." && pwd)
json_dir="${repo_dir}/build/ccd-definitions-json"

output_file="${1:-${repo_dir}/build/ccd-config/ccd-bail-config-${CCD_ENV:-dev}.xlsx}"
if [[ "${output_file}" != /* ]]; then
  output_file="${repo_dir}/${output_file}"
fi

mkdir -p "$(dirname "${output_file}")"

echo "Generating Excel file: ${output_file}"

cd "${definitions_dir}/ccd-definition-processor"
yarn json2xlsx -D "${json_dir}" -o "${output_file}"
