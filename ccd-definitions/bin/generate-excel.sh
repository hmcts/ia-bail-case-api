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
# Under `yarn generate-*`, NODE_OPTIONS loads this project's Plug'n'Play resolver, which rejects
# the processor's own dependencies; the processor installs to node_modules, so plain node is enough.
env -u NODE_OPTIONS node bin/json2xlsx.js -D "${json_dir}" -o "${output_file}"
