#!/usr/bin/env bash

set -eu

definitions_dir=$(cd "$(dirname "${0}")/.." && pwd)
repo_dir=$(cd "${definitions_dir}/.." && pwd)
json_dir="${repo_dir}/build/ccd-definitions-json"

rm -rf "${json_dir}"
mkdir -p "${json_dir}"

cp "${definitions_dir}/json/"*.json "${json_dir}/"
cp "${definitions_dir}/env/${CCD_ENV:-dev}/"*.json "${json_dir}/"
