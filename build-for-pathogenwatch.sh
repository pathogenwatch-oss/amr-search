#!/usr/bin/env bash

set -euo pipefail

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <species-code>" >&2
  exit 1
fi

species_code=$1
if [[ ! $species_code =~ ^[0-9]+$ ]]; then
  echo "Species code must be numeric: $species_code" >&2
  exit 1
fi

version_for_commit() {
  local repository=$1

  git -C "$repository" describe --exact-match --tags HEAD 2>/dev/null \
    || git -C "$repository" rev-parse --short HEAD
}

paarsnp_version=$(version_for_commit .)
amr_library_version=$(version_for_commit libraries/amr-libraries)
image="902121496535.dkr.ecr.eu-west-2.amazonaws.com/pathogenwatch-source/paarsnp:${paarsnp_version}_${amr_library_version}_${species_code}"

docker build \
  --build-arg "SPECIES_CODE=${species_code}" \
  --build-arg "AMR_LIBRARY_VERSION=${amr_library_version}" \
  --tag "$image" .

echo "Built $image"
