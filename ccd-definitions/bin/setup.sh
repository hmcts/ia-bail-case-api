#!/usr/bin/env bash

set -eu

definitions_dir=$(cd "$(dirname "${0}")/.." && pwd)

processor_dir="${definitions_dir}/ccd-definition-processor"

# Jenkins restores the workspace on the environment agent by unstashing rather than
# cloning, so the submodule's files can be present while its git metadata is not.
if [ ! -f "${processor_dir}/package.json" ]; then
  git -C "${definitions_dir}" submodule update --init --recursive
fi

cd "${processor_dir}" && yarn install
