#!/usr/bin/env bash

set -euo pipefail

BASE_DIR="${1:-/app}"
BRANCH="${BRANCH:-main}"

declare -a REPOS=(
  "global-stock-webapi"
  "global-stock-webui"
  "global-stock-ui"
  "global-stock-sprider"
)

mkdir -p "${BASE_DIR}"

for repo in "${REPOS[@]}"; do
  repo_dir="${BASE_DIR}/${repo}"
  repo_url="https://github.com/19230973574/${repo}.git"

  echo "==> Processing ${repo}"

  if [[ -d "${repo_dir}/.git" ]]; then
    git -C "${repo_dir}" fetch --all --prune
    git -C "${repo_dir}" checkout "${BRANCH}"
    git -C "${repo_dir}" pull --ff-only origin "${BRANCH}"
  else
    git clone -b "${BRANCH}" "${repo_url}" "${repo_dir}"
  fi
done

echo "==> All repositories are up to date under ${BASE_DIR}"
