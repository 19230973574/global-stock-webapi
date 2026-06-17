#!/usr/bin/env bash

set -euo pipefail

BASE_DIR="${1:-/app}"
BRANCH="${BRANCH:-main}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
UPDATE_SCRIPT="${SCRIPT_DIR}/update-repos.sh"

if [[ ! -x "${UPDATE_SCRIPT}" ]]; then
  echo "ERROR: update script not found or not executable: ${UPDATE_SCRIPT}" >&2
  exit 1
fi

echo "==> Updating repositories into ${BASE_DIR}"
"${UPDATE_SCRIPT}" "${BASE_DIR}"

if [[ ! -f "${BASE_DIR}/docker-compose.yml" ]]; then
  cp "${DEPLOY_DIR}/docker-compose.yml" "${BASE_DIR}/docker-compose.yml"
  echo "==> Copied docker-compose.yml to ${BASE_DIR}"
fi

if [[ ! -f "${BASE_DIR}/.env" ]]; then
  cp "${DEPLOY_DIR}/.env.example" "${BASE_DIR}/.env"
  echo "==> Created ${BASE_DIR}/.env from deploy/.env.example"
  echo "==> Please edit ${BASE_DIR}/.env and rerun deploy if secrets are still empty"
fi

cd "${BASE_DIR}"

echo "==> Pulling latest base images"
docker compose pull || true

echo "==> Building and starting services"
docker compose up -d --build

echo "==> Deployment status"
docker compose ps
